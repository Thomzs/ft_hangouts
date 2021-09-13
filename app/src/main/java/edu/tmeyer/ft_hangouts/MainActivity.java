package edu.tmeyer.ft_hangouts;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.tmeyer.ft_hangouts.database.Contact;
import edu.tmeyer.ft_hangouts.database.DatabaseHelper;

//Touch events for both language & create button

public class MainActivity extends AppCompatActivity {

    public static final int         MODE_CREATE = 1;
    public static final int         MODE_EDIT = 2;
    public static final int         MODE_REQUEST = 3;

    private final List<Contact>     contactList = new ArrayList<>();
    private ArrayAdapter<Contact>   listViewAdapter;
    private ListView                listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<Contact> contacts = databaseHelper.getAllContacts();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contacts.sort(Comparator.comparing(Contact::getLastName));
        }

        this.contactList.addAll(contacts);
        this.listView = (ListView) findViewById(R.id.contact_list);
        this.listViewAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                this.contactList
        );
        this.listView.setAdapter(this.listViewAdapter);

        this.listView.setOnItemClickListener((adapterView, view, i, l) -> { //Click on a contact to edit it
            Contact contact = (Contact) listView.getItemAtPosition(i);
            contact = databaseHelper.getContact(contact.getId()); //Retrieve all info about the wanted contact
            Intent intent = new Intent(this, AddEditContactActivity.class);
            intent.putExtra("contact", contact);
            intent.putExtra("mode", MODE_EDIT);
            startActivityForResult(intent, MODE_REQUEST);
        });

        //register to context menu to call and send message

        TextView newButton = (TextView) findViewById(R.id.new_button);
        newButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddEditContactActivity.class);
            intent.putExtra("mode", MODE_CREATE);
            startActivityForResult(intent, MODE_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == MODE_REQUEST) {
            boolean needRefresh = data.getBooleanExtra("needRefresh", true);
            if (needRefresh) {
                this.contactList.clear();
                DatabaseHelper databaseHelper = new DatabaseHelper(this);
                List<Contact> list = databaseHelper.getAllContacts();
                this.contactList.addAll(list);
                this.listViewAdapter.notifyDataSetChanged();
            }
        }
    }
}
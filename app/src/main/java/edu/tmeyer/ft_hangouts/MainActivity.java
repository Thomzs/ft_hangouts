package edu.tmeyer.ft_hangouts;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.tmeyer.ft_hangouts.database.Contact;
import edu.tmeyer.ft_hangouts.database.DatabaseHelper;

//Touch events for both language & create button

public class MainActivity extends AppCompatActivity {

    private static final int        CREATE = 1;
    private static final int        EDIT = 2;
    private static final int        REQUEST = 3;

    private final List<Contact>     contactList = new ArrayList<>();
    private ArrayAdapter<Contact>   listViewAdapter;
    private ListView                listView;
    private final Context           context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        databaseHelper.test();                                                                      //DELETE
        List<Contact> contacts = databaseHelper.getAllContacts();

        this.listView = (ListView) findViewById(R.id.contact_list);
        this.listViewAdapter = new ArrayAdapter<Contact>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                this.contactList
        );
        this.listView.setAdapter(this.listViewAdapter);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Contact contact = (Contact) listView.getItemAtPosition(i);
                contact = databaseHelper.getContact(contact.getId()); //Retrieve all info about the wanted contact
                Intent intent = new Intent(context, AddEditContactActivity.class);
                startActivityForResult(intent, REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST) {
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
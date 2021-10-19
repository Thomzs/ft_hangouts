package edu.tmeyer.ft_hangouts;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.tmeyer.ft_hangouts.activity.CustomActivityResult;
import edu.tmeyer.ft_hangouts.database.Contact;
import edu.tmeyer.ft_hangouts.database.DatabaseHelper;

//Touch events for both language & create button

public class MainActivity extends AppCompatActivity {

    public static final int         MODE_CREATE = 1;
    public static final int         MODE_EDIT = 2;

    private final ArrayList<Contact>contactList = new ArrayList<>();
    private ContactAdapter          listViewAdapter;
    private ListView                listView;

    protected final CustomActivityResult<Intent, ActivityResult>
                                    activityLauncher = CustomActivityResult.registerActivityForResult(this);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<Contact> contacts = databaseHelper.getAllContacts();

        this.contactList.addAll(contacts);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.contactList.sort(Comparator.comparing(Contact::getLastName));
        }
        this.listView = (ListView) findViewById(R.id.contact_list);
        this.listViewAdapter = new ContactAdapter(this, this.contactList);
        this.listView.setAdapter(this.listViewAdapter);
        this.listView.setOnItemClickListener((adapterView, view, i, l) -> { //Click on a contact to edit it
            Contact contact = (Contact) listView.getItemAtPosition(i);
            contact = databaseHelper.getContact(contact.getId()); //Retrieve all info about the contact wanted
            Intent intent = new Intent(this, AddEditContactActivity.class);
            intent.putExtra("contact", contact);
            intent.putExtra("mode", MODE_EDIT);
            openContactActivityForResult(intent);
        });
        this.listView.setOnTouchListener((view, motionEvent) -> { //Hide keyboard on touch on list
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return false;
        });

        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listViewAdapter.getFilter().filter(newText);
                return true;
            }
        });

        TextView newButton = (TextView) findViewById(R.id.new_button);
        newButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddEditContactActivity.class);
            intent.putExtra("mode", MODE_CREATE);
            openContactActivityForResult(intent);
        });
    }

    private void openContactActivityForResult(Intent intent) {
        activityLauncher.launch(intent, result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                boolean needRefresh = data.getBooleanExtra("needRefresh", true);

                if (needRefresh) {
                    DatabaseHelper databaseHelper = new DatabaseHelper(this);
                    List<Contact> list = databaseHelper.getAllContacts();

                    this.contactList.clear();
                    this.contactList.addAll(list);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        this.contactList.sort(Comparator.comparing(Contact::getLastName));
                    }
                    this.listViewAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
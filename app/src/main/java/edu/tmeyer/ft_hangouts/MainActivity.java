package edu.tmeyer.ft_hangouts;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.tmeyer.ft_hangouts.activity.CustomActivityResult;
import edu.tmeyer.ft_hangouts.activity.PermissionsUtils;
import edu.tmeyer.ft_hangouts.database.Contact;
import edu.tmeyer.ft_hangouts.database.DatabaseHelper;

//Touch events for both language & create button

public class MainActivity extends AppCompatActivity {

    public static final int         MODE_CREATE = 1;
    public static final int         MODE_EDIT = 2;

    private final ArrayList<Contact>contactList = new ArrayList<>();
    private ContactAdapter          listViewAdapter;
    private ListView                listView;
    private SwipeRefreshLayout      refreshLayout;

    private String                  color;

    protected SharedPreferences     sharedPref;

    protected final CustomActivityResult<Intent, ActivityResult>
                                    activityLauncher = CustomActivityResult.registerActivityForResult(this);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);

        this.sharedPref = this.getPreferences(MODE_PRIVATE);
        this.color = this.sharedPref.getString("headerColor", getResources().getString(R.string.white));
        updateColor(this.color); //headerColor

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
            Intent intent = new Intent(this, AddEditContactActivity.class);
            intent.putExtra("contact", contact.getId());
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

        this.refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        this.refreshLayout.setOnRefreshListener(this::doUpdate);

        TextView newButton = (TextView) findViewById(R.id.new_button);
        newButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddEditContactActivity.class);
            intent.putExtra("mode", MODE_CREATE);
            openContactActivityForResult(intent);
        });

        TextView settingsButton = (TextView) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> showSettingsMenu());
        askForReceiveSMS();
    }

    private void openContactActivityForResult(Intent intent) {
        activityLauncher.launch(intent, result -> {
            try {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    boolean needRefresh = data.getBooleanExtra("needRefresh", true);

                    if (needRefresh) {
                        needRefresh();
                    }
                }
            } catch (NullPointerException ignored) {}
        });
    }

    private void askForReceiveSMS() {
        if (!PermissionsUtils.hasPermission(this, Manifest.permission.RECEIVE_SMS)) {
            PermissionsUtils.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 0);
            if (!PermissionsUtils.hasPermission(this, Manifest.permission.RECEIVE_SMS)) {
                Snackbar.make(findViewById(R.id.layout), R.string.need_receive_sms_permission, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void needRefresh() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<Contact> list = databaseHelper.getAllContacts();

        this.contactList.clear();
        this.contactList.addAll(list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.contactList.sort(Comparator.comparing(Contact::getLastName));
        }
        this.listViewAdapter.notifyDataSetChanged();
    }

    private void doUpdate() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        if (databaseHelper.getCount() != this.contactList.size()) {
            needRefresh();
        }
        this.refreshLayout.setRefreshing(false);
    }

    @SuppressLint("NonConstantResourceId")
    private void showSettingsMenu() {
        MaterialAlertDialogBuilder dialogMenu = new MaterialAlertDialogBuilder(this);
        View view = this.getLayoutInflater().inflate(R.layout.dialog_menu, null);
        TextInputLayout tmp = view.findViewById(R.id.menu_locale);
        AutoCompleteTextView colorView = (AutoCompleteTextView) tmp.getEditText();

        assert colorView != null;
        colorView.setText(this.color);
        colorView.setAdapter(new ArrayAdapter<>(this, R.layout.list_item, getResources().getStringArray(R.array.colors)));
        dialogMenu
                .setTitle(R.string.settings)
                .setCancelable(true)
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newColor = colorView.getText().toString();
                    SharedPreferences.Editor editor = this.sharedPref.edit();
                    int colorCode = updateColor(newColor);
                    this.color = newColor;

                    ((BackgroundObserver) getApplicationContext()).setHeaderColor(colorCode);
                    editor.putString("headerColor", newColor);
                    editor.apply();
                })
                .show();
    }

    private int updateColor(String newColor) {
        int color = ContextCompat.getColor(this, android.R.color.white);

        if (newColor.equalsIgnoreCase(getResources().getString(R.string.white))) {
            color = ContextCompat.getColor(this, android.R.color.white);
        } else if (newColor.equalsIgnoreCase(getResources().getString(R.string.red))) {
            color = ContextCompat.getColor(this, android.R.color.holo_red_light);
        } else if (newColor.equalsIgnoreCase(getResources().getString(R.string.blue))) {
            color = ContextCompat.getColor(this, android.R.color.holo_blue_bright);
        }
        changeHeaderColor(color);
        return color;
    }

    private void changeHeaderColor(Integer color) {
        Toolbar toolbar = findViewById(R.id.toolbar2);
        SearchView searchView = findViewById(R.id.searchView);

        if (color == null) {
            color = ContextCompat.getColor(this, android.R.color.white);
        }
        toolbar.setBackground(new ColorDrawable(color));
        searchView.setBackgroundColor(color);
    }
}
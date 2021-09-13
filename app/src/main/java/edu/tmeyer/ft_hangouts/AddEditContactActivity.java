package edu.tmeyer.ft_hangouts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import edu.tmeyer.ft_hangouts.database.Contact;
import edu.tmeyer.ft_hangouts.database.DatabaseHelper;

public class AddEditContactActivity extends AppCompatActivity {

    private EditText    textFirstName;
    private EditText    textLastName;
    private EditText    textPhone;
    private EditText    textNote;
    private TextView    buttonCancel;
    private TextView    buttonOk;
    private TextView    buttonDelete;

    private Contact     contact;
    private boolean     needRefresh;
    private int         mode;


    private class GenericTextWatcher implements TextWatcher {

        private GenericTextWatcher() {
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            enableOkIfReady();
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_add_edit_contact);

        //Declaration of fields and TextChangedEvent to activate or deactivate the OK button
        this.textFirstName = (EditText) this.findViewById(R.id.contact_first_name);
        this.textFirstName.addTextChangedListener(new GenericTextWatcher());
        this.textLastName = (EditText) this.findViewById(R.id.contact_last_name);
        this.textLastName.addTextChangedListener(new GenericTextWatcher());
        this.textPhone = (EditText) this.findViewById(R.id.contact_phone);
        this.textPhone.addTextChangedListener(new GenericTextWatcher());
        this.textNote = (EditText) this.findViewById(R.id.contact_note);
        this.textNote.addTextChangedListener(new GenericTextWatcher());
        this.buttonCancel = (TextView) this.findViewById(R.id.cancel_button);
        this.buttonDelete = (TextView) this.findViewById(R.id.button_delete);
        this.buttonOk = (TextView) this.findViewById(R.id.ok_button);


        Intent intent = this.getIntent();
        this.mode = (int) intent.getSerializableExtra("mode");
        if (mode == MainActivity.MODE_EDIT) {
            this.contact = (Contact) intent.getSerializableExtra("contact");
            this.textFirstName.setText(contact.getFirstName());
            this.textLastName.setText(contact.getLastName());
            this.textPhone.setText(contact.getPhone());
            this.textNote.setText(contact.getNote());

            //Delete button is enabled only in edit mode, otherwise it is disabled
            this.buttonDelete.setOnClickListener(view -> {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.delete_warning)
                        .setCancelable(true)
                        .setPositiveButton(
                                R.string.delete,
                                (dialogInterface, i) -> {
                                    DatabaseHelper databaseHelper = new DatabaseHelper(this);
                                    databaseHelper.deleteContact(contact);
                                    this.needRefresh = true;
                                    onBackPressed();
                                })
                        .show();

            });
        } else {
            this.mode = MainActivity.MODE_CREATE;
            this.buttonDelete.setVisibility(View.INVISIBLE);
        }

        this.buttonCancel.setOnClickListener(view -> {
            this.onBackPressed(); //On cancel pressed, do nothing just get back to main view
        });

        this.buttonOk.setClickable(mode == MainActivity.MODE_EDIT);
        this.buttonOk.setOnClickListener(view -> {
            DatabaseHelper databaseHelper = new DatabaseHelper(this);

            if (mode == MainActivity.MODE_CREATE) {
                Contact contact = new Contact(
                        this.textFirstName.getText().toString(),
                        this.textLastName.getText().toString(),
                        this.textPhone.getText().toString(),
                        this.textNote.getText().toString(),
                        new byte[0]
                );
                databaseHelper.addContact(contact);
            } else {
                this.contact.setFirstName(this.textFirstName.getText().toString());
                this.contact.setLastName(this.textLastName.getText().toString());
                this.contact.setPhone(this.textPhone.getText().toString());
                this.contact.setNote(this.textNote.getText().toString());
                databaseHelper.updateContact(contact);
            }
            this.needRefresh = true;
            onBackPressed();
        });

    }

    public void enableOkIfReady() {
        boolean isReady = this.textFirstName.getText().toString().length() > 0
                || this.textLastName.getText().toString().length() > 0
                || this.textPhone.getText().toString().length() > 0
                || this.textNote.getText().toString().length() > 0;

        this.buttonOk.setClickable(isReady);
        if (isReady) {
            this.buttonOk.setTextColor(Color.parseColor("#659DB3"));
        } else {
            this.buttonOk.setTextColor(Color.parseColor("#808080"));
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();

        data.putExtra("needRefresh", this.needRefresh);
        this.setResult(Activity.RESULT_OK, data);
        super.finish();
    }
}

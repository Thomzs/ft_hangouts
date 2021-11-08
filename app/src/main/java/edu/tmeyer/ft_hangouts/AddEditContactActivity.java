package edu.tmeyer.ft_hangouts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import edu.tmeyer.ft_hangouts.activity.CustomActivityResult;
import edu.tmeyer.ft_hangouts.activity.PermissionsUtils;
import edu.tmeyer.ft_hangouts.database.Contact;
import edu.tmeyer.ft_hangouts.database.DatabaseHelper;
import edu.tmeyer.ft_hangouts.image.ImageHelper;

public class AddEditContactActivity extends AppCompatActivity {

    private EditText        textFirstName;
    private EditText        textLastName;
    private EditText        textPhone;
    private EditText        textNote;
    private TextView        buttonCancel;
    private TextView        buttonOk;
    private TextView        buttonDelete;
    private ImageView       imageContact;
    private LinearLayout    linearLayout;
    private FloatingActionButton callButton;
    private FloatingActionButton textButton;

    private Contact         contact;
    private boolean         needRefresh;
    private int             mode;

    protected final CustomActivityResult<Intent, ActivityResult>
                            activityLauncher = CustomActivityResult.registerActivityForResult(this);
    private final DatabaseHelper  databaseHelper = new DatabaseHelper(this);


    private Context         context = this;
    private PictureMode     pictureStatus = PictureMode.DEFAULT;

    private enum PictureMode {
        CHANGED, DELETED, DEFAULT;
    }

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


    @SuppressLint("ClickableViewAccessibility")
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
        this.textNote.setOnTouchListener((view, event) -> {
                if (view.getId() == R.id.contact_note) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction()&MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            });

        this.buttonCancel = (TextView) this.findViewById(R.id.cancel_button);
        this.buttonDelete = (TextView) this.findViewById(R.id.button_delete);
        this.buttonOk = (TextView) this.findViewById(R.id.ok_button);

        this.imageContact = (ImageView) this.findViewById(R.id.contact_image);


        Intent intent = this.getIntent();
        this.mode = (int) intent.getSerializableExtra("mode");
        if (mode == MainActivity.MODE_EDIT) {
            long id = (long) intent.getSerializableExtra("contact");
            this.contact = databaseHelper.getContact(id);
            this.textFirstName.setText(contact.getFirstName());
            this.textLastName.setText(contact.getLastName());
            this.textPhone.setText(contact.getPhone());
            this.textNote.setText(contact.getNote());

            byte[] picture = this.contact.getPicture();
            if (picture == null || picture.length == 0) {
                try {
                    InputStream is = getAssets().open("default_contact.png");
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    this.imageContact.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bitmap, 1000));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Bitmap bmp = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                this.imageContact.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bmp, 1000));
            }

            //Delete button is enabled only in edit mode, otherwise it is disabled
            this.buttonDelete.setOnClickListener(view -> {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.delete_warning)
                        .setCancelable(true)
                        .setPositiveButton(
                                R.string.delete,
                                (dialogInterface, i) -> {
                                    databaseHelper.deleteContact(contact);
                                    this.needRefresh = true;
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra("needRefresh", this.needRefresh);
                                    setResult(Activity.RESULT_OK, returnIntent);
                                    finish();
                                })
                        .show();

            });
        } else {
            this.buttonOk.setClickable(false);
            this.mode = MainActivity.MODE_CREATE;
            this.buttonDelete.setVisibility(View.INVISIBLE);
            try {
                InputStream is = getAssets().open("default_contact.png");
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                this.imageContact.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bitmap, 1000));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.buttonCancel.setOnClickListener(view -> {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish(); //On cancel pressed, do nothing just get back to main view
        });

        this.buttonOk.setOnClickListener(view -> {
            DatabaseHelper databaseHelper = new DatabaseHelper(this);

            if (mode == MainActivity.MODE_CREATE) {
                Contact contact = new Contact(
                        this.textFirstName.getText().toString().trim(),
                        this.textLastName.getText().toString().trim(),
                        this.textPhone.getText().toString().trim(),
                        this.textNote.getText().toString().trim(),
                        this.pictureStatus == PictureMode.CHANGED ? getPicture() : new byte[0]
                );
                databaseHelper.addContact(contact);
            } else {
                this.contact.setFirstName(this.textFirstName.getText().toString().trim());
                this.contact.setLastName(this.textLastName.getText().toString().trim());
                this.contact.setPhone(this.textPhone.getText().toString().trim());
                this.contact.setNote(this.textNote.getText().toString().trim());
                if (this.pictureStatus == PictureMode.CHANGED) {
                    this.contact.setPicture(getPicture());
                } else if (this.pictureStatus == PictureMode.DELETED) {
                    this.contact.setPicture(new byte[0]);
                }
                databaseHelper.updateContact(contact);
            }
            this.needRefresh = true;
            Intent returnIntent = new Intent();
            returnIntent.putExtra("needRefresh", this.needRefresh);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });
        this.buttonOk.setClickable(mode == MainActivity.MODE_EDIT);

        this.linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        this.linearLayout.setOnTouchListener((view, motionEvent) -> { //Hide keyboard on touch elsewhere
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return false;
        });

        this.callButton = (FloatingActionButton) findViewById(R.id.call_button);
        this.callButton.setOnClickListener(view -> {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse(this.textPhone.getText().toString()));
                startActivity(callIntent);
            } catch (Exception e) {
                Toast toast = Toast.makeText(this, R.string.incorrect_call, Toast.LENGTH_LONG);
                toast.show();
            }
        });

        this.textButton = (FloatingActionButton) findViewById(R.id.message_button);
        this.textButton.setOnClickListener(new SMSDialog(this, textPhone.getText().toString()));

        registerForContextMenu(this.imageContact);
    }

    public void enableOkIfReady() {
        boolean isReady = this.textFirstName.getText().toString().length() > 0
                || this.textLastName.getText().toString().length() > 0
                || this.textPhone.getText().toString().length() > 0
                || this.textNote.getText().toString().length() > 0;

        this.buttonOk.setClickable(isReady);
        if (isReady) {
            this.buttonOk.setTextColor(Color.parseColor("#78929F"));
        } else {
            this.buttonOk.setTextColor(Color.parseColor("#808080"));
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        byte[] picture;

        if (this.mode == MainActivity.MODE_EDIT) {
            picture = this.contact.getPicture();
        } else {
            picture = null;
        }

        if (picture != null && picture.length > 0) {
            menu.add(0, 0, 1, R.string.delete_picture);
        }
        menu.add(0, 1, 0, R.string.select_picture);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0: //delete picture
                try {
                    InputStream is = getAssets().open("default_contact.png");
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    this.imageContact.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bitmap, 1000));
                    this.pictureStatus = PictureMode.DELETED;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1: //Select a picture
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                openGalleryForResult(chooserIntent);
                this.pictureStatus = PictureMode.CHANGED;
                break;
        }
        return true;
    }

    private void openGalleryForResult(Intent chooserIntent) {

        activityLauncher.launch(chooserIntent, result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent resultData = result.getData();

                if (resultData != null) {
                    Uri selectedImageUri = resultData.getData();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    try {
                        InputStream is = getContentResolver().openInputStream(selectedImageUri);
                        byte[] buf = new byte[1024];
                        int n;

                        while (-1 != (n = is.read(buf)))
                            baos.write(buf, 0, n);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    byte[] newPicture = baos.toByteArray();
                    Bitmap bmp = BitmapFactory.decodeByteArray(newPicture, 0, newPicture.length);

                    this.imageContact.setImageBitmap(ImageHelper.getRoundedCornerBitmap(bmp, 200));
                }
            }
        });
    }

    private byte[] getPicture() {
        try {
            Bitmap bitmap = ((BitmapDrawable) this.imageContact.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}

package edu.tmeyer.ft_hangouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.logging.Logger;

import edu.tmeyer.ft_hangouts.database.Contact;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private ArrayList<Contact> contacts;

    public ContactAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Contact> contacts) {
        super(context, resource, contacts);
        this.contacts = contacts;
    }


    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        try {
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            Contact contact = contacts.get(position);
            TextView tt = (TextView) v.findViewById(android.R.id.text1);

            if (contact != null && tt != null) {
                tt.setText(Html.fromHtml(contact.toString()));
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().warning(e.getMessage());
        }
        return v;
    }

}

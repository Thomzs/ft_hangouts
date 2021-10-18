package edu.tmeyer.ft_hangouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.logging.Logger;

import edu.tmeyer.ft_hangouts.database.Contact;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private ArrayList<Contact> contacts;
    private ArrayList<Contact> filteredContacts;

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

    @Override
    public Filter getFilter() {
        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredContacts = (ArrayList<Contact>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Contact> FilteredArrList = new ArrayList<Contact>();

                if (contacts == null) {
                    contacts = new ArrayList<Contact>(filteredContacts); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = contacts.size();
                    results.values = contacts;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < contacts.size(); i++) {
                        String data = contacts.get(i).toPlainString();
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            FilteredArrList.add(new Contact(contacts.get(i)));
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
    }
}

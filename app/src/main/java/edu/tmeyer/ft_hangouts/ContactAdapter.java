package edu.tmeyer.ft_hangouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.logging.Logger;

import edu.tmeyer.ft_hangouts.database.Contact;

public class ContactAdapter extends BaseAdapter implements Filterable {

    private ArrayList<Contact> contacts = new ArrayList<>(0);
    private ArrayList<Contact> filteredContacts = new ArrayList<>(0);
    LayoutInflater inflater;

    public ContactAdapter(@NonNull Context context, @NonNull ArrayList<Contact> contacts) {
        this.contacts = contacts;
        this.filteredContacts = contacts;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return filteredContacts.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        try {
            if (v == null) {
                v = this.inflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            Contact contact = filteredContacts.get(position);
            TextView tt = (TextView) v.findViewById(android.R.id.text1);

            if (contact != null && tt != null) {
                tt.setText(Html.fromHtml(contact.toHTMLString()));
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
                        String data = contacts.get(i).toString();
                        if (data.toLowerCase().contains(constraint.toString().toLowerCase())) {
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

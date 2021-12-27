package com.datasolvent.runawaze.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.datasolvent.runawaze.R;

import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<Contact> {
    private ArrayList<Contact> items;
    private Context adapterContext;

    public ContactAdapter(Context context, ArrayList<Contact> items) {
        super(context, R.layout.list_item, items);
        adapterContext = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        try {
            Contact contact = items.get(position);

            if (v == null) {

                LayoutInflater vi = (LayoutInflater) adapterContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }

            TextView contactName = v.findViewById(R.id.textContactName);
            TextView contactNumber = v.findViewById(R.id.textPhoneNumber);
            TextView raceTypeTextView = v.findViewById(R.id.raceTypeTextView);
            Button b = v.findViewById(R.id.buttonDeleteContact);

            contactName.setText(contact.getContactName());
            contactNumber.setText(contact.getPhoneNumber());
            raceTypeTextView.setText(contact.getRaceType());
            b.setVisibility(View.INVISIBLE);


        } catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }

        return v;
    }


    //show delete button when user click on a contact
    public void showDelete(final int position, final View convertView, final Context context, final Contact contact) {
        View v = convertView;
        final Button b = v.findViewById(R.id.buttonDeleteContact);

        if (b.getVisibility() == View.INVISIBLE) {
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideDelete(position, convertView, context);
                    items.remove(contact);
                    deleteOption(contact.getContactID(), context);
                }
            });
        } else {
            hideDelete(position, convertView, context);
        }

    }

    //delete contact when user click on delete
    private void deleteOption(int contactToDelete, Context context) {
        ContactDataSource db = new ContactDataSource(context);
        try {
            db.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.deleteContact(contactToDelete);
        db.close();
        this.notifyDataSetChanged();

    }

    //hide delete contacts when user press on done deleting
    private void hideDelete(int position, View convertView, Context context) {
        View v = convertView;
        final Button b = v.findViewById(R.id.buttonDeleteContact);
        b.setVisibility(View.INVISIBLE);
        b.setOnClickListener(null);
    }
}


package com.datasolvent.runawaze.contacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.datasolvent.runawaze.R;

import java.sql.SQLException;
import java.util.ArrayList;

public class ContactListActivity extends AppCompatActivity {
    boolean isDeleting = false;
    ListView listView;
    ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        listView = findViewById(R.id.list);

        initListButton();
        initMapButton();
        initSettingsButton();

        //Delete & Add Button at the toolbar section
        initDeleteButton();
        initAddContactButton();


    }


    /**
     * 1.4 Completing the ContactList Activity
     * We placed fetching contacts data in onResume rather than onCreate
     * to gurantee updating list every time we change sort type form setting activity
     * see - 1.4 / Sort the Contacts List
     * */
    @Override
    protected void onResume() {
        super.onResume();

        //retrieve sort type form the shared preferences
        String sortBy = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getString("sortfield", "contactname");
        String sortOrder = getSharedPreferences("MyContactListPreferences", Context.MODE_PRIVATE).getString("sortorder", "ASC");

        ContactDataSource ds = new ContactDataSource(this);

        try {
            ds.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //read all contacts sorted
        final ArrayList<Contact> contacts = ds.getContacts(sortBy, sortOrder);
        ds.close();

        //if there are contacts saved
        //show them in the list
        if (contacts.size() > 0) {
            adapter = new ContactAdapter(this, contacts);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View itemClicked, int position, long l) {
                    Contact selectedContact = contacts.get(position);
                    if (isDeleting) {
                        adapter.showDelete(position, itemClicked, ContactListActivity.this, selectedContact);
                    } else {
                        Intent intent = new Intent(ContactListActivity.this, ContactActivity.class);
                        intent.putExtra("contactid", selectedContact.getContactID());
                        startActivity(intent);
                    }
                }
            });

            //if there is no contacts, go to ContactActivity to create new contacts..
        } else {
            Intent intent = new Intent(ContactListActivity.this, ContactActivity.class);
            startActivity(intent);
        }

    }

    /**
     * 1.3 Complex Lists
     *  init buttonDelete button
     *  this button enable/disable deleting contacts option
     * */
    private void initDeleteButton() {
        final Button deleteButton = findViewById(R.id.buttonDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDeleting) {
                    deleteButton.setText("Delete");
                    isDeleting = false;
                    adapter.notifyDataSetChanged();
                } else {
                    deleteButton.setText("Done Deleting");
                    isDeleting = true;
                }
            }
        });

    }

    /**
     *  1.4 Completing the ContactList Activity
     *  init buttonAdd button
     *  this button displays the contact in the ContactActivity to edit
     * */
    private void initAddContactButton() {
        Button newContact = (Button) findViewById(R.id.buttonAdd);
        newContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactListActivity.this, ContactActivity.class);
                startActivity(intent);
            }
        });
    }


    private void initListButton() {
        ImageButton list = (ImageButton) findViewById(R.id.imageButtonList);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactListActivity.this, ContactListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initMapButton() {
        ImageButton list = (ImageButton) findViewById(R.id.imageButtonMap);
        list.setVisibility(View.INVISIBLE);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactListActivity.this, ContactMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initSettingsButton() {
        ImageButton list = (ImageButton) findViewById(R.id.imageButtonSettings);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactListActivity.this, ContactSettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}


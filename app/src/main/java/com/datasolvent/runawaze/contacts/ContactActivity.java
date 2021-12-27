package com.datasolvent.runawaze.contacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.datasolvent.runawaze.R;

import java.sql.SQLException;

public class ContactActivity extends AppCompatActivity implements DatePickerDialog.SaveDateListener, AdapterView.OnItemSelectedListener {
    private Contact currentContact;
    private boolean editingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //1.4 Completing the ContactList Activity
        //check if there is a contact send from ContactListActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //display that contact
            initContact(extras.getInt("contactid"));
        } else {
            currentContact = new Contact();
        }

        initListButton();
        initMapButton();
        initSettingsButton();
        initToggleButton();
        setForEditing(false);
        initChangeDateButton();
        initTextChangedEvents();
        initSaveButton();
    }


    /**
     * 1.4 Completing the ContactList Activity
     * Display contact details in the ContactActivity
     * @param id contact id to fetch and display
     * */
    private void initContact(int id) {
        ContactDataSource ds = new ContactDataSource(ContactActivity.this);
        try {
            ds.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentContact = ds.getSpecificContact(id);
        ds.close();

        Log.i("HINT",""+currentContact.getContactID());

        EditText editName = (EditText) findViewById(R.id.editName);
        EditText editAddress = (EditText) findViewById(R.id.editAddress);
        EditText editCity = (EditText) findViewById(R.id.editCity);
        EditText editState = (EditText) findViewById(R.id.editState);
        EditText editZipCode = (EditText) findViewById(R.id.editZipcode);
        EditText editPhone = (EditText) findViewById(R.id.editHome);
        EditText editCell = (EditText) findViewById(R.id.editCell);
        EditText editEmail = (EditText) findViewById(R.id.editEMail);
        TextView birthDay = (TextView) findViewById(R.id.textBirthday);

        editName.setText(currentContact.getContactName());
        editAddress.setText(currentContact.getStreetAddress());
        editCity.setText(currentContact.getCity());
        editState.setText(currentContact.getState());
        editZipCode.setText(currentContact.getZipCode());
        editPhone.setText(currentContact.getPhoneNumber());
        editCell.setText(currentContact.getCellNumber());
        editEmail.setText(currentContact.getEMail());
        birthDay.setText(DateFormat.format("MM/dd/yyyy", currentContact.getBirthday().toMillis(false)).toString());

        initSpinner();
    }


    private void initListButton() {
        ImageButton list = (ImageButton) findViewById(R.id.imageButtonList);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactActivity.this, ContactListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initMapButton() {
        ImageButton list = (ImageButton) findViewById(R.id.imageButtonMap);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactActivity.this, ContactMapActivity.class);
                if (currentContact.getContactID() == -1) {
                    Toast.makeText(getBaseContext(), "Contact must be saved before it can be mapped", Toast.LENGTH_SHORT).show();
                } else {
                    intent.putExtra("contactid", currentContact.getContactID());
                }
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
                Intent intent = new Intent(ContactActivity.this, ContactSettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void initToggleButton() {
        final ToggleButton editToggle = (ToggleButton) findViewById(R.id.toggleButtonEdit);
        editToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                setForEditing(editToggle.isChecked());
            }
        });
    }

    private void initSaveButton() {
        Button saveButton = (Button) findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideKeyboard();
                ContactDataSource ds = new ContactDataSource(ContactActivity.this);  //1
                try {
                    ds.open();                                                       //2
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                boolean wasSuccessful = false;                                      //3
                if (currentContact.getContactID() == -1) {                          //4
                    wasSuccessful = ds.insertContact(currentContact);
                    int newID = ds.getLastContactId();
                    currentContact.setContactID(newID);
                } else {
                    wasSuccessful = ds.updateContact(currentContact);
                }
                ds.close();                                                         //5

                if (wasSuccessful) {                                                //6
                    ToggleButton editToggle = (ToggleButton) findViewById(R.id.toggleButtonEdit);
                    editToggle.toggle();
                    setForEditing(false);
                }
            }

        });
    }

    private void initTextChangedEvents() {

        final EditText contactName = (EditText) findViewById(R.id.editName);
        contactName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setContactName(contactName.getText().toString());
            }
        });

        final EditText streetAddress = (EditText) findViewById(R.id.editAddress);
        streetAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setStreetAddress(streetAddress.getText().toString());
            }
        });

        final EditText city = (EditText) findViewById(R.id.editCity);
        city.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setCity(city.getText().toString());
            }
        });

        final EditText state = (EditText) findViewById(R.id.editState);
        state.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setState(state.getText().toString());
            }
        });

        final EditText zipCode = (EditText) findViewById(R.id.editZipcode);
        zipCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setZipCode(zipCode.getText().toString());
            }
        });

        final EditText homePhone = (EditText) findViewById(R.id.editHome);
        homePhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setPhoneNumber(homePhone.getText().toString());
            }
        });

        final EditText cellPhone = (EditText) findViewById(R.id.editCell);
        cellPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setCellNumber(cellPhone.getText().toString());
            }
        });

        final EditText eMail = (EditText) findViewById(R.id.editEMail);
        eMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentContact.setEMail(eMail.getText().toString());
            }
        });

        homePhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        cellPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

    }

    private void setForEditing(boolean enabled) {
        editingEnabled = enabled;

        EditText editName = (EditText) findViewById(R.id.editName);
        EditText editAddress = (EditText) findViewById(R.id.editAddress);
        EditText editCity = (EditText) findViewById(R.id.editCity);
        EditText editState = (EditText) findViewById(R.id.editState);
        EditText editZipCode = (EditText) findViewById(R.id.editZipcode);
        EditText editPhone = (EditText) findViewById(R.id.editHome);
        EditText editCell = (EditText) findViewById(R.id.editCell);
        EditText editEmail = (EditText) findViewById(R.id.editEMail);
        Button buttonChange = (Button) findViewById(R.id.btnBirthday);
        Button buttonSave = (Button) findViewById(R.id.buttonSave);

        editName.setEnabled(enabled);
        editAddress.setEnabled(enabled);
        editCity.setEnabled(enabled);
        editState.setEnabled(enabled);
        editZipCode.setEnabled(enabled);
        editPhone.setEnabled(enabled);
        editCell.setEnabled(enabled);
        editEmail.setEnabled(enabled);
        buttonChange.setEnabled(enabled);
        buttonSave.setEnabled(enabled);

        initSpinner();

        if (enabled) {
            editName.requestFocus();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        EditText editName = (EditText) findViewById(R.id.editName);
        imm.hideSoftInputFromWindow(editName.getWindowToken(), 0);
        EditText editAddress = (EditText) findViewById(R.id.editAddress);
        imm.hideSoftInputFromWindow(editAddress.getWindowToken(), 0);
        EditText editCity = (EditText) findViewById(R.id.editCity);
        imm.hideSoftInputFromWindow(editCity.getWindowToken(), 0);
        EditText editState = (EditText) findViewById(R.id.editState);
        imm.hideSoftInputFromWindow(editState.getWindowToken(), 0);
        EditText editZipCode = (EditText) findViewById(R.id.editZipcode);
        imm.hideSoftInputFromWindow(editZipCode.getWindowToken(), 0);
        EditText editPhone = (EditText) findViewById(R.id.editHome);
        imm.hideSoftInputFromWindow(editPhone.getWindowToken(), 0);
        EditText editCell = (EditText) findViewById(R.id.editCell);
        imm.hideSoftInputFromWindow(editCell.getWindowToken(), 0);
        EditText editEmail = (EditText) findViewById(R.id.editEMail);
        imm.hideSoftInputFromWindow(editEmail.getWindowToken(), 0);
    }

    private void initChangeDateButton() {
        Button changeDate = (Button) findViewById(R.id.btnBirthday);
        changeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                DatePickerDialog datePickerDialog = new DatePickerDialog();
                datePickerDialog.show(fm, "DatePick");
            }
        });
    }

    @Override
    public void didFinishDatePickerDialog(Time selectedTime) {
        TextView birthDay = (TextView) findViewById(R.id.textBirthday);
        birthDay.setText(DateFormat.format("MM/dd/yyyy", selectedTime.toMillis(false)).toString());
        currentContact.setBirthday(selectedTime);
    }

    private void initSpinner() {
        Spinner raceTypeSpinner = (Spinner) findViewById(R.id.raceTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.race_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceTypeSpinner.setAdapter(adapter);
        raceTypeSpinner.setOnItemSelectedListener(this);

        if (editingEnabled) {
            raceTypeSpinner.setEnabled(true);
        } else {
            raceTypeSpinner.setEnabled(false);
        }

        String raceType = currentContact.getRaceType();

        switch(raceType) {
            case ("Marathon"): {
                raceTypeSpinner.setSelection(0);

                break;
            } case ("Half Marathon"): {
                raceTypeSpinner.setSelection(1);

                break;
            } case ("Ten Miler"): {
                raceTypeSpinner.setSelection(2);

                break;
            } case ("10K"): {
                raceTypeSpinner.setSelection(3);

                break;
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        currentContact.setRaceType(parent.getItemAtPosition(pos).toString());
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }
}
package com.datasolvent.runawaze.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;

import java.sql.SQLException;
import java.util.ArrayList;

public class ContactDataSource {
    private SQLiteDatabase database;
    private ContactDBHelper dbHelper;

    public ContactDataSource(Context context) {
        dbHelper = new ContactDBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean insertContact(Contact c) {
        boolean didSucceed = false;
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put("contactname", c.getContactName());
            initialValues.put("streetaddress", c.getStreetAddress());
            initialValues.put("city", c.getCity());
            initialValues.put("state", c.getState());
            initialValues.put("zipcode", c.getZipCode());
            initialValues.put("phonenumber", c.getPhoneNumber());
            initialValues.put("cellnumber", c.getCellNumber());
            initialValues.put("email", c.getEMail());
            initialValues.put("birthday", String.valueOf(c.getBirthday().toMillis(false)));
            initialValues.put("racetype", c.getRaceType());

            didSucceed = database.insert("contact", null, initialValues) > 0;
        } catch (Exception e) {
            //Do nothing - will return false if there is an exception
        }
        return didSucceed;
    }

    public boolean updateContact(Contact c) {
        boolean didSucceed = false;
        try {
            Long rowId = Long.valueOf(c.getContactID()); //6
            ContentValues updateValues = new ContentValues();
            updateValues.put("contactname", c.getContactName());
            updateValues.put("streetaddress", c.getStreetAddress());
            updateValues.put("city", c.getCity());
            updateValues.put("state", c.getState());
            updateValues.put("zipcode", c.getZipCode());
            updateValues.put("phonenumber", c.getPhoneNumber());
            updateValues.put("cellnumber", c.getCellNumber());
            updateValues.put("email", c.getEMail());
            updateValues.put("birthday", String.valueOf(c.getBirthday().toMillis(false)));
            updateValues.put("racetype", c.getRaceType());

            didSucceed = database.update("contact", updateValues, "_id=" + rowId, null) > 0;
        } catch (Exception e) {
            //Do nothing - will return false if there is an exception
        }
        return didSucceed;
    }

    public int getLastContactId() {
        int lastId = -1;
        try {
            String query = "Select Max(_id) from contact";
            Cursor cursor = database.rawQuery(query, null);

            cursor.moveToFirst();
            lastId = cursor.getInt(0);
            cursor.close();
        } catch (Exception e) {
            lastId = -1;
        }
        return lastId;
    }

    /**
     *  1.2 Simple Lists
     * getting contact names only
     * */
    public ArrayList<String> getContactName() {
        ArrayList<String> contactNames = new ArrayList<>();//1
        try {
            String query = "SELECT contactName FROM contact";//2
            Cursor cursor = database.rawQuery(query, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {//3
                contactNames.add(cursor.getString(0));
                cursor.moveToNext();
            }
            cursor.close();

        } catch (Exception e) {
            contactNames = new ArrayList<>(); //4
        }

        return contactNames;
    }

    /**
     *  1.3 Complex Lists
     *  1.4 Sort the Contacts List
     * @param sortField which filed in the contacts table to sort
     * @param sortOrder sorting type ASC or DEC
     *  this method read all contacts from the database
     * @return  ArrayList<Contact>
     * */
    public ArrayList<Contact> getContacts(String sortField,String sortOrder) {
        ArrayList<Contact> contacts = new ArrayList<>();
        try {
            String query = "SELECT * FROM contact ORDER BY " + sortField + " " + sortOrder;
            Cursor cursor = database.rawQuery(query, null);
            Contact newContact;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                newContact = new Contact();
                newContact.setContactID(cursor.getInt(0));
                newContact.setContactName(cursor.getString(1));
                newContact.setStreetAddress(cursor.getString(2));
                newContact.setCity(cursor.getString(3));
                newContact.setState(cursor.getString(4));
                newContact.setZipCode(cursor.getString(5));
                newContact.setPhoneNumber(cursor.getString(6));
                newContact.setCellNumber(cursor.getString(7));
                newContact.setEMail(cursor.getString(8));
                Time t = new Time();
                t.set(Long.valueOf(cursor.getString(9)));
                newContact.setBirthday(t);
                newContact.setRaceType(cursor.getString(10));
                contacts.add(newContact);
                cursor.moveToNext();

            }
            cursor.close();

        } catch (Exception e) {
            contacts = new ArrayList<>();
        }

        return contacts;
    }

    /**
     * 1.3 Complex Lists
     * delete a specific contact with id
     * @param contactId id of contact to delete
     * @return boolean true or false
     * */
    public boolean deleteContact(int contactId) {
        boolean didDelete = false;
        try {
            didDelete = database.delete("contact", "_id=" + contactId, null) > 0;
        } catch (Exception e) {

        }

        return didDelete;
    }

    /**
     * 1.4 Completing the ContactList Activity
     * fetch a specific contact with id
     * @param  contactId id of contact to fetch
     * @return Contact
     * */
    public Contact getSpecificContact(int contactId){
        Contact contact=new Contact();
        String query="SELECT * FROM contact WHERE _id ="+contactId;
        Cursor cursor=database.rawQuery(query,null);
        if(cursor.moveToFirst()){
            contact.setContactID(cursor.getInt(0));
            contact.setContactName(cursor.getString(1));
            contact.setStreetAddress(cursor.getString(2));
            contact.setCity(cursor.getString(3));
            contact.setState(cursor.getString(4));
            contact.setZipCode(cursor.getString(5));
            contact.setPhoneNumber(cursor.getString(6));
            contact.setCellNumber(cursor.getString(7));
            contact.setEMail(cursor.getString(8));
            Time t = new Time();
            t.set(Long.valueOf(cursor.getString(9)));
            contact.setBirthday(t);
            contact.setRaceType(cursor.getString(10));
            cursor.close();
        }
        return contact;
    }

}


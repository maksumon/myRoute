package com.maksumon.myroute;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class ContactDB {

	String name;
	String number;
	String address;
	ProgressDialog progressDialog;

	OutputStreamWriter phoneBook;
	OutputStreamWriter addressBook;

	String vrsn=Build.VERSION.RELEASE;

	Context _context;
	
	ArrayList<String> names;

	/** Called to initialize the class.
	 * context = Application context */
	public ContactDB(Context context){
		_context = context;
	}

	/** Called to initialize variables. */
	public void contactDBInit(){
		name = "";
		number = "";
		address = "";
		names = new ArrayList<String>();

		filesExist();
		openFiles();
		phoneBookEntry();
		addressBookEntry();
	}

	/** Called to open files to parse. */
	public void openFiles(){
		// try to write the content
		try {
			// open Files for writing
			phoneBook = new OutputStreamWriter(_context.openFileOutput("myroute-phone-book.txt",0));
			addressBook = new OutputStreamWriter(_context.openFileOutput("myroute-address-book.txt",0));

		} catch (java.io.IOException e) {
			//do something if an IOException occurs.
		}
	}

	/** Called to check if file exists. */
	public void filesExist(){

		File dir = _context.getFilesDir();
		File phoneBookFile = new File(dir, "myroute-phone-book.txt");
		File addressBookFile = new File(dir, "myroute-address-book.txt");
		boolean phoneExists = phoneBookFile.exists();
		boolean addressExists = addressBookFile.exists();

		if(phoneExists){
			phoneBookFile.delete();
			//Toast.makeText(this, "Phone Book File Deleted", Toast.LENGTH_SHORT).show();
		}
		if(addressExists){
			addressBookFile.delete();
			//Toast.makeText(this, "Address Book File Deleted", Toast.LENGTH_SHORT).show();
		}
	}

	/** Called to traverse through the Contact API and populate list with phone contacts. */
	public void phoneBookEntry(){
		try {
			ContentResolver cr = _context.getContentResolver();
			String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, sortOrder);

			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {

					name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

					if(name != null && !names.contains(name)){

						Cursor pCur = cr.query(
								ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
								null, 
								ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" = ?", 
								new String[]{name}, sortOrder);

						if(pCur.getCount()>0){

							while (pCur.moveToNext()) {

								int phoneType = pCur.getInt(pCur.getColumnIndex(Phone.TYPE));
								if (phoneType == Phone.TYPE_MOBILE)
								{
									number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
									if(number != null){

										phoneBook.write(name + ":" + number + "\n");
										names.add(name);
									}
								}
							}
						}
						pCur.close();
					}
				}
			}
			cur.close();
			phoneBook.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Called to traverse through the Contact API and populate list with address contacts. */
	public void addressBookEntry(){
		try {
			ContentResolver cr = _context.getContentResolver();
			String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, sortOrder);

			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {

					name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

					if(name != null){

						String addrWhere = ContactsContract.Contacts.DISPLAY_NAME + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
						String[] addrWhereParams = new String[]{name,ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE}; 
						Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null, addrWhere, addrWhereParams, null);
						if (addrCur.getCount() > 0){
							while(addrCur.moveToNext()) {


								//								if(vrsn.startsWith("4"))
								//								{
								//									String fa = addrCur.getString(
								//											addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS ));
								//
								//									addressBook.write(name + ":" +fa+ "\n");
								//								}
								//
								//								else
								//								{
								String street = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
								//String city = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
								//String state = addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));

								//addressBook.write(name + ":" + street + " " + city + " " + state + "\n");
								addressBook.write(name + ":" + street + "\n");
								//								}
							}
						}
						addrCur.close();
					}
				}
			}
			cur.close();
			addressBook.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

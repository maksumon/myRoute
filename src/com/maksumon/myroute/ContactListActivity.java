package com.maksumon.myroute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ContactListActivity extends Activity{

	Button btnClose;
	TextView txtTitle;
	ListView listContact;
	boolean cb=true,rb=false;
	
	ArrayAdapter<String> adapter;

	ArrayList<String> namesHasAddress;
	ArrayList<String> address;
	ArrayList<String> phone;
	InputStream addressBookRead;	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.contactlistactivity);
		
		txtTitle = (TextView)findViewById(R.id.txtContactListTitle);
		txtTitle.setText("Choose A Contact");
		
		namesHasAddress = new ArrayList<String>();
		address = new ArrayList<String>();
		phone = new ArrayList<String>();

		// try opening the myfilename.txt
		try {
			// open the file for reading
			addressBookRead = openFileInput("myroute-address-book.txt");

			// if file the available for reading
			if (addressBookRead != null) {
				// prepare the file for reading
				InputStreamReader inputreader = new InputStreamReader(addressBookRead);
				BufferedReader buffreader = new BufferedReader(inputreader);

				String line;
				// read every line of the file into the line-variable, on line at the time
				try {
					while ((line = buffreader.readLine()) != null) {
						// do something with the settings from the file
						if(line.contains(":")){
							String[] separated = line.split(":");
							for(int i=0; i<=1; i++){
								if(separated[i] == separated[0]){
									namesHasAddress.add(separated[0]);
									//address.add(separated[1]);
								} else if(separated[i] == separated[1] && separated[i]!=null){
									address.add(separated[1]);
								}
							}
						}
					}
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}

			// close the file again
			try {
				addressBookRead.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		} catch (java.io.FileNotFoundException e) {
			
			e.printStackTrace();
		}

		btnClose = (Button)findViewById(R.id.btnContactListClose);
		
		listContact=(ListView)findViewById(R.id.listContact);
		listContact.setAdapter(new ArrayAdapter<String>(ContactListActivity.this,android.R.layout.simple_list_item_1,namesHasAddress));
		listContact.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				
				ContentResolver cr = getContentResolver();

				Cursor pCur = cr.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
						null,
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +" = ?",
								new String[]{namesHasAddress.get(position).toString()}, null);

				if(pCur.getCount() > 0){
					while (pCur.moveToNext()) {

						String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
						phone.add(number);
					}
				}

				pCur.close();
				
				Intent data = new Intent();
				data.putExtra("Address", address.get(position));
				data.putExtra("Phone", phone.get(0));

				setResult(RESULT_OK, data);

				finish();				
			}
		});
	}
	
	/** Called when Contact Button on Search Text Field pressed **/
	public void onClosePress(View v){
		
		finish();
	}
	
	/** Called by the system when the device configuration changes while your component is running. */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig); 
	}	
}
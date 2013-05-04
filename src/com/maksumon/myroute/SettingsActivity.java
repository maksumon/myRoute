package com.maksumon.myroute;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: MAKSumon
 * Date: 5/5/13
 * Time: 12:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class SettingsActivity extends PreferenceActivity{

    Button btnClose;
    TextView txtTitle;
    ProgressDialog progressDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.preflayout);

        txtTitle = (TextView)findViewById(R.id.txtSettingsTitle);
        txtTitle.setText("Settings");

        final CheckBoxPreference highwaysPref = (CheckBoxPreference) getPreferenceManager().findPreference("highways");
        final CheckBoxPreference tollsPref = (CheckBoxPreference) getPreferenceManager().findPreference("tolls");
        final Preference btnContactPref = (Preference)findPreference("btnContact");

        highwaysPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().equals("true"))
                {
                    Toast.makeText(getApplicationContext(), "Highways: " + "Enabled", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Highways: " + "Disabled", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        tollsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().equals("true"))
                {
                    Toast.makeText(getApplicationContext(), "Tolls: " + "Enabled", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Tolls: " + "Disabled", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        btnContactPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                //code for what you want it to do

                ContactDBAsync contactDBAsync = new ContactDBAsync();
                contactDBAsync.execute();

                return true;
            }
        });


        btnClose=(Button)findViewById(R.id.btnSettingsClose);
        btnClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    /** Called to re-cache Contact from Address book and DB. */
    public class ContactDBAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(SettingsActivity.this, "Please Wait...", "Refreshing Contact Data", true);
        }

        //@Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                ContactDB contactDB = new ContactDB(SettingsActivity.this.getApplicationContext());
                contactDB.contactDBInit();
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }
    }

    /** Called by the system when the device configuration changes while your component is running. */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

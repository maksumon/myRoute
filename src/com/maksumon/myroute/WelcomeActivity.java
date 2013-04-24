package com.maksumon.myroute;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: MAKSumon
 * Date: 4/24/13
 * Time: 10:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class WelcomeActivity extends Activity {

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new ContactDBAsync().execute();
    }

    /** Called to Geocode address from current latitude and longitude.
     * lt_ = Latitude
     * lg_ = Longitude */
    public String latlongToAddress(double lt, double lg) {

        Geocoder geoCoder = new Geocoder(WelcomeActivity.this);
        List<Address> addresses;

        String a = "";
        try {
            addresses = geoCoder.getFromLocation(lt, lg, 1);
            if (addresses.size() > 0) {

                Address address = addresses.get(0);

                a = address.getAddressLine(0) +", "+
                        address.getLocality() +", "+
                        address.getAdminArea() +", "+
                        address.getCountryCode();
            }
        } catch (IOException e) {

            e.printStackTrace();
        }

        return a;
    }

    /** Called to cache Contact from Address book */
    private class ContactDBAsync extends AsyncTask<String, Integer, String> {

        public static final String PREFS_NAME = "firstrunpreference";
        SharedPreferences firstRunPref;
        boolean firstRun;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(WelcomeActivity.this, "Please Wait...", "Caching Data", true);

            firstRunPref = getSharedPreferences(PREFS_NAME, 0);
            firstRun = firstRunPref.getBoolean("firstRun", true);
        }

        //@Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();

            new GetLocationAsync().execute();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                if(firstRun){

                    SharedPreferences.Editor editor = firstRunPref.edit();
                    editor.putBoolean("firstRun", false);

                    // Commit the edits!
                    editor.commit();

                    ContactDB contactDB = new ContactDB(WelcomeActivity.this.getApplicationContext());
                    contactDB.contactDBInit();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }
    }

    /** Called to cache Contact from Address book */
    private class GetLocationAsync extends AsyncTask<String, Integer, String> {

        //Get Current Location
        GPSTracker gps = new GPSTracker(WelcomeActivity.this);

        double latitude, longitude;
        String startAddress;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(WelcomeActivity.this, "Please Wait...", "Waiting for Location", true);
        }

        //@Override
        protected void onPostExecute(String result) {

            Log.i("Info","Address: " + startAddress);

            progressDialog.dismiss();

            gps.stopUsingGPS();

            Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
            intent.putExtra("address", startAddress);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            startActivity(intent);
            finish();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                // check if GPS enabled
                if(gps.canGetLocation()){

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                    startAddress = latlongToAddress(latitude,longitude);
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }
    }
}

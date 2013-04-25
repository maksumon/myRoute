package com.maksumon.myroute;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import android.widget.TextView.OnEditorActionListener;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	GPSTracker gps;

	private MapView mapView;
	private MapController mapController;
    private MyItemizedOverlay myItemizedOverlay;

    private Drawable marker;
    ResourceProxy resourceProxy;

	private GeoPoint startPoint;
	private GeoPoint searchPoint;

    private Address address;

	AutoCompleteTextView txtSearch;
    AutoCompleteTextView txtDestination;

	Button btnMap, btnDirection, btnSettings;
	Button btnClearSearch, btnContactSearch, btnClearDestination, btnContactDestination;

    RelativeLayout layoutSearch;
    RelativeLayout layoutDestination;

    ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CloudmadeUtil.retrieveCloudmadeKey(getApplicationContext());

        layoutSearch = (RelativeLayout)findViewById(R.id.layoutSearch);
        layoutDestination = (RelativeLayout)findViewById(R.id.layoutDestination);

        Typeface typeface = Typeface.createFromAsset(getAssets(),"Fondamento-Regular.ttf");

        txtSearch = (AutoCompleteTextView)findViewById(R.id.txtSearch);
        txtSearch.setTypeface(typeface);
		txtSearch.addTextChangedListener(textChecker);
		txtSearch.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (addressToLatLong(txtSearch.getText().toString())){

                    myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
                    mapView.getOverlays().add(myItemizedOverlay);
                    myItemizedOverlay.addItem(searchPoint, "", "");
                    mapController.setCenter(searchPoint);
                }

				return false;
			}
		});

        txtDestination = (AutoCompleteTextView)findViewById(R.id.txtDestination);
        txtDestination.setTypeface(typeface);
        txtDestination.addTextChangedListener(textChecker);
        txtDestination.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (addressToLatLong(txtDestination.getText().toString())) {
                    new DirectionRequestAsync().execute();
                }

                return false;
            }
        });

		btnMap = (Button)findViewById(R.id.btnMap);
		btnDirection = (Button)findViewById(R.id.btnDirection);
		btnSettings = (Button)findViewById(R.id.btnSettings);

		btnClearSearch = (Button)findViewById(R.id.btnClearSearch);
		btnContactSearch = (Button)findViewById(R.id.btnContactSearch);

        btnClearDestination = (Button)findViewById(R.id.btnClearDestination);
        btnContactDestination = (Button)findViewById(R.id.btnContactDestination);

		btnMap.setPressed(true);

//		//Get Current Location
//		gps = new GPSTracker(MainActivity.this);
//
//		// check if GPS enabled
//		if(gps.canGetLocation()){
//
//			double latitude = gps.getLatitude();
//			double longitude = gps.getLongitude();
//
//			startPoint = new GeoPoint(latitude,longitude);
//			txtSearch.setText(latlongToAddress(latitude, longitude));
//			btnClearSearch.setVisibility(View.VISIBLE);
//		}else{
//			// can't get location
//			// GPS or Network is not enabled
//			// Ask user to enable GPS/network in settings
//			gps.showSettingsAlert();
//		}

		//startPoint = new GeoPoint(23.822823,90.36256);
		//startPoint = new GeoPoint(34.123581,-118.146332);

        /* taking data from caller activity page */
        // ###################Receiving data starts
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        } else {
            txtSearch.setText(extras.getString("address"));
            startPoint = new GeoPoint(extras.getDouble("latitude"), extras.getDouble("longitude"));
            btnClearSearch.setVisibility(View.VISIBLE);
        }
        // ##### Receiving data ends

		mapView = (MapView)findViewById(R.id.mapView);
		mapView.setTileSource(TileSourceFactory.CLOUDMADESTANDARDTILES);
		mapView.setMultiTouchControls(true);

		mapController = mapView.getController();
		mapController.setZoom(16);
		mapController.setCenter(startPoint);

//		myLocationOverlay = new SimpleLocationOverlay(this, new DefaultResourceProxyImpl(this));
//		mapView.getOverlays().add(myLocationOverlay);
//		myLocationOverlay.setLocation(startPoint);

        Drawable tempMarker = getResources().getDrawable(R.drawable.marker2);
        Bitmap bitmap = ((BitmapDrawable) tempMarker).getBitmap();
        marker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));

        resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

        myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
        mapView.getOverlays().add(myItemizedOverlay);
        myItemizedOverlay.addItem(startPoint, "", "");
	}

	final TextWatcher textChecker = new TextWatcher() {

		public void afterTextChanged(Editable s) {

		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			btnClearSearch.setVisibility(View.VISIBLE);
			btnContactSearch.setVisibility(View.GONE);
		}
	};

	/** Called to Geocode address from current latitude and longitude.
	 * lt_ = Latitude
	 * lg_ = Longitude */
	public String latlongToAddress(double lt, double lg) {

		Geocoder geoCoder = new Geocoder(MainActivity.this);
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

	/** Called to Geocode address.
	 * src = Address */
	public boolean addressToLatLong(String src){

		Geocoder geoCoder = new Geocoder(MainActivity.this);
		List<Address> addresses;
        boolean success = false;

		try {

			addresses = geoCoder.getFromLocationName(src,1);

			if (addresses.size() > 0) {

				address = addresses.get(0);

				searchPoint = new GeoPoint(address.getLatitude(),address.getLongitude());

                success = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

        return success;
	}

	/** Called when Map Button pressed **/
	public void onMapPress(View v) {

		btnMap.setPressed(true);
        btnDirection.setPressed(false);
        btnSettings.setPressed(false);

        layoutDestination.setVisibility(View.GONE);

        myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
        mapView.getOverlays().add(myItemizedOverlay);
        myItemizedOverlay.addItem(startPoint, "", "");
		mapController.setCenter(startPoint);
	}

	/** Called when Direction Button pressed **/
	public void onDirectionPress(View v) {

		btnDirection.setPressed(true);
        btnMap.setPressed(false);
        btnSettings.setPressed(false);

        layoutDestination.setVisibility(View.VISIBLE);

        txtDestination.requestFocus();
	}

	/** Called when Settings Button pressed **/
	public void onSettingsPress(View v) {

		btnSettings.setPressed(true);
        btnMap.setPressed(false);
        btnDirection.setPressed(false);
	}

	/** Called when Clear Button on Search Text Field pressed **/
	public void onClearSearchPress(View v){

		txtSearch.setText("");

		btnClearSearch.setVisibility(View.GONE);
		btnContactSearch.setVisibility(View.VISIBLE);
	}

	/** Called when Contact Button on Search Text Field pressed **/
	public void onContactSearchPress(View v){

		Intent i = new Intent(MainActivity.this,ContactListActivity.class);
		startActivityForResult(i, 101);
	}

    /** Called when Clear Button on Search Text Field pressed **/
    public void onClearDestinationPress(View v){

        txtDestination.setText("");

        btnClearDestination.setVisibility(View.GONE);
        btnContactDestination.setVisibility(View.VISIBLE);
    }

    /** Called when Contact Button on Search Text Field pressed **/
    public void onContactDestinationPress(View v){

        Intent i = new Intent(MainActivity.this,ContactListActivity.class);
        startActivityForResult(i, 101);
    }

	/** Called to Geocode contact list address and update Map accordingly. */
	public class ContactAddressAsync extends AsyncTask<String, Integer, String> {

        boolean success;

		@Override
		protected void onPreExecute() {
            success = false;
		}

		@Override
		protected String doInBackground(String... params) {

			try {
				success = addressToLatLong(params[0]);

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
		}

        protected void onPostExecute(String result) {
            if(success){
                txtSearch.setText(address.getAddressLine(0)+", "+
                        address.getLocality()+", "+
                        address.getAdminArea()+", "+
                        address.getCountryCode());

                txtSearch.clearFocus();

                btnClearSearch.setVisibility(View.VISIBLE);
                btnContactSearch.setVisibility(View.GONE);

                myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
                mapView.getOverlays().add(myItemizedOverlay);
                myItemizedOverlay.addItem(searchPoint, "", "");
                mapController.setCenter(searchPoint);
            } else {
                Toast.makeText(MainActivity.this,"Unable to Locate\nPlease try again",Toast.LENGTH_SHORT).show();
            }
        }
	}

    /** Called to Fetch Direction Data and update Map accordingly. */
    private class DirectionRequestAsync extends AsyncTask<String, Integer, String>{

        Road road;
        PathOverlay roadOverlay;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "Please Wait...", "Searching for Direction", true);
        }

        @Override
        protected String doInBackground(String... strings) {

            RoadManager roadManager = new MapQuestRoadManager();
            roadManager.addRequestOption("routeType=fastest");
            roadManager.addRequestOption("avoids=Limited%20Access");

            ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
            waypoints.add(startPoint);
            waypoints.add(searchPoint);

            road = roadManager.getRoad(waypoints);

            roadOverlay = RoadManager.buildRoadOverlay(road, mapView.getContext());

            return null;
        }

        protected void onPostExecute(String result) {

            mapView.getOverlays().clear();
            mapView.getOverlays().add(roadOverlay);
            mapView.invalidate();

            final ArrayList<ExtendedOverlayItem> roadItems =
                    new ArrayList<ExtendedOverlayItem>();
            ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes =
                    new ItemizedOverlayWithBubble<ExtendedOverlayItem>(MainActivity.this, roadItems, mapView);
            mapView.getOverlays().add(roadNodes);

            Drawable marker = getResources().getDrawable(R.drawable.marker_node);
            Drawable icon = getResources().getDrawable(R.drawable.moreinfo_arrow);

            for (int i=0; i<road.mNodes.size(); i++){
                RoadNode node = road.mNodes.get(i);
                ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Step "+i, "", node.mLocation, MainActivity.this);
                nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
                nodeMarker.setMarker(marker);
                roadNodes.addItem(nodeMarker);
                nodeMarker.setDescription(node.mInstructions);
                nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
                nodeMarker.setImage(icon);
            }

            progressDialog.dismiss();

            btnClearDestination.setVisibility(View.VISIBLE);
            btnContactDestination.setVisibility(View.GONE);
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == 101){
			if (resultCode == RESULT_OK) {
				String contactAddress = data.getStringExtra("Address");

				new ContactAddressAsync().execute(contactAddress);
			}
		}
	}

	/** Called when activity resumed. */
	@Override
	protected void onResume() {

		super.onResume();

		btnMap.setPressed(true);
	}
}
package com.maksumon.myroute;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.google.wrapper.MyLocationOverlay;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

	GPSTracker gps;

	private MapView mapView;
	private MapController mapController;
	private SimpleLocationOverlay myLocationOverlay;
    private MyLocationOverlay mLocationOverlay;

	private GeoPoint startPoint;
	private GeoPoint searchPoint;

	AutoCompleteTextView txtSearch;

	Button btnMap, btnDirection, btnSettings;
	Button btnClearSearch, btnContactSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CloudmadeUtil.retrieveCloudmadeKey(getApplicationContext());

        Typeface typeface = Typeface.createFromAsset(getAssets(),"Fondamento-Regular.ttf");

        txtSearch = (AutoCompleteTextView)findViewById(R.id.txtSearch);
        txtSearch.setTypeface(typeface);
		txtSearch.addTextChangedListener(textChecker);
		txtSearch.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				addressToLatLong(txtSearch.getText().toString());

				myLocationOverlay = new SimpleLocationOverlay(MainActivity.this, new DefaultResourceProxyImpl(MainActivity.this));
				mapView.getOverlays().add(myLocationOverlay);
				myLocationOverlay.setLocation(searchPoint);
				mapController.setCenter(searchPoint);

				return false;
			}
		});

		btnMap = (Button)findViewById(R.id.btnMap);
		btnDirection = (Button)findViewById(R.id.btnDirection);
		btnSettings = (Button)findViewById(R.id.btnSettings);

		btnClearSearch = (Button)findViewById(R.id.btnClearSearch);
		btnContactSearch = (Button)findViewById(R.id.btnContactSearch);

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

		myLocationOverlay = new SimpleLocationOverlay(this, new DefaultResourceProxyImpl(this));
		mapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.setLocation(startPoint);

		//		RoadManager roadManager = new MapQuestRoadManager();
		//		roadManager.addRequestOption("routeType=fastest");
		//		roadManager.addRequestOption("avoids=Limited%20Access");
		//
		//		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
		//		waypoints.add(startPoint);
		//		waypoints.add(new GeoPoint(34.108328,-117.969283));
		//		//waypoints.add(new GeoPoint(23.804095,90.377151)); //end point
		//
		//		Road road = roadManager.getRoad(waypoints);
		//
		//		PathOverlay roadOverlay = RoadManager.buildRoadOverlay(road, mapView.getContext());
		//
		//		mapView.getOverlays().add(roadOverlay);
		//		mapView.invalidate();
		//
		//		final ArrayList<ExtendedOverlayItem> roadItems = 
		//				new ArrayList<ExtendedOverlayItem>();
		//		ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = 
		//				new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, mapView);
		//		mapView.getOverlays().add(roadNodes);
		//
		//		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		//		Drawable icon = getResources().getDrawable(R.drawable.moreinfo_arrow);
		//
		//		for (int i=0; i<road.mNodes.size(); i++){
		//			RoadNode node = road.mNodes.get(i);
		//			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Step "+i, "", node.mLocation, this);
		//			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
		//			nodeMarker.setMarker(marker);
		//			roadNodes.addItem(nodeMarker);
		//			nodeMarker.setDescription(node.mInstructions);
		//			nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
		//			nodeMarker.setImage(icon);
		//		}
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
	 * str = Address */
	public void addressToLatLong(String src){

		Geocoder geoCoder = new Geocoder(MainActivity.this);
		List<Address> addresses;

		try {

			addresses = geoCoder.getFromLocationName(src,1);

			if (addresses.size() > 0) {

				final Address address = addresses.get(0);

				searchPoint = new GeoPoint(address.getLatitude(),address.getLongitude());

				MainActivity.this.runOnUiThread(new Runnable() {
					public void run() {

						txtSearch.setText(address.getAddressLine(0)+", "+
								address.getLocality()+", "+
								address.getAdminArea()+", "+
								address.getCountryCode());
					}
				});
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Called when Map Button pressed **/
	public void onMapPress(View v) {

		btnMap.setPressed(true);

		myLocationOverlay = new SimpleLocationOverlay(MainActivity.this, new DefaultResourceProxyImpl(MainActivity.this));
		mapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.setLocation(startPoint);
		mapController.setCenter(startPoint);
	}

	/** Called when Direction Button pressed **/
	public void onDirectionPress(View v) {

		btnDirection.setPressed(true);
	}

	/** Called when Settings Button pressed **/
	public void onSettingsPress(View v) {

		btnSettings.setPressed(true);
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

	/** Called to Geocode contact list address and update Map accordingly. */
	public class ContactAddressAsync extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			//isContact=true;
		}

		protected void onPostExecute(String result) {
			txtSearch.clearFocus();

			btnClearSearch.setVisibility(View.VISIBLE);
			btnContactSearch.setVisibility(View.GONE);

			myLocationOverlay = new SimpleLocationOverlay(MainActivity.this, new DefaultResourceProxyImpl(MainActivity.this));
			mapView.getOverlays().add(myLocationOverlay);
			myLocationOverlay.setLocation(searchPoint);
			mapController.setCenter(searchPoint);
		}

		@Override
		protected String doInBackground(String... params) {

			try {
				addressToLatLong(params[0]);

			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;
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

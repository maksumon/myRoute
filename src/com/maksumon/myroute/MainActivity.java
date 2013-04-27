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

    private Drawable currentMarker;
    private Drawable sourceMarker;
    private Drawable destinationMarker;
    ResourceProxy resourceProxy;

	private GeoPoint startPoint;
	private GeoPoint searchPoint;

    private Address address;

	AutoCompleteTextView txtSearch;
    AutoCompleteTextView txtDestination;

	Button btnMap, btnDirection, btnSettings;
	Button btnClearSearch, btnContactSearch, btnClearDestination, btnContactDestination;
    Button btnCurrent, btnCar, btnBicycle, btnPedestrian, btnItinerary;

    RelativeLayout layoutSearch;
    RelativeLayout layoutDestination;
    LinearLayout layoutRouteOptions;

    ProgressDialog progressDialog;

    protected Road road;
    protected PathOverlay roadOverlay;
    protected ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers;

    boolean isRouteFound = false;

    protected static final int ROUTE_REQUEST = 1;
    protected static final int CONTACT_REQUEST = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CloudmadeUtil.retrieveCloudmadeKey(getApplicationContext());

        layoutSearch = (RelativeLayout)findViewById(R.id.layoutSearch);
        layoutDestination = (RelativeLayout)findViewById(R.id.layoutDestination);
        layoutRouteOptions = (LinearLayout)findViewById(R.id.layoutRouteOptions);

        Typeface typeface = Typeface.createFromAsset(getAssets(),"Fondamento-Regular.ttf");

        txtSearch = (AutoCompleteTextView)findViewById(R.id.txtSearch);
        txtSearch.setTypeface(typeface);
		txtSearch.addTextChangedListener(textChecker);
		txtSearch.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (addressToLatLong(txtSearch.getText().toString())){

                    myItemizedOverlay = new MyItemizedOverlay(currentMarker, resourceProxy);
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

                    txtDestination.setText(address.getAddressLine(0) +", "+
                            address.getLocality() +", "+
                            address.getAdminArea() +", "+
                            address.getCountryCode());

                    new DirectionRequestAsync().execute("routeType=fastest");
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

        btnCurrent = (Button)findViewById(R.id.btnCurrent);
        btnCar = (Button)findViewById(R.id.btnCar);
        btnBicycle = (Button)findViewById(R.id.btnBicycle);
        btnPedestrian = (Button)findViewById(R.id.btnPedestrian);
        btnItinerary = (Button)findViewById(R.id.btnItinerary);

		btnMap.setSelected(true);

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
        currentMarker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));

        resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

        myItemizedOverlay = new MyItemizedOverlay(currentMarker, resourceProxy);
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

    /** Called to Get Current Location **/
    private void getCurrentLocation(){

        //Get Current Location
        gps = new GPSTracker(MainActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            startPoint = new GeoPoint(latitude,longitude);
            txtSearch.setText(latlongToAddress(latitude, longitude));
            btnClearSearch.setVisibility(View.VISIBLE);
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

	/** Called when Map Button pressed **/
	public void onMapPress(View v) {

		btnMap.setSelected(true);
        btnDirection.setSelected(false);
        btnSettings.setSelected(false);

        layoutDestination.setVisibility(View.GONE);
        layoutRouteOptions.setVisibility(View.GONE);

        getCurrentLocation();

        myItemizedOverlay = new MyItemizedOverlay(currentMarker, resourceProxy);
        mapView.getOverlays().clear();
        mapView.getOverlays().add(myItemizedOverlay);
        myItemizedOverlay.addItem(startPoint, "", "");
		mapController.setCenter(startPoint);
	}

	/** Called when Direction Button pressed **/
	public void onDirectionPress(View v) {

        if (!isRouteFound){

            btnDirection.setSelected(true);
            btnMap.setSelected(false);
            btnSettings.setSelected(false);

            layoutDestination.setVisibility(View.VISIBLE);

            txtDestination.requestFocus();
        }
	}

	/** Called when Settings Button pressed **/
	public void onSettingsPress(View v) {

		btnSettings.setSelected(true);
        btnMap.setSelected(false);
        btnDirection.setSelected(false);
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
		startActivityForResult(i, CONTACT_REQUEST);
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
        startActivityForResult(i, CONTACT_REQUEST);
    }

    /** Called when Current Button pressed **/
    public void onCurrentPress(View v) {

    }

    /** Called when Car Button pressed **/
    public void onCarPress(View v) {

        if (isRouteFound){
            new DirectionRequestAsync().execute("routeType=fastest");

            btnCar.setSelected(true);
            btnBicycle.setSelected(false);
            btnPedestrian.setSelected(false);
        }
    }

    /** Called when Bicycle Button pressed **/
    public void onBicyclePress(View v) {

        if (isRouteFound){
            new DirectionRequestAsync().execute("routeType=bicycle");

            btnBicycle.setSelected(true);
            btnCar.setSelected(false);
            btnPedestrian.setSelected(false);
        }
    }

    /** Called when Pedestrian Button pressed **/
    public void onPedestrianPress(View v) {

        if (isRouteFound){
            new DirectionRequestAsync().execute("routeType=pedestrian");

            btnPedestrian.setSelected(true);
            btnCar.setSelected(false);
            btnBicycle.setSelected(false);
        }
    }

    /** Called when Itinerary Button pressed **/
    public void onItineraryPress(View v) {

        Intent i = new Intent(this, RouteActivity.class);
        i.putExtra("ROAD", road);
        i.putExtra("NODE_ID", roadNodeMarkers.getBubbledItemId());
        i.putExtra("source",txtSearch.getText().toString());
        i.putExtra("destination",txtDestination.getText().toString());
        startActivityForResult(i, ROUTE_REQUEST);
    }

	/** Called to Geocode contact itinerary address and update Map accordingly. */
	public class ContactAddressAsync extends AsyncTask<String, Integer, String> {

        boolean success;

		@Override
		protected void onPreExecute() {
            success = false;
		}

		@Override
		protected String doInBackground(String... strings) {

			try {
				success = addressToLatLong(strings[0]);

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

                myItemizedOverlay = new MyItemizedOverlay(currentMarker, resourceProxy);
                mapView.getOverlays().clear();
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

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "Please Wait...", "Searching for Direction", true);
        }

        @Override
        protected String doInBackground(String... strings) {

            RoadManager roadManager = new MapQuestRoadManager();
            //roadManager.addRequestOption("routeType=fastest");
            roadManager.addRequestOption(strings[0]);
            roadManager.addRequestOption("avoids=Limited%20Access");

            ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
            waypoints.add(startPoint);
            waypoints.add(searchPoint);

            road = roadManager.getRoad(waypoints);

            roadOverlay = RoadManager.buildRoadOverlay(road, mapView.getContext());

            return strings[0];
        }

        protected void onPostExecute(String result) {

            mapView.getOverlays().clear();
            mapView.getOverlays().add(roadOverlay);

            Drawable tempMarker = getResources().getDrawable(R.drawable.source2);
            Bitmap bitmap = ((BitmapDrawable) tempMarker).getBitmap();
            sourceMarker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));

            tempMarker = getResources().getDrawable(R.drawable.destination2);
            bitmap = ((BitmapDrawable) tempMarker).getBitmap();
            destinationMarker = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));

            myItemizedOverlay = new MyItemizedOverlay(sourceMarker, resourceProxy);
            mapView.getOverlays().add(myItemizedOverlay);
            myItemizedOverlay.addItem(startPoint, "", "");

            myItemizedOverlay = new MyItemizedOverlay(destinationMarker, resourceProxy);
            mapView.getOverlays().add(myItemizedOverlay);
            myItemizedOverlay.addItem(searchPoint, "", "");

            mapView.invalidate();

            final ArrayList<ExtendedOverlayItem> roadItems =
                    new ArrayList<ExtendedOverlayItem>();
            roadNodeMarkers =
                    new ItemizedOverlayWithBubble<ExtendedOverlayItem>(MainActivity.this, roadItems, mapView);
            mapView.getOverlays().add(roadNodeMarkers);

            Drawable marker = getResources().getDrawable(R.drawable.marker_node);
            //Drawable icon = getResources().getDrawable(R.drawable.moreinfo_arrow);

            for (int i=0; i<road.mNodes.size(); i++){
                RoadNode node = road.mNodes.get(i);
                ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Step "+i, "", node.mLocation, MainActivity.this);
                nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
                nodeMarker.setMarker(marker);
                roadNodeMarkers.addItem(nodeMarker);
                nodeMarker.setDescription(node.mInstructions);
                nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
                //nodeMarker.setImage(icon);
            }

            progressDialog.dismiss();

            btnClearDestination.setVisibility(View.VISIBLE);
            btnContactDestination.setVisibility(View.GONE);

            layoutRouteOptions.setVisibility(View.VISIBLE);

            if (result.equals("routeType=fastest")){
                btnCar.setSelected(true);
            } else if (result.equals("routeType=bicycle")){
                btnBicycle.setSelected(true);
            } else {
                btnPedestrian.setSelected(true);
            }

            isRouteFound = true;
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ROUTE_REQUEST:
                if (resultCode == RESULT_OK) {
                    int nodeId = data.getIntExtra("NODE_ID", 0);
                    mapView.getController().setCenter(road.mNodes.get(nodeId).mLocation);
                    roadNodeMarkers.showBubbleOnItem(nodeId, mapView, true);
                }
                break;
            case CONTACT_REQUEST:
                if (resultCode == RESULT_OK) {
                    String contactAddress = data.getStringExtra("Address");
                    new ContactAddressAsync().execute(contactAddress);
                }
                break;
            default:
                break;
        }
	}

	/** Called when activity resumed. */
	@Override
	protected void onResume() {

		super.onResume();
	}
}
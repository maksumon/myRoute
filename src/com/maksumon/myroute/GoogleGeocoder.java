package com.maksumon.myroute;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleGeocoder {
	
	/** Called to get JSON from URL. */
	public static JSONObject getJSONfromURL(String urlString){

		//convert response to string
		String result = "";
		JSONObject jArray = null;

		try{
			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();

			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			reader.close();
			result=sb.toString();
		}catch(Exception e){
			//Log.e("log_tag", "Error converting result "+e.toString());
		}

		//try parse the string to a JSON object
		try{
			jArray = new JSONObject(result);
		}catch(JSONException e){
			//Log.e("log_tag", "Error parsing data "+e.toString());
		}

		return jArray;
	}
	
	/** Called to Geocode address from current location. **/
	public static String fromLocation(double latitude, double longitude){
		
		String address = "";
		
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.googleapis.com/maps/api/geocode/json?latlng=");
		urlString.append(latitude);
		urlString.append(",");
		urlString.append(longitude);
		urlString.append("&sensor=false");
		
		try {
        	JSONObject jsonObj = getJSONfromURL(urlString.toString());
        	if(jsonObj.get("status").equals("OK")){
        		JSONArray results = jsonObj.getJSONArray("results");
        		address = results.getJSONObject(0).getString("formatted_address");
        	}
            
        } catch (Exception e) {

            e.printStackTrace();
        }
		
		return address;
	}
	
	/** Called to Reverse Geocode location. **/
	public static ArrayList<HashMap<String, Double>> getLocation(String address){
		
		ArrayList<HashMap<String, Double>> location = new ArrayList<HashMap<String, Double>>();
		double latitude = 0.0;
		double longitude = 0.0;
		
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.googleapis.com/maps/api/geocode/json?address=");
		urlString.append(address);
		urlString.append("&sensor=false");
		
		try {
        	JSONObject jsonObj = getJSONfromURL(urlString.toString());
        	if(jsonObj.get("status").equals("OK")){
        		JSONArray results = jsonObj.getJSONArray("results");
        		
        		latitude = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
        		longitude = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
        		
        		HashMap<String, Double>tempMap = new HashMap<String, Double>();
        		tempMap.put("latitude", latitude);
        		tempMap.put("longitude", longitude);
        		
        		location.add(tempMap);
        	}
            
        } catch (Exception e) {

            e.printStackTrace();
        }
		
		return location;
	}
}

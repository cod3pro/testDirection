package com.example.borhan.testDirection;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

    public class MainActivity extends FragmentActivity {

        GoogleMap map;
        ArrayList<LatLng> markerPoints;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Initializing
            markerPoints = new ArrayList<LatLng>();

            // Getting reference to SupportMapFragment of the activity_main
            SupportMapFragment sMapfrag = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

            // Getting Map for the SupportMapFragment
            map = sMapfrag.getMap();

            if(map!=null) {

                // Enable MyLocation Button in the Map
                map.setMyLocationEnabled(true);

                // Setting onclick event listener for the map
                map.setOnMapClickListener(new OnMapClickListener() {


                    @Override
                    public void onMapClick(LatLng point) {

                        // Already two locations
                        if (markerPoints.size() > 2) {
                            markerPoints.clear();
                            map.clear();
                        }

                        //Adding new item to the ArrayList
                        markerPoints.add(point);


                        // Creating MarkerOptions
                        MarkerOptions options = new MarkerOptions();


                        // Setting the position of the marker
                        options.position(point);


                        /**
                         * For the start location, the color of marker is GREEN and
                         * for the end location, the color of marker is RED.
                         */
                        if (markerPoints.size() == 1) {
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        } else if (markerPoints.size() == 2) {
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        } else if (markerPoints.size() == 3) {
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        }

                        map.addMarker(options).setTitle(options.getPosition() + "");


                        for (int i = 0; i < markerPoints.size() - 1; i++) {

                            //Checks, whether start and end locations are captured
                            if (markerPoints.size() >= 3) {
                                LatLng origin = markerPoints.get(i);
                                LatLng dest = markerPoints.get(i + 1);

                                // Getting URL to the Google Directions API
                                String url = getDirectionsUrl(origin, dest);
                                DownloadTask downloadTask = new DownloadTask();

                                // Start downloading json data from Google Directions API
                                downloadTask.execute(url);
                            }
                        }
                    }
                });

            }


            Button button = (Button) findViewById(R.id.googleBtn);
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    //When user clicks on Google Direciton, the google maps app opens and show all the directions
                    //For now it shows three pre-defined locations
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                            "https://www.google.com/maps/dir/Current+Location/34.201361,-118.5181532/34.202361,-118.5291532/34.222361,-118.5391532"));
                            //"http://maps.google.com/maps?saddr=Current+Location&daddr=34.201360955015699,-118.51815316826104"));
                    startActivity(intent);
                }
            });


            Button wazeBtn = (Button) findViewById(R.id.wazeButton);
            wazeBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    //When user clicks on Google Direciton, the google maps app opens and show all the directions
                    //For now it shows one pre-defined destination
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
                            "waze://?ll=34.201360955015699,-118.51815316826104"));
                    startActivity(intent);
                }

            });

        }


        private String getDirectionsUrl(LatLng origin,LatLng dest){

            // Origin of route
            String str_origin = "origin="+origin.latitude+","+origin.longitude;

            // Destination of route
            String str_dest = "destination="+dest.latitude+","+dest.longitude;

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = str_origin+"&"+str_dest+"&"+sensor;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

            return url;
        }


        //A method to download json data from url
        private String downloadUrl(String strUrl) throws IOException{
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb  = new StringBuffer();

                String line = "";
                while( ( line = br.readLine())  != null){
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            }catch(Exception e){
                Log.d("Exception while downloading url", e.toString());

            }finally{
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }


        // Fetches data from url passed
        private class DownloadTask extends AsyncTask<String, Void, String>{

            // Downloading data in non-ui thread
            @Override
            protected String doInBackground(String... url) {

                // For storing data from web service
                String data = "";

                try{
                    // Fetching the data from web service
                    data = downloadUrl(url[0]);
                }catch(Exception e){
                    Log.d("Background Task",e.toString());
                }
                return data;
            }

            // Executes in UI thread, after the execution of
            // doInBackground()
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                ParserTask parserTask = new ParserTask();

                // Invokes the thread for parsing the JSON data
                parserTask.execute(result);

            }
        }


        // A class to parse the Google Places in JSON format */
        private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

            // Parsing the data in non-ui thread
            @Override
            protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

                JSONObject jObject;
                List<List<HashMap<String, String>>> routes = null;

                try{
                    jObject = new JSONObject(jsonData[0]);
                    DirectionsJSONParser parser = new DirectionsJSONParser();

                    // Starts parsing data
                    routes = parser.parse(jObject);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return routes;
            }

            // Executes in UI thread, after the parsing process
            @Override
            protected void onPostExecute(List<List<HashMap<String, String>>> result) {
                ArrayList<LatLng> points = null;
                PolylineOptions lineOptions = null;
                MarkerOptions markerOptions = new MarkerOptions();

                // Traversing through all the routes
                for(int i=0;i<result.size();i++){
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for(int j=0;j<path.size();j++){
                        HashMap<String,String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);

                    }


                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(15);
                    lineOptions.color(Color.BLUE);

                }

                // Drawing polyline in the Google Map for the i-th route
                map.addPolyline(lineOptions);
            }
        }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
    }
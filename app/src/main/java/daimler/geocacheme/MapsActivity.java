package daimler.geocacheme;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import daimler.geocacheme.GeoCacheLogic.GeoCache;
import daimler.geocacheme.GeoCacheLogic.GeoCacheProvider;
import daimler.geocacheme.InternetConnection.InternetConnectionTester;
import daimler.geocacheme.Server.JSONParser;
import daimler.geocacheme.UserManagement.UserManagement;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMarkerClickListener
{
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private LatLng latLng;
    MarkerOptions markerOptions;
    public InternetConnectionTester internetTester;
    public boolean internetCheck = false;
    public Handler handler = new Handler();
    public List<Marker> Markers;
    private Marker myLocationMarker;
    private static BitmapDescriptor markerIconBitmapDescriptor;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        internetTester = new InternetConnectionTester();

        Markers = new ArrayList<Marker>();
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = fragment.getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(this);
        mMap.setOnMarkerClickListener(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1 * 1000)        // 1 seconds, in milliseconds
                .setFastestInterval(1 * 500); // 0,5 second, in milliseconds

        ImageButton btn_find = (ImageButton) findViewById(R.id.btn_find);
        btn_find.setImageResource(R.drawable.suche);
        // Defining button click event listener for the find button
        View.OnClickListener findClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Getting reference to EditText to get the user input location
                EditText etLocation = (EditText) findViewById(R.id.et_location);

                // Getting user input location
                String location = etLocation.getText().toString();

                Thread t = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        internetCheck = internetTester.hasActiveInternetConnection(MapsActivity.this);
                    }
                });
                t.start();
                if (location != null && !location.equals("") && internetCheck)
                {
                    new GeocoderTask().execute(location);
                }
            }
        };

        // Setting button click event listener for the find button
        btn_find.setOnClickListener(findClickListener);

        // Load custom marker icon
        markerIconBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.auto48);

        GeoCache_Setup();
    }


    public void GeoCache_Setup()
    {
        ImageButton showVisitorsButton = (ImageButton) findViewById(R.id.showusers);
        showVisitorsButton.setVisibility(View.INVISIBLE);
        PlaceGeoCacheMarkers();
        mMap.setOnMapLongClickListener(this);
        MarkerAdder.run();
    }

    public Runnable MarkerAdder = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                for (GeoCache geoCache : GeoCacheProvider.GetGeoCacheList())
                {
                    if (geoCache.MarkerID == null)
                    {
                        PlaceMarkerforNewGeoCache(geoCache);
                    }
                }
                handler.postDelayed(MarkerAdder, 1000);
            } catch (Exception e)
            {
                handler.postDelayed(MarkerAdder, 1000);
            }

        }
    };

    public void PlaceMarkerforNewGeoCache(GeoCache geoCache)
    {
        geoCache.Visited = false;
        LatLng markerLatLng = new LatLng(geoCache.Latitude, geoCache.Longitude);
        String geoCacheName = geoCache.Name;
        MarkerOptions options = new MarkerOptions();
        options.position(markerLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.blau_logo_72)).title(geoCacheName);
        Marker marker = mMap.addMarker(options);
        geoCache.MarkerID = marker.getId();
        Markers.add(marker);
    }

    public Runnable DistanceCalculator = new Runnable()
    {
        double distance = 40;
        double currentLatitude = 0;
        double currentLongitude = 0;
        Location currentLocation;

        @Override
        public void run()
        {
            try
            {
                for (GeoCache geoCache : GeoCacheProvider.GeoCacheList)
                {
                    currentLocation = mMap.getMyLocation();

                    if (currentLocation != null)
                    {
                        currentLatitude = currentLocation.getLatitude();
                        currentLongitude = currentLocation.getLongitude();
                        distance = CalculateDistance(currentLatitude, currentLongitude, geoCache.Latitude, geoCache.Longitude);
                        if (distance < 100 && geoCache.MarkerID != null)
                        {
                            setMarkerAsVisited(geoCache.MarkerID, geoCache.Visited);
                            geoCache.Visited = true;
                            GeoCacheProvider.saveGeoCacheListIntoPrefs(MapsActivity.this);
                        }
                    }
                }
                handler.postDelayed(DistanceCalculator, 1000);
            } catch (Exception e)
            {
                handler.postDelayed(DistanceCalculator, 1000);
            }

        }
    };

    public static double CalculateDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = (earthRadius * c);
        return dist;
    }

    public void setMarkerAsVisited(String geoCacheID, boolean visited)
    {
        for (Marker marker : Markers)
        {
            if (marker.getId().equals(geoCacheID) && !visited)
            {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.gruen_logo_72));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.i(TAG, "Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else
        {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location)
    {
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        latLng = new LatLng(currentLatitude, currentLongitude);
        float zoomLevel = 17; //Max:21
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DistanceCalculator.run();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        handler.removeCallbacks(DistanceCalculator);
        if (mGoogleApiClient.isConnected())
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        if (connectionResult.hasResolution())
        {
            try
            {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        handleNewLocation(location);
    }

    public void PlaceGeoCacheMarkers()
    {
        List<GeoCache> geoCacheList = GeoCacheProvider.GetGeoCacheList();
        for (GeoCache geoCache : geoCacheList)
        {
            LatLng markerLatLng = new LatLng(geoCache.Latitude, geoCache.Longitude);
            String geoCacheName = geoCache.Name;
            MarkerOptions options = new MarkerOptions();
            options.position(markerLatLng).title(geoCacheName);
            if (geoCache.Visited)
            {
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.gruen_logo_72));
            }
            else
            {
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.blau_logo_72));
            }
            Marker marker = mMap.addMarker(options);
            geoCache.MarkerID = marker.getId();
            Markers.add(marker);
        }
    }

    @Override
    public void onMapLongClick(LatLng point)
    {
        ImageButton showVisitorsButton = (ImageButton) findViewById(R.id.showusers);
        showVisitorsButton.setVisibility(View.INVISIBLE);
        View view = (LayoutInflater.from(MapsActivity.this)).inflate(R.layout.geocache_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setView(view);
        final EditText geoCacheInput = (EditText) view.findViewById(R.id.geocachenameinput);
        final LatLng location = point;

        builder.setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String input = geoCacheInput.getText().toString();
                CreateNewGeoCache(location, input);
            }
        });
        builder.show();
    }

    private void CreateNewGeoCache(LatLng point, String name)
    {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(name)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blau_logo_72)));
        Markers.add(marker);
        String id = UUID.randomUUID().toString();
        String userId = UserManagement.getUserFromPrefs(MapsActivity.this).ID;
        GeoCacheProvider.CreateGeoCache(name, id, point.latitude, point.longitude, marker.getId(), userId);
    }

    @Override
    public void onMyLocationChange(Location location)
    {
        // Remove the old marker object
        if (myLocationMarker != null)
        {
            myLocationMarker.remove();
        }

        // Add a new marker object at the new (My Location dot) location
        myLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .icon(markerIconBitmapDescriptor));
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        String geoCacheID = null;
        for (GeoCache geoCache : GeoCacheProvider.GetGeoCacheList())
        {
            if (geoCache.MarkerID.equals(marker.getId()))
            {
                geoCacheID = geoCache.Id;
            }
        }
        final String text = geoCacheID;
        ImageButton userButton = (ImageButton) findViewById(R.id.showusers);
        View.OnClickListener onClickListener = new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Log.i("GetGeoCacheVisitors", "Hier");
                try
                {
                    GetAllVisitorsTask getAllVisitorsTask = new GetAllVisitorsTask();
                    getAllVisitorsTask.GeoCacheID = text;
                    getAllVisitorsTask.execute();
                } catch (Exception e)
                {
                    // list.append("Server Connection Error");
                }
            }
        };
        if (geoCacheID != null)
        {
            userButton.setVisibility(View.VISIBLE);
            userButton.setOnClickListener(onClickListener);
        }
        else
        {
            userButton.setVisibility(View.INVISIBLE);
        }
        return false;
    }

    class GetAllVisitorsTask extends AsyncTask<String, String, String>
    {
        private ProgressDialog pDialog;
        JSONParser jParser = new JSONParser();
        // url to get all visitors list
        private String url_all_visitors = "http://geocacheme.bplaced.net/return_visitors_of_geocache.php";
        // JSON Node names
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_VISITORS = "visitors";
        private static final String TAG_NAME = "name";
        public String GeoCacheID = null;
        public List<String> VisitorList;
        // visitors JSONArray
        JSONArray visitors = null;

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Loading visitors. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All visitors from url
         */
        protected String doInBackground(String... args)
        {
            VisitorList = new ArrayList<>();
            String id = GeoCacheID;
            // Building Parameters
            Log.i("Visitors_found: ", "Task started");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("ID", id));

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_visitors, "POST", params);

            if (json != null)
            {
                // Check your log cat for JSON reponse
                Log.i("Visitors_found: ", "Response received");
                try
                {
                    // Checking for SUCCESS TAG
                    int success = 0;
                    try
                    {
                        success = json.getInt(TAG_SUCCESS);
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                    if (success == 1)
                    {
                        Log.i("Visitors_found: ", "We found some Visitors");
                        // visitors found
                        // Getting Array of Products
                        visitors = json.getJSONArray(TAG_VISITORS);
                        // looping through All Products
                        for (int i = 0; i < visitors.length(); i++)
                        {
                            JSONObject c = visitors.getJSONObject(i);

                            // Storing each json item in variable
                            String name = c.getString(TAG_NAME);
                            Log.i("VisitorName", name);
                            VisitorList.add(name);
                        }
                    }
                    else
                    {
                        Log.i("GETVISITORS", "funktioniert nicht");
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url)
        {
            // dismiss the dialog after getting all visitors
            AlertDialog.Builder exitBuilder = new AlertDialog.Builder(MapsActivity.this);
            View visitorsView = (LayoutInflater.from(MapsActivity.this)).inflate(R.layout.visitors_window, null);
            TextView list = (TextView) visitorsView.findViewById(R.id.list);
            exitBuilder.setView(visitorsView);
            exitBuilder.setCancelable(true);
            exitBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    ImageButton showVisitorsButton = (ImageButton) findViewById(R.id.showusers);
                    showVisitorsButton.setVisibility(View.INVISIBLE);
                    dialog.dismiss();
                    dialog.cancel();
                }
            });
            pDialog.dismiss();
            List<String> visitors = VisitorList;
            if (visitors != null)
            {
                if (!visitors.isEmpty())
                {
                    for (String visitor : visitors)
                    {
                        list.append("\n" + visitor);
                    }
                }
                else
                {
                    list.append("No visitors found");
                }
            }
            else
            {
                list.append("No visitors found");
            }
            exitBuilder.show();
        }
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>
    {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Searching. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected List<Address> doInBackground(String... locationName)
        {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try
            {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses)
        {

            if (addresses == null || addresses.size() == 0)
            {
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map
            mMap.clear();

            // Adding Markers on Google Map for each matching address
            for (int i = 0; i < addresses.size(); i++)
            {
                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                latLng = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);

                mMap.addMarker(markerOptions);
                PlaceGeoCacheMarkers();
                pDialog.dismiss();
                // Locate the first location
                if (i == 0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }
}
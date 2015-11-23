package daimler.geocacheme;

import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import daimler.geocacheme.GeoCacheLogic.GeoCache;
import daimler.geocacheme.GeoCacheLogic.GeoCacheProvider;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapLongClickListener
{
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private LatLng latLng;
    MarkerOptions markerOptions;
    GeoCacheProvider provider;
    public InternetConnectionTester internetTester;
    public boolean internetCheck = false;
    public Handler handler = new Handler();
    public List<Marker> Markers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        provider = new GeoCacheProvider(this);
        internetTester = new InternetConnectionTester();

        Markers = new ArrayList<Marker>();
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = fragment.getMap();
        mMap.setMyLocationEnabled(true);

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


        //TODO: Noch statisch zum Testen

       String id = UUID.randomUUID().toString();
       provider.CreateGeoCache("Berlin", id, 52.520007, 13.404953999999975);

       id = UUID.randomUUID().toString();
       provider.CreateGeoCache("Hochschule Esslingen", id, 48.7453375, 9.322090099999969);

       id = UUID.randomUUID().toString();
       provider.CreateGeoCache("Mensa Hochschule Esslingen", id, 48.74438725435462, 9.32416534420554);

       provider.saveGeoCacheListIntoPrefs(this);
     //  provider.getGeoCacheListFromPrefs(this);

        PlaceGeoCacheMarkers();

        mMap.setOnMapLongClickListener(this);
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
            for (GeoCache geoCache : provider.GeoCacheList)
            {
                currentLocation = mMap.getMyLocation();

                if (currentLocation != null)
                {
                    currentLatitude = currentLocation.getLatitude();
                    currentLongitude = currentLocation.getLongitude();
                    distance = distFrom(currentLatitude, currentLongitude, geoCache.Latitude, geoCache.Longitude);
                    if (distance < 100 && geoCache.MarkerID != null)
                    {
                        setMarkerAsVisited(geoCache.MarkerID);
                    }
                }
            }
            handler.postDelayed(DistanceCalculator, 1000);
        }
    };

    public static double distFrom(double lat1, double lng1, double lat2, double lng2)
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

    public void setMarkerAsVisited(String geoCacheID)
    {
        for (Marker marker : Markers)
        {
            if (marker.getId().equals(geoCacheID))
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
        } else
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
        DistanceCalculator.run();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
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
        } else
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
        List<GeoCache> geoCacheList = provider.GetGeoCacheList();
        for (GeoCache geoCache : geoCacheList)
        {
            LatLng markerLatLng = new LatLng(geoCache.Latitude, geoCache.Longitude);
            String geoCacheName = geoCache.Name;
            MarkerOptions options = new MarkerOptions();
            options.position(markerLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.blau_logo_72)).title(geoCacheName);
            Marker marker = mMap.addMarker(options);
            geoCache.MarkerID = marker.getId();
            Markers.add(marker);
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        //TODO: Eingabefenster für Benutzer starten
        String name = "Neuer GeoCache";

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(name)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blau_logo_72)));

        String id = UUID.randomUUID().toString();
        provider.CreateGeoCache(name, id, point.latitude, point.longitude, marker.getId());
        provider.saveGeoCacheListIntoPrefs(this);
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>>
    {

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
                // Locate the first location
                if (i == 0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }
}



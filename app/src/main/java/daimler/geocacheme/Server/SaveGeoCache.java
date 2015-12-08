package daimler.geocacheme.Server;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import daimler.geocacheme.GeoCacheLogic.GeoCache;
import daimler.geocacheme.GeoCacheLogic.GeoCacheProvider;

/**
 * Created by CGsch on 01.12.2015.
 */
public class SaveGeoCache
{

    private static String url_create_geocache = "http://geocacheme.bplaced.net/create_geoCache.php";
    public String GeoCacheName;
    public String GeoCacheID;
    public double GeoCacheLatitude;
    public double GeoCacheLongitude;
    public String GeoCacheOwnerID;

    public void StartSaveGeoCache(String ownerID)
    {
        for (GeoCache geoCache : GeoCacheProvider.GetGeoCacheList())
        {
            if(ownerID.equals(geoCache.OwnerID))
            {
                GeoCacheName = geoCache.Name;
                GeoCacheID = geoCache.Id;
                GeoCacheLatitude = geoCache.Latitude;
                GeoCacheLongitude = geoCache.Longitude;
              //  GeoCacheOwnerID = geoCache.OwnerID;
                new SaveGeoCacheTask().execute();
            }
        }
    }

    class SaveGeoCacheTask extends AsyncTask<String, String, String>
    {
        // url to create new product

        JSONParser jsonParser = new JSONParser();
        // JSON Node names
        private static final String TAG_SUCCESS = "success";


        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args)
        {
            String name = GeoCacheName;
            String id = GeoCacheID;
            double gpsLatitude = GeoCacheLatitude;
            double gpsLongitude = GeoCacheLongitude;

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("id", id));
            params.add(new BasicNameValuePair("gpsLatitude", "" + gpsLatitude));
            params.add(new BasicNameValuePair("gpsLongitude", "" + gpsLongitude));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_geocache,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try
            {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1)
                {
                    // successfully created product

                }
                else
                {
                    // failed to create product
                }
            } catch (JSONException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url)
        {
            // dismiss the dialog once done
        }
    }
}

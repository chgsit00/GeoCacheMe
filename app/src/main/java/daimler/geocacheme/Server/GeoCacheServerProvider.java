package daimler.geocacheme.Server;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import daimler.geocacheme.GeoCacheLogic.GeoCacheProvider;

/**
 * Created by CGsch on 01.12.2015.
 */
public class GeoCacheServerProvider
{
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    // url to get all geoCaches list
    private static String url_all_geocaches = "http://geocacheme.bplaced.net/get_all_geoCaches.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_GEOCACHES = "geoCaches";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_LATITUDE = "gpsLatitude";
    private static final String TAG_LONGITUDE = "gpsLongitude";

    // geoCaches JSONArray
    JSONArray geoCaches = null;

    public void StartGeoCacheServerProvider()
    {
        // Loading geoCaches in Background Thread
        new LoadAllGeoCaches().execute();
    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     */
    class LoadAllGeoCaches extends AsyncTask<String, String, String>
    {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        /**
         * getting All geoCaches from url
         */
        protected String doInBackground(String... args)
        {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_geocaches, "GET", params);

            // Check your log cat for JSON reponse
            Log.i("All Products: ", json.toString());

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
                    // geoCaches found
                    // Getting Array of Products
                    geoCaches = json.getJSONArray(TAG_GEOCACHES);

                    // looping through All Products
                    for (int i = 0; i < geoCaches.length(); i++)
                    {
                        JSONObject c = geoCaches.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAME);
                        double latitude = c.getDouble(TAG_LATITUDE);
                        double longitude = c.getDouble(TAG_LONGITUDE);
                        GeoCacheProvider.CreateGeoCache(name, id, latitude, longitude);
                    }
                }
                else
                {
                    // no geoCaches found
                    // Launch Add New product Activity
                    //   Intent i = new Intent(getApplicationContext(),
                    //           NewProductActivity.class);
                    //   // Closing all previous activities
                    //   i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //   startActivity(i);
                    Log.i("GET", "funktioniert nicht");
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
            // dismiss the dialog after getting all geoCaches

        }
    }
}

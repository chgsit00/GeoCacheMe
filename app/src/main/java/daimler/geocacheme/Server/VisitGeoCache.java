package daimler.geocacheme.Server;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;

import daimler.geocacheme.GeoCacheLogic.GeoCache;
import daimler.geocacheme.GeoCacheLogic.GeoCacheProvider;

/**
 * Created by CGsch on 09.12.2015.
 */
public class VisitGeoCache
{
    private static String url_create_geocache = "http://geocacheme.bplaced.net/geovisit.php";
    public String UserID;
    public String GeoCacheID;

    public void StartVisitGeoCache(String userID)
    {
        UserID = userID;
        for (GeoCache geoCache : GeoCacheProvider.GetGeoCacheList())
        {
            if (geoCache.Visited)
            {
                GeoCacheID = geoCache.Id;
                new VisitGeoCacheTask().execute();
            }
        }
    }

    class VisitGeoCacheTask extends AsyncTask<String, String, String>
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
            String userID = UserID;
            String geoCacheID = GeoCacheID;

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("G_ID", geoCacheID));
            params.add(new BasicNameValuePair("V_ID", userID));

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

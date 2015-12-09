package daimler.geocacheme.Server;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CGsch on 09.12.2015.
 */
public class GetGeoCacheVisitors
{
    JSONParser jParser = new JSONParser();

    // url to get all visitors list
    private static String url_all_visitors = "http://geocacheme.bplaced.net/return_visitors_of_geocache.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_VISITORS = "visitors";

    public String GeoCacheID = null;

    // visitors JSONArray
    JSONArray visitors = null;

    public void StartGetGeoCacheVisitors(String geoCacheID)
    {
        GeoCacheID = geoCacheID;
        if (GeoCacheID != null)
        {
            new GetAllVisitorsTask().execute();
        }
    }

    class GetAllVisitorsTask extends AsyncTask<String, String, String>
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
         * getting All visitors from url
         */
        protected String doInBackground(String... args)
        {
            String id = GeoCacheID;
            // Building Parameters
            Log.i("Visitors_found: ", "Task started");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("ID", id));

            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_visitors, "GET", params);

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
                        String name = c.toString();
                    }
                }
                else
                {
                    // no visitors found
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
            // dismiss the dialog after getting all visitors

        }
    }
}

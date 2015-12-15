package daimler.geocacheme.Server;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CGsch on 08.12.2015.
 */
public class UserServerProvider
{
    static String UserName = "";
    static String UserID;
    //TODO: Auf richtigen PHP Namen ändern
    private static String url_create_user = "http://geocacheme.bplaced.net/create_visitor.php";

    public void StartUserServerProvider(String userName, String userID)
    {
        if (!UserName.equals(userName))
        {
            UserName = userName;
            UserID = userID;
            new SaveUserTask().execute();
        }
    }

    class SaveUserTask extends AsyncTask<String, String, String>
    {
        // url to create new product

        JSONParser jsonParser = new JSONParser();
        // JSON Node names
        private static final String TAG_SUCCESS = "success";

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        protected String doInBackground(String... args)
        {
            String name = UserName;
            String id = UserID;

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", id));
            params.add(new BasicNameValuePair("name", name));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_user,
                    "POST", params);
            if (json != null)
            {
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
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url)
        {
            // dismiss the dialog once done
            super.onPostExecute(file_url);
        }
    }
}

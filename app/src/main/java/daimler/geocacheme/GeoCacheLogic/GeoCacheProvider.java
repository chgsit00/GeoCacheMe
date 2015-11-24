package daimler.geocacheme.GeoCacheLogic;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import daimler.geocacheme.MapsActivity;

/**
 * Created by CGsch on 16.11.2015.
 */
public class GeoCacheProvider
{
    public List<GeoCache> GeoCacheList;
    SharedPreferences geoCachePrefs;
    SharedPreferences.Editor prefsEditor;

    public GeoCacheProvider(Context context)
    {
        GeoCacheList = getGeoCacheListFromPrefs(context);
        if (GeoCacheList == null)
        {
            GeoCacheList = new ArrayList<GeoCache>();
        }
    }

    public void CreateGeoCache(String name, String iD, double latitude, double longitude)
    {
        GeoCache geoCache = new GeoCache();
        geoCache.Id = iD;
        geoCache.Name = name;
        geoCache.Latitude = latitude;
        geoCache.Longitude = longitude;
        geoCache.visited = false;
        GeoCacheList.add(geoCache);
    }

    public void CreateGeoCache(String name, String iD, double latitude, double longitude, String markerID)
    {
        GeoCache geoCache = new GeoCache();
        geoCache.Id = iD;
        geoCache.Name = name;
        geoCache.Latitude = latitude;
        geoCache.Longitude = longitude;
        geoCache.MarkerID = markerID;
        geoCache.visited = false;
        GeoCacheList.add(geoCache);
    }

    public List<GeoCache> GetGeoCacheList()
    {
        return GeoCacheList;
    }

    public void saveGeoCacheListIntoPrefs(Context context)
    {
        geoCachePrefs = context.getSharedPreferences("GeoCacheObject", Context.MODE_PRIVATE);
        prefsEditor = geoCachePrefs.edit();
        Gson gson = new Gson();
        String jsonGeoCaches = gson.toJson(GeoCacheList); // myObject - instance of MyObject
        prefsEditor.putString("GeoCacheObject", jsonGeoCaches);
        prefsEditor.apply();
    }

    public List<GeoCache> getGeoCacheListFromPrefs(Context context)
    {
        geoCachePrefs = context.getSharedPreferences("GeoCacheObject", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonGeoCaches = geoCachePrefs.getString("GeoCacheObject", "");
        Type type = new TypeToken<List<GeoCache>>()
        {
        }.getType();
        return gson.fromJson(jsonGeoCaches, type);
    }
}

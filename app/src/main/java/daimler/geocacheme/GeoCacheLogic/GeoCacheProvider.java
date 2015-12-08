package daimler.geocacheme.GeoCacheLogic;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;

import daimler.geocacheme.MainActivity;

/**
 * Created by CGsch on 16.11.2015.
 */
public class GeoCacheProvider
{
    public static List<GeoCache> GeoCacheList = new ArrayList<GeoCache>();
    static SharedPreferences geoCachePrefs;
    static SharedPreferences.Editor prefsEditor;

    public static void SetGeoCacheListfromPrefs(Context context)
    {
        GeoCacheList = getGeoCacheListFromPrefs(context);
        if (GeoCacheList == null)
        {
            GeoCacheList = new ArrayList<GeoCache>();
        }
    }

    public static void CreateGeoCache(String name, String iD, double latitude, double longitude)
    {
        if (!GeoCacheProvider.GeoCacheAlreadyExists(iD))
        {
            GeoCache geoCache = new GeoCache();
            geoCache.Id = iD;
            geoCache.Name = name;
            geoCache.Latitude = latitude;
            geoCache.Longitude = longitude;
            geoCache.Currentlyvisited = false;
            GeoCacheList.add(geoCache);
        }
    }

    public static void CreateGeoCache(String name, String iD, double latitude, double longitude, String markerID)
    {
        if (!GeoCacheProvider.GeoCacheAlreadyExists(iD))
        {
            GeoCache geoCache = new GeoCache();
            geoCache.Id = iD;
            geoCache.Name = name;
            geoCache.Latitude = latitude;
            geoCache.Longitude = longitude;
            geoCache.MarkerID = markerID;
            geoCache.Currentlyvisited = false;
            GeoCacheList.add(geoCache);
        }
    }

    public static List<GeoCache> GetGeoCacheList()
    {
        return GeoCacheList;
    }

    public static void saveGeoCacheListIntoPrefs(Context context)
    {
        context.getSharedPreferences("GeoCacheObject", 0).edit().clear().apply();
        geoCachePrefs = context.getSharedPreferences("GeoCacheObject", Context.MODE_PRIVATE);
        prefsEditor = geoCachePrefs.edit();
        Gson gson = new Gson();
        String jsonGeoCaches = gson.toJson(GeoCacheList); // myObject - instance of MyObject
        prefsEditor.putString("GeoCacheObject", jsonGeoCaches);
        prefsEditor.apply();
    }

    public static List<GeoCache> getGeoCacheListFromPrefs(Context context)
    {
        geoCachePrefs = context.getSharedPreferences("GeoCacheObject", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonGeoCaches = geoCachePrefs.getString("GeoCacheObject", "");
        Type type = new TypeToken<List<GeoCache>>()
        {
        }.getType();
        return gson.fromJson(jsonGeoCaches, type);
    }

    public static boolean GeoCacheAlreadyExists(String id)
    {
        boolean s = false;
        for (GeoCache geoCache : GeoCacheList)
        {
            if (id.equals(geoCache.Id))
            {
                s = true;
                return s;
            }
        }
        return s;
    }
}

package daimler.geocacheme.GeoCacheLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CGsch on 16.11.2015.
 */
public class GeoCacheProvider
{
    public List<GeoCache> GeoCacheList;

    public GeoCacheProvider()
    {
        GeoCacheList = new ArrayList<GeoCache>();
    }

    public void CreateGeoCache(String name, String iD, double latitude, double longitude)
    {
        GeoCache geoCache = new GeoCache();
        geoCache.Id = iD;
        geoCache.Name = name;
        geoCache.Latitude = latitude;
        geoCache.Longitude = longitude;
        GeoCacheList.add(geoCache);
    }

    public List<GeoCache> GetGeoCacheList()
    {
        return GeoCacheList;
    }
}

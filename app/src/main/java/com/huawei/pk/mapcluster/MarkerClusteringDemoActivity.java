
package com.huawei.pk.mapcluster;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;

import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.FragmentActivity;


public class MarkerClusteringDemoActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MarkerClusteringDemoActivity";
    public final List<Marker> mMarkers = new ArrayList<Marker>();
    private HuaweiMap hMap;
    private boolean mIsRestore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsRestore = savedInstanceState != null;
        setContentView(R.layout.activity_main);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview_markerclusteringdemo))
                .getMapAsync(this);

    }

    @SuppressLint("LongLogTag")
    @Override
    public void onMapReady(HuaweiMap map) {
        Log.i(TAG, "onMapReady: ");
        //Create map
        hMap = map;
        //Move camera to position
        if (!mIsRestore) {
            hMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.5, -0.1), 10));
        }

        //Read points from JSON to List
        ArrayList<MarkerOptions> list = new ArrayList<>();

        Reader reader = new InputStreamReader(getResources().openRawResource(R.raw.radar_search_10000));

        Locations[] locations = new Gson().fromJson(reader, Locations[].class);


        for (int i = 0; i < locations.length; i++) {
            MarkerOptions m_options = new MarkerOptions();
            m_options.position(new LatLng(locations[i].lat,locations[i].lng));
            m_options.clusterable(true);
            list.add(m_options);
        }
        //Add markers to map
        addMarkers(list,true);
        //Turn clustering on
        hMap.setMarkersClustering(true);
    }


    private void addMarkers(ArrayList<MarkerOptions> items, boolean enableAnimation) {
        if (null == hMap) {
            return;
        }
        for (MarkerOptions item : items) {
            Marker marker = hMap.addMarker(item);
            mMarkers.add(marker);
        }
    }


}

class Locations{
     double lat;
     double lng;
}

package com.xomena.so49343164;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Context context = getApplicationContext();

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng center = new LatLng(41.385064,2.173403);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 13.0f));

        new MapGetNearbyPlacesData().execute(mMap);
    }

    private static class MapGetNearbyPlacesData extends AsyncTask<GoogleMap, Void, List<MarkerOptions>> {

        private GoogleMap map;
        private String TAG = "so49343164";

        @Override
        protected List<MarkerOptions> doInBackground(GoogleMap... maps) {
            this.map = maps[0];

            List<MarkerOptions> options = new ArrayList<>();

            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBrPt88vvoPDDn_imh-RzCXl5Ha2F2LYig")
                    .build();

            NearbySearchRequest req = PlacesApi.nearbySearchQuery(context, new com.google.maps.model.LatLng(41.385064,2.173403));
            try {
                PlacesSearchResponse resp = req.keyword("pizza").type(PlaceType.RESTAURANT).radius(2000).await();
                if (resp.results != null && resp.results.length > 0) {
                    for (PlacesSearchResult r : resp.results) {
                        PlaceDetails details = PlacesApi.placeDetails(context,r.placeId).await();

                        String name = details.name;
                        String address = details.formattedAddress;
                        URL icon = details.icon;
                        double lat = details.geometry.location.lat;
                        double lng = details.geometry.location.lng;
                        String vicinity = details.vicinity;
                        String placeId = details.placeId;
                        String phoneNum = details.internationalPhoneNumber;
                        String[] openHours = details.openingHours!=null ? details.openingHours.weekdayText : new String[0];
                        String hoursText = "";
                        for(String sv : openHours) {
                            hoursText += sv + "\n";
                        }
                        float rating = details.rating;

                        String content = address + "\n" +
                                "Place ID: " + placeId + "\n" +
                                "Rating: " + rating + "\n" +
                                "Phone: " + phoneNum + "\n" +
                                "Open Hours: \n" + hoursText;

                        options.add(new MarkerOptions().position(new LatLng(lat, lng))
                                .title(name)
                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeStream(icon.openConnection().getInputStream())))
                                .snippet(content)
                        );
                    }
                }
            } catch(Exception e) {
                Log.e(TAG, "Error getting places", e);
            }
            return options;
        }

        @Override
        protected void onPostExecute(List<MarkerOptions> options) {
            for(MarkerOptions opts : options) {
                this.map.addMarker(opts);
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}

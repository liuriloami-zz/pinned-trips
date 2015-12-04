package liuriloami.pinnedtrips.activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.List;

import liuriloami.pinnedtrips.R;
import liuriloami.pinnedtrips.database.DatabaseApplication;

public class ItineraryMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    DatabaseApplication database;
    List<HashMap<String, String>> placesList;
    String itineraryId;
    GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary_map);

        database = (DatabaseApplication) getApplicationContext();
        itineraryId = getIntent().getStringExtra("itinerary_id");

        //Get the map fragment and set this activity as its onMapReady listener
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    //When the map is ready, its context is stored in the googleMap attribute
    //The places list is retrieved from the database
    //When the map is loaded, the method onMapLoaded will be called.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        placesList = database.getDatabase().getItineraryPlaces(itineraryId);
        googleMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {

        //The LatLngBounds Builder is used to adjust the map zoom in order to
        // display all added places.
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //The Polyline is used to trace lines between the places
        PolylineOptions poly = new PolylineOptions();
        poly.geodesic(true);

        //For each place in this itinerary, create its marker, add it on the map,
        // on the builder and on the polyline.
        for (int i = 0; i < placesList.size(); i++) {
            LatLng markerPosition = new LatLng(Double.parseDouble(placesList.get(i).get("latitude")),Double.parseDouble(placesList.get(i).get("longitude")));
            MarkerOptions marker = new MarkerOptions().title(placesList.get(i).get("name")).position(markerPosition);
            googleMap.addMarker(marker);
            builder.include(markerPosition);
            poly.add(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        //The lines are inserted on the map
        googleMap.addPolyline(poly);

        //The camera is moved according to the bounds created
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

        //A zoomOut() is called in order to avoid places displayed near the screen border
        googleMap.moveCamera(CameraUpdateFactory.zoomOut());

        //The user location is displayed on the map.
        googleMap.setMyLocationEnabled(true);
    }
}
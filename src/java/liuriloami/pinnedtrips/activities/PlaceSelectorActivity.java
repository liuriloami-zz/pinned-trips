package liuriloami.pinnedtrips.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import liuriloami.pinnedtrips.R;
import liuriloami.pinnedtrips.database.DatabaseApplication;
import liuriloami.pinnedtrips.places.PlacesAdapter;

public class PlaceSelectorActivity extends Activity {

    List<HashMap<String, String>> placesList;
    ListView placesListView;
    DatabaseApplication database;
    ArrayList<String> placeNamesList, locationsList;
    String itineraryId;

    //Retrieve all places from the database, showing them on the screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selector);

        database = (DatabaseApplication) getApplicationContext();
        placesListView = (ListView) findViewById(R.id.place_selector_list);

        itineraryId = getIntent().getStringExtra("itinerary_id");
        placesList = database.getDatabase().getPlacesList();

        placeNamesList = new ArrayList<String>();
        locationsList = new ArrayList<String>();

        for (int i = 0; i < placesList.size(); i++) {
            HashMap<String, String> place = placesList.get(i);
            String name = place.get("name");
            String city = place.get("city");
            String country = place.get("country");
            String location = null;
            if (city != null)
                location = city + ", " + country;
            else
                location = country;
            placeNamesList.add(name);
            locationsList.add(location);
        }

        placesListView.setAdapter(new PlacesAdapter(this, placeNamesList, locationsList));

        //When a place is selected, it is inserted on the database and the activity is closed
        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                database.getDatabase().insertItineraryPlace(itineraryId, placesList.get(position).get("_id"));
                PlaceSelectorActivity.this.finish();
            }
        });
    }
}

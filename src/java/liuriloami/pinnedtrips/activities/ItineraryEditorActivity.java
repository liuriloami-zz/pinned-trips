package liuriloami.pinnedtrips.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import liuriloami.pinnedtrips.R;
import liuriloami.pinnedtrips.database.DatabaseApplication;
import liuriloami.pinnedtrips.places.PlacesAdapter;

public class ItineraryEditorActivity extends Activity {

    ListView placesListView;
    List<HashMap<String, String>> placesList;
    DatabaseApplication database;
    ArrayList<String> placeNames, placeLocations;
    String itineraryId, itineraryTitle;

    //Retrieve all places that belongs to this itinerary from the database
    //Create location names based on cities and countries
    //Change the places list with the new data (changing the adapter)
    private void refreshList() {
        placesList = database.getDatabase().getItineraryPlaces(itineraryId);
        placeNames = new ArrayList<String>();
        placeLocations = new ArrayList<String>();
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
            placeNames.add(name);
            placeLocations.add(location);
        }
        placesListView.setAdapter(new PlacesAdapter(this, placeNames, placeLocations));
    }

    //When a child activity is closed, resuming this activity, the list of places is refreshed
    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary_editor);

        //Get the database application
        database = (DatabaseApplication) getApplicationContext();

        //Get the itinerary id and title from the intent
        Intent intent = getIntent();
        itineraryId = intent.getStringExtra("_id");
        itineraryTitle = intent.getStringExtra("title");

        //Set the activity title
        setTitle(itineraryTitle);

        //Get the places list view and set its listener
        placesListView = (ListView) findViewById(R.id.itinerary_places_list);
        placesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //Create a new AlertDialog to confirm the deletion of the selected place
                AlertDialog.Builder builder = new AlertDialog.Builder(ItineraryEditorActivity.this);
                builder.setTitle("Confirm deletion");
                builder.setMessage("Are you sure you want to delete this place?");
                final int idx = position;

                //If confirmed, the place is removed from the itinerary, the list is refreshed
                // and a confirmation message is displayed on the screen.
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.getDatabase().removeItineraryPlace(itineraryId, placesList.get(idx).get("_id"));
                        refreshList();
                        Toast.makeText(getApplicationContext(), "Place deleted.", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                return true;
            }
        });

        //Populate the list of places for the first time
        refreshList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_itinerary_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_place_itinerary) {
            Intent intent = new Intent(this, PlaceSelectorActivity.class);
            intent.putExtra("itinerary_id", itineraryId);
            startActivity(intent);

        //If the itinerary has no places inserted, an error message is shown.
        } else if (id == R.id.show_on_map) {
            if (placesList.size() > 0) {
                Intent intent = new Intent(this, ItineraryMapActivity.class);
                intent.putExtra("itinerary_id", itineraryId);
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(this, "The itinerary needs to have at least one place inserted.", Toast.LENGTH_LONG);
                toast.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

}

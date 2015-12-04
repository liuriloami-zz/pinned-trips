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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import liuriloami.pinnedtrips.database.DatabaseApplication;
import liuriloami.pinnedtrips.places.PlacesAdapter;
import liuriloami.pinnedtrips.R;

public class MyPlacesActivity extends Activity {

    ArrayList<String> placeNames, placeLocations;
    List<HashMap<String, String>> placesList;
    ListView placesListView;
    DatabaseApplication database;

    //Delete files and folders recursivelly
    private void deleteRecursive (File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    //Retrieve all places from the database
    //Create location names based on cities and countries
    //Change the places list with the new data (changing its adapter)
    private void refreshList() {
        placesList = database.getDatabase().getPlacesList();
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
        setContentView(R.layout.activity_my_places);

        database = (DatabaseApplication) getApplicationContext();
        placesListView= (ListView) findViewById(R.id.my_places_list);

        //When a place item is clicked, its editor is opened
        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyPlacesActivity.this, PlaceEditorActivity.class);
                intent.putExtra("_id", placesList.get(position).get("_id"));
                startActivity(intent);
            }
        });

        //When a place item is selected, an AlertDialog is opened to confirm its deletion
        placesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyPlacesActivity.this);
                builder.setTitle("Confirm deletion");
                builder.setMessage("Are you sure you want to delete this place?");
                final int idx = position;

                //If the user confirms the deletion, the place and its related informations are
                // removed from the database, including its folder (with media and notes).
                //The places list is refreshed and a confirmation message is shown.
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Delete informations about the place from the database, including itineraries,
                        // notes and media informations.
                        database.getDatabase().removePlace(placesList.get(idx).get("_id"));

                        //The place folder is opened and its files are deleted recursivelly
                        String placeFolder = android.os.Environment.getExternalStorageDirectory() + File.separator +
                                "PinnedTrips" + File.separator +
                                placesList.get(idx).get("name").replaceAll("[^a-zA-Z0-9.-]", "_");
                        File folder = new File (placeFolder);
                        deleteRecursive(folder);

                        Toast.makeText(getApplicationContext(), "Place deleted.", Toast.LENGTH_LONG).show();
                        refreshList();
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
        getMenuInflater().inflate(R.menu.menu_my_places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //If the "Add place" button is selected, a PlacePicker is created
        //When the user chooses a place, the onActivityResult() is called
        if (id == R.id.add_place_button) {
            try {
                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                Intent intent = intentBuilder.build(this);
                startActivityForResult(intent, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //The place ID from the place selected by the user is sent to its activity
    //There, a new place will be created.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(this, PlaceEditorActivity.class);
            Place place = PlacePicker.getPlace(data, this);
            intent.putExtra("place_id", place.getId());
            startActivity(intent);
        }
    }
}

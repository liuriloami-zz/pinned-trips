package liuriloami.pinnedtrips.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import liuriloami.pinnedtrips.R;
import liuriloami.pinnedtrips.database.DatabaseApplication;

public class MyItinerariesActivity extends Activity {

    ListView itinerariesListView;
    DatabaseApplication database;
    List<HashMap<String, String>> itinerariesList;

    //When needed, refresh the list of itineraries, retrieving new data from the database
    private void refreshList() {
        itinerariesList = database.getDatabase().getItinerariesList();
        ArrayList<String> itinerariesTitles = new ArrayList<String>();
        for (int i = 0; i < itinerariesList.size(); i++)
            itinerariesTitles.add(itinerariesList.get(i).get("title"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, android.R.id.text1, itinerariesTitles);
        itinerariesListView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_itineraries);

        database = (DatabaseApplication) getApplicationContext();
        itinerariesListView = (ListView) findViewById(R.id.itineraries_list);

        //When an itinerary item is clicked, its editor is opened
        itinerariesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyItinerariesActivity.this, ItineraryEditorActivity.class);
                intent.putExtra("_id", itinerariesList.get(position).get("_id"));
                intent.putExtra("title", itinerariesList.get(position).get("title"));
                startActivity(intent);
            }
        });

        //When a itinerary item is selected, an AlertDialog is opened to confirm its deletion
        itinerariesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyItinerariesActivity.this);
                builder.setTitle("Confirm deletion");
                builder.setMessage("Are you sure you want to delete this itinerary?");
                final int idx = position;

                //If the user confirms the deletion, the itinerary is removed from the database,
                // the itineraries list is refreshed and a message is shown.
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.getDatabase().removeItinerary(itinerariesList.get(idx).get("_id"));
                        refreshList();
                        Toast.makeText(getApplicationContext(), "Itinerary deleted.", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                return true;
            }
        });

        //The list of itineraries  is retrieved for the first time
        refreshList();
    }

    //When a child activity is closed, resuming this activity, the list of itineraries is refreshed
    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_itineraries, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //When the "new itinerary" button is pressed, a dialog box is opened, asking for its name
        if (id == R.id.add_itinerary_button) {

            //Create a new AlertDialog and set its title
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Itinerary name:");

            //Create an EditText
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(32, 0, 32, 0);

            //Create a LinearLayout and put the EditText inside of it
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(input, params);

            //Set the view of the AlertDialog as the LinearLayout created
            builder.setView(layout);

            //When the "Create" button is pressed, the itinerary is inserted on the database and
            // its editor is opened.
            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String title = input.getText().toString();
                    long _id = database.getDatabase().insertItinerary(title);
                    Intent intent = new Intent(MyItinerariesActivity.this, ItineraryEditorActivity.class);
                    intent.putExtra("_id", String.valueOf(_id));
                    intent.putExtra("title", title);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }
}

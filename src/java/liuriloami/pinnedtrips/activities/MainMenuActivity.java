package liuriloami.pinnedtrips.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import liuriloami.pinnedtrips.R;
import liuriloami.pinnedtrips.database.DatabaseApplication;

public class MainMenuActivity extends Activity implements View.OnClickListener {

    private Button myPlacesButton, myItinerariesButton, optionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_menu);

        myPlacesButton = (Button) findViewById(R.id.my_places_button);
        myItinerariesButton = (Button) findViewById(R.id.my_itineraries_button);

        myPlacesButton.setOnClickListener(this);
        myItinerariesButton.setOnClickListener(this);

        DatabaseApplication database = (DatabaseApplication) getApplicationContext();
        database.openDatabase();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.my_places_button:
                intent = new Intent(this, MyPlacesActivity.class);
                startActivity(intent);
                break;
            case R.id.my_itineraries_button:
                intent = new Intent(this, MyItinerariesActivity.class);
                startActivity(intent);
                break;
        }
    }
}

package liuriloami.pinnedtrips.places;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import liuriloami.pinnedtrips.activities.MyPlacesActivity;
import liuriloami.pinnedtrips.R;

/**
 * Created by liuriloami on 22/11/15.
 */
public class PlacesAdapter extends BaseAdapter  {
    ArrayList<String> places, parents;
    LayoutInflater inflater;
    Activity activity;

    public PlacesAdapter (Activity activity, ArrayList<String> places, ArrayList<String> parents) {
        this.places = places;
        this.parents = parents;
        this.activity = activity;
        inflater = ( LayoutInflater )activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return places.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.place_item, null);

        TextView placeName = (TextView) rowView.findViewById(R.id.place_item_name);
        placeName.setText(places.get(position));

        TextView placeParents = (TextView) rowView.findViewById(R.id.place_item_parents);
        placeParents.setText(parents.get(position));

        return rowView;
    }
}

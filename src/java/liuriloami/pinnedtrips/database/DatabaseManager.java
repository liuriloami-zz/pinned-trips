package liuriloami.pinnedtrips.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuriloami on 23/11/15.
 */
public class DatabaseManager extends SQLiteOpenHelper {

    //CREATE TABLES SCRIPTS
    private static final String createPlacesTable = "CREATE TABLE places ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name VARCHAR(100), city VARCHAR(100), country VARCHAR(100), place_id varchar(100), " +
            "latitude REAL, longitude REAL);";

    private static final String createNotesTable = "CREATE TABLE notes ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "place_id INTEGER, title VARCHAR(100));";

    private static final String createItinerariesTable = "CREATE TABLE itineraries ( _id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(100));";

    private static final String createItineraryPlacesTable = "CREATE TABLE itinerary_places ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "place_id INTEGER, itinerary_id INTEGER);";

    //DROP TABLES SCRIPT
    private static final String dropTables = "DROP TABLE IF EXISTS places; " +
            "DROP TABLE IF EXISTS notes; " +
            "DROP TABLE IF EXISTS itineraries;" +
            "DROP TABLE IF EXISTS itinerary_places";

    private SQLiteDatabase database;

    public DatabaseManager(Context context) {
        super(context, "PinnedTrips", null, 1);
    }

    //When the database is created, all tables need to be created as well
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(createPlacesTable);
            db.execSQL(createNotesTable);
            db.execSQL(createItinerariesTable);
            db.execSQL(createItineraryPlacesTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //When the database is upgraded, all previous tables need to be deleted and all new created
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(dropTables);
        onCreate(db);
    }

    public void openDatase() {
        database = getWritableDatabase();
    }

    public void closeDatabase() {
        this.database.close();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////  Places methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public long insertPlace (String name, String city, String country, String placeId, double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("city", city);
        values.put("country", country);
        values.put("place_id", placeId);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        Cursor cursor = this.database.query("places", new String[]{"_id"}, "place_id = ?", new String[]{placeId}, null, null, null, null);
        if (!cursor.moveToFirst())
            return this.database.insert("places", null, values);
        else
            return cursor.getLong(cursor.getColumnIndex("_id"));
    }

    public List<HashMap<String, String>> getPlacesList() {
        Cursor cursor = this.database.query("places", new String[] {"_id", "name", "city", "country", "place_id", "latitude", "longitude"},
                null, null, null, null, null);
        List<HashMap<String, String>> rows = new ArrayList<HashMap<String, String>>();
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> place =  new HashMap<String, String>();
                place.put("_id", String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
                place.put("name", cursor.getString(cursor.getColumnIndex("name")));
                place.put("city", cursor.getString(cursor.getColumnIndex("city")));
                place.put("country", cursor.getString(cursor.getColumnIndex("country")));
                place.put("place_id", cursor.getString(cursor.getColumnIndex("place_id")));
                place.put("latitude", String.valueOf(cursor.getDouble(cursor.getColumnIndex("latitude"))));
                place.put("longitude", String.valueOf(cursor.getDouble(cursor.getColumnIndex("longitude"))));
                rows.add(place);
            } while (cursor.moveToNext());
        }
        return rows;
    }

    public HashMap<String, String> getPlaceById(String _id) {
        Cursor cursor = this.database.query("places", new String[] {"_id", "name", "city", "country", "place_id", "latitude", "longitude"}, "_id = ?", new String[] { _id }, null, null, null);
        HashMap<String, String> place = new HashMap<String, String>();
        cursor.moveToFirst();
        place.put("_id", String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
        place.put("name", cursor.getString(cursor.getColumnIndex("name")));
        place.put("city", cursor.getString(cursor.getColumnIndex("city")));
        place.put("country", cursor.getString(cursor.getColumnIndex("country")));
        place.put("place_id", cursor.getString(cursor.getColumnIndex("place_id")));
        place.put("latitude", String.valueOf(cursor.getDouble(cursor.getColumnIndex("latitude"))));
        place.put("longitude", String.valueOf(cursor.getDouble(cursor.getColumnIndex("longitude"))));
        return place;
    }

    public void removePlace (String _id) {
        database.delete("places", "_id = ?", new String[]{_id});
        database.delete("notes", "place_id = ?", new String[]{_id});
        database.delete("itinerary_places", "place_id = ?", new String[]{_id});
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////  Notes methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public long insertNote (String title, String place_id) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("place_id", place_id);
        return this.database.insert("notes", null, values);
    }

    public List<HashMap<String, String>> getNotesList(String place_id) {
        Cursor cursor = this.database.query("notes", new String[] {"_id", "title"}, "place_id = ?", new String[] { place_id }, null, null, null);
        List<HashMap<String, String>> rows = new ArrayList<HashMap<String, String>>();
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> place =  new HashMap<String, String>();
                place.put("_id", String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
                place.put("title", cursor.getString(cursor.getColumnIndex("title")));
                rows.add(place);
            } while (cursor.moveToNext());
        }
        return rows;
    }

    public HashMap<String, String> getNoteById(int _id) {
        Cursor cursor = this.database.query("notes", new String[] {"_id", "title"}, "_id = ?", new String[] { String.valueOf(_id)}, null, null, null);
        HashMap<String, String> place = new HashMap<String, String>();
        cursor.moveToFirst();
        place.put("_id", String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
        place.put("title", cursor.getString(cursor.getColumnIndex("title")));
        return place;
    }

    public void removeNote (String _id) {
        database.delete("notes", "_id = ?", new String[] { _id });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////  Itineraries methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public long insertItinerary (String title) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        return this.database.insert("itineraries", null, values);
    }

    public long insertItineraryPlace (String itinerary_id, String place_id) {
        ContentValues values = new ContentValues();
        values.put("itinerary_id", itinerary_id);
        values.put("place_id", place_id);
        return this.database.insert("itinerary_places", null, values);
    }

    public List<HashMap<String, String>> getItineraryPlaces(String _id) {
        Cursor itineraryCursor = this.database.query("itinerary_places", new String[] {"place_id"}, "itinerary_id = ?", new String[] { _id }, null, null, null);
        List<HashMap<String, String>> places = new ArrayList<HashMap<String, String>>();
        if (itineraryCursor.moveToFirst()) {
            do {
                String placeId = itineraryCursor.getString(itineraryCursor.getColumnIndex("place_id"));
                Cursor placeCursor = this.database.query("places", new String[] {"_id", "name", "city", "country", "latitude", "longitude"}, "_id = ?", new String[] { placeId }, null, null, null);
                if (placeCursor.moveToFirst()) {
                    do {
                        HashMap<String, String> place = new HashMap<String, String>();
                        place.put("name", placeCursor.getString(placeCursor.getColumnIndex("name")));
                        place.put("_id", String.valueOf(placeCursor.getLong(placeCursor.getColumnIndex("_id"))));
                        place.put("city", placeCursor.getString(placeCursor.getColumnIndex("city")));
                        place.put("country", placeCursor.getString(placeCursor.getColumnIndex("country")));
                        place.put("latitude", String.valueOf(placeCursor.getDouble(placeCursor.getColumnIndex("latitude"))));
                        place.put("longitude", String.valueOf(placeCursor.getDouble(placeCursor.getColumnIndex("longitude"))));
                        place.put("place_id", placeId);
                        places.add(place);
                    } while (placeCursor.moveToNext());
                }
            } while (itineraryCursor.moveToNext());
        }
        return places;
    }

    public List<HashMap<String, String>> getItinerariesList() {
        Cursor cursor = this.database.query("itineraries", new String[] {"_id", "title"}, null, null, null, null, null);
        List<HashMap<String, String>> rows = new ArrayList<HashMap<String, String>>();
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> place =  new HashMap<String, String>();
                place.put("_id", String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
                place.put("title", cursor.getString(cursor.getColumnIndex("title")));
                rows.add(place);
            } while (cursor.moveToNext());
        }
        return rows;
    }

    public void removeItinerary (String _id) {
        database.delete("itineraries", "_id = ?", new String[] { _id });
        database.delete("itinerary_places", "itinerary_id = ?", new String[] { _id });
    }

    public void removeItineraryPlace(String itinerary_id, String place_id) {
        database.delete("itinerary_places", "itinerary_id = ? AND place_id = ?", new String[] { itinerary_id, place_id });
    }
}

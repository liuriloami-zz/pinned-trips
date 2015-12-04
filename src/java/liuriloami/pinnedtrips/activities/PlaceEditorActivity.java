package liuriloami.pinnedtrips.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import liuriloami.pinnedtrips.database.DatabaseApplication;
import liuriloami.pinnedtrips.gallery.GalleryGridAdapter;
import liuriloami.pinnedtrips.gallery.GalleryGridView;
import liuriloami.pinnedtrips.R;

public class PlaceEditorActivity extends Activity implements ResultCallback<PlaceBuffer> {

    DatabaseApplication database;

    GoogleApiClient googleApiClient;
    Geocoder geocoder;

    TextView placeNameView, locationView;
    ListView notesListView;
    GalleryGridView galleryView;
    Button addMediaButton, addNoteButton;

    String placeFolder, mediaFolder, placeId;
    ArrayList<String> imagesList;
    List<HashMap<String, String>> notesList;

    //When needed, refresh the list of notes and medias, retrieving new data from the database and from their folders
    private void refreshList() {

        //If the media folder is null, the data are not ready to be retrieved
        if (mediaFolder == null)
            return;
        
        imagesList = new ArrayList<String>();
        ArrayList<String> notesTitle = new ArrayList<String>();

        //If the media folder does not exists, create it
        File folder = new File(mediaFolder);
        if (!folder.exists())
            folder.mkdirs();

        //Retrieve all images from the media folder
        File files[] = folder.listFiles();

        //Get the path of each image and put it on the images list
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String path = files[i].getAbsolutePath();
                imagesList.add(path);
            }
        }

        //Change the gallery, inserting the images retrieved
        galleryView.setAdapter(new GalleryGridAdapter(this, imagesList));

        //Get all notes from the database
        notesList = database.getDatabase().getNotesList(placeId);
        for (int i = 0; i < notesList.size(); i++)
            notesTitle.add(notesList.get(i).get("title"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, android.R.id.text1, notesTitle);
        notesListView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_place_editor);

        database = (DatabaseApplication) getApplication();
        mediaFolder = placeFolder = null;

        //Get the header and the footer layouts
        View header = getLayoutInflater().inflate(R.layout.place_editor_header, null);
        View footer = getLayoutInflater().inflate(R.layout.place_editor_footer, null);

        //Get the ListView and insert the header and footer on it
        notesListView = (ListView) findViewById(R.id.notes_list);
        notesListView.addHeaderView(header);
        notesListView.addFooterView(footer);

        addNoteButton = (Button) findViewById(R.id.new_note_button);
        galleryView = (GalleryGridView) findViewById(R.id.gallery_grid);
        addMediaButton = (Button) findViewById(R.id.add_media_button);
        placeNameView = (TextView) findViewById(R.id.place_editor_name);
        locationView = (TextView) findViewById(R.id.place_editor_location);

        //When a note item is clicked, its editor is opened
        notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Position 0 is the header, not a note
                if (position == 0) return;

                Intent intent = new Intent(PlaceEditorActivity.this, NoteEditorActivity.class);
                intent.putExtra("place_folder", placeFolder);
                intent.putExtra("place_id", placeId);
                intent.putExtra("_id", notesList.get(position - 1).get("_id"));
                intent.putExtra("title", notesList.get(position - 1).get("title"));
                startActivity(intent);
            }
        });

        //When a note item is selected, an AlertDialog is opened to confirm its deletion
        notesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlaceEditorActivity.this);
                builder.setTitle("Confirm deletion");
                builder.setMessage("Are you sure you want to delete this note?");
                final int idx = position;

                //If the user confirms the deletion, the note is removed from the database and its
                // file is retrieved and deleted. A confirmation message is shown.
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String noteId = notesList.get(idx - 1).get("_id");
                        database.getDatabase().removeNote(noteId);
                        File folder = new File(placeFolder + File.separator + "notes");
                        String filename = placeFolder + File.separator + "notes" + File.separator + noteId + "-";
                        File files[] = folder.listFiles();
                        for (int i = 0; i < files.length; i++)
                            if (files[i].getAbsolutePath().indexOf(filename) != -1) {
                                files[i].delete();
                                break;
                            }
                        Toast.makeText(getApplicationContext(), "Note deleted.", Toast.LENGTH_LONG).show();
                        refreshList();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                return true;
            }
        });

        //When the "add note" button is clicked, the note editor is opened
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaceEditorActivity.this, NoteEditorActivity.class);
                intent.putExtra("place_folder", placeFolder);
                intent.putExtra("place_id", placeId);
                startActivity(intent);
            }
        });

        //When a gallery image is clicked, the image is opened by an external image viewer
        galleryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_VIEW);
                gallery.setDataAndType(Uri.parse("file://" + imagesList.get(position)), "image/*");
                startActivity(gallery);
            }
        });

        //When a gallery image is selected, an AlertDialog is opened to confirm its deletion
        galleryView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlaceEditorActivity.this);
                builder.setTitle("Confirm deletion");
                builder.setMessage("Are you sure you want to delete this place?");
                final int idx = position;

                //If the user confirms the image delete, the image file is deleted
                //The gallery is refreshed and a confirmation messaged is shown.
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Liuri", "File: " + imagesList.get(idx));
                        File imageFile = new File(imagesList.get(idx));
                        imageFile.delete();
                        refreshList();
                        Toast.makeText(getApplicationContext(), "Media deleted.", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                return true;
            }
        });

        //When the "add media" button is clicked, an external image selector is opened
        addMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), 1);
            }
        });

        //If the intent has a "place_id" extra, than it is necessary to retrieve the
        // informations about the place and insert it on the database
        Intent intent = getIntent();
        if (intent.hasExtra("place_id")) {
            String placeId = intent.getStringExtra("place_id");

            //Connect to Google API and request informations about the place
            googleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).build();
            googleApiClient.connect();
            Places.GeoDataApi.getPlaceById(googleApiClient, placeId).setResultCallback(this);

        //If the intent has a "_id" extra, all informations can be retrieved from the database
        } else if (intent.hasExtra("_id")) {
            placeId = intent.getStringExtra("_id");
            HashMap<String, String> place = database.getDatabase().getPlaceById(placeId);
            String name = place.get("name");
            String city = place.get("city");
            String country = place.get("country");
            String location = null;
            if (city != null)
                location = city + ", " + country;
            else
                location = country;

            placeNameView.setText(name);
            locationView.setText(location);
            placeFolder = android.os.Environment.getExternalStorageDirectory() + File.separator +
                    "PinnedTrips" + File.separator +
                    placeNameView.getText().toString().replaceAll("[^a-zA-Z0-9.-]", "_");
            mediaFolder = placeFolder + File.separator + "media";

            //The notes and media data are retrieved for the first time
            refreshList();
        }
    }

    @Override
    public void onDestroy() {
        if (googleApiClient != null)
            googleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    //When the informations of the place is received, do a second request
    // to Geocoder asking for informations about its location
    //The place is inserted on the database and displayed on the screen
    @Override
    public void onResult(PlaceBuffer places) {
        try {
            if (places.getStatus().isSuccess()) {
                final Place place = places.get(0);

                //Geocoder request and response
                geocoder = new Geocoder(this, Locale.getDefault());
                LatLng latLng = place.getLatLng();
                List<Address> addresses = null;
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                Address addressObj = addresses.get(0);

                String name = (String) place.getName();
                String city = addressObj.getLocality();
                String country = addressObj.getCountryName();


                String location = "";
                if (city != null)
                    location += city + ", ";
                if (country != null)
                    location+= country;

                placeNameView.setText(name);
                locationView.setText(location);

                placeId = String.valueOf(database.getDatabase().insertPlace(name, city, country, place.getId(), latLng.latitude, latLng.longitude));

                placeFolder = android.os.Environment.getExternalStorageDirectory() + File.separator +
                        "PinnedTrips" + File.separator +
                        placeNameView.getText().toString().replaceAll("[^a-zA-Z0-9.-]", "_");
                mediaFolder = placeFolder + File.separator + "media";

                refreshList();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Get the real media path, based on the URI with its ID
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        String wholeID = DocumentsContract.getDocumentId(contentUri);
        String id = wholeID.split(":")[1];
        String[] column = { MediaStore.Images.Media.DATA };
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = context.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);
        String filePath = "";
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    //When new images are added, the images are copied to the media folder
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == Activity.RESULT_OK && data != null) {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {

                ClipData.Item item = clipData.getItemAt(i);
                Uri imageUri = item.getUri();

                String url = getRealPathFromUri(this, imageUri);
                File originalImage = new File(url);

                File newImage = new File(mediaFolder + File.separator + Uri.parse(url).getLastPathSegment());
                if (newImage.exists())
                    newImage.delete();
                newImage.createNewFile();

                InputStream in = new FileInputStream(originalImage);
                OutputStream out = new FileOutputStream(newImage);
                byte[] buff = new byte[1024];
                int len;
                while ((len = in.read(buff)) > 0) {
                    out.write(buff, 0, len);
                }
                in.close();
                out.close();
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    }
}

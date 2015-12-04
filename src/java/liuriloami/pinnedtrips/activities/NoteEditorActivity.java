package liuriloami.pinnedtrips.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import liuriloami.pinnedtrips.R;
import liuriloami.pinnedtrips.database.DatabaseApplication;

public class NoteEditorActivity extends Activity {

    Button submitButton;
    DatabaseApplication database;
    String notesFolder;
    String placeId, noteId;
    TextView noteView, titleView;
    File noteFile;

    //An Activity attribute is needed for the listeners
    final Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        database = (DatabaseApplication) getApplication();
        titleView = (TextView) findViewById(R.id.note_title);
        noteView = (TextView) findViewById(R.id.note_text);
        submitButton = (Button) findViewById(R.id.save_note_button);

        //When the note is saved, all its previous informations are deleted from
        // the database, including its file, and then all new informations are saved.
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteId != null) {
                    noteFile.delete();
                    database.getDatabase().removeNote(String.valueOf(noteId));
                }

                String title = titleView.getText().toString();
                String note = noteView.getText().toString();

                long id = database.getDatabase().insertNote(title, placeId);

                try {
                    File newNoteFile = new File(notesFolder, String.valueOf(id) + "-" + title.replaceAll("[^a-zA-Z0-9.-]", "_") + ".note");
                    FileWriter fileWriter = new FileWriter(newNoteFile);
                    fileWriter.write(note);
                    fileWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                activity.finish();
            }
        });

        //Get the place folder and ID from the intent
        //If the notes folder does not exists, create it.
        Intent intent = getIntent();
        notesFolder = intent.getStringExtra("place_folder") + File.separator + "notes";
        placeId = intent.getStringExtra("place_id");
        File folder = new File(notesFolder);
        if (!folder.exists())
            folder.mkdirs();
        ;

        //If the note exists (user is editing it), retrieve its informations
        if (intent.hasExtra("_id")) {
            titleView.setText(intent.getStringExtra("title"));
            noteId = intent.getStringExtra("_id");
            String filename = notesFolder + File.separator + String.valueOf(noteId) + "-";
            File files[] = folder.listFiles();

            //Search for the notes file
            //When the file is found, copy its content to a String
            for (int i = 0; i < files.length; i++) {
                if (files[i].getAbsolutePath().indexOf(filename) != -1) {
                    try {
                        String text = new Scanner(files[i]).useDelimiter("\\A").next();
                        Log.d("Liuri", "Text: " + text);
                        noteView.setText(text);
                        noteFile = files[i];
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            noteFile = null;
        }
    }
}
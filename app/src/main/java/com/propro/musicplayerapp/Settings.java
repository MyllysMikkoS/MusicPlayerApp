package com.propro.musicplayerapp;

import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class Settings extends AppCompatActivity {

    Toolbar toolbar;
    ListView settingsListView;
    ImageButton addFolderButton;

    private static SourcesAdapter adapter;
    ArrayList<Source> paths;
    final int PICKFILE_REQUEST_CODE = 43;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.setTitle("Settings");

        // Init views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        settingsListView = findViewById(R.id.queueListView);

        paths = new ArrayList<Source>();
        adapter = new SourcesAdapter(this, paths);

        // TESTING --
        MusicSources sources = MusicSources.getInstance();
        for (Source source : sources) {
            adapter.add(source);
        }
        // TESTING --

        settingsListView.setAdapter(adapter);

        // Add folder button
        addFolderButton = (ImageButton) findViewById(R.id.addFolderButton);
        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });
    }

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));
            String path = CustomUtilities.getPath(this, docUri);

            // Add source to adapter and dataContainer
            boolean alreadyExists = false;
            Source newSource = new Source(path);
            for (Source src : MusicSources.getInstance()) {
                if (newSource.path.equals(src.path)) {
                    alreadyExists = true;
                }
            }

            // Add path if it doesn't already exist
            if (!alreadyExists) {
                Log.d("ADDING PATH: ", path);
                MusicSources.getInstance().add(newSource);
                adapter.add(newSource);
            } else {
                Log.d("PATH ALREADY ADDED: ", path);
                Toast.makeText(this, "Path already added",
                        Toast.LENGTH_SHORT).show();
            }

            // Update listView
            ((SourcesAdapter) settingsListView.getAdapter()).notifyDataSetChanged();
        }

        super.onActivityResult(requestCode, resultCode, data);

    }
}

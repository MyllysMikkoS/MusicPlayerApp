package com.propro.musicplayerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Homescreen extends AppCompatActivity {

    // Views
    RelativeLayout mediaButtonsRelativeLayout;
    MediaButtons mediaButtons;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        // Init views
        textView = findViewById(R.id.textView);

        // Create mediabuttons
        mediaButtonsRelativeLayout = findViewById(R.id.MediaButtonsRelativeLayout);
        mediaButtons = new MediaButtons(this);
        mediaButtons.setOnSliceClickListener(new MediaButtons.OnSliceClickListener() {
            @Override
            public void onSlickClick(int slicePosition, float progress) {
                String text = String.valueOf(slicePosition) + " " + String.valueOf(progress);
                textView.setText(text);
            }
        });
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        Log.d("WIDTH:", String.valueOf(mediaButtonsRelativeLayout.getWidth()));
        mediaButtonsRelativeLayout.addView(mediaButtons, params);
    }
}

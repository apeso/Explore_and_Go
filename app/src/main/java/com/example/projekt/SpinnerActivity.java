package com.example.projekt;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;

public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
    public String odabrano;
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        odabrano = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}

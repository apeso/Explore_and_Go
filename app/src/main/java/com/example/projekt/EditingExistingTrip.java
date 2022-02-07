package com.example.projekt;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class EditingExistingTrip extends AppCompatActivity {
    ImageView img,addNewImage;
    ProgressBar progressBar;
    BottomNavigationView bottomNavigationView;
    Spinner spinnerCountry,spinnerCities;
    CheckBox publicChb;
    Button btnSave;
    EditText et_title,et_description;
    DatePicker datePicker;
    ActivityResultLauncher<String> activityResultLauncher;
    Uri selectedImage;
    public FirebaseAuth mAuth;
    public FirebaseFirestore fstore;
    public StorageReference fStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing_existing_trip);
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        fStorage= FirebaseStorage.getInstance().getReference();

        spinnerCountry=(Spinner) findViewById(R.id.spinnerCountry);
        spinnerCities=(Spinner) findViewById(R.id.spinnerCities);
        fillSpinnerWithCountries();
        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(spinnerCountry.getSelectedItem()!=null){
                    fillSpinnerWithCities(spinnerCountry.getSelectedItem().toString());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(EditingExistingTrip.this,"Select country first",Toast.LENGTH_LONG).show();
                return;
            }
        });

        img=(ImageView) findViewById(R.id.img);
        addNewImage=(ImageView) findViewById(R.id.add);
        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        et_title=(EditText) findViewById(R.id.title);
        et_description=(EditText) findViewById(R.id.description);
        datePicker=(DatePicker) findViewById(R.id.datePicker);
        publicChb=(CheckBox) findViewById(R.id.checkBox);
        btnSave=(Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(view -> saveTrip());

        Calendar c = Calendar.getInstance();
        datePicker.setMaxDate(c.getTimeInMillis());

        selectImageFromGallery();
        activityResultLauncher= registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                img.setImageURI(result);
                selectedImage=result;
                progressBar.setVisibility(View.INVISIBLE);
                addNewImage.setVisibility(View.VISIBLE);
                //trenutno necemo pozivati upload na bazu jer prvo cemo pricekati da korisnik klikne save button
                //i onda cemo dohvatit id tripa koji se spremio na fStore
                //uploadImageToFirebase(result);
            }
        });

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setSelectedItemId(R.id.dos);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.uno:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        return true;
                    case R.id.dos:
                        startActivity(new Intent(getApplicationContext(), NewTripActivity.class));
                        return true;
                    case R.id.tres:
                        startActivity(new Intent(getApplicationContext(), MyTravelsActivity.class));
                        return true;
                    case R.id.cuatro:
                        Intent i=new Intent(getApplicationContext(), ProfileActivity.class);
                        startActivity(i);
                        return true;
                }
                updateNavigationBarState(item.getItemId());

                return true;
            }
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String id_trip = extras.getString("key");
            fillForm(id_trip);
        }
    }

    private void fillForm(String key) {
        Log.i("kate",key);

        DocumentReference documentReference = fstore.collection("trips").document(key);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                et_title.setText(value.getString("title"));
                et_description.setText(value.getString("description"));
                String date=value.getString("date");
                String[] date2=date.split("/");
                Integer year=Integer.parseInt(date2[2]);
                Integer month=Integer.parseInt(date2[1]);
                Integer day=Integer.parseInt(date2[0]);
                try {
                    datePicker.updateDate(year,month-1,day);
                }catch(Exception e) {
                    Log.i("kate","nece da ucita datum");
                }
                publicChb.setChecked(value.getBoolean("published"));
                //gradovi ostaju kako su pocetni zadani jer je pretesko mi ucitat ove jer se spremaju po vrijednosti a ne po poziciji

            }
        });
    }

    private void selectImageFromGallery() {
        addNewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();

            }
        });
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

    }
    private void selectImage(){
        addNewImage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        //otvara galeriju slika na uređaju
        activityResultLauncher.launch("image/*");

    }
    public void saveTrip() {
        String title=et_title.getText().toString().trim();
        String description=et_description.getText().toString().trim();
        String date=datePicker.getDayOfMonth()+"/"+(datePicker.getMonth()+1)+"/"+datePicker.getYear();
        String country=spinnerCountry.getSelectedItem().toString();
        String city=spinnerCities.getSelectedItem().toString();
        Boolean publicCheckBoxState=publicChb.isChecked();

        if (title.isEmpty()) {
            et_title.setError("Title is required!");
            et_title.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            et_description.setError("Description is required!");
            et_description.requestFocus();
            return;
        }
        if (date.isEmpty()) {
            //datePicker.setError("Name is required!");
            datePicker.requestFocus();
            return;
        }
        if (country.isEmpty()) {
            //spinnerCountry.setError("Name is required!");
            spinnerCountry.requestFocus();
            return;
        }
        if (city.isEmpty()) {
            //spinnerCities.setError("Name is required!");
            spinnerCities.requestFocus();
            return;
        }
        if(selectedImage==null){
            img.requestFocus();
            showMessage("Morate odabrati sliku!");
            return;
        }


        DocumentReference myRef = fstore.collection("trips").document();
        // get trip unique ID and upadte trip key
        String key = myRef.getId();

        Trip novi_trip=new Trip();
        novi_trip.setId(key);
        novi_trip.setName(title);
        novi_trip.setdescription(description);
        novi_trip.setDate(date);
        novi_trip.setId_city(city);
        novi_trip.setId_country(country);
        novi_trip.setPublished(publicCheckBoxState);
        novi_trip.setLink_to_image(selectedImage.toString());

        //uploadImageAndPostToFirebase(selectedImage,key,novi_trip);

    }

    private void fillSpinnerWithCountries() {
        ArrayList<String> list=new ArrayList<>();
        String json=loadJSONFromAsset();

        if(json!=null){
            try {
                JSONObject jsonObj = new JSONObject(json);
                JSONObject list_cit = jsonObj.getJSONObject("countries");

                Iterator keys=list_cit.keys();

                while (keys.hasNext()){
                    list.add(keys.next().toString());
                }
                ArrayAdapter<String> arrayAdapterofCountries=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,list);
                arrayAdapterofCountries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCountry.setAdapter(arrayAdapterofCountries);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(EditingExistingTrip.this,"Nije ucitan json file",Toast.LENGTH_LONG).show();
            return;

        }
    }
    private void fillSpinnerWithCities(String selectedItem) {
        String json=loadJSONFromAsset();
        if(json!=null){
            try {
                JSONObject jsonObj = new JSONObject(json);
                JSONArray cities = jsonObj.getJSONObject("countries").getJSONArray(selectedItem);
                ArrayList<String> list=new ArrayList<>();

                for (int i=0;i<cities.length();i++){
                    list.add(cities.get(i).toString());
                }
                ArrayAdapter<String> arrayAdapterofCities=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,list);
                arrayAdapterofCities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCities.setAdapter(arrayAdapterofCities);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(EditingExistingTrip.this,"Nije ucitan json file",Toast.LENGTH_LONG).show();
            return;
        }
    }
    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getApplicationContext().getAssets().open("countries.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
    public  void showMessage(String message){
        Toast.makeText(EditingExistingTrip.this,message,Toast.LENGTH_LONG).show();
    }
    private void updateNavigationBarState(int actionId){
        MenuItem item = bottomNavigationView.getMenu().findItem(actionId);
        item.setChecked(true);
    }
}
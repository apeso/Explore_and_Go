package com.example.projekt;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.widget.DatePicker;
public class NewTripActivity extends AppCompatActivity{
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);
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
                Toast.makeText(NewTripActivity.this,"Select country first",Toast.LENGTH_LONG).show();
                return;
            }
        });

        img=(ImageView) findViewById(R.id.img);
        addNewImage=(ImageView) findViewById(R.id.add);
        progressBar=(ProgressBar) findViewById(R.id.progressBar);
        et_title=(EditText) findViewById(R.id.title);
        et_description=(EditText) findViewById(R.id.description);
        et_description.setImeOptions(EditorInfo.IME_ACTION_DONE);
        et_description.setRawInputType(InputType.TYPE_CLASS_TEXT);
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
        //bottomNavigationView.setSelectedItemId(R.id.dos);

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
                return true;
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
    public void uploadImageAndPostToFirebase(Uri imageUri,String key,Trip t){
        et_title.setVisibility(View.INVISIBLE);
        et_description.setVisibility(View.INVISIBLE);
        datePicker.setVisibility(View.INVISIBLE);
        publicChb.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
        spinnerCountry.setVisibility(View.INVISIBLE);
        spinnerCities.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.VISIBLE);


        StorageReference fillRef=fStorage.child("trips/"+"/"+key+"/trip_profile.jpg");
        fillRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fillRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(img);
                        DocumentReference documentReference = fstore.collection("trips").document(key);
                        Map<String, Object> trip = new HashMap<>();
                        trip.put("id",key);
                        trip.put("title", t.getTitle());
                        trip.put("description", t.getdescription());
                        trip.put("date", t.getDate());
                        trip.put("published",t.getPublished());
                        trip.put("country", t.getCountry());
                        trip.put("city", t.getCity());
                        trip.put("id_user",mAuth.getCurrentUser().getUid());
                        trip.put("link_to_image",uri.toString());
                        documentReference.set(trip).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("KATE","success");
                                Toast.makeText(NewTripActivity.this, "Trip successfully saved!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), MyTravelsActivity.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("KATE", "failure"+ e.toString());
                                Toast.makeText(NewTripActivity.this, "Failed!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewTripActivity.this,"Image not uploaded!",Toast.LENGTH_LONG).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewTripActivity.this,"Image not uploaded!",Toast.LENGTH_LONG).show();

            }
        });
    }
    public void saveTrip() {
        String title=et_title.getText().toString().trim();
        String description=et_description.getText().toString().trim();
        String mjesec;
        if(datePicker.getMonth()<9){
            mjesec="0"+datePicker.getMonth()+1;
        }
        else{
            mjesec=""+datePicker.getMonth()+1;
        }
        String date=datePicker.getYear()+"/"+mjesec+"/"+datePicker.getDayOfMonth();
        String dan=String.valueOf(datePicker.getDayOfMonth());
        if(dan.length()==1){
            date=datePicker.getYear()+"/"+mjesec+"/"+"0"+dan;
        }
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
        // get trip unique ID and update trip key
        String key = myRef.getId();

        Trip novi_trip=new Trip();
        novi_trip.setId(key);
        novi_trip.setTitle(title);
        novi_trip.setdescription(description);
        novi_trip.setDate(date);
        novi_trip.setCity(city);
        novi_trip.setCountry(country);
        novi_trip.setPublished(publicCheckBoxState);
        novi_trip.setLink_to_image(selectedImage.toString());

        uploadImageAndPostToFirebase(selectedImage,key,novi_trip);

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
            Toast.makeText(NewTripActivity.this,"Nije ucitan json file",Toast.LENGTH_LONG).show();
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
            Toast.makeText(NewTripActivity.this,"Nije ucitan json file",Toast.LENGTH_LONG).show();
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
        Toast.makeText(NewTripActivity.this,message,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        int seletedItemId = bottomNavigationView.getSelectedItemId();
        if (R.id.dos == seletedItemId) {
            super.onBackPressed();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.dos);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        bottomNavigationView.getMenu().getItem(1).setChecked(true);
    }
}
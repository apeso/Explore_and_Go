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
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EditingExistingTrip extends AppCompatActivity {
    ImageView img,addNewImage;
    ProgressBar progressBar;
    BottomNavigationView bottomNavigationView;
    CheckBox publicChb;
    Button btnSave,btnDelete;
    EditText et_title,et_description;
    DatePicker datePicker;
    ActivityResultLauncher<String> activityResultLauncher;
    Uri selectedImage;
    Uri novo_odabrana_slika=null;
    public FirebaseAuth mAuth;
    public FirebaseFirestore fstore;
    public StorageReference fStorage;
    public String kljuc="";
    public String city,country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing_existing_trip);
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        fStorage= FirebaseStorage.getInstance().getReference();


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

        btnDelete=(Button) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(view -> deleteTrip());

        Calendar c = Calendar.getInstance();
        datePicker.setMaxDate(c.getTimeInMillis());

        selectImageFromGallery();
        activityResultLauncher= registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                img.setImageURI(result);
                novo_odabrana_slika=result;
                progressBar.setVisibility(View.INVISIBLE);
                addNewImage.setVisibility(View.VISIBLE);
                //trenutno necemo pozivati upload na bazu jer prvo cemo pricekati da korisnik klikne save button
                //i onda cemo dohvatit id tripa koji se spremio na fStore
                //uploadImageToFirebase(result);
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String id_trip = extras.getString("key");
            fillForm(id_trip);
        }
    }
    private void deleteTrip() {
        DocumentReference documentReference = fstore.collection("trips").document(kljuc);
        documentReference.delete();
        startActivity(new Intent(getApplicationContext(), MyTravelsActivity.class));


    }
    private void fillForm(String key) {
        Log.i("kate",key);
        kljuc=key;
        DocumentReference documentReference = fstore.collection("trips").document(key);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Picasso.get().load(value.getString("link_to_image")).into(img);
                selectedImage=Uri.parse(value.getString("link_to_image"));
                et_title.setText(value.getString("title"));
                et_description.setText(value.getString("description"));
                city=value.getString("city");
                country=value.getString("country");
                String[] date=value.getString("date").split("/");
                Integer year=Integer.parseInt(date[0]);
                Integer month=Integer.parseInt(date[1]);
                Integer day=Integer.parseInt(date[2]);
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
        String key="";
        String title=et_title.getText().toString().trim();
        String description=et_description.getText().toString().trim();
        String mjesec;
        if(datePicker.getMonth()<9){
            mjesec="0"+String.valueOf(datePicker.getMonth()+1);
        }
        else{
            mjesec=""+String.valueOf(datePicker.getMonth()+1);
        }
        String date=datePicker.getYear()+"/"+mjesec+"/"+datePicker.getDayOfMonth();
        String dan=String.valueOf(datePicker.getDayOfMonth());
        if(dan.length()==1){
            date=datePicker.getYear()+"/"+mjesec+"/"+"0"+dan;
        }

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

        if(selectedImage==null){
            img.requestFocus();
            showMessage("Morate odabrati sliku!");
            return;
        }

        Trip novi_trip=new Trip();
        novi_trip.setId(key);
        novi_trip.setTitle(title);
        novi_trip.setdescription(description);
        novi_trip.setDate(date);
        novi_trip.setCity(city);
        novi_trip.setCountry(country);

        novi_trip.setPublished(publicCheckBoxState);
        novi_trip.setLink_to_image(selectedImage.toString());

        if(kljuc!=""){
            uploadImageAndPostToFirebase(selectedImage,kljuc,novi_trip);
            Log.i("kate",kljuc+"  slika"+selectedImage.toString());
        }
        else Log.i("kate","KLJUC PRAZAN");

    }
    public void uploadImageAndPostToFirebase(Uri imageUri,String key,Trip t){
        et_title.setVisibility(View.INVISIBLE);
        et_description.setVisibility(View.INVISIBLE);
        datePicker.setVisibility(View.INVISIBLE);
        publicChb.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
        btnDelete.setVisibility(View.INVISIBLE);
        addNewImage.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        if(novo_odabrana_slika!=null){
            //ovo znaci da imamo novu sliku i da nju moramo uploadati
            StorageReference documentReference = fStorage.child("trips/"+"/"+key+"/trip_profile.jpg");
            documentReference.delete();
            //nakon sta smo izbirsali staru refererencu na sliku,kreiramo novu
            StorageReference fillRef=fStorage.child("trips/"+"/"+key+"/trip_profile.jpg");
            fillRef.putFile(novo_odabrana_slika).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fillRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(img);
                            t.setLink_to_image(uri.toString());
                            updatePost(t,key);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditingExistingTrip.this,"Image not uploaded!",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), MyTravelsActivity.class));
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("KATE", "nije odabrana slika"+ e.toString());
                }
            });

        }
        else{
            //ovo znaci da je zadrzana stara slika
            //onda je dovoljno samo uplodati ostatka tripa
            updatePost(t,key);
        }

    }
    private void updatePost(Trip t,String key){
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
        trip.put("link_to_image",t.getLink_to_image());
        documentReference.set(trip).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i("KATE","success");
                Toast.makeText(EditingExistingTrip.this, "Trip successfully saved!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), MyTravelsActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("KATE", "failure"+ e.toString());
                Toast.makeText(EditingExistingTrip.this, "Failed!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), MyTravelsActivity.class));
            }
        });
    }
    public  void showMessage(String message){
        Toast.makeText(EditingExistingTrip.this,message,Toast.LENGTH_LONG).show();
    }
    private void updateNavigationBarState(int actionId){
        MenuItem item = bottomNavigationView.getMenu().findItem(actionId);
        item.setChecked(true);
    }
}
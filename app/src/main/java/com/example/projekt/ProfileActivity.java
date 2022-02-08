package com.example.projekt;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private EditText etName, etSurname, etUsername, etEmail;
    private ProgressBar progressBar;
    private Button btnSaveChanges,btnResetPassword;
    private TextView tvName,txtPublishedPosts,txtPrivatePosts;
    private ImageView profileImage,btnChangeProfileImage, addNew;
    private TextView txtPublished, txtPrivate;
    public FirebaseAuth mAuth;
    public FirebaseFirestore fstore;
    public StorageReference fStorage;
    public FirebaseUser user;
    public String userId;
    //sluzi za update baze podataka
    public String password;
    BottomNavigationView bottomNavigationView;
    ActivityResultLauncher<String> activityResultLauncher;

    Integer counter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        fStorage= FirebaseStorage.getInstance().getReference();

        userId=mAuth.getCurrentUser().getUid();
        user=mAuth.getCurrentUser();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        bottomNavigationView.setBackground(null);
        //bottomNavigationView.setSelectedItemId(R.id.cuatro);

        counter = 0;

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
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        return true;
                }
                return true;
            }
        });

        profileImage= (ImageView) findViewById(R.id.img);
        addNew = (ImageView) findViewById(R.id.add);
        tvName = (TextView) findViewById(R.id.tv_name);
        etName = (EditText) findViewById(R.id.txtNameInput);
        etSurname = (EditText) findViewById(R.id.txtSurnameInput);
        etUsername = (EditText) findViewById(R.id.txtusernameInput);
        etEmail = (EditText) findViewById(R.id.txtemailInput);
        progressBar=(ProgressBar) findViewById(R.id.progressBar);

        txtPrivate = (TextView) findViewById(R.id.txtPrivatePosts);
        txtPublished = (TextView) findViewById(R.id.txtPublishedPosts);

        txtPrivatePosts=(TextView) findViewById(R.id.txtPrivatePosts);
        txtPublishedPosts=(TextView) findViewById(R.id.txtPublishedPosts);


        btnSaveChanges = (Button) findViewById(R.id.btnSaveChanges);
        btnResetPassword = (Button) findViewById(R.id.btnResetPassword);

        btnSaveChanges.setOnClickListener(view -> saveChanges());
        btnResetPassword.setOnClickListener(view -> resetPassword());
        addNew.setOnClickListener(view -> changeProfileImage());


        //ovo je launcher za otvorit galeriju slika
        activityResultLauncher= registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            Toast.makeText(ProfileActivity.this,"Image trying!",Toast.LENGTH_LONG).show();
            profileImage.setImageURI(result);
            progressBar.setVisibility(View.INVISIBLE);
            addNew.setVisibility(View.VISIBLE);
            uploadImageToFirebase(result);
        }
        });

        fillForm();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            //case R.id.item1:
                //changeProfileImage();
                //return true;
            case R.id.item2:
                logoutClick();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    private void fillForm(){
        //ovamo dohvacamo podatke za napunit editTextove o trenutno prijavljenom korisniku
        DocumentReference documentReference = fstore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                tvName.setText(value.getString("name")+" "+value.getString("surname"));
                etName.setText(value.getString("name"));
                etSurname.setText(value.getString("surname"));
                etUsername.setText(value.getString("username"));
                etEmail.setText(value.getString("email"));
                password=value.getString("password");
            }
        });
        ArrayList<Trip> trip_list = new ArrayList<>();
        CollectionReference trips = fstore.collection("trips");
        Query query = trips.whereEqualTo("id_user", userId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot document : task.getResult())
                    {
                        Trip trip = document.toObject(Trip.class);
                        trip_list.add(trip);
                    }
                    Integer brojac_published=0;
                    Integer brojac_private=0;

                    for(Trip t:trip_list){
                        if(t.getPublished()==true){
                            brojac_published++;
                        }
                        else{brojac_private++;}
                    }
                    txtPublishedPosts.setText(brojac_published.toString());
                    txtPrivatePosts.setText(brojac_private.toString());
                }
            }
        });

        //ovamo dohvaćamo profilnu sliku trenutno prijavljenog korisnika i tako pomocu picasso liba punimo imageview
        //ovamo mozda bude bacalo gresku jer mozda ce ucitavat slike s mobitela pod drugom ekstenzijom(npr .jpg)
        try{
        StorageReference profileRef=fStorage.child("users/"+userId+"/profile.jpeg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });}
        catch (Exception e){
            Toast.makeText(this,"Nije jpeg",Toast.LENGTH_LONG).show();
        }
        try{
            StorageReference profileRef=fStorage.child("users/"+userId+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(profileImage);
                }
            });}
        catch (Exception e){
            Toast.makeText(this,"Nije jpg",Toast.LENGTH_LONG).show();
        }
    }

    private void saveChanges(){
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required!");
            etName.requestFocus();
            return;
        }

        if (surname.isEmpty()) {
            etSurname.setError("Surname is required!");
            etSurname.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("Username is required!");
            etUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required!");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please provide valid email address!");
            etEmail.requestFocus();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setCancelable(true);

        builder.setTitle("Updating your profile!");
        builder.setMessage("Sure you want to change your profile?");

        //za cancel button na pop-upu
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        //za proceed to registration button
        builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DocumentReference documentReference = fstore.collection("users").document(userId);
                Map<String, Object> user = new HashMap<>();
                user.put("name", name);
                user.put("surname", surname);
                user.put("username", username);
                user.put("password",password);
                user.put("email", email);
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProfileActivity.this,"SAVED",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this,"Not saved",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        builder.show();
    }

    public void changeProfileImage(){
        //otvara galeriju slika na uređaju
        addNew.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        //Toast.makeText(ProfileActivity.this,"pokusava otvorit galeriju!",Toast.LENGTH_LONG).show();
        activityResultLauncher.launch("image/*");
        onBackPressed();
    }
    public void logoutClick(){
        //logout,klikom na ikonicu logout vraća nas kod na login stranicu
        Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    public void uploadImageToFirebase(Uri imageUri){
        //upload image to firebase storage
        //dohvacamo referencu na zeljenu sliku
        //ovamo mozda bude bacalo gresku zbog ekstenzije slike(mozda triba .jpg)
        StorageReference fillRef=fStorage.child("users/"+userId+"/profile.jpg");
        fillRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fillRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this,"Image not uploaded!",Toast.LENGTH_LONG).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this,"Image not uploaded!",Toast.LENGTH_LONG).show();

            }
        });
    }
    public void resetPassword(){

        AlertDialog.Builder passResetDialog=new AlertDialog.Builder(ProfileActivity.this);
        passResetDialog.setCancelable(true);
        passResetDialog.setTitle("Reset password?");
        passResetDialog.setMessage("Enter new password (8 characters long):");

        final EditText resetPassword=new EditText(ProfileActivity.this);
        passResetDialog.setView(resetPassword);

        passResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        passResetDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String newPassword = resetPassword.getText().toString();

                if(newPassword.length()<8){
                    return;
                    //ovamo bi triba dio da se ponovno otvori dialog da se upise lozinka
                }

                user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProfileActivity.this,"Success!",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this,"Not saved!",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        passResetDialog.show();
    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        int seletedItemId = bottomNavigationView.getSelectedItemId();
        if (R.id.cuatro == seletedItemId) {
            super.onBackPressed();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.cuatro);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        bottomNavigationView.getMenu().getItem(3).setChecked(true);
    }

}

package com.example.projekt;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private EditText etName, etSurname, etUsername, etEmail;
    private ProgressBar progressBar;
    private Button btnSaveChanges,btnResetPassword;
    private TextView tvName;
    private ImageView profileImage,btnChangeProfileImage;
    public FirebaseAuth mAuth;
    public FirebaseFirestore fstore;
    public StorageReference fStorage;
    public FirebaseUser user;
    public String userId;
    //sluzi za update baze podataka
    public String password;
    BottomNavigationView bottomNavigationView;
    ActivityResultLauncher<String> activityResultLauncher;


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
        bottomNavigationView.setSelectedItemId(R.id.cuatro);

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

        profileImage=(ImageView) findViewById(R.id.imgUserProfileImage);
        tvName = (TextView) findViewById(R.id.tv_name);
        etName = (EditText) findViewById(R.id.txtNameInput);
        etSurname = (EditText) findViewById(R.id.txtSurnameInput);
        etUsername = (EditText) findViewById(R.id.txtusernameInput);
        etEmail = (EditText) findViewById(R.id.txtemailInput);

        btnSaveChanges = (Button) findViewById(R.id.btnSaveChanges);
        btnResetPassword = (Button) findViewById(R.id.btnResetPassword);

        btnSaveChanges.setOnClickListener(view -> saveChanges());
        btnResetPassword.setOnClickListener(view -> resetPassword());
        //btnChangeProfileImage.setOnClickListener(view -> changeProfileImage());

        //ovo je launcher za otvorit galeriju slika
        activityResultLauncher= registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            Toast.makeText(ProfileActivity.this,"Image trying!",Toast.LENGTH_LONG).show();
            profileImage.setImageURI(result);
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
            case R.id.item1:
                changeProfileImage();
                return true;
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
        Toast.makeText(ProfileActivity.this,"pokusava otvorit galeriju!",Toast.LENGTH_LONG).show();
        activityResultLauncher.launch("image/*");

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


}

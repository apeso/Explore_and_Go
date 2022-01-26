package com.example.projekt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity{
    private EditText etName, etSurname, etUsername, etEmail, etPassword, etRepeatPasssword;
    private ProgressBar progressBar;
    private Button btnRegister;
    private TextView tvLogin;

    public FirebaseAuth mAuth;
    public FirebaseFirestore fstore;

    //private DatabaseReference mFirebaseReference;
    //private FirebaseDatabase mFirebaseInstance;
    String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //ovo je za bazu
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        etName = (EditText) findViewById(R.id.nameEt);
        etSurname = (EditText) findViewById(R.id.surnameEt);
        etUsername = (EditText) findViewById(R.id.usernameEt);
        etEmail = (EditText) findViewById(R.id.emailEt);
        etPassword = (EditText) findViewById(R.id.passwordEt);
        etRepeatPasssword = (EditText) findViewById(R.id.passwordRepeatEt);
        btnRegister = (Button) findViewById(R.id.registerBtn);
        tvLogin = (TextView) findViewById(R.id.loginTv);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //samo da bude skrivena lozinka pri pojavljivanju
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPassword.setSelection(etPassword.length());
        etRepeatPasssword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etRepeatPasssword.setSelection(etRepeatPasssword.length());

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        btnRegister.setOnClickListener(view -> registerUser());
    }

    //za provjeru unosa
    private void registerUser() {
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String repPass = etRepeatPasssword.getText().toString().trim();

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


        if (pass.isEmpty()) {
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return;
        }

        if (pass.length() < 8) {
            etPassword.setError("Password needs to be at least 8 characters!");
            etPassword.requestFocus();
            return;
        }

        if (repPass.isEmpty()) {
            etRepeatPasssword.setError("Repeat password is required!");
            etRepeatPasssword.requestFocus();
            return;
        }

        if (!repPass.equals(pass)) {
            etRepeatPasssword.setError("Passwords don't match!");
            etRepeatPasssword.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        //unos korisnika u bazu i autentifikacija
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(RegisterActivity.this, "User created!", Toast.LENGTH_LONG).show();
                    userId = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fstore.collection("users").document(userId);
                    Map<String, Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("surname", surname);
                    user.put("username", username);
                    user.put("email", email);
                    user.put("password", pass);
                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("KATE","success");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("KATE", "failure"+ e.toString());
                        }
                    });
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                else
                {
                    Toast.makeText(RegisterActivity.this, "Error!"+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

package com.example.projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView tvIme;

    public Country[] countries;
    final Gson mGson = new Gson();
    Button btn;

    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tvIme = (TextView) findViewById(R.id.ime);
        fStore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String name = user.getDisplayName();
        tvIme.setText(uid);

        //btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*String jsonFileString = Utils.getJsonFromAssets(getApplicationContext(), "countries.json");
                countries = mGson.fromJson(jsonFileString, Country[].class);
                for(int i = 0; i < countries.length; i++)
                {
                    Log.i("data", ">Item" + i + "\n" + countries[i]);
                    Toast.makeText(getApplicationContext(),"data"+countries[i],Toast.LENGTH_LONG).show();
                }*/
                // Write a message to the database
                fStore.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("KATE", document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.w("KATE", "Error getting documents.", task.getException());
                                }
                            }
                        });
            }
        });
    }
}
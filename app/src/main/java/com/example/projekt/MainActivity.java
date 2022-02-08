package com.example.projekt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    RecyclerView all_trips_list_view;
    List<Trip> all_trips_list;
    private AllTripsRecylerAdapter allTripsRecylerAdapter;

    FirebaseFirestore fstore;
    public FirebaseUser user;
    public String userId;
    public FirebaseAuth mAuth;
    private RadioGroup radioGroupSort;
    private RadioButton radioButtonSortNovo,radioButtonSortStaro;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fstore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        user=mAuth.getCurrentUser();


        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        bottomNavigationView.setBackground(null);
        //bottomNavigationView.setSelectedItemId(R.id.uno);
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
        all_trips_list_view = (RecyclerView) findViewById(R.id.all_trips_list_view);
        all_trips_list = new ArrayList<>();

        allTripsRecylerAdapter = new AllTripsRecylerAdapter(all_trips_list);
        all_trips_list_view.setLayoutManager(new LinearLayoutManager(this));
        all_trips_list_view.setAdapter(allTripsRecylerAdapter);
        CollectionReference trips = fstore.collection("trips");
        Query query= trips.whereEqualTo("published", true);
        //dobij rezultate querija
        ArrayList<String> lista_pojmova=new ArrayList<>();
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot document : task.getResult())
                    {
                        Trip trip = document.toObject(Trip.class);
                        if(!userId.equals(trip.getId_user())){
                            all_trips_list.add(trip);
                            allTripsRecylerAdapter.notifyDataSetChanged();
                            if(!lista_pojmova.contains(trip.getCity().toLowerCase())){
                                lista_pojmova.add(trip.getCity().toLowerCase());
                            }
                            if(!lista_pojmova.contains(trip.getCountry().toLowerCase())){
                                lista_pojmova.add(trip.getCountry().toLowerCase());
                            }

                        }
                    }

                }
                sortirajNajnovij();
            }
        });

        radioGroupSort=(RadioGroup)findViewById(R.id.radioGroupSort);
        //najnovije
        radioButtonSortNovo=(RadioButton) findViewById(R.id.radioButton2);
        radioButtonSortStaro=(RadioButton) findViewById(R.id.radioButton);
        radioButtonSortNovo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortirajNajnovij();
            }
        });
        radioButtonSortStaro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortirajNajstariji();
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, lista_pojmova);
        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.BLACK);

        //dovrsit sta se dogodi klikom na search


    }

    @Override
    public void onBackPressed() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        int seletedItemId = bottomNavigationView.getSelectedItemId();
        if (R.id.uno == seletedItemId) {
            super.onBackPressed();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.uno);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);
    }

    public void sortirajNajnovij()
    {
        Comparator<Trip> dateSorter= (o1, o2) -> o1.getDate().compareTo(o2.getDate());
        Collections.sort(all_trips_list,dateSorter);
        allTripsRecylerAdapter.notifyDataSetChanged();
    }
    public void sortirajNajstariji(){
        Comparator<Trip> dateSorter2= (o1,o2)->o2.getDate().compareTo(o1.getDate());
        Collections.sort(all_trips_list,dateSorter2);
        allTripsRecylerAdapter.notifyDataSetChanged();
    }
}
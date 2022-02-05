package com.example.projekt;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyTravelsActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    RecyclerView blog_list_view;
    List<Trip> trip_list;
    private TripRecylerAdapter tripRecylerAdapter;

    FirebaseFirestore fstore;
    public FirebaseUser user;
    public String userId;
    public FirebaseAuth mAuth;

    @Override
    public void onBackPressed()
    {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        if(bottomNavigationView.getSelectedItemId() == R.id.tres)
        {
            super.onBackPressed();
        }
        else {
            bottomNavigationView.setSelectedItemId(R.id.tres);
        }
    }

    private void updateNavigationBarState(int actionId){
        MenuItem item = bottomNavigationView.getMenu().findItem(actionId);
        item.setChecked(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travels);

        fstore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        user=mAuth.getCurrentUser();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnavigationbar);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.setSelectedItemId(R.id.tres);

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
                }
                updateNavigationBarState(item.getItemId());

                return true;
            }
        });

        blog_list_view = (RecyclerView) findViewById(R.id.blog_list_view);
        trip_list = new ArrayList<>();

        tripRecylerAdapter = new TripRecylerAdapter(trip_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(this));
        blog_list_view.setAdapter(tripRecylerAdapter);


        CollectionReference trips = fstore.collection("trips");
        Query query = trips.whereEqualTo("user_id", userId);
        //dobij rezultate querija
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot document : task.getResult())
                    {
                        Trip trip = document.toObject(Trip.class);
                        trip_list.add(trip);
                        Comparator<Trip> dateSorter
                                = (o1, o2) -> o2.getDate().compareTo(o1.getDate());
                        Collections.sort(trip_list,dateSorter);
                        tripRecylerAdapter.notifyDataSetChanged();
                    }

                }
            }
        });
    }
}


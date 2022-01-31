package com.example.projekt;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyTravelsActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    RecyclerView blog_list_view;
    List<Trip> trip_list;
    FirebaseFirestore firebaseFirestore;
    private TripRecylerAdapter tripRecylerAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travels);

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
                        return true;
                }
                return false;
            }
        });

        blog_list_view = (RecyclerView) findViewById(R.id.blog_list_view);
        trip_list = new ArrayList<>();

        tripRecylerAdapter = new TripRecylerAdapter(trip_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(this));
        blog_list_view.setAdapter(tripRecylerAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("trips").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {
                for(DocumentChange doc:documentSnapshots.getDocumentChanges())
                {
                    //provjeravamo je li sta dodano u kolekciju trips pa tek onda djelujemo
                    if(doc.getType() == DocumentChange.Type.ADDED)
                    {
                        Trip trip = doc.getDocument().toObject((Trip.class));
                        trip_list.add(trip);
                        tripRecylerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }
}


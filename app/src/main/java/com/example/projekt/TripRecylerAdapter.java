package com.example.projekt;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TripRecylerAdapter extends RecyclerView.Adapter<TripRecylerAdapter.ViewHolder> {

    public List<Trip> trip_list;
    public Context context;
    public TripRecylerAdapter(List<Trip> trip_list)
    {
        this.trip_list = trip_list;
    }

    public FirebaseAuth mAuth;
    public FirebaseFirestore fstore;
    public StorageReference fStorage;
    public FirebaseUser user;
    public String userId;
    public Uri userUri;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //postavljamo layout za oni layout sta smo stvorili
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();

        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        fStorage= FirebaseStorage.getInstance().getReference();
        userId=mAuth.getCurrentUser().getUid();
        user=mAuth.getCurrentUser();

        return new ViewHolder(view);
    }

    //ode bas postavljamo podatke
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String descData = trip_list.get(position).getdescription();
        holder.setDescText(descData);

        String title = trip_list.get(position).getTitle();
        holder.setTitleText(title);

        String dateData = trip_list.get(position).getDate();
        holder.setDateText(dateData);

        String location = trip_list.get(position).getCity()+","+trip_list.get(position).getCountry();
        holder.setLocationText(location);

        holder.setTripImage(trip_list.get(position).getLink_to_image());

    }


    @Override
    public int getItemCount() {
        return trip_list.size();
    }
    public  class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView descView;
        private TextView usernameView;
        private TextView dateView;
        private TextView locationView;
        private ImageView tripImageView;
        private ImageView userImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //on click listener za svaki klik na svaki trip u listi tripova
                    //dohvati id tripa i njega salje sljedećem activityu

                    Intent editingTripIntent = new Intent(context, EditingExistingTrip.class);
                    String value1=trip_list.get(getAdapterPosition()).getId();

                    editingTripIntent.putExtra("key",value1);
                    context.startActivity(editingTripIntent);
                }
            });
        }

        //metode za punjenje inputa sa podacima iz baze --> prvo za opis tripa
        public void setDescText(String descText)
        {
            descView = mView.findViewById(R.id.description_trip);
            descView.setText(descText);
        }

        //pa username
        public void setTitleText(String titleText)
        {
            usernameView = mView.findViewById(R.id.username_main);
            usernameView.setText(titleText);
        }

        //za datum
        public void setDateText(String dateText)
        {
            dateView = mView.findViewById(R.id.datum_objave);
            dateView.setText(dateText);
        }
        //za lokaciju
        public void setLocationText(String locationText)
        {
            locationView = mView.findViewById(R.id.lokacija);
            locationView.setText(locationText);
        }

        public void setTripImage(String downloadUri)
        {
            tripImageView = mView.findViewById(R.id.trip_image);
            Picasso.get().load(downloadUri).into(tripImageView);
        }


    }

}

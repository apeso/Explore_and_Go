package com.example.projekt;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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

        //s obzirom da username nije spremljen u trip, moramo prvo dohvatit
        fstore.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String user_name = documentSnapshot.getString("username");
                holder.setUsernameText(user_name);
            }
        });

        String dateData = trip_list.get(position).getDate();
        holder.setDateText(dateData);

        StorageReference storageReference = fStorage.child("tbSgwaEhNRLZzdm5zNRu/profile.jpg");
        holder.setTripImage(storageReference.toString());
    }

    @Override
    public int getItemCount() {
        return trip_list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private TextView usernameView;
        private TextView dateView;
        private ImageView tripImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        //metode za punjenje inputa sa podacima iz baze --> prvo za opis tripa
        public void setDescText(String descText)
        {
            descView = mView.findViewById(R.id.description_trip);
            descView.setText(descText);
        }

        //pa username
        public void setUsernameText(String usernameText)
        {
            usernameView = mView.findViewById(R.id.username_main);
            usernameView.setText(usernameText);
        }

        //za datum
        public void setDateText(String dateText)
        {
            dateView = mView.findViewById(R.id.datum_objave);
            dateView.setText(dateText);
        }

        public void setTripImage(String downloadUri)
        {
            tripImageView = mView.findViewById(R.id.trip_image);
            Picasso.get().load(downloadUri).into(tripImageView);
        }
    }
}

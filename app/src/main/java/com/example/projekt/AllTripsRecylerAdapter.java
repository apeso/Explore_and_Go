package com.example.projekt;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

public class AllTripsRecylerAdapter extends RecyclerView.Adapter<AllTripsRecylerAdapter.ViewHolder> {

    public List<Trip> all_trips_list;
    public Context context;
    public AllTripsRecylerAdapter(List<Trip> all_trips_list)
    {
        this.all_trips_list = all_trips_list;
    }

    public FirebaseAuth mAuth;
    public FirebaseFirestore fstore;
    public StorageReference fStorage;
    public FirebaseUser user;
    public String userId;
    public Uri userUri;
    String locGradData;
    String user1;
    String user2;
    TextView descView;
    RecyclerView recyclerView;
    String grad;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //postavljamo layout za oni layout sta smo stvorili
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_trips_list_item, parent, false);
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

        String descData = all_trips_list.get(position).getdescription();
        holder.setDescText(descData);
        if(descView.getText().length()>=135){
            makeTextViewResizable(descView, 3, "...Read More", true);}

        String titleData = all_trips_list.get(position).getTitle();
        holder.setTitleText(titleData);

        //s obzirom da username nije spremljen u trip, moramo prvo dohvatit po user_idu koji je u tripu
        fstore.collection("trips").document(all_trips_list.get(position).getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user1 = documentSnapshot.getString("id_user");
                fstore.collection("users").document(user1).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String user_name = documentSnapshot.getString("username");
                        holder.setUsernameText(user_name);
                    }
                });
            }
        });

        String dateData = all_trips_list.get(position).getDate();
        holder.setDateText(dateData);
        locGradData = all_trips_list.get(position).getCity();

        String locDrzavaData = all_trips_list.get(position).getCountry();
        //holder.setLocText(locGradData);
        holder.setLocText(locGradData+", "+locDrzavaData);

        holder.setTripImage(all_trips_list.get(position).getLink_to_image());


        //s obzirom da username nije spremljen u trip, moramo prvo dohvatit po user_idu koji je u tripu
        fstore.collection("trips").document(all_trips_list.get(position).getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user2 = documentSnapshot.getString("id_user");
                fStorage.child("users/"+user2+"/profile.jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            userUri = task.getResult();
                            holder.setUserImage(userUri);
                        } else {
                            Log.d("KATE", "neuspješno dohvaćanje user image!");
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return all_trips_list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        //private TextView descView;
        private TextView usernameView;
        private TextView dateView;
        private TextView titleView;
        private ImageView tripImageView;
        private ImageView userImageView;
        private TextView locView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            mView.findViewById(R.id.btnKarta).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context.getApplicationContext(),MapsActivity.class);
                    //ovdje bi trebalo dohvacat ono sta je napisano u polju grad u itemu u recyleru viewu za koji je stisnuto show on map
                    intent.putExtra("city", all_trips_list.get(getLayoutPosition()).getCity());
                    Log.d("KATE", "karta: "+ all_trips_list.get(getLayoutPosition()).getCity());
                    context.startActivity(intent);
                }
            });

        }

        //metode za punjenje inputa sa podacima iz baze --> prvo za opis tripa
        public void setDescText(String descText)
        {
            descView = mView.findViewById(R.id.description_trip);
            descView.setText(descText);
        }

        public void setTitleText(String titleText)
        {
            titleView = mView.findViewById(R.id.title_trip);
            titleView.setText(titleText);
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

        public void setLocText(String locText)
        {
            locView = mView.findViewById(R.id.trip_location);
            locView.setText(locText);
        }

        public void setTripImage(String downloadUri)
        {
            tripImageView = mView.findViewById(R.id.trip_image);
            Picasso.get().load(downloadUri).into(tripImageView);
        }

        public void setUserImage(Uri downloadUri)
        {
            userImageView = mView.findViewById(R.id.user_image);
            Picasso.get().load(downloadUri).into(userImageView);
        }
    }

    //da se ne prikaze cijeli description text ako je predug
    public static void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

        if (tv.getTag() == null) {
            tv.setTag(tv.getText());
        }
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                String text;
                int lineEndIndex;
                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeOnGlobalLayoutListener(this);

                if (maxLine == 0) {
                    lineEndIndex = tv.getLayout().getLineEnd(0);
                    text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                    lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                    text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                } else {
                    lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                    text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                }
                tv.setText(text);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setText(
                        addClickablePartTextViewResizable(new SpannableString(tv.getText().toString()), tv, lineEndIndex, expandText,
                                viewMore), TextView.BufferType.SPANNABLE);
            }
        });
    }

    private static SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
                                                                            final int maxLine, final String spanableText, final boolean viewMore) {
        String str = strSpanned.toString();
        SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

        if (str.contains(spanableText)) {
            ssb.setSpan(new MySpannable(false){
                @Override
                public void onClick(View widget) {
                    tv.setLayoutParams(tv.getLayoutParams());
                    tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                    tv.invalidate();
                    if (viewMore) {
                        makeTextViewResizable(tv, -1, " Read Less", false);
                    } else {
                        makeTextViewResizable(tv, 3, "...Read More", true);
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

        }
        return ssb;
    }

}

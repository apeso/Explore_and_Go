package com.example.projekt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TripRecylerAdapter extends RecyclerView.Adapter<TripRecylerAdapter.ViewHolder> {

    public List<Trip> trip_list;
    public TripRecylerAdapter(List<Trip> trip_list)
    {
        this.trip_list = trip_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //postavljamo layout za oni layout sta smo stvorili
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);

        return new ViewHolder(view);
    }

    //ode bas postavljamo podatke
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String descData = trip_list.get(position).getdescription();
        holder.setDescText(descData);

    }

    @Override
    public int getItemCount() {
        return trip_list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        //metode za punjenje inputa sa podacima iz baze
        public void setDescText(String descText)
        {
            descView = mView.findViewById(R.id.description_trip);
            descView.setText(descText);
        }
    }
}

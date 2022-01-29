package com.example.projekt;
//post predstavlja kao neku aktivnost unutar putovanja
public class Post {
    public String id_trip, review, link_to_image, date, time, id_place,id_user;

    public Post()
    {

    }

    public Post(String id_user, String review, String link_to_image, String date, String time, String id_place)
    {
        this.id_user = id_user;
        this.review = review;
        this.link_to_image = link_to_image;
        this.date = date;
        this.time = time;
        this.id_place = id_place;
    }

    public void setId_trip(String id_trip) {
        this.id_trip = id_trip;
    }

    public String getId_trip() {
        return id_trip;
    }
}

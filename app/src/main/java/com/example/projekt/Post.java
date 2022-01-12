package com.example.projekt;
//post predstavlja kao neku aktivnost unutar putovanja
public class Post {
    public String id_trip, review, link_to_image, date, time, id_place;

    public Post()
    {

    }

    public Post(String id_trip, String review, String link_to_image, String date, String time, String id_place)
    {
        this.id_trip = id_trip;
        this.review = review;
        this.link_to_image = link_to_image;
        this.date = date;
        this.time = time;
        this.id_place = id_place;
    }
}

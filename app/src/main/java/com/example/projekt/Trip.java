package com.example.projekt;

public class Trip {
    public String id_user;
    public String name; //naziv izleta koji će se prikazivati na naslovnici
    public String date; //datum objave posta
    public String review; //kratki opis
    public String link_to_image; //putanja do slike
    public String published; //je li objava javno dostupna (true ili false)
    public String id_city; //id grada za koji vežemo ovo putovanje

    public Trip()
    {

    }

    public Trip(String id_user, String name, String date, String review, String link_to_image, String published, String id_city)
    {
        this.id_user = id_user;
        this.name = name;
        this.date = date;
        this.review = review;
        this.link_to_image = link_to_image;
        this.published = published;
        this.id_city = id_city;
    }
}

package com.example.projekt;

public class Trip {
    private String id;
    private String id_user;
    private String title; //naziv izleta koji će se prikazivati na naslovnici
    private String date; //datum objave posta
    private String description; //kratki opis
    public String link_to_image; //putanja do slike
    private Boolean published; //je li objava javno dostupna (true ili false)
    private String city;//id grada za koji vežemo ovo putovanje
    private String country;

    public Trip()
    {

    }

    public Trip(String id,String id_user, String title, String date, String description, String city,
                String country,String link_to_image,Boolean published)
    {
        this.id=id;
        this.id_user=id_user;
        this.setTitle(title);
        this.date=date;
        this.description = description;
        this.link_to_image = link_to_image;
        this.published=published;
        this.setCity(city);
        this.setCountry(country);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



    public String getdescription() {
        return description;
    }

    public void setdescription(String description) {
        this.description = description;
    }

    public void setLink_to_image(String link) {
        this.link_to_image = link;
    }
    public String getLink_to_image() {
        return link_to_image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


}

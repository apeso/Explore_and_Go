package com.example.projekt;

public class Trip {
    private String id;
    private String id_user;
    private String name; //naziv izleta koji će se prikazivati na naslovnici
    private String date; //datum objave posta
    private String description; //kratki opis
    public String link_to_image; //putanja do slike
    private Boolean published; //je li objava javno dostupna (true ili false)
    private String id_city;//id grada za koji vežemo ovo putovanje
    private String id_country;

    public Trip()
    {

    }

    public Trip(String id,String id_user, String name, String date, String description, String id_city,
                String id_country,String link_to_image,Boolean published)
    {
        this.id=id;
        this.id_user=id_user;
        this.name = name;
        this.date=date;
        this.description = description;
        this.link_to_image = link_to_image;
        this.published=published;
        this.id_city = id_city;
        this.id_country=id_country;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getdescription() {
        return description;
    }

    public void setdescription(String description) {
        this.description = description;
    }

    public String getId_city() {
        return id_city;
    }

    public void setId_city(String id_city) {
        this.id_city = id_city;
    }
    public void setId_country(String id_country) {
        this.id_country = id_country;
    }
    public String getId_country() {
        return id_country;
    }
    public void setLink_to_image(String link) {
        this.link_to_image = link;
    }
    public String getLink_to_image() {
        return link_to_image;
    }
}

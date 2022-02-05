package com.example.projekt;

import java.util.List;


public class Country {
    public String name;
    public List<City> list_of_cities;
    //public String id_continent;

    public Country()
    {

    }

    public Country(String name, List<City> list)
    {
        this.name = name;
        this.list_of_cities = list;
    }
}

package ru.asartamonov.sinmatcher;

/**
 * Alexander Artamonov (asartamonov@gmail.com) 2016.
 */
public class User {
    private final int age;
    private final String name;
    private final String city;
    private final int sinID;

    public User(int age, String name, String city, int sinID){
        this.age = age;
        this.name = name;
        this.city = city;
        this.sinID = sinID;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public int getSinID() {
        return sinID;
    }
}
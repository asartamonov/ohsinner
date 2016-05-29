package ru.asartamonov.ohsinner;

/**
 * Entity - person
 */
public class Person {

    private final int age;
    private final String name;
    private final String city;
    private final int sinID;

    private final boolean isFamous;

    private Person(int age, String name, String city, int sinID, boolean isFamous){
        this.age = age;
        this.name = name;
        this.city = city;
        this.sinID = sinID;
        this.isFamous = isFamous;
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

    public boolean isFamous() {
        return isFamous;
    }

    static class PersonManager {
        public static Person newSimplePerson(int age, String name, String city, int sinID) {
            return new Person(age, name, city, sinID, false);
        }

        public static Person newFamousPerson(String name, String city, int sinID) {
            return new Person(0, name, city, sinID, true);
        }
    }
}
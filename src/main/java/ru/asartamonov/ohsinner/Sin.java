package ru.asartamonov.ohsinner;

/**
 * Entity - sin
 */
public class Sin {

    private final int sinID;
    private final String sinName;
    private final Person[] fPersons;
    private Path path;

    private Sin(int sinID, String sinName, Person[] fPersons, Path path) {
        this.sinName = sinName;
        this.sinID = sinID;
        this.fPersons = fPersons;
        this.path = path;
    }

    public int getSinID() {
        return sinID;
    }

    public String getSinName() {
        return sinName;
    }

    public Person[] getFamPersons() {
        return fPersons;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    static class SinManager {
        public static Sin getInstance(String sinName) {
            Sin sin = DbManager.getSin(sinName);
            return sin == null ? DbManager.createSin(sinName) : sin;
        }

        public static Sin getInstance(int sinID, String sinName, Person[] fPersons, Path path) {
            return new Sin(sinID, sinName, fPersons, path);
        }
    }
}

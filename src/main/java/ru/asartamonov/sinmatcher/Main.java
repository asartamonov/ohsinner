package ru.asartamonov.sinmatcher;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Alexander Artamonov (asartamonov@gmail.com) 2016.
 */
public class Main {
    /* Our Database's URL and Driver to use. */
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sinsdb?autoReconnect=true&useSSL=false";

    /* Database credentials */
    static final String USER = "root";
    static final String PASS = "Rfvsibr90";

    /*
    *  Asks for user prompt and creates user object.
    *  Than it checks for legendary heroes with same sins and gives
    *  a hint, connected with user's sin.
    *  */
    public static void main(String[] args) {
        Statement statement;
        StringBuilder sb; // we gonna use it for SQL statements dynamic building

        /* Get user data, write user to database, get path and hero from
         * database according to user's sin
         * only part of input used to prevent sql-injections
         *
         * Scanner used to get and parse user input
         */
        Scanner sc = new Scanner(System.in);

        System.out.println("What is your name?");
        String[] inputName = sc.nextLine().trim().split("\\s");
        String userName = "";
        for (int i = 0; i < inputName.length && i < 3; i++)
            userName = userName + inputName[i];

        System.out.println("How old are you?");
        int userAge;

        /* to prevent crashing in case of wrong user input */
        try {
            userAge = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            userAge = 0;
        }

        System.out.println("Where are you from?");
        String[] inputCity = sc.nextLine().trim().split("\\s");
        String userCity = "";
        for (int i = 0; i < inputCity.length && i < 3; i++)
            userCity = userCity + inputCity[i];

        System.out.println("What is your heaviest sin? Only one word please.");
        String[] inputSin = sc.nextLine().trim().toLowerCase().split("\\s");
        String userSinName = "";
        for (int i = 0; i < inputSin.length && i < 3; i++)
            userSinName = userSinName + inputSin[i];

        System.out.printf("Well, %s, %d years old from %s with %s sin...\n", userName, userAge, userCity, userSinName);

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Class.forName(JDBC_DRIVER);
            sb = new StringBuilder("SELECT sinID " +
                    "FROM sinsdb.sins " +
                    "WHERE sinName='';")
                    .insert(45, userSinName);
            statement = connection.prepareStatement(sb.toString());
            ResultSet sinResults = statement.executeQuery(sb.toString());

            /* important value - unique identifier of user's sin */
            int sinID;

            /* get sin ID if sin presented in db else - add sin as new (and get newly added sin ID) */
            if (sinResults.next()) {
                sinID = sinResults.getInt("sinID");
            } else {
                /* add new sin to db */
                sb.delete(0, sb.length())
                        .append("INSERT INTO sinsdb.sins (sinName) " +
                                "VALUES ('');")
                        .insert(43, userSinName);
                statement = connection.prepareStatement(sb.toString());
                statement.executeUpdate(sb.toString());

                /* get sinID of the newly added sin */
                sb.delete(0, sb.length())
                        .append("SELECT sinID " +
                                "FROM sinsdb.sins " +
                                "WHERE sinName='';")
                        .insert(45, userSinName);
                statement = connection.prepareStatement(sb.toString());
                sinResults = statement.executeQuery(sb.toString());
                sinResults.first();
                sinID = sinResults.getInt("sinID");
            }

            /* add new user to database */
            User user = new User(userAge, userName, userCity, sinID);
            sb.delete(0, sb.length())
                    .append("INSERT INTO sinsdb.users (userName, userAge, userCity, sins_sinID) " +
                            "VALUES ('', ,'', );")
                    .insert(84, user.getSinID())
                    .insert(81, user.getCity())
                    .insert(79, user.getAge())
                    .insert(76, user.getName());
            statement = connection.prepareStatement(sb.toString());
            statement.executeUpdate(sb.toString());

            /* Get path to resolve user's sin if available */
            sb.delete(0, sb.length())
                    .append("SELECT pathDescription, confirmed " +
                            "FROM sins " +
                            "JOIN paths ON paths_pathID=pathID " +
                            "WHERE sinID=;")
                    .insert(90, user.getSinID());
            statement = connection.prepareStatement(sb.toString());
            ResultSet paths = statement.executeQuery(sb.toString());

            /* Get legendary hero with same sin as user's*/
            sb.delete(0, sb.length())
                    .append("SELECT legSinnerName, legSinnerCity FROM legendary_sinners WHERE sins_sinID=;")
                    .insert(76, user.getSinID());
            statement = connection.prepareStatement(sb.toString());
            ResultSet heroes = statement.executeQuery(sb.toString());

            List<User> legendarySinners = new ArrayList<>();
            while (heroes.next()) {
                String hName = heroes.getNString("legSinnerName");
                String hCity = heroes.getNString("legSinnerCity");
                legendarySinners.add(new User(0, hName, hCity, 0));
            }
            if (!legendarySinners.isEmpty()) {
                System.out.println("There are couple of people I know, who had the same problems:");
                for (User legendarySinner : legendarySinners)
                    System.out.printf("%s from %s\n", legendarySinner.getName(), legendarySinner.getCity());
            } else {
                System.out.println("Well, I don't know anyone who have the same problems...");
            }

            String pathDescription;
            if (paths.next() && (paths.getInt("confirmed") == 1)) {
                pathDescription = paths.getNString("pathDescription");
                System.out.printf("Some people think there is a way. %s", pathDescription);
            } else {
                System.out.printf("I don't know what to say, " +
                        "I have to think on it... " +
                        "What are your thoughts how to handle your %s?\nPlease type and press ENTER:\n", userSinName);
                pathDescription = sc.nextLine();

                /* attempt to prevent sql-injection using stop-list of sql keywords and binary number deleting */
                pathDescription = pathDescription
                        .trim()
                        .replaceAll("[1]", " one ")
                        .replaceAll("[0]", " zero ")
                        .replaceAll("[\'\"]", " &amp ")
                        .toLowerCase();
                Pattern sqlKeyWordsPat = Pattern.compile("(insert|from|select|delete|drop|alter|create)");
                Matcher sqlMatcher = sqlKeyWordsPat.matcher(pathDescription);
                Set<String> foundKeyWords = new TreeSet<>();
                while (sqlMatcher.find()) {
                    foundKeyWords.add(sqlMatcher.group());
                }
                for (String foundKeyWord : foundKeyWords) {
                    pathDescription = pathDescription.replaceAll(foundKeyWord, "!!!" + foundKeyWord + "!!!");
                }
                sb.delete(0, sb.length())
                        .append("INSERT INTO sinsdb.paths (pathDescription, confirmed) " +
                                "VALUES ('', 0);")
                        .insert(63, pathDescription);
                statement = connection.prepareStatement(sb.toString());
                statement.executeUpdate(sb.toString());
                System.out.println("I hear you, give me time to consider, goodbye for now...");
            }
        } catch (SQLException e) {
            System.out.println("\n" + "SQL query error\n" + e.getMessage() + "\n");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Error while JDBC driver loading. Exiting...");
        }
    }
}

package ru.asartamonov.ohsinner;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Database interaction layer: queries, updates.
 *  Methods invoked from outside of the class return
 *  Java-object replies, not database entities.
 *  All interaction with database only within this class.
 * */
public class DbManager {
    /* Our Database's URL and Driver to use. */
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sinsdb?autoReconnect=true&useSSL=false";

    /* Database credentials */
    private static final String USER = "user";
    private static final String PASS = "Cfif000*";

    /* Database queries */

    /**
     * Returns ID of created person, -1 if error
     */
    public static int createPerson(Person person) {
        PreparedStatement statement;
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Class.forName(JDBC_DRIVER);
            String createPersonQuery = "" +
                    "INSERT INTO persons " +
                    "(personName, personAge, personCity, sins_sinID, isFamous) " +
                    "VALUES (?, ?, ?, ?, ?);";
            statement = connection.prepareStatement(createPersonQuery,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, person.getName());
            statement.setInt(2, person.getAge());
            statement.setString(3, person.getCity());
            statement.setInt(4, person.getSinID());
            statement.setInt(5, person.isFamous() ? 1 : 0);
            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver loading error");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Nullable
    public static Person[] getFamousPersons(int sinID) {
        ResultSet resultSet;
        PreparedStatement statement;
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Class.forName(JDBC_DRIVER);
            Person[] fPersons;
            String fPersonName;
            String fPersonCity;
            String getSinInfoQuery = "SELECT * FROM persons WHERE sins_sinID=? AND isFamous=1;";
            statement = connection.prepareStatement(getSinInfoQuery);
            statement.setInt(1, sinID);
            resultSet = statement.executeQuery();
            /* results parsing */
            if (resultSet.next()) {
                resultSet.last();
                int size = resultSet.getRow();
                resultSet.first();
                fPersons = new Person[size];
                for (int i = 0; i < fPersons.length; i++) {
                    fPersonName = resultSet.getNString("personName");
                    fPersonCity = resultSet.getNString("personCity");
                    fPersons[i] = Person.PersonManager.newFamousPerson(fPersonName, fPersonCity, sinID);
                }
            } else {
                fPersons = null;
            }
            return fPersons;
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver loading error");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static Sin getSin(String sinName) {
        ResultSet resultSet;
        PreparedStatement statement;
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Class.forName(JDBC_DRIVER);
            String getSinInfoQuery = "" +
                    "SELECT * " +
                    "FROM sins " +
                    "LEFT JOIN paths " +
                    "ON paths.pathID = sins.paths_pathID " +
                    "WHERE sinName=?;";
            statement = connection.prepareStatement(getSinInfoQuery);
            statement.setString(1, sinName);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int sinID = resultSet.getInt("sinID");
                int pathID = resultSet.getInt("pathID");
                String pathDescription = resultSet.getNString("pathDescription");
                boolean pathIsApproved = resultSet.getInt("isApproved") == 1;
                Person[] fPearsons = DbManager.getFamousPersons(sinID);
                Path path;
                if (pathIsApproved)
                    path = Path.PathManager.newApprovedPath(pathID, pathDescription);
                else path = Path.PathManager.newInapprovedPath(pathID, pathDescription);
                return Sin.SinManager.newSin(sinID, sinName, fPearsons, path);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver loading error");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns ID of created sin, -1 if error
     */
    @Nullable
    public static Sin createSin(String sinName) {
        PreparedStatement statement;
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Class.forName(JDBC_DRIVER);
            String createSinQuery = "" +
                    "INSERT INTO sins (sinName) " +
                    "VALUES (?);";
            statement = connection.prepareStatement(createSinQuery, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, sinName);
            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            int sinID = 0;
            if (keys.next()) {
                sinID = keys.getInt(1);
            }
            return Sin.SinManager.newSin(sinID, sinName, null, null);
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver loading error");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setSinPath(int pathID, int sinID) {
        PreparedStatement statement;
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Class.forName(JDBC_DRIVER);
            String updateSinPathQuery = "" +
                    "UPDATE sins " +
                    "SET paths_pathID=? " +
                    "WHERE sinID=?;";
            statement = connection.prepareStatement(updateSinPathQuery);
            statement.setInt(1, pathID);
            statement.setInt(2, sinID);
            statement.executeUpdate();
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver loading error");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int createPath(String pathDescription, boolean isApproved) {
        PreparedStatement statement;
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Class.forName(JDBC_DRIVER);
            String createPathQuery = "" +
                    "INSERT INTO paths (pathDescription, isApproved)  " +
                    "VALUES (?, ?);";
            statement = connection.prepareStatement(createPathQuery, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, pathDescription);
            statement.setInt(2, isApproved ? 1 : 0);
            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver loading error");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setPathDescription(String pathDescription, int pathID) {
        PreparedStatement statement;
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Class.forName(JDBC_DRIVER);
            String normalizedPathDescription = pathDescription
                    .trim()
                    .concat(" ")
                    .replaceAll("[1]", " one ")
                    .replaceAll("[0]", " zero ")
                    .replaceAll("[\'\"]", "&amp")
                    .toLowerCase();
            Pattern sqlKeyWordsPat = Pattern.compile("(insert|from|select|delete|drop|alter|create)");
            Matcher sqlMatcher = sqlKeyWordsPat.matcher(normalizedPathDescription);
            Set<String> foundKeyWords = new HashSet<>();
            while (sqlMatcher.find()) {
                foundKeyWords.add(sqlMatcher.group());
            }
            for (String foundKeyWord : foundKeyWords) {
                normalizedPathDescription = normalizedPathDescription
                        .replaceAll(foundKeyWord, "!!!" + foundKeyWord);
            }
            String updatePathDescrQuery = "" +
                    "UPDATE paths " +
                    "SET pathDescription=? " +
                    "WHERE pathID=?;";
            statement = connection.prepareStatement(updatePathDescrQuery);
            statement.setString(1, normalizedPathDescription);
            statement.setInt(2, pathID);
            statement.executeUpdate();
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver loading error");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

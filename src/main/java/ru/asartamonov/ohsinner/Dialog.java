package ru.asartamonov.ohsinner;

import java.util.Scanner;

/**
 * User interaction layer: user input, info messages.
 * This is only one layer, which user touches.
 * */
public class Dialog {

    private String userName = "";
    private int userAge = 0;
    private String userCity = "";
    private String userSinName = "";

    /* gathering user input and inits user data fields */
    private void getUserData() {

        /* Scanner to get and parse user input */
        Scanner sc = new Scanner(System.in);

        System.out.println("What is your name?");
        String[] inputName = sc.nextLine().trim().split("\\s");
        for (int i = 0; i < inputName.length && i < 3; i++)
            userName = userName + inputName[i];

        System.out.println("How old are you?");
        try {
            userAge = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            userAge = 0;
        }

        System.out.println("Where are you from?");
        String[] inputCity = sc.nextLine().trim().split("\\s");
        for (int i = 0; i < inputCity.length && i < 3; i++)
            userCity = userCity + inputCity[i];

        System.out.println("What is your heaviest sin? Only one word please.");
        String[] sinArr = sc.nextLine().trim().toLowerCase().split("\\s");
        userSinName = userSinName + sinArr[0];

        System.out.printf("Well, %s, %d years old from %s with %s sin...\n",
                userName, userAge, userCity, userSinName);
    }

    public static void main(String[] args) {
        /* start and gather user data */
        Dialog dialog = new Dialog();
        dialog.getUserData();

        /* init sin object according to userinput:
         * - create new if not present in database
         * - take from database if already presented
         * */
        Sin sin = Sin.SinManager.newSin(dialog.userSinName);
        Person person;
        Person[] fPersons = null;
        Path path = null;

        /* init data */
        if (sin != null) {
            person = Person.PersonManager
                    .newSimplePerson(dialog.userAge, dialog.userName, dialog.userCity, sin.getSinID());
            DbManager.createPerson(person);
            fPersons = sin.getFamPersons();
            path = sin.getPath();
        }

        /* print famous users with same sins */
        if (fPersons != null && fPersons.length > 0) {
            System.out.printf("I hear you. I know some people with similar behavior:\n");
            for (Person fPerson : fPersons) {
                System.out.printf("%s from %s\n", fPerson.getName(), fPerson.getCity());
            }
        }
        /* get user thoughts on possible paths */
        String userPathDescription;
        if (path == null) {
            System.out.println("There is no path known yet...");
            System.out.println("I don't know what to say, I have to think on it... " +
                    "What are your thoughts how to handle your sin?\nPlease type and press ENTER:\n");
            Scanner sc = new Scanner(System.in);
            userPathDescription = sc.nextLine();
            if (!userPathDescription.isEmpty()) {
                Path newPath = Path.PathManager.newInapprovedPath(0, userPathDescription);
                Path.PathManager.syncPathWithDB(sin, newPath);
                System.out.println("I hear you, give me time to consider, goodbye for now...");
            } else {
                System.out.println("Well, lets settle down in quietness..");
            }
        } else if (path.isApproved()) {
            System.out.printf("Some people think there is a way. %s", path.getPathDescription());
        } else {
            System.out.printf("I don't know what to say, I have to think on it... " +
                            "What are your thoughts how to handle your %s?\nPlease type and press ENTER:\n",
                    sin.getSinName());
            Scanner sc = new Scanner(System.in);
            userPathDescription = sc.nextLine();
            path.setPathDescription(path.getPathDescription() + " " + userPathDescription);
            Path.PathManager.syncPathWithDB(sin, path);
            System.out.println("I hear you, give me time to consider, goodbye for now...");
        }
    }
}

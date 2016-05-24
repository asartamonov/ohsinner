Oh!Sinner<return>
is just an exercise in JDBC / SQL connections, updates etc.
But it has funny underlaying logic, in fact it is electronic
reconciliation-room, where you can share your sin and know a
movie-hero with similar problems and a way to solve it if possible.
I do not plan to work further on that, it's to precise from
ethic point of view.

What is done?
MySQL database, Java code.

Logic
Console application which works with
user input, writes it user name, age, city and sin to database,
trying to find hero and path based on user's sin, than if nothing
is found it asks user to share his thoughts about path to resolve,
and writes new sin to database with corresponding path.

Security
User generated paths do not shown to the user until database
administrator checks it.
SQL injections eliminated by checking user input, replacing
special symbols and words from SQL stop-list with another.
Long numbers also replaced to prevent binary and numeric data and
possible corresponding treats.
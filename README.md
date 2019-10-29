MiniTicketer
=
A proof-of-concept program that allows users to book tickets for _screenings_. 
A screening is a period of time where a _movie_ is shown at a _venue_. Venues are numbered,
and they have a specified seating dimension (rows x columns).

Compilation
-
**Note**: A pre-compiled JAR file exists in the repository already.

Otherwise, you will need to use [Maven](https://maven.apache.org/) to compile. 
Once Maven is installed, type the following into your commandline:
```
cd /path/to/MiniTicketer
mvn clean install
```
Maven automatically downloads the other required dependencies. 
Output JAR will be placed in the `/target` folder which can be clicked to run the program.

First-Time Running
-
**Note**: You need a MySQL v8.0+ server to run this program! Please contact `lavuh` if you want a username and password to access your own database on his VPS.

When running the program for the first time:
1. Double click the JAR to run the program & generate the configuration file. The program will not load as the MySQL server has not been configured.
2. Open the `ticketer.settings` file which is now beside the JAR file in your favourite text editor, and provide correct server & login details to the relevant fields.
3. Double click the JAR to run the program again. You may experience a delay of up to 20 seconds while the tables are generated and the test data is imported. _This will only happen once._
4. Troubleshoot any additional connection errors using the error messages that the dialogs provide.
5. **Enjoy!**

Quick-Start Guide
-
There is a quick start guide included with the program in the *About* section of the top menu. This one is a little bit more comprehensive. Here's a quick demonstration of program flow:
1. Open MiniTicketer.
2. On the left half of the screen, navigate through the pages of available movie screenings and click "Book Now" on one that you find interesting!
3. Once you have selected a screening to book, the dropdown list on the right side will now be enabled. Select a date+time slot that suits you. _Note: Each screening only plays at certain days and times!_
4. Once you have selected a time slot, specify the number of people who will be attending the screening. Submit your answer and the program will inform you of any errors.
5. Once you have entered a valid number of attendees, click "Confirm & Select Seats" to move onto the next step in the booking process.
6. Click on seats to highlight them green, indicating that you are booking this seat. Select enough seats for all of your attendees to move onto the next step. _Seats highlighted in red and un-clickable indicate that they are already taken._
7. You are given a chance to review your choices before finalising your booking. If you are happy with your selection, enter a username to book the tickets under. The program will let you know if there is anything wrong with your username selection.
8. Once you have entered a valid username to store the booking under, click "Make Booking" to finalise your booking! You will then be brought back to the first menu.
9. You may view your booked tickets at any time by clicking "View Tickets" and supplying the username they are kept under.
10. Book as many tickets at any time for as many screenings as you like! **Have fun!!**
> **TIP 1:** Too many movies? Try sorting by a specific genre using the tool in the top menu!

> **TIP 2:** If you have selected a large number of attendees, try using the "auto select" feature to automatically select enough seats for all of your attendees!

Before Modifying the Database...
-
* It is not recommended to alter the structure of the database tables. 
* If you wish to add additional genres, movies, venues, or screenings, do so with great care. 
* If screening time periods for _the same movie_ overlap, one will be automatically removed by the program. 
* If you modify values, do not induce erroneous values that you would not be able to induce through regular use of the program. Data is extensively validated on the front-end!
* Tables enforce certain checks and have primary & foreign key constraints. Keep this in mind when removing, adding, or modifying table rows.

That's it!
=
Have fun using MiniTicketer!
- Joshua / `lavuh`
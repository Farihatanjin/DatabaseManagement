import java.sql.* ;
import java.sql.Date;
import java.util.*;


class GoBabbyApp
{
    public static void main ( String [ ] args ) throws SQLException
    {
        // Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
        String tableName = "";
        int sqlCode=0;      // Variable to hold SQLCODE
        String sqlState="00000";  // Variable to hold SQLSTATE

        if ( args.length > 0 )
            tableName += args [ 0 ] ;
        else
            tableName += "exampletbl";

        // Register the driver.  You must register the driver before you can use it.
        try { DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ; }
        catch (Exception cnfe){ System.out.println("Class not found"); }

        // This is the url you must use for DB2.
        //Note: This url may not valid now ! Check for the correct year and semester and server name.
        String url = "jdbc:db2://winter2022-comp421.cs.mcgill.ca:50000/cs421";

        //REMEMBER to remove your user id and password before submitting your code!!
        String your_userid = "";
        String your_password = "";
        //AS AN ALTERNATIVE, you can just set your password in the shell environment in the Unix (as shown below) and read it from there.
        //$  export SOCSPASSWD=yoursocspasswd
        if(your_userid == null && (your_userid = System.getenv("SOCSUSER")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        if(your_password == null && (your_password = System.getenv("SOCSPASSWD")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        Connection con = DriverManager.getConnection (url,your_userid,your_password) ;
        Statement statement = con.createStatement ( ) ;


        // Inserting Data into the table
        try
        {
            Scanner scanner = new Scanner(System.in);
            mainmenu:
            while (true) {
                try{
                    System.out.println("Please enter your practitioner id [E] to exit: ");
                    String id = scanner.nextLine();

                    if (id.equals("E")){
                        statement.close();
                        con.close();
                        System.out.println("Session terminated");
                        break mainmenu;
                    }

                    //search midwife

                    String sql = "SELECT * FROM Midwife WHERE practID=\'" + id + "\'";
                    ResultSet rs = statement.executeQuery(sql);

                    if(!rs.next()){
                        System.out.println("Invalid practitioner id entered");
                        continue mainmenu;
                    }
                    else {
                        enterdatemenu:
                        while (true) {
                            System.out.println("Please enter the date for appointment list [E] to exit: ");
                            String adate = scanner.nextLine();
                            Date date=Date.valueOf(adate);


                            if (date.equals("E")) {
                                statement.close();
                                con.close();
                                System.out.println("Session terminated");
                                break;
                            } else {
                                datemenu:
                                while (true) {
                                    String getappts = "SELECT Appointment.appttime, Appointment.relationship, Mother.mname,  Mother.healthID FROM Appointment INNER JOIN Mother ON  Mother.healthID = Appointment.healthID WHERE Appointment.practID =\'" + id + "\' AND Appointment.apptdate =\'" + date + "\' ORDER BY Appointment.appttime";
                                    rs = statement.executeQuery(getappts);
                                    ArrayList<String> appts = new ArrayList<>();
                                    int index = 0;


                                    while (rs.next()) {
                                        index++;
                                        String atime = rs.getString(1);
                                        String arel = rs.getString(2);
                                        String mname = rs.getString(3);
                                        String mID = rs.getString(4);
                                        String apptdetails = atime.trim() + " " + arel.trim() + " " + mname.trim() + " " + mID.trim();
                                        appts.add(apptdetails);
                                        System.out.println(index + ": " + apptdetails);
                                    }
                                    if (appts.size() == 0) {
                                        System.out.println("No appointments found for this date");
                                        continue enterdatemenu;
                                    }

                                    System.out.println("\nEnter the appointment number that you would like to work on." +
                                            " [E] to exit [D] to go back to another date : ");

                                    String input = scanner.nextLine();

                                    if (input.equals("E")) {
                                        statement.close();
                                        con.close();
                                        System.out.println("Session terminated");
                                        break mainmenu;
                                    } else if (input.equals("D")) {
                                        continue enterdatemenu;
                                    } else {
                                        optionsmenu:
                                        while (true) {
                                            int i = Integer.parseInt(input);
                                            String[] details = appts.get(i - 1).split(" ");
                                            System.out.println("For " + details[2] + " " + details[3] + " " + details[4] + "\n");
                                            System.out.println("1. Review notes\n2. Review tests\n3. Add a note\n4. Prescribe a test \n5. Go back to the appointments.\n\nEnter your choice");

                                            Time time = Time.valueOf(details[0]);

                                            String healthID = details[4];
                                            String apptID = "SELECT Appointment.apptID FROM Appointment WHERE Appointment.practID  =\'" + id + "\' AND Appointment.healthID =\'" + healthID + "\' AND Appointment.apptdate =\'" + date + "\' AND Appointment.appttime =\'" + time  + "\'";
                                            rs = statement.executeQuery(apptID);
                                            if (rs.next()){
                                                apptID = rs.getString(1);
                                            }

                                            String choice = scanner.nextLine();

                                            if (choice.equals("1")) {
                                                String getNotes = "SELECT Appointment.apptdate, Notes.notetimestamp, Notes.note FROM Appointment INNER JOIN Notes ON Appointment.apptID = Notes.apptID WHERE Appointment.healthID =\'" + healthID + "\'ORDER BY Appointment.apptdate DESC,Appointment.appttime DESC";
                                                rs = statement.executeQuery(getNotes);
                                                ArrayList<String> notes = new ArrayList<>();

                                                while (rs.next()) {

                                                    String notedate = rs.getString(1);
                                                    String atimestamp = rs.getString(2);
                                                    String anote = rs.getString(3);
                                                    String note = notedate.trim() + " " + atimestamp.trim() + " " + anote.trim();
                                                    appts.add(note);
                                                    System.out.println(note);
                                                }

                                                System.out.println("\n");

                                                continue optionsmenu;

                                            } else if (choice.equals("2")) {

                                                String getTests = "SELECT Test.sampledate, Test.testType, Test.resultNote From Appointment INNER JOIN Prescription ON Prescription.apptID = Appointment.apptID  INNER JOIN Test ON Test.testID = Prescription.testID WHERE Appointment.healthID =\'" + healthID + "\'ORDER BY Test.sampledate DESC";
                                                rs = statement.executeQuery(getTests);
                                                ArrayList<String> tests = new ArrayList<>();

                                                while (rs.next()) {

                                                    String testdate = rs.getString(1);
                                                    String atype = rs.getString(2);
                                                    String testnote = rs.getString(3);
                                                    if (rs.wasNull()) {
                                                        testnote = "PENDING";
                                                    }

                                                    String test = testdate.trim() + " [" + atype.trim() + "] " + testnote.trim();
                                                    appts.add(test);
                                                    System.out.println(test);
                                                }

                                                System.out.println("\n");

                                                continue optionsmenu;

                                            } else if (choice.equals("3")) {

                                                System.out.println("Please type your observation: ");
                                                String note = scanner.nextLine();

                                                long range = 1234567L;
                                                Random r = new Random();
                                                long number = (long)(r.nextDouble()*range);

                                                Time currentTime = Time.valueOf(details[0]);
                                                currentTime.setTime(currentTime.getTime() + number);

                                               // java.sql.Time currentTime = new java.sql.Time(Calendar.getInstance().getTime().getTime());

                                                String insert = " INSERT INTO Notes (apptID, notetimestamp, note) VALUES (\'" + apptID + "\', \'" + currentTime + "\', \'" + note + "\')";
                                                statement.executeUpdate(insert);

                                                System.out.println("\n");

                                                continue optionsmenu;

                                            } else if (choice.equals("4")) {

                                                System.out.println("Please enter the type of test: ");
                                                String test = scanner.nextLine();
                                                String testID = getID();

                                                String insert = " INSERT INTO Test (testID, sampledate, labdate, testType) VALUES (\'" + testID + "\', \'" + date + "\', \'" + date + "\', \'" + test + "\')";
                                                statement.executeUpdate(insert);
                                                String ins = "INSERT INTO Prescription (apptID, testID) VALUES (\'" + apptID + "\', \'" + testID + "\')";
                                                statement.executeUpdate(ins);

                                                System.out.println("\n");

                                                continue optionsmenu;

                                            } else {
                                                continue datemenu;
                                            }


                                        }


                                    }
                                }
                            }


                        }
                    }



                } catch (SQLException e) {
                    sqlCode = e.getErrorCode(); // Get SQLCODE
                    sqlState = e.getSQLState(); // Get SQLSTATE
                    // Your code to handle errors comes here;
                    // something more meaningful than a print would be good
                    System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
                    System.out.println(e);
                    break;
                }

            }
            statement.close();
            con.close();

        }

        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }

        // Querying a table
        try
        {
            String querySQL = "SELECT id, name from " + tableName + " WHERE NAME = \'Vicki\'";
            System.out.println (querySQL) ;
            java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

            while ( rs.next ( ) )
            {
                int id = rs.getInt ( 1 ) ;
                String name = rs.getString (2);
                System.out.println ("id:  " + id);
                System.out.println ("name:  " + name);
            }
            System.out.println ("DONE");
        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }

        //Updating a table
        try
        {
            String updateSQL = "UPDATE " + tableName + " SET NAME = \'Mimi\' WHERE id = 3";
            System.out.println(updateSQL);
            statement.executeUpdate(updateSQL);
            System.out.println("DONE");

            // Dropping a table
            String dropSQL = "DROP TABLE " + tableName;
            System.out.println ( dropSQL ) ;
            statement.executeUpdate ( dropSQL ) ;
            System.out.println ("DONE");
        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }

        // Finally but importantly close the statement and connection
        statement.close ( ) ;
        con.close ( ) ;
    }

    public static String getID(){
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder(20);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }

}


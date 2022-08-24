import java.sql.*;
import java.util.Scanner;

/***********************************************************************************************************************
 *
 * In this program I will assume that the table "EmployeeTable" already exists with the columns in the order
 * ________________________________________________________________________________________________________________
 * | Id |  Password  |  Name  |  Surname |DOB |Department | Position  |  Salary | Pension |MedicalAid | NetSalary |
 * ________________________________________________________________________________________________________________
 * |    |            |        |          |    |           |           |         |         |           |           |
 * ________________________________________________________________________________________________________________
 * and will be using mysql database. In case it does not exist, a query for creating table [ String queryCreateTable =
 * "CREATE TABLE EmployeeTable (
 *                                      ID INT,
 *                                      Password VARCHAR[20],
 *                                      Name VARCHAR[20],
 *                                      Surname VARCHAR[45],
 *                                      DOB VARCHAR[20],
 *                                      Department VARCHAR[45],
 *                                      Position VARCHAR[45],
 *                                      Salary FLOAT,
 *                                      Pension FLOAT,
 *                                      MedicalAid FLOAT,
 *                                      NetSalary FLOAT
 *                                  )"; ]
 *
 * STEPS FOR DATABASE CONNECTION:
 * ------------------------------
 * 1.   import the java.sql package
 * 2.   Load 'com.mysql.cj.jdbc.Driver' and register it with Class.forName("com.mysql.cj.jdbc.Driver")
 * 3.   Establish the connection using the Connection interface
 * 4.   Create statement:   Statement or PreparedStatement or CallableStatement
 * 5.   Execute the sql query with the executeQuery method
 * 6.   Process the results
 * 7.   close the connection
 *
 *                                  *****************************
 *                                  * Special Thanks to Telusko *
 **********************************************************************************************************************/

// Using the Data Access Object design pattern
public class Controller {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);
        DatabaseConnector dbc = new DatabaseConnector();
        int id;
        String employeePassword, url, username, dbPassword;
        int choice;

        System.out.println("First things first, your database credentials!");
        System.out.println();
        System.out.println("Enter database url ");
        url = scanner.nextLine();
        System.out.println("Enter your username ");
        username = scanner.nextLine();
        System.out.println("Enter your database password ");
        dbPassword = scanner.nextLine();

        System.out.println("Enter your choice:\n0. Exit\n1. Add employee data to database\n2. Get employee data from database");
        choice = scanner.nextInt();

        dbc.connect(url, username, dbPassword);
        while (true) {
            if (choice == 0) {
                dbc.closeConnection();
                System.out.println("Bye!");
                break;
            } else if (choice == 1) {
                dbc.addEmployee();
            } else if (choice == 2) {
                System.out.println("Enter employee id ");
                id = scanner.nextInt();
                scanner.nextLine();
                System.out.println("Enter employee password ");
                employeePassword = scanner.nextLine();

                String pass = dbc.getPass(id);

                if (employeePassword.equals(pass)) {
                    dbc.getEmployee(id);
                } else {
                    System.out.println("Incorrect employee id or password. Please, try again");
                }
            } else {
                System.out.println("Invalid option!");
            }
        }
    }
}

// this class does all the work, from connecting to the database to executing queries
class DatabaseConnector {
    Employee employee1 = new Employee();
    Connection con = null;
    Scanner scanner = new Scanner(System.in);

    public void connect(String url, String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver"); // load driver
        con = DriverManager.getConnection(url, username, password); // establish connection
    }

    public String getPass(int id) throws SQLException {
        String query = String.format("select Password from EmployeeTable where Id=%d", id);
        Statement st = con.createStatement(); // create statement
        ResultSet rs = st.executeQuery(query); // execute query
        rs.next();
        return rs.getString(1);
    }

    public void addEmployee() throws SQLException {
        Employee e = new Employee();
        String query = "insert into EmployeeTable values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        System.out.println("Employee id ");
        e.id = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Employee password ");
        e.password = scanner.nextLine();
        System.out.println("Employee name ");
        e.name = scanner.nextLine();
        System.out.println("Employee surname ");
        e.surname = scanner.nextLine();
        System.out.println("Employee date of birth ");
        e.dob = scanner.nextLine();
        System.out.println("Employee department ");
        e.department = scanner.nextLine();
        System.out.println("Employee position ");
        e.position = scanner.nextLine();
        e.setPension();
        e.setNetSalary();

        PreparedStatement pst = con.prepareStatement(query); // create a PreparedStatement
        pst.setInt(1, e.id);
        pst.setString(2, e.password);
        pst.setString(3, e.name);
        pst.setString(4, e.surname);
        pst.setString(5, e.dob);
        pst.setString(6, e.department);
        pst.setString(7, e.position);
        pst.setFloat(8, e.salary);
        pst.setFloat(9, e.pension);
        pst.setFloat(10, e.medicalAid);
        pst.setFloat(11, e.netSalary);
        pst.executeUpdate(); // execute query

        pst.close();
    }

    public void getEmployee(int id) {
        String query = String.format("select Name, Surname, DOB, Department, Position, NetSalary from EmployeeTable " +
                "where id = %d", id);
        float rate;
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            rs.next();
            String name = rs.getString(1);
            String surname = rs.getString(2);
            String dob = rs.getString(3);
            String department = rs.getString(4);
            String position = rs.getString(5);
            float netSalary = rs.getFloat(6);

            System.out.println("Enter the current market USD/RTGS rate ");
            rate = scanner.nextFloat();

            System.out.println(name + " " + surname + ", " + dob + ": " + position + "[" + department + "] " +
                        "" + "RTGS" + netSalary + " --> " + "USD$" + employee1.convertNetSalary(rate, netSalary));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void closeConnection() throws SQLException {
        con.close();
    }
}
class Employee {
    String name, surname, password, dob, department, position;
    float salary = 50000;
    float medicalAid = 10000;
    float pension, netSalary;
    int id;

    public void setPension() {
        pension = (float) (salary * 0.1);
    }

    public void setNetSalary() {
        netSalary = salary - (pension + medicalAid);
    }

    public float convertNetSalary(float rate, float netSalary) {
        return netSalary / rate;
    }
}

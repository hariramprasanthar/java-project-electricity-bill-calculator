import java.sql.*;
import java.util.Scanner;

class Customer {
    private String name;
    private int customerID;
    private double unitsConsumed;

    public Customer(String name, int customerID, double unitsConsumed) {
        this.name = name;
        this.customerID = customerID;
        this.unitsConsumed = unitsConsumed;
    }

    public String getName() {
        return name;
    }

    public int getCustomerID() {
        return customerID;
    }

    public double getUnitsConsumed() {
        return unitsConsumed;
    }

    public void setUnitsConsumed(double unitsConsumed) {
        this.unitsConsumed = unitsConsumed;
    }

    @Override
    public String toString() {
        return "Customer ID: " + customerID + ", Name: " + name + ", Units Consumed: " + unitsConsumed;
    }
}

public class ElectricityBillingSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/electricity_billing";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Replace with your MySQL password

    private Connection connect() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }

    private void addCustomer() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Customer ID: ");
        int id = scanner.nextInt();
        System.out.print("Enter Units Consumed: ");
        double units = scanner.nextDouble();

        String sql = "INSERT INTO customers (id, name, units_consumed) VALUES (?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setDouble(3, units);
            stmt.executeUpdate();
            System.out.println("Customer added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding customer: " + e.getMessage());
        }
    }

    private void updateUsage() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Customer ID: ");
        int id = scanner.nextInt();
        System.out.print("Enter Updated Units Consumed: ");
        double units = scanner.nextDouble();

        String sql = "UPDATE customers SET units_consumed = ? WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, units);
            stmt.setInt(2, id);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Usage updated successfully.");
            } else {
                System.out.println("Customer not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating usage: " + e.getMessage());
        }
    }

    private void generateBill() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Customer ID: ");
        int id = scanner.nextInt();

        String sql = "SELECT name, units_consumed FROM customers WHERE id = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                double unitsConsumed = rs.getDouble("units_consumed");
                double billAmount = calculateBill(unitsConsumed);
                System.out.println("\n--- Electricity Bill Statement ---");
                System.out.println("Customer Name: " + name);
                System.out.println("Units Consumed: " + unitsConsumed);
                System.out.println("Total Bill: Rs" + billAmount);
                System.out.println("----------------------------------");
            } else {
                System.out.println("Customer not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error generating bill: " + e.getMessage());
        }
    }

    private double calculateBill(double units) {
        final double RATE_1 = 1.5;
        final double RATE_2 = 2.0;
        final double RATE_3 = 3.0;
        double total;

        if (units <= 100) {
            total = units * RATE_1;
        } else if (units <= 200) {
            total = (100 * RATE_1) + ((units - 100) * RATE_2);
        } else {
            total = (100 * RATE_1) + (100 * RATE_2) + ((units - 200) * RATE_3);
        }

        return total;
    }

    private void viewAllCustomers() {
        String sql = "SELECT * FROM customers";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (!rs.isBeforeFirst()) {
                System.out.println("No customers available.");
            }
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double units = rs.getDouble("units_consumed");
                System.out.println("Customer ID: " + id + ", Name: " + name + ", Units Consumed: " + units);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving customers: " + e.getMessage());
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n1. Add Customer\n2. Update Customer Usage\n3. Generate Bill\n4. View All Customers\n5. Exit");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    addCustomer();
                    break;
                case 2:
                    updateUsage();
                    break;
                case 3:
                    generateBill();
                    break;
                case 4:
                    viewAllCustomers();
                    break;
                case 5:
                    running = false;
                    System.out.println("Exiting... Thank you!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public static void main(String[] args) {
        new ElectricityBillingSystem().run();
    }
}

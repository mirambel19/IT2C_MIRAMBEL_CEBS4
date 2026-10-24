package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Authentication {
    private final Scanner sc;
    private final UserDao userDao;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.sc = scanner;
        this.userDao = userDao;
    }

    public void start() {
        while (true) {
            System.out.println("=".repeat(50));
            System.out.println("      CAMPUS EQUIPMENT BORROWING SYSTEM");
            System.out.println("=".repeat(50));
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            switch (sc.nextLine().trim()) {
                case "1" -> loginForm();
                case "2" -> registerForm();
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option. Try again.\n");
            }
        }
    }

    private void registerForm() {
        System.out.println("\n--- Register ---");
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        System.out.println("\nSelect Role:");
        System.out.println("1. STAFF");
        System.out.println("2. BORROWER");
        System.out.print("Choose role (1-2): ");
        String roleChoice = sc.nextLine().trim();
        String role = switch (roleChoice) {
            case "1" -> "STAFF";
            case "2" -> "BORROWER";
            default -> {
                System.out.println("Defaulted to BORROWER");
                yield "BORROWER";
            }
        };

        if (userDao.register(username, password, role)) {
            System.out.println("Registration successful! Status: PENDING\n");
        }
    }

    private void loginForm() {
        System.out.println("\n--- Login ---");
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        Optional<User> user = userDao.login(username, password);
        if (user.isPresent()) {
            User u = user.get();

            if (!"APPROVED".equals(u.status()) && !"ADMIN".equals(u.roles())) {
                System.out.println("Account PENDING approval. Contact admin.\n");
                return;
            }

            // ✅ Exact format from screenshot
            System.out.printf("-> Welcome %s | ROLE: %s%n%n", u.username(), u.roles());
            routeToDashboard(u);
        } else {
            System.out.println("Invalid username or password.\n");
        }
    }

    private void routeToDashboard(User user) {
        switch (user.roles()) {
            case "ADMIN" -> adminDashboard();
            case "STAFF" -> staffDashboard();
            case "BORROWER" -> borrowerDashboard();
            default -> System.out.println("Access denied.\n");
        }
    }

    private void adminDashboard() {
        while (true) {
            // ✅ Exact format from screenshot
            System.out.println("=".repeat(50));
            System.out.println("           ADMIN DASHBOARD");
            System.out.println("=".repeat(50));
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");

            switch (sc.nextLine().trim()) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> addNewUser();
                case "5" -> updateUserRoleStatus();
                case "6" -> deleteUser();
                case "7" -> {
                    System.out.println("Logged out.\n");
                    return;
                }
                default -> System.out.println("Invalid option.\n");
            }
        }
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        System.out.println("\n--- All Users ---");
        if (users.isEmpty()) {
            System.out.println("No users found.\n");
            return;
        }
        for (User u : users) {
            System.out.printf("ID: %d | User: %s | Role: %s | Status: %s%n",
                    u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println();
    }

    private void viewPendingUsers() {
        List<User> pending = userDao.getPendingUsers();
        System.out.println("\n--- Pending Approvals ---");
        if (pending.isEmpty()) {
            System.out.println("No pending accounts.\n");
            return;
        }
        for (User u : pending) {
            System.out.printf("ID: %d | User: %s | Role: %s%n",
                    u.id(), u.username(), u.roles());
        }
        System.out.println();
    }

    private void approveUser() {
        viewPendingUsers();
        System.out.print("Enter User ID to approve (0 = Cancel): ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            if (id == 0) return;
            if (userDao.updateStatus(id, "APPROVED")) {
                System.out.println("User approved!\n");
            } else {
                System.out.println("User not found.\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.\n");
        }
    }

    private void addNewUser() {
        System.out.println("\n--- Add New User ---");
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        System.out.print("Role (ADMIN/STAFF/BORROWER): ");
        String role = sc.nextLine().trim();
        if (userDao.register(username, password, role)) {
            List<User> all = userDao.getAllUsers();
            userDao.updateStatus(all.get(all.size() - 1).id(), "APPROVED");
            System.out.println("User added & approved.\n");
        }
    }

    private void updateUserRoleStatus() {
        viewAllUsers();
        System.out.print("Enter User ID to update: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("New Role (ADMIN/STAFF/BORROWER) or press Enter to skip: ");
            String newRole = sc.nextLine().trim();
            System.out.print("New Status (APPROVED/PENDING) or press Enter to skip: ");
            String newStatus = sc.nextLine().trim();

            if (!newRole.isBlank()) userDao.updateRole(id, newRole);
            if (!newStatus.isBlank()) userDao.updateStatus(id, newStatus);
            System.out.println("User updated.\n");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.\n");
        }
    }

    private void deleteUser() {
        viewAllUsers();
        System.out.print("Enter User ID to delete (0 = Cancel): ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            if (id == 0) return;
            System.out.print("Confirm delete? (yes/no): ");
            if ("yes".equalsIgnoreCase(sc.nextLine().trim())) {
                if (userDao.deleteUser(id)) System.out.println("User deleted.\n");
                else System.out.println("Failed.\n");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.\n");
        }
    }

    private void staffDashboard() {
        while (true) {
            System.out.println("=".repeat(50));
            System.out.println("         STAFF DASHBOARD");
            System.out.println("=".repeat(50));
            System.out.println("1. Process Borrow Request");
            System.out.println("2. Record Equipment Return");
            System.out.println("3. Check Equipment Availability");
            System.out.println("4. Logout");
            System.out.print("Choose an option (1-4): ");

            switch (sc.nextLine().trim()) {
                case "1" -> System.out.println("Processing borrow request...\n");
                case "2" -> System.out.println("Recording return...\n");
                case "3" -> System.out.println("Checking availability...\n");
                case "4" -> { System.out.println("Logged out.\n"); return; }
                default -> System.out.println("Invalid option.\n");
            }
        }
    }

    private void borrowerDashboard() {
        while (true) {
            System.out.println("=".repeat(50));
            System.out.println("        BORROWER DASHBOARD");
            System.out.println("=".repeat(50));
            System.out.println("1. View Available Equipment");
            System.out.println("2. Submit Borrow Request");
            System.out.println("3. My Borrowed Items");
            System.out.println("4. Logout");
            System.out.print("Choose an option (1-4): ");

            switch (sc.nextLine().trim()) {
                case "1" -> System.out.println("Loading equipment list...\n");
                case "2" -> System.out.println("Opening borrow form...\n");
                case "3" -> System.out.println("Loading your borrows...\n");
                case "4" -> { System.out.println("Logged out.\n"); return; }
                default -> System.out.println("Invalid option.\n");
            }
        }
    }
}
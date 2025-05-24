package com.mycompany.dbms1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HotelManagementApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }

    // Database connection utility
    static class DBConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/sys";
        private static final String USER = "root";
        private static final String PASS = "dbms";

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASS);
        }
    }

    // Login window
    static class LoginFrame extends JFrame {
        private final JTextField userField = new JTextField();
        private final JPasswordField passField = new JPasswordField();

        LoginFrame() {
            setTitle("Hotel Management - Login");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(350, 200);
            setLayout(new GridLayout(3, 2, 10, 10));

            add(new JLabel("Username:")); add(userField);
            add(new JLabel("Password:")); add(passField);

            JButton loginBtn = new JButton("Login");
            JButton regBtn = new JButton("Register");
            loginBtn.addActionListener(e -> doLogin());
            regBtn.addActionListener(e -> { dispose(); new RegisterFrame(); });

            add(loginBtn); add(regBtn);

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void doLogin() {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT id, role FROM users WHERE username=? AND password=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, user);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int uid = rs.getInt("id");
                    String role = rs.getString("role");
                    JOptionPane.showMessageDialog(this, "Welcome, " + user);
                    dispose();
                    if ("admin".equals(role)) new AdminFrame();
                    else new DashboardFrame(uid);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Registration window
    static class RegisterFrame extends JFrame {
        private final JTextField userField = new JTextField();
        private final JPasswordField passField = new JPasswordField();

        RegisterFrame() {
            setTitle("Hotel Management - Register");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(350, 180);
            setLayout(new GridLayout(3, 2, 10, 10));

            add(new JLabel("Username:")); add(userField);
            add(new JLabel("Password:")); add(passField);

            JButton regBtn = new JButton("Register");
            regBtn.addActionListener(e -> doRegister());
            add(regBtn);

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void doRegister() {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'user')";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, user);
                ps.setString(2, pass);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration successful");
                dispose();
                new LoginFrame();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Registration error", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // User dashboard
    static class DashboardFrame extends JFrame {
        DashboardFrame(int userId) {
            setTitle("Hotel Management - User Dashboard");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(800, 600);

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Available Rooms", new RoomListPanel());
            tabs.addTab("Book Room", new BookRoomPanel(userId));

            add(tabs);
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    // Panel to list available rooms
    static class RoomListPanel extends JPanel {
        RoomListPanel() {
            setLayout(new BorderLayout());
            JTable table = new JTable(new DefaultTableModel(new Object[]{"Room ID", "Room Type", "Price", "Status"}, 0));
            loadData((DefaultTableModel) table.getModel());
            add(new JScrollPane(table), BorderLayout.CENTER);
        }

        private void loadData(DefaultTableModel model) {
            model.setRowCount(0);
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT id, room_type, price, status FROM rooms WHERE status='Available'";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4)
                    });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading rooms", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Panel to book a room
    static class BookRoomPanel extends JPanel {
        private final JComboBox<String> roomTypeCombo = new JComboBox<>(new String[]{"Single", "Double", "Suite"});
        private final JTextField checkInField = new JTextField();
        private final JTextField checkOutField = new JTextField();
        private final int userId;

        BookRoomPanel(int userId) {
            this.userId = userId;
            setLayout(new GridLayout(5, 2, 10, 10));

            add(new JLabel("Room Type:")); add(roomTypeCombo);
            add(new JLabel("Check-in Date:")); add(checkInField);
            add(new JLabel("Check-out Date:")); add(checkOutField);

            JButton bookBtn = new JButton("Book Room");
            bookBtn.addActionListener(e -> doBook());
            add(bookBtn);
        }

        private void doBook() {
            String roomType = (String) roomTypeCombo.getSelectedItem();
            String checkInDate = checkInField.getText();
            String checkOutDate = checkOutField.getText();
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO bookings (user_id, room_type, check_in_date, check_out_date, status) " +
                             "VALUES (?, ?, ?, ?, 'PENDING')";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, roomType);
                ps.setString(3, checkInDate);
                ps.setString(4, checkOutDate);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Booking submitted! Awaiting approval.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error booking room", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Admin panel for approving/rejecting bookings
    static class AdminFrame extends JFrame {
        private final JTable table = new JTable();

        AdminFrame() {
            setTitle("Admin - Manage Bookings");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(700, 500);

            DefaultTableModel model = new DefaultTableModel(new Object[]{"Booking ID", "User", "Room Type", "Check-in", "Check-out", "Action"}, 0) {
                public boolean isCellEditable(int r, int c) { return c == 5; }
            };
            table.setModel(model);
            loadPendingBookings();

            JScrollPane sp = new JScrollPane(table);
            JButton refreshBtn = new JButton("Refresh");
            refreshBtn.addActionListener(e -> loadPendingBookings());

            add(sp, BorderLayout.CENTER);
            add(refreshBtn, BorderLayout.SOUTH);

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void loadPendingBookings() {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT b.id, u.username, b.room_type, b.check_in_date, b.check_out_date " +
                             "FROM bookings b JOIN users u ON b.user_id = u.id WHERE b.status = 'PENDING'";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    int bookingId = rs.getInt(1);
                    JButton approveBtn = new JButton("Approve");
                    approveBtn.addActionListener(e -> updateBookingStatus(bookingId, "APPROVED"));
                    JButton rejectBtn = new JButton("Reject");
                    rejectBtn.addActionListener(e -> updateBookingStatus(bookingId, "REJECTED"));
                    JPanel actionPanel = new JPanel();
                    actionPanel.add(approveBtn);
                    actionPanel.add(rejectBtn);

                    model.addRow(new Object[]{
                        bookingId,
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        actionPanel
                    });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading pending bookings", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void updateBookingStatus(int bookingId, String status) {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET status = ? WHERE id = ?");
                ps.setString(1, status);
                ps.setInt(2, bookingId);
                ps.executeUpdate();
                loadPendingBookings();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating booking status", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

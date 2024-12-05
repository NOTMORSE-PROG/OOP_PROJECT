import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;

public class Setting extends JFrame implements ActionListener {
    private final String userEmail;
    private final JPasswordField newPasswordField, confirmPasswordField;
    private final JButton updateAccountButton, deleteAccountButton, goBackButton;
    private final JComboBox<String> campusComboBox;

    public Setting(String userEmail) {
        this.userEmail = userEmail;
        setTitle("Profile Settings");
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel titleLabel = new JLabel("PROFILE SETTINGS", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.decode("#0F149a"));
        add(mainPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String fullName = "", currentCampus = "", studentId = "";
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT CONCAT(first_name, ' ', last_name) AS full_name, tip_branch, student_id FROM students WHERE student_email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                fullName = rs.getString("full_name");
                currentCampus = rs.getString("tip_branch");
                studentId = rs.getString("student_id");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading user data: " + e.getMessage());
        }

        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        fullNameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(fullNameLabel, gbc);

        JLabel fullNameValue = new JLabel(fullName);
        fullNameValue.setFont(new Font("Arial", Font.PLAIN, 28));
        fullNameValue.setForeground(Color.WHITE);
        gbc.gridx = 1;
        mainPanel.add(fullNameValue, gbc);

        JLabel studentIdLabel = new JLabel("Student ID:");
        studentIdLabel.setFont(new Font("Arial", Font.BOLD, 28));
        studentIdLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(studentIdLabel, gbc);

        JLabel studentIdValue = new JLabel(studentId);
        studentIdValue.setFont(new Font("Arial", Font.PLAIN, 28));
        studentIdValue.setForeground(Color.WHITE);
        gbc.gridx = 1;
        mainPanel.add(studentIdValue, gbc);

        JLabel campusLabel = new JLabel("Campus:");
        campusLabel.setFont(new Font("Arial", Font.BOLD, 28));
        campusLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(campusLabel, gbc);

        String[] campuses = {"TIP Manila", "TIP QC"};
        campusComboBox = new JComboBox<>(campuses);
        campusComboBox.setFont(new Font("Arial", Font.PLAIN, 28));
        campusComboBox.setSelectedItem(currentCampus);
        gbc.gridx = 1;
        mainPanel.add(campusComboBox, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 28));
        emailLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(emailLabel, gbc);

        JLabel emailValue = new JLabel(userEmail);
        emailValue.setFont(new Font("Arial", Font.PLAIN, 28));
        emailValue.setForeground(Color.WHITE);
        gbc.gridx = 1;
        mainPanel.add(emailValue, gbc);

        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordLabel.setFont(new Font("Arial", Font.BOLD, 28));
        newPasswordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(newPasswordLabel, gbc);

        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 28));
        gbc.gridx = 1;
        mainPanel.add(newPasswordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.BOLD, 28));
        confirmPasswordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 28));
        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);

        updateAccountButton = new JButton("Update Account");
        updateAccountButton.setFont(new Font("Arial", Font.BOLD, 28));
        updateAccountButton.setForeground(Color.WHITE);
        updateAccountButton.setBackground(Color.decode("#fd9b4d"));
        updateAccountButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        mainPanel.add(updateAccountButton, gbc);

        deleteAccountButton = new JButton("Delete Account");
        deleteAccountButton.setFont(new Font("Arial", Font.BOLD, 28));
        deleteAccountButton.setForeground(Color.WHITE);
        deleteAccountButton.setBackground(Color.RED);
        deleteAccountButton.addActionListener(this);
        gbc.gridy = 7;
        mainPanel.add(deleteAccountButton, gbc);

        goBackButton = new JButton("Go Back");
        goBackButton.setFont(new Font("Arial", Font.BOLD, 28));
        goBackButton.setForeground(Color.WHITE);
        goBackButton.setBackground(Color.decode("#fd9b4d"));
        goBackButton.addActionListener(this);
        gbc.gridy = 8;
        mainPanel.add(goBackButton, gbc);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == updateAccountButton) {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String selectedCampus = (String) campusComboBox.getSelectedItem();

            int minPasswordLength = 8;
            int maxPasswordLength = 16;

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in both password fields.");
                return;
            }

            if (newPassword.length() < minPasswordLength || newPassword.length() > maxPasswordLength) {
                JOptionPane.showMessageDialog(this, "Password must be between " + minPasswordLength + " and " + maxPasswordLength + " characters.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }

            try (Connection conn = DBConnector.getConnection()) {
                conn.setAutoCommit(false);

                String updateSQL = "UPDATE students SET tip_branch = ?";

                updateSQL += ", password = ?";

                updateSQL += " WHERE student_email = ?";

                PreparedStatement pstmt = conn.prepareStatement(updateSQL);
                pstmt.setString(1, selectedCampus);

                pstmt.setString(2, newPassword);
                pstmt.setString(3, userEmail);

                pstmt.executeUpdate();

                String updateItemsSQL = "UPDATE items SET campus = ? WHERE user_email = ?";
                PreparedStatement itemsPstmt = conn.prepareStatement(updateItemsSQL);
                itemsPstmt.setString(1, selectedCampus);
                itemsPstmt.setString(2, userEmail);
                itemsPstmt.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Account updated successfully.");

                newPasswordField.setText("");
                confirmPasswordField.setText("");

            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating account: " + ex.getMessage());
            }
        } else if (e.getSource() == deleteAccountButton) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete your account? This action cannot be undone.",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnector.getConnection()) {
                    conn.setAutoCommit(false);
                    String checkOrdersSQL = """
                                            SELECT COUNT(*) AS order_count
                                            FROM orders
                                            WHERE buyer_email = ? OR item_id IN (SELECT id FROM items WHERE user_email = ?)""";
                    try (PreparedStatement pstmt = conn.prepareStatement(checkOrdersSQL)) {
                        pstmt.setString(1, userEmail);
                        pstmt.setString(2, userEmail);
                        ResultSet rs = pstmt.executeQuery();

                        if (rs.next() && rs.getInt("order_count") > 0) {
                            JOptionPane.showMessageDialog(this, "Cannot delete account. There are orders associated with this account.");
                            conn.rollback();
                            return;
                        }
                    }

                    String deleteOrdersSQL = """
                                            DELETE FROM orders
                                            WHERE buyer_email = ? OR item_id IN (SELECT id FROM items WHERE user_email = ?)""";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteOrdersSQL)) {
                        pstmt.setString(1, userEmail);
                        pstmt.setString(2, userEmail);
                        pstmt.executeUpdate();
                    }

                    String deleteItemsSQL = "DELETE FROM items WHERE user_email = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteItemsSQL)) {
                        pstmt.setString(1, userEmail);
                        pstmt.executeUpdate();
                    }

                    String deleteUserSQL = "DELETE FROM students WHERE student_email = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSQL)) {
                        pstmt.setString(1, userEmail);
                        pstmt.executeUpdate();
                    }
                    conn.commit();

                    File userFolder = new File("sellingItems/" + userEmail);
                    if (userFolder.exists() && userFolder.isDirectory()) {
                        deleteFolder(userFolder);
                    }

                    JOptionPane.showMessageDialog(this, "Account and all associated data deleted successfully.");
                    System.exit(0);

                } catch (SQLException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Unable to delete account: You have unresolved Reports from Admin");
                }
            }
        } else if (e.getSource() == goBackButton) {
            this.dispose();
            new StudentDashboard(userEmail);
        }
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

}
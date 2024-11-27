import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class registerPage extends JFrame implements ActionListener {
    private final JTextField lastNameField, firstNameField, middleNameField, studentIdField, emailField;
    private final JPasswordField passwordField, confirmPasswordField;
    private final JComboBox<String> yearLevelComboBox, branchComboBox;
    private final JButton backButton;


    public registerPage() {
        setTitle("Student Business System - Register");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("STUDENT BUSINESS SYSTEM - REGISTER");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.decode("#0F149a"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.PLAIN, 30);
        Font fieldFont = new Font("Arial", Font.PLAIN, 28);

        JLabel lastNameLabel = new JLabel("Last Name: *");
        lastNameLabel.setFont(labelFont);
        lastNameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lastNameLabel, gbc);

        lastNameField = new JTextField(20);
        lastNameField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(lastNameField, gbc);

        JLabel firstNameLabel = new JLabel("First Name: *");
        firstNameLabel.setFont(labelFont);
        firstNameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(firstNameLabel, gbc);

        firstNameField = new JTextField(20);
        firstNameField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(firstNameField, gbc);

        JLabel middleNameLabel = new JLabel("Middle Name:");
        middleNameLabel.setFont(labelFont);
        middleNameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(middleNameLabel, gbc);

        middleNameField = new JTextField(20);
        middleNameField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(middleNameField, gbc);

        JLabel studentIdLabel = new JLabel("Student ID: *");
        studentIdLabel.setFont(labelFont);
        studentIdLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(studentIdLabel, gbc);

        studentIdField = new JTextField(6);
        studentIdField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(studentIdField, gbc);

        JLabel emailLabel = new JLabel("Student Email: *");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        JLabel yearLevelLabel = new JLabel("Year Level: *");
        yearLevelLabel.setFont(labelFont);
        yearLevelLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(yearLevelLabel, gbc);

        String[] yearLevels = {"1st Year", "2nd Year", "3rd Year", "4th Year"};
        yearLevelComboBox = new JComboBox<>(yearLevels);
        yearLevelComboBox.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(yearLevelComboBox, gbc);

        JLabel branchLabel = new JLabel("TIP Branch: *");
        branchLabel.setFont(labelFont);
        branchLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(branchLabel, gbc);

        String[] branches = {"TIP Manila", "TIP QC"};
        branchComboBox = new JComboBox<>(branches);
        branchComboBox.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(branchComboBox, gbc);

        JLabel passwordLabel = new JLabel("Password: *");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password: *");
        confirmPasswordLabel.setFont(labelFont);
        confirmPasswordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(fieldFont);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        JButton submitButton = new JButton("Submit");
        submitButton.setBackground(Color.decode("#fd9b4d"));
        submitButton.setFont(new Font("Arial", Font.BOLD, 30));
        submitButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(submitButton, gbc);

        add(formPanel, BorderLayout.CENTER);

        backButton = new JButton("Go Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 30));
        backButton.setBackground(Color.decode("#fd9b4d"));
        backButton.addActionListener(this);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.decode("#0F149a"));
        bottomPanel.add(backButton);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String lastName = lastNameField.getText();
        String firstName = firstNameField.getText();
        String middleName = middleNameField.getText();
        String studentId = studentIdField.getText().trim();
        String email = emailField.getText().trim();
        String yearLevel = (String) yearLevelComboBox.getSelectedItem();
        String branch = (String) branchComboBox.getSelectedItem();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (e.getSource() == backButton) {
            dispose();
            new indexFrame();
            return;
        }

        if (lastName.isEmpty() || firstName.isEmpty() || studentId.isEmpty() || email.isEmpty() ||
                yearLevel == null || branch == null || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
            return;
        }

        if (!studentId.matches("\\d{7}")) {
            JOptionPane.showMessageDialog(this, "Invalid Student ID. It must be 7 digits.");
            return;
        }

        if (!email.matches("^[^@]+@tip\\.edu\\.ph$")) {
            JOptionPane.showMessageDialog(this, "Invalid email. It must be a valid @tip.edu.ph email.");
            return;
        }

        if (password.length() < 8 || password.length() > 16) {
            JOptionPane.showMessageDialog(this, "Password must be between 8 and 16 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        try {
            Connection conn = DBConnector.getConnection();
            String checkIdQuery = "SELECT COUNT(*) FROM students WHERE student_id = ?";
            PreparedStatement checkIdStmt = conn.prepareStatement(checkIdQuery);
            checkIdStmt.setString(1, studentId);
            ResultSet rsId = checkIdStmt.executeQuery();
            rsId.next();
            if (rsId.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This Student ID is already registered.");
                return;
            }

            String checkEmailQuery = "SELECT COUNT(*) FROM students WHERE student_email = ?";
            PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailQuery);
            checkEmailStmt.setString(1, email);
            ResultSet rsEmail = checkEmailStmt.executeQuery();
            rsEmail.next();
            if (rsEmail.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This email is already registered.");
                return;
            }

            String sql = "INSERT INTO students (last_name, first_name, middle_name, student_id, student_email, year_level, tip_branch, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, middleName);
            pstmt.setString(4, studentId);
            pstmt.setString(5, email);
            pstmt.setString(6, yearLevel);
            pstmt.setString(7, branch);
            pstmt.setString(8, password);
            pstmt.executeUpdate();

            conn.close();
            JOptionPane.showMessageDialog(this, "Registration successful!");

            dispose();
            new loginPage();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registration failed.");
        }
    }

}

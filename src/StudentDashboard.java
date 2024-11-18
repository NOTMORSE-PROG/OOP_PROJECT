import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentDashboard extends JFrame implements ActionListener {
    private final JButton notificationsButton, sellItemsButton, buyItemsButton, checkSellingButton, profileSettingsButton, logoutButton;
    private final JLabel userNameLabel;

    public StudentDashboard(String userEmail) {
        setTitle("Student Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("STUDENT BUSINESS MANAGEMENT SYSTEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 50));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        String userFullName = fetchUserName(userEmail);
        userNameLabel = new JLabel("Hello, " + userFullName, JLabel.CENTER);
        userNameLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        userNameLabel.setForeground(Color.WHITE);
        userNameLabel.setBounds(650, 250, 600, 40);
        add(userNameLabel);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(Color.decode("#0F149a"));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        notificationsButton = createButton("Notifications");
        buttonPanel.add(notificationsButton, gbc);

        gbc.gridy = 1;
        sellItemsButton = createButton("Sell Items");
        buttonPanel.add(sellItemsButton, gbc);

        gbc.gridy = 2;
        buyItemsButton = createButton("Buy Items");
        buttonPanel.add(buyItemsButton, gbc);

        gbc.gridy = 3;
        checkSellingButton = createButton("Check Your Selling Items");
        buttonPanel.add(checkSellingButton, gbc);

        gbc.gridy = 4;
        profileSettingsButton = createButton("Profile Settings");
        buttonPanel.add(profileSettingsButton, gbc);

        add(buttonPanel, BorderLayout.CENTER);

        // Logout Button
        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 30));
        logoutButton.setPreferredSize(new Dimension(200, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(Color.RED);
        logoutButton.setOpaque(true);
        logoutButton.setBorderPainted(false);
        logoutButton.addActionListener(this);

        JPanel logoutPanel = new JPanel();
        logoutPanel.setBackground(Color.decode("#0F149a"));
        logoutPanel.add(logoutButton);
        add(logoutPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 35));
        button.setPreferredSize(new Dimension(500, 60));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.decode("#fd9b4d"));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == notificationsButton) {
            JOptionPane.showMessageDialog(this, "Notifications clicked.");
        } else if (e.getSource() == sellItemsButton) {
            JOptionPane.showMessageDialog(this, "Sell Items clicked.");
        } else if (e.getSource() == buyItemsButton) {
            JOptionPane.showMessageDialog(this, "Buy Items clicked.");
        } else if (e.getSource() == checkSellingButton) {
            JOptionPane.showMessageDialog(this, "Check Your Selling Items clicked.");
        } else if (e.getSource() == profileSettingsButton) {
            JOptionPane.showMessageDialog(this, "Profile Settings clicked.");
        } else if (e.getSource() == logoutButton) {
            JOptionPane.showMessageDialog(this, "Logging out...");
            this.dispose();
            new loginPage();
        }
    }

    private String fetchUserName(String userEmail) {
        String fullName = "";
        try {
            Connection conn = DBConnector.getConnection();
            String sql = "SELECT first_name, middle_name, last_name FROM students WHERE student_email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String middleName = rs.getString("middle_name");
                String lastName = rs.getString("last_name");

                String middleInitial = (middleName != null && !middleName.isEmpty())
                        ? middleName.substring(0, 1).toUpperCase() + "."
                        : "";
                fullName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1) + " " +
                        middleInitial + " " +
                        lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
            }
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return fullName;
    }
}
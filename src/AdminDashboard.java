import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminDashboard extends JFrame implements ActionListener {
    private final JButton reportsButton;
    private final JButton studentManagementButton;
    private final JButton logoutButton;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 20, 30, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("WELCOME ADMIN", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        add(titleLabel, gbc);

        reportsButton = new JButton("Reports");
        styleButton(reportsButton, "#fd9b4d");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(reportsButton, gbc);

        studentManagementButton = new JButton("Student Management");
        styleButton(studentManagementButton, "#fd9b4d");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(studentManagementButton, gbc);

        logoutButton = new JButton("Logout");
        styleButton(logoutButton, "#FF0000");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(logoutButton, gbc);

        reportsButton.addActionListener(this);
        studentManagementButton.addActionListener(this);
        logoutButton.addActionListener(this);

        setVisible(true);
    }

    private void styleButton(JButton button, String color) {
        button.setFont(new Font("Arial", Font.BOLD, 36));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.decode(color));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(400, 80));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == reportsButton) {
            this.dispose();
            new Reports();
        } else if (e.getSource() == studentManagementButton) {
            this.dispose();
            new StudentManagement();
        } else if (e.getSource() == logoutButton) {
            new loginPage();
            dispose();
        }
    }

}

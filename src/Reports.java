import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Reports extends JFrame implements ActionListener {
    private final JPanel notificationPanel;
    private final JButton backButton;

    public Reports() {
        setTitle("Admin Notifications");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JLabel titleLabel = new JLabel("User Reports", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setPreferredSize(new Dimension(getWidth(), 80));
        add(titleLabel, BorderLayout.NORTH);

        notificationPanel = new JPanel(new GridBagLayout());
        notificationPanel.setBackground(Color.decode("#0F149a"));
        notificationPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(notificationPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.decode("#0F149a"));
        add(scrollPane, BorderLayout.CENTER);

        backButton = new JButton("Go Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 36));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(Color.decode("#fd9b4d"));
        backButton.setFocusPainted(false);
        backButton.addActionListener(this);
        add(backButton, BorderLayout.SOUTH);

        loadReports();
        setVisible(true);
    }

    private void loadReports() {
        notificationPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try (Connection conn = DBConnector.getConnection()) {
            String sql = """
                        SELECT r.id AS report_id,
                               reported_user.student_email AS reported_email,
                               reported_user.student_id AS reported_student_id,
                               CONCAT(reported_user.last_name, ', ', reported_user.first_name, ' ', IFNULL(reported_user.middle_name, '')) AS reported_full_name,
                               reported_user.year_level AS reported_year_level,
                               reported_user.tip_branch AS reported_branch,
                               r.comment,
                               r.created_at,
                               reporter_user.student_email AS reporter_email
                        FROM reports r
                        JOIN students reported_user ON r.reported_user_id = reported_user.id
                        JOIN students reporter_user ON r.reporter_user_id = reporter_user.id;
                        """;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            int row = 0;
            while (rs.next()) {
                String reporterEmail = rs.getString("reporter_email");
                String reportedEmail = rs.getString("reported_email");
                String studentId = rs.getString("reported_student_id");
                String fullName = rs.getString("reported_full_name");
                String yearLevel = rs.getString("reported_year_level");
                String tipBranch = rs.getString("reported_branch");
                String comment = rs.getString("comment");
                String reportTime = rs.getString("created_at");

                String notificationText = String.format(
                        "<html>Reporter Email: %s<br>Reported Email: %s<br>Student ID: %s<br>Full Name: %s<br>Year Level: %s<br>TIP Branch: %s<br>Comment: %s<br>Reported on: %s</html>",
                        reporterEmail, reportedEmail, studentId, fullName, yearLevel, tipBranch, breakIntoLines(comment), reportTime
                );

                if (row > 0) {
                    JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
                    separator.setForeground(Color.WHITE);
                    gbc.gridx = 0;
                    gbc.gridy = row * 2 - 1;
                    gbc.gridwidth = 3;
                    notificationPanel.add(separator, gbc);
                }

                gbc.gridx = 0;
                gbc.gridy = row * 2;
                gbc.gridwidth = 2;

                JLabel notificationLabel = new JLabel(notificationText);
                notificationLabel.setFont(new Font("Arial", Font.PLAIN, 32));
                notificationLabel.setForeground(Color.WHITE);
                notificationPanel.add(notificationLabel, gbc);

                JButton resolvedButton = new JButton("Resolved");
                resolvedButton.setFont(new Font("Arial", Font.BOLD, 28));
                resolvedButton.setForeground(Color.WHITE);
                resolvedButton.setBackground(Color.decode("#fd9b4d"));
                resolvedButton.setFocusPainted(false);
                gbc.gridx = 2;
                gbc.gridwidth = 1;

                int reportId = rs.getInt("report_id");
                resolvedButton.addActionListener(_ -> resolveReport(reportId));
                notificationPanel.add(resolvedButton, gbc);

                row++;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading reports: " + e.getMessage());
        }

        revalidate();
        repaint();
    }

    private String breakIntoLines(String text) {
        StringBuilder formatted = new StringBuilder();
        int lineLength = 0;

        for (String word : text.split(" ")) {
            if (lineLength + word.length() > 50) {
                formatted.append("<br>");
                lineLength = 0;
            }

            while (word.length() > 50) {
                formatted.append(word, 0, 50).append("<br>");
                word = word.substring(50);
            }

            formatted.append(word).append(" ");
            lineLength += word.length() + 1;
        }

        return formatted.toString().trim();
    }

    private void resolveReport(int reportId) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to mark this report as resolved?",
                "Confirm Resolution",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnector.getConnection()) {
                String sql = "DELETE FROM reports WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, reportId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Report resolved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    notificationPanel.removeAll();
                    loadReports();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to resolve the report.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ((e.getSource() == backButton)) {
            dispose();
            new AdminDashboard();
        }
    }

}
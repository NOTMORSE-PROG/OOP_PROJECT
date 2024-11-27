import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Notifications extends JFrame implements ActionListener {
    private final JPanel notificationPanel;
    private final String userEmail;

    public Notifications(String userEmail) {
        this.userEmail = userEmail;
        setTitle("Notifications");
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel titleLabel = new JLabel("Notifications", JLabel.CENTER);
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

        JButton backButton = new JButton("Go Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 36));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(Color.decode("#fd9b4d"));
        backButton.setFocusPainted(false);
        backButton.addActionListener(this);
        add(backButton, BorderLayout.SOUTH);

        loadNotifications();

        setVisible(true);
    }

    private void loadNotifications() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try (Connection conn = DBConnector.getConnection()) {
            String orderSql = """
        SELECT o.id AS order_id, s.student_email AS buyer_email,
               i.item_name, i.quantity AS stock_left,
               o.quantity AS quantity_bought, o.created_at AS order_time,
               od.payment_status, od.pickup_date
        FROM orders o
        JOIN students s ON o.buyer_email = s.student_email
        JOIN items i ON o.item_id = i.id
        LEFT JOIN order_details od ON o.id = od.order_id
        WHERE i.user_email = ?;
        """;

            String stockSql = """
        SELECT id, item_name, quantity
        FROM items
        WHERE user_email = ? AND quantity = 0;
        """;

            int row = 0;

            PreparedStatement pstmt = conn.prepareStatement(orderSql);
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String buyerEmail = rs.getString("buyer_email");
                String itemName = rs.getString("item_name");
                int stockLeft = rs.getInt("stock_left");
                int quantityBought = rs.getInt("quantity_bought");
                String orderTime = rs.getString("order_time");
                String paymentStatus = rs.getString("payment_status");
                String pickupDate = rs.getString("pickup_date");

                String notificationText = String.format(
                        "Buyer Email: %s\nBought %d of your item '%s'.\nStock Left: %d\nOrdered on: %s\nPayment Status: %s\nPickup Date: %s\nPlease email the buyer for further contact.",
                        buyerEmail, quantityBought, itemName, stockLeft, orderTime, paymentStatus, pickupDate
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

                JLabel notificationLabel = new JLabel("<html>" + notificationText.replace("\n", "<br>") + "</html>");
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
                resolvedButton.putClientProperty("orderId", orderId);
                resolvedButton.putClientProperty("itemName", itemName);
                resolvedButton.addActionListener(this);
                notificationPanel.add(resolvedButton, gbc);

                row++;
            }

            if (row > 0) {
                JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
                separator.setForeground(Color.WHITE);
                gbc.gridx = 0;
                gbc.gridy = row * 2 - 1;
                gbc.gridwidth = 3;
                notificationPanel.add(separator, gbc);
            }

            pstmt = conn.prepareStatement(stockSql);
            pstmt.setString(1, userEmail);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int itemId = rs.getInt("id");
                String itemName = rs.getString("item_name");

                String stockNotificationText = String.format(
                        "Item '%s' is currently OUT OF STOCK!\nPlease restock to continue selling.",
                        itemName
                );

                gbc.gridx = 0;
                gbc.gridy = row * 2;
                gbc.gridwidth = 2;

                JLabel stockNotificationLabel = new JLabel("<html>" + stockNotificationText.replace("\n", "<br>") + "</html>");
                stockNotificationLabel.setFont(new Font("Arial", Font.PLAIN, 32));
                stockNotificationLabel.setForeground(Color.RED);
                notificationPanel.add(stockNotificationLabel, gbc);

                JButton restockButton = new JButton("Restock");
                restockButton.setFont(new Font("Arial", Font.BOLD, 28));
                restockButton.setForeground(Color.WHITE);
                restockButton.setBackground(Color.RED);
                restockButton.setFocusPainted(false);
                gbc.gridx = 2;
                gbc.gridwidth = 1;
                restockButton.putClientProperty("itemId", itemId);
                restockButton.putClientProperty("itemName", itemName);
                restockButton.addActionListener(_ -> {
                    this.dispose();
                    new student_Items(userEmail);
                });

                notificationPanel.add(restockButton, gbc);

                row++;
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + e.getMessage());
        }

        revalidate();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton sourceButton = (JButton) e.getSource();
        if (sourceButton.getText().equals("Go Back")) {
            this.dispose();
            new StudentDashboard(userEmail);
        } else {
            int orderId = (int) sourceButton.getClientProperty("orderId");
            String itemName = (String) sourceButton.getClientProperty("itemName");
            resolveNotification(orderId, itemName);
        }
    }

    private void resolveNotification(int orderId, String itemName) {
        try (Connection conn = DBConnector.getConnection()) {
            String deleteDetailsSql = "DELETE FROM order_details WHERE order_id = ?";
            try (PreparedStatement pstmtDetails = conn.prepareStatement(deleteDetailsSql)) {
                pstmtDetails.setInt(1, orderId);
                pstmtDetails.executeUpdate();
            }

            String deleteOrderSql = "DELETE FROM orders WHERE id = ?";
            try (PreparedStatement pstmtOrder = conn.prepareStatement(deleteOrderSql)) {
                pstmtOrder.setInt(1, orderId);

                int rowsAffected = pstmtOrder.executeUpdate();
                if (rowsAffected > 0) {
                    for (Component comp : notificationPanel.getComponents()) {
                        if (comp instanceof JButton resolvedButton) {
                            if (resolvedButton.getClientProperty("orderId") != null
                                && (int) resolvedButton.getClientProperty("orderId") == orderId) {
                                Component label = notificationPanel.getComponent(notificationPanel.getComponentZOrder(resolvedButton) - 1);
                                notificationPanel.remove(label);
                                notificationPanel.remove(resolvedButton);
                                break;
                            }
                        }
                    }
                    notificationPanel.revalidate();
                    notificationPanel.repaint();
                    JOptionPane.showMessageDialog(this, "Notification for item '" + itemName + "' resolved!");
                } else {
                    JOptionPane.showMessageDialog(this, "No matching order found to resolve. It may have been already handled.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error resolving notification: " + e.getMessage());
        }
    }
}
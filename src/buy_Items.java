import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import com.toedter.calendar.JDateChooser;
import java.sql.Date;

public class buy_Items extends JFrame implements ActionListener {
    private final JComboBox<String> campusFilter;
    private final JPanel itemPanel;
    private final String userEmail;
    private final JButton goBack;

    public buy_Items(String userEmail) {
        this.userEmail = userEmail;
        setTitle("Buy Items");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setBackground(Color.decode("#0F149a"));
        getContentPane().setBackground(Color.decode("#0F149a"));
        setLayout(new BorderLayout());

        goBack = new JButton("Go Back");
        goBack.setBounds(20, 10, 150, 50);
        goBack.setFont(new Font("Arial", Font.BOLD, 24));
        goBack.setBackground(Color.decode("#fd9b4d"));
        goBack.setForeground(Color.WHITE);
        goBack.addActionListener(this);
        add(goBack);

        JLabel titleLabel = new JLabel("Buy Items");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 60));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        filterPanel.setBackground(Color.decode("#0F149a"));

        JLabel filterLabel = new JLabel("Filter by Campus:");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 36));
        filterLabel.setForeground(Color.WHITE);
        filterPanel.add(filterLabel);

        campusFilter = new JComboBox<>(new String[]{"All", "TIP Manila", "TIP QC"});
        campusFilter.setFont(new Font("Arial", Font.PLAIN, 28));
        campusFilter.addActionListener(this);
        filterPanel.add(campusFilter);

        add(filterPanel, BorderLayout.SOUTH);

        itemPanel = new JPanel();
        itemPanel.setLayout(new GridBagLayout());
        itemPanel.setBackground(Color.decode("#0F149a"));

        JScrollPane scrollPane = new JScrollPane(itemPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        loadItems(null);

        setVisible(true);
    }

    private void loadItems(String campusFilter) {
        itemPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int column = 0;
        int row = 0;

        try {
            Connection conn = DBConnector.getConnection();
            String sql = "SELECT items.*, students.first_name, students.last_name FROM items JOIN students ON items.user_email = students.student_email";
            if (campusFilter != null && !campusFilter.equals("All")) {
                sql += " WHERE campus = ?";
            }

            PreparedStatement pstmt = conn.prepareStatement(sql);
            if (campusFilter != null && !campusFilter.equals("All")) {
                pstmt.setString(1, campusFilter);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                double cost = rs.getDouble("cost");
                int quantity = rs.getInt("quantity");
                String imagePath = rs.getString("image_path");
                String campus = rs.getString("campus");
                String sellerName = rs.getString("first_name") + " " + rs.getString("last_name");
                JPanel itemContainer = createItemContainer(itemName, cost, quantity, imagePath, rs.getInt("id"), campus, sellerName);

                gbc.gridx = column;
                gbc.gridy = row;

                itemPanel.add(itemContainer, gbc);

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        }

        itemPanel.revalidate();
        itemPanel.repaint();
    }

    private JPanel createItemContainer(String itemName, double cost, int quantity, String imagePath, int itemId, String campus, String sellerName) {
        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        container.setBackground(Color.decode("#0F149a"));
        container.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel campusLabel = new JLabel("Campus: " + campus);
        campusLabel.setFont(new Font("Arial", Font.BOLD, 28));
        campusLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        container.add(campusLabel, gbc);

        JLabel nameLabel = new JLabel("Item: " + itemName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        nameLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        container.add(nameLabel, gbc);

        JLabel costLabel = new JLabel("Cost: PHP " + cost);
        costLabel.setFont(new Font("Arial", Font.BOLD, 28));
        costLabel.setForeground(Color.WHITE);
        gbc.gridy = 2;
        container.add(costLabel, gbc);

        JLabel quantityLabel = new JLabel("Available: " + quantity);
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 28));
        quantityLabel.setForeground(Color.WHITE);
        gbc.gridy = 3;
        container.add(quantityLabel, gbc);

        JLabel sellerLabel = new JLabel("Seller: " + sellerName);
        sellerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        sellerLabel.setForeground(Color.WHITE);
        gbc.gridy = 4;
        container.add(sellerLabel, gbc);

        if (imagePath != null) {
            JLabel imageLabel = new JLabel(new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH)));
            gbc.gridy = 5;
            container.add(imageLabel, gbc);
        }

        JTextField buyQuantityField = new JTextField("Enter quantity", 10);
        buyQuantityField.setFont(new Font("Arial", Font.PLAIN, 24));
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        container.add(buyQuantityField, gbc);

        JButton buyButton = createButton("Buy Item", itemId, buyQuantityField);
        gbc.gridx = 1;
        container.add(buyButton, gbc);

        JButton reportButton = createButton("Report User", itemId, null);
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        container.add(reportButton, gbc);

        if (quantity <= 0) {
            quantityLabel.setText("Out of Stock");
            quantityLabel.setForeground(Color.RED);
            buyButton.setText("Out of Stock");
            buyButton.setEnabled(false);
            buyQuantityField.setEnabled(false);
        } else {
            quantityLabel.setText("Available: " + quantity);
        }

        return container;
    }

    private JButton createButton(String text, int itemId, JTextField quantityField) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBackground(Color.decode("#fd9b4d"));
        button.setForeground(Color.WHITE);
        button.addActionListener(_ -> {
            if (text.equals("Buy Item")) {
                buyItem(itemId, quantityField);
            } else if (text.equals("Report User")) {
                reportUser(userEmail);
            }
        });
        return button;
    }

    private void buyItem(int itemId, JTextField quantityField) {
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.");
                return;
            }

            Connection conn = DBConnector.getConnection();
            try {
                String checkStockSql = "SELECT quantity FROM items WHERE id = ?";
                PreparedStatement checkStockStmt = conn.prepareStatement(checkStockSql);
                checkStockStmt.setInt(1, itemId);
                ResultSet stockResult = checkStockStmt.executeQuery();

                if (!stockResult.next()) {
                    JOptionPane.showMessageDialog(this, "Item not found.");
                    conn.close();
                    return;
                }

                int availableStock = stockResult.getInt("quantity");

                if (availableStock < quantity) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Not enough stock available. Requested: " + quantity + ", Available: " + availableStock,
                            "Stock Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    conn.close();
                    return;
                }
            } finally {
                conn.close();
            }

            LocalDate selectedPickupDate = showCalendarDialog();
            if (selectedPickupDate == null) {
                return;
            }

            String[] paymentOptions = {"Cash", "E-Wallet"};
            int paymentChoice = JOptionPane.showOptionDialog(
                    this,
                    "Select Payment Method",
                    "Payment Method",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    paymentOptions,
                    paymentOptions[0]
            );

            if (paymentChoice == -1) {
                return;
            }

            conn = DBConnector.getConnection();
            conn.setAutoCommit(false);

            try {
                String checkStockSql = "SELECT cost, user_email FROM items WHERE id = ?";
                PreparedStatement checkStockStmt = conn.prepareStatement(checkStockSql);
                checkStockStmt.setInt(1, itemId);
                ResultSet stockResult = checkStockStmt.executeQuery();
                stockResult.next();

                double itemCost = stockResult.getDouble("cost");
                String sellerEmail = stockResult.getString("user_email");

                String insertOrderSql = "INSERT INTO orders (item_id, buyer_email, quantity, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
                PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                insertOrderStmt.setInt(1, itemId);
                insertOrderStmt.setString(2, userEmail);
                insertOrderStmt.setInt(3, quantity);
                insertOrderStmt.executeUpdate();

                ResultSet generatedKeys = insertOrderStmt.getGeneratedKeys();
                int orderId = -1;
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                }

                boolean paymentSuccessful;

                if (paymentChoice == 0) {
                    paymentSuccessful = handleCashPayment(conn, orderId, selectedPickupDate);
                } else {
                    paymentSuccessful = handleEWalletPayment(conn, orderId, selectedPickupDate, itemCost, sellerEmail, quantity);
                }

                if (paymentSuccessful) {
                    String updateStockSql = "UPDATE items SET quantity = quantity - ? WHERE id = ?";
                    PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSql);
                    updateStockStmt.setInt(1, quantity);
                    updateStockStmt.setInt(2, itemId);
                    updateStockStmt.executeUpdate();

                    conn.commit();
                    loadItems((String) campusFilter.getSelectedItem());
                    JOptionPane.showMessageDialog(this, "Order placed successfully!");
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Payment was not successful. Order canceled.");
                }

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error processing order: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please input a valid number.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }


    private LocalDate showCalendarDialog() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setMinSelectableDate(new java.sql.Date(System.currentTimeMillis()));
        dateChooser.setMaxSelectableDate(new java.sql.Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)));

        int result = JOptionPane.showConfirmDialog(
                this,
                dateChooser,
                "Select Pickup Date",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            java.util.Date selectedDate = dateChooser.getDate();

            if (selectedDate != null) {
                return selectedDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
        }
        return null;
    }

    private boolean handleCashPayment(Connection conn, int orderId, LocalDate pickupDate) throws SQLException {
        insertOrderDetails(conn, orderId, pickupDate, "Cash", "Unpaid", null, null);
        JOptionPane.showMessageDialog(this, "Order placed successfully (Unpaid)!");
        return true;
    }

    private boolean handleEWalletPayment(Connection conn, int orderId, LocalDate pickupDate,
                                         double itemCost, String sellerEmail, int quantity) throws SQLException {
        String[] ewalletProviders = {"GCash", "Maya", "PayPal"};
        String selectedProvider = (String) JOptionPane.showInputDialog(
                this,
                "Select E-Wallet Provider",
                "E-Wallet Payment",
                JOptionPane.QUESTION_MESSAGE,
                null,
                ewalletProviders,
                ewalletProviders[0]
        );

        if (selectedProvider == null) {
            return false;
        }

        String ewalletNumber = "";
        boolean validPhoneNumber = false;

        while (!validPhoneNumber) {
            ewalletNumber = JOptionPane.showInputDialog(this, "Enter " + selectedProvider + " Number:");

            if (ewalletNumber == null || ewalletNumber.trim().isEmpty()) {
                return false;
            }

            String phoneRegex = "^09\\d{9}$";

            if (!ewalletNumber.matches(phoneRegex)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid 11-digit number starting with 09.");
            } else {
                validPhoneNumber = true;
            }
        }

        String billMessage = String.format(
                """
                        Bill Details:
                        Item Cost: PHP %.2f
                        Quantity: %d
                        Total: PHP %.2f
                        Seller Email: %s
                        E-Wallet: %s (%s)""",
                itemCost, quantity, itemCost * quantity, sellerEmail,
                selectedProvider, ewalletNumber
        );

        int payNowResult = JOptionPane.showConfirmDialog(
                this,
                billMessage,
                "Confirm Payment",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (payNowResult == JOptionPane.OK_OPTION) {
            insertOrderDetails(
                    conn,
                    orderId,
                    pickupDate,
                    "E-Wallet",
                    "Paid",
                    selectedProvider,
                    ewalletNumber
            );

            JOptionPane.showMessageDialog(this, "Payment Successful!");
            return true;
        } else {
            insertOrderDetails(
                    conn,
                    orderId,
                    pickupDate,
                    "E-Wallet",
                    "Unpaid",
                    selectedProvider,
                    ewalletNumber
            );

            JOptionPane.showMessageDialog(this, "Payment Cancelled. Order remains unpaid.");
            return false;
        }
    }

    private void insertOrderDetails(Connection conn, int orderId, LocalDate pickupDate,
                                    String paymentMethod, String paymentStatus,
                                    String ewalletProvider, String ewalletNumber) throws SQLException {
        String insertDetailsSql = "INSERT INTO order_details (order_id, pickup_date, payment_method, payment_status, e_wallet_provider, e_wallet_number) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(insertDetailsSql);
        pstmt.setInt(1, orderId);
        pstmt.setDate(2, Date.valueOf(pickupDate));
        pstmt.setString(3, paymentMethod);
        pstmt.setString(4, paymentStatus);
        pstmt.setString(5, ewalletProvider);
        pstmt.setString(6, ewalletNumber);
        pstmt.executeUpdate();
    }

    private void reportUser(String reportedUserEmail) {
        JTextArea commentArea = new JTextArea(10, 30);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setDocument(new LimitedDocument(500));

        JScrollPane scrollPane = new JScrollPane(
                commentArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Enter your comment about the reported user (max 500 characters):",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String comment = commentArea.getText().trim();
            if (!comment.isEmpty()) {
                try (Connection conn = DBConnector.getConnection()) {
                    String fetchReporterIdSql = "SELECT id FROM students WHERE student_email = ?";
                    PreparedStatement pstmt = conn.prepareStatement(fetchReporterIdSql);
                    pstmt.setString(1, userEmail);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        int reporterUserId = rs.getInt("id");
                        String fetchReportedIdSql = "SELECT id FROM students WHERE student_email = ?";
                        pstmt = conn.prepareStatement(fetchReportedIdSql);
                        pstmt.setString(1, reportedUserEmail);
                        rs = pstmt.executeQuery();

                        if (rs.next()) {
                            int reportedUserId = rs.getInt("id");
                            String insertReportSql = "INSERT INTO reports (reported_user_id, reporter_user_id, comment) VALUES (?, ?, ?)";
                            pstmt = conn.prepareStatement(insertReportSql);
                            pstmt.setInt(1, reportedUserId);
                            pstmt.setInt(2, reporterUserId);
                            pstmt.setString(3, comment);

                            int rowsAffected = pstmt.executeUpdate();
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(this, "Report submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(this, "Failed to submit the report.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Reported user not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Reporter not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Comment cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    static class LimitedDocument extends PlainDocument {
        private final int limit;

        public LimitedDocument(int limit) {
            this.limit = limit;
        }

        @Override
        public void insertString(int offset, String str, javax.swing.text.AttributeSet attr) throws BadLocationException {
            if (str == null) return;
            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == campusFilter) {
            loadItems((String) campusFilter.getSelectedItem());
        } else if (e.getSource() == goBack) {
            this.dispose();
            new StudentDashboard(getUserEmail());
        }
    }

    public String getUserEmail() {
        return userEmail;
    }
}
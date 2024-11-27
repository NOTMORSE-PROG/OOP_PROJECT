import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class student_Items extends JFrame implements ActionListener {
    private final JPanel itemPanel;
    private final String userEmail;

    public student_Items(String userEmail) {
        this.userEmail = userEmail;
        setTitle("Your Selling Items");
        setResizable(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F149a"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel titleLabel = new JLabel("YOUR SELLING ITEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setPreferredSize(new Dimension(getWidth(), 80));
        add(titleLabel, BorderLayout.NORTH);

        itemPanel = new JPanel(new GridBagLayout());
        itemPanel.setBackground(Color.decode("#0F149a"));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(itemPanel);
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
        backButton.addActionListener(_ -> {
            this.dispose();
            new StudentDashboard(userEmail);
        });
        add(backButton, BorderLayout.SOUTH);

        loadItems();
        setVisible(true);
    }

    private void loadItems() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try (Connection conn = DBConnector.getConnection()) {
            String sql = """
            SELECT id, item_name, cost, campus, image_path, quantity
            FROM items
            WHERE user_email = ?;
            """;

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();

            int row = 0;
            while (rs.next()) {
                int itemId = rs.getInt("id");
                String itemName = rs.getString("item_name");
                double cost = rs.getDouble("cost");
                String campus = rs.getString("campus");
                String imagePath = rs.getString("image_path");
                int quantity = rs.getInt("quantity");

                JLabel itemLabel = new JLabel(String.format("<html>Item Name: %s<br>Cost: %.2f<br>Campus: %s<br>Quantity: %d</html>", itemName, cost, campus, quantity));
                itemLabel.setFont(new Font("Arial", Font.PLAIN, 28));
                itemLabel.setForeground(Color.WHITE);
                gbc.gridx = 1;
                gbc.gridy = row;
                itemPanel.add(itemLabel, gbc);

                JLabel imageLabel = new JLabel();
                imageLabel.setPreferredSize(new Dimension(100, 100));
                if (imagePath != null && !imagePath.isEmpty()) {
                    imageLabel.setIcon(new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
                } else {
                    imageLabel.setText("No Image");
                    imageLabel.setForeground(Color.WHITE);
                }
                gbc.gridx = 0;
                itemPanel.add(imageLabel, gbc);

                JButton editButton = new JButton("Edit Item");
                editButton.setFont(new Font("Arial", Font.BOLD, 28));
                editButton.setForeground(Color.WHITE);
                editButton.setBackground(Color.decode("#fd9b4d"));
                editButton.putClientProperty("itemId", itemId);
                editButton.addActionListener(this);
                gbc.gridx = 2;
                itemPanel.add(editButton, gbc);

                JButton deleteButton = new JButton("Delete Item");
                deleteButton.setFont(new Font("Arial", Font.BOLD, 28));
                deleteButton.setForeground(Color.WHITE);
                deleteButton.setBackground(Color.decode("#fd9b4d"));
                deleteButton.putClientProperty("itemId", itemId);
                deleteButton.addActionListener(this);
                gbc.gridx = 3;
                itemPanel.add(deleteButton, gbc);

                row++;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage());
        }

        itemPanel.revalidate();
        itemPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton sourceButton = (JButton) e.getSource();
        int itemId = (int) sourceButton.getClientProperty("itemId");
        if (sourceButton.getText().equals("Edit Item")) {
            editItem(itemId);
        } else if (sourceButton.getText().equals("Delete Item")) {
            deleteItem(itemId);
        }
    }

    private void editItem(int itemId) {
        JTextField itemNameField = new JTextField();
        JTextField costField = new JTextField();
        JTextField quantityField = new JTextField();
        JComboBox<String> campusComboBox = new JComboBox<>(new String[]{"TIP Manila", "TIP QC"});
        JButton imageButton = new JButton("Choose Image");
        JLabel imagePathLabel = new JLabel("No image selected");

        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT item_name, cost, campus, image_path, quantity FROM items WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                itemNameField.setText(rs.getString("item_name"));
                costField.setText(String.valueOf(rs.getDouble("cost")));
                quantityField.setText(String.valueOf(rs.getInt("quantity")));
                campusComboBox.setSelectedItem(rs.getString("campus"));
                imagePathLabel.setText(rs.getString("image_path"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching item data: " + e.getMessage());
        }

        imageButton.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            int choice = fileChooser.showOpenDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                File userFolder = new File("sellingItems/" + userEmail);
                if (!userFolder.exists()) {
                    userFolder.mkdirs();
                }

                String newImagePath = userFolder.getAbsolutePath() + "/" + selectedFile.getName();
                File newFile = new File(newImagePath);
                if (newFile.exists()) {
                    JOptionPane.showMessageDialog(this, "An image with the same name already exists. Please choose another image.");
                    return;
                }

                try {
                    Files.copy(selectedFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage());
                }

                imagePathLabel.setText(newImagePath);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.add(new JLabel("Item Name:"));
        panel.add(itemNameField);
        panel.add(new JLabel("Cost:"));
        panel.add(costField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Campus:"));
        panel.add(campusComboBox);
        panel.add(imageButton);
        panel.add(new JLabel("Selected Image:"));
        panel.add(imagePathLabel);

        UIManager.put("OptionPane.minimumSize", new Dimension(500, 300));
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 20));

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Item", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String itemName = itemNameField.getText();
            String costText = costField.getText();
            String quantityText = quantityField.getText();
            String campus = (String) campusComboBox.getSelectedItem();
            String imagePath = imagePathLabel.getText();

            double cost;
            try {
                cost = Double.parseDouble(costText);
                if (cost <= 0) {
                    JOptionPane.showMessageDialog(this, "Cost must be a positive number!");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid cost input!");
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
                if (quantity < 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be a non-negative number!");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity input!");
                return;
            }

            try (Connection conn = DBConnector.getConnection()) {
                String sql = "UPDATE items SET item_name = ?, cost = ?, quantity = ?, campus = ?, image_path = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, itemName.isEmpty() ? "Untitled Item" : itemName);
                pstmt.setDouble(2, cost);
                pstmt.setInt(3, quantity);
                pstmt.setString(4, campus);
                pstmt.setString(5, imagePath);
                pstmt.setInt(6, itemId);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Item updated successfully!");
                itemPanel.removeAll();
                loadItems();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating item: " + e.getMessage());
            }
        }
    }

    private void deleteItem(int itemId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnector.getConnection()) {
                String imagePath = getImagePath(itemId, conn);
                if (imagePath != null && !imagePath.isEmpty()) {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }

                String sql = "DELETE FROM items WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, itemId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Item deleted successfully!");
                itemPanel.removeAll();
                loadItems();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Unable to delete item: There are unresolved orders associated with this item.");
            }
        }
    }

    private String getImagePath(int itemId, Connection conn) throws SQLException {
        String imagePath = null;
        String sql = "SELECT image_path FROM items WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, itemId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            imagePath = rs.getString("image_path");
        }
        return imagePath;
    }

}
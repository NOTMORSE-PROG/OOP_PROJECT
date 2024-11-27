import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class sell_Items extends JFrame implements ActionListener {
    private final JTextField itemNameField, costField, quantityField;
    private final JButton uploadButton, submitButton, goBackButton;
    private final JLabel imageLabel;
    private File selectedImageFile;
    private final String userEmail;
    private final JComboBox<String> campusComboBox;

    public sell_Items(String userEmail) {
        this.userEmail = userEmail;
        setTitle("Sell Item");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        setBackground(Color.decode("#0F149a"));
        getContentPane().setBackground(Color.decode("#0F149a"));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Sell Your Item");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 60));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        addLabelAndField("Item Name:", itemNameField = new JTextField(30), gbc, 1);
        addLabelAndField("Cost:", costField = new JTextField(30), gbc, 2);
        addLabelAndField("Quantity Available:", quantityField = new JTextField(30), gbc, 3);

        uploadButton = createButton("Upload Image");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(uploadButton, gbc);

        imageLabel = new JLabel("No image selected");
        imageLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        imageLabel.setForeground(Color.WHITE);
        gbc.gridx = 1;
        add(imageLabel, gbc);

        JLabel campusLabel = new JLabel("Select Campus:");
        campusLabel.setFont(new Font("Arial", Font.BOLD, 36));
        campusLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(campusLabel, gbc);

        String[] campuses = {"TIP Manila", "TIP QC"};
        campusComboBox = new JComboBox<>(campuses);
        campusComboBox.setFont(new Font("Arial", Font.PLAIN, 36));
        gbc.gridx = 1;
        add(campusComboBox, gbc);

        gbc.gridy = 6;
        gbc.gridx = 0;
        submitButton = createButton("Submit Item");
        add(submitButton, gbc);

        gbc.gridx = 1;
        goBackButton = createButton("Go Back");
        add(goBackButton, gbc);

        setVisible(true);
    }

    private void addLabelAndField(String labelText, JTextField field, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 36));
        label.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = row;
        add(label, gbc);

        gbc.gridx = 1;
        field.setFont(new Font("Arial", Font.PLAIN, 36));
        add(field, gbc);
    }

    private JButton createButton(String buttonText) {
        JButton button = new JButton(buttonText);
        button.setFont(new Font("Arial", Font.BOLD, 28));
        button.setBackground(Color.decode("#fd9b4d"));
        button.setForeground(Color.WHITE);
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == uploadButton) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = fileChooser.getSelectedFile();
                imageLabel.setText(selectedImageFile.getName());
            }
        } else if (e.getSource() == submitButton) {
            submitItem();
        } else if (e.getSource() == goBackButton) {
            dispose();
            new StudentDashboard(userEmail);
        }
    }

    private void submitItem() {
        String itemName = itemNameField.getText();
        String costStr = costField.getText();
        String quantityStr = quantityField.getText();
        String selectedCampus = (String) campusComboBox.getSelectedItem();

        try {
            if (itemName.isEmpty() || costStr.isEmpty() || quantityStr.isEmpty() || selectedImageFile == null) {
                throw new IllegalArgumentException("All fields are required, including the image.");
            }

            double cost = Double.parseDouble(costStr);
            if (cost <= 0) {
                throw new IllegalArgumentException("Cost must be greater than zero.");
            }

            int quantity = Integer.parseInt(quantityStr);
            File destinationFile = getFile(quantity);
            java.nio.file.Files.copy(selectedImageFile.toPath(), destinationFile.toPath());
            saveItemToDatabase(itemName, cost, quantity, destinationFile.getPath(), selectedCampus);
            itemNameField.setText("");
            costField.setText("");
            quantityField.setText("");
            imageLabel.setText("No image selected");
            JOptionPane.showMessageDialog(this, "Item submitted successfully!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cost and Quantity must be numeric values.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error uploading item: " + ex.getMessage());
        }
    }

    private File getFile(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        String fileName = selectedImageFile.getName();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".png")) {
            throw new IllegalArgumentException("Only JPG and PNG files are allowed.");
        }

        File userFolder = new File("sellingItems/" + userEmail);
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }

        File destinationFile = new File(userFolder, fileName);
        if (destinationFile.exists()) {
            throw new IllegalArgumentException("An image with the same name already exists. Please rename your image or choose a different one.");
        }

        return destinationFile;
    }

    private void saveItemToDatabase(String itemName, double cost, int quantity, String imagePath, String campus) {
        try {
            Connection conn = DBConnector.getConnection();
            String sql = "INSERT INTO items (user_email, item_name, cost, quantity, image_path, campus) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userEmail);
            pstmt.setString(2, itemName);
            pstmt.setDouble(3, cost);
            pstmt.setInt(4, quantity);
            pstmt.setString(5, imagePath);
            pstmt.setString(6, campus);
            pstmt.executeUpdate();

            conn.close();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

}

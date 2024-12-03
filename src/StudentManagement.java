import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;

public class StudentManagement extends JFrame implements ActionListener {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton goBackButton;
    private Connection conn;

    public StudentManagement() {
        setTitle("Student Management");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Color.decode("#0F149a"));
        initializeComponents();
        connectToDatabase();
        loadStudentData();
        setResizable(false);
    }

    private void initializeComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titleLabel = new JLabel("Student Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        add(titleLabel, gbc);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search Email:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        searchField = new JTextField(40);
        searchField.setFont(new Font("Arial", Font.PLAIN, 18));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchStudents();
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(searchPanel, gbc);

        String[] columns = {"Full Name", "Email", "Student ID", "Year Level", "TIP Branch", "Password", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setFillsViewportHeight(true);
        studentTable.setFont(new Font("Arial", Font.PLAIN, 14));

        studentTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        studentTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(studentTable);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 20, 10, 20);
        add(scrollPane, gbc);

        goBackButton = new JButton("Go Back");
        goBackButton.setFont(new Font("Arial", Font.BOLD, 30));
        goBackButton.setBackground(Color.decode("#fd9b4d"));
        goBackButton.setForeground(Color.WHITE);
        goBackButton.addActionListener(this);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 20, 20, 20);
        add(goBackButton, gbc);

        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            conn = DBConnector.getConnection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }

    private void loadStudentData() {
        tableModel.setRowCount(0);
        try {
            String query = "SELECT * FROM students";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String fullName = rs.getString("first_name") + " " +
                        (rs.getString("middle_name") != null ? rs.getString("middle_name") + " " : "") +
                        rs.getString("last_name");
                String email = rs.getString("student_email");
                String studentId = rs.getString("student_id");
                String yearLevel = rs.getString("year_level");
                String tipBranch = rs.getString("tip_branch");
                String password = rs.getString("password");

                tableModel.addRow(new Object[]{
                        fullName, email, studentId, yearLevel, tipBranch, password, "Remove"
                });
            }

            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage());
        }
    }

    private void searchStudents() {
        String searchText = searchField.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        studentTable.setRowSorter(sorter);

        if (searchText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText, 1));
        }
    }

    private void deleteFolder(File folder) {
        if (folder.exists()) {
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

    private void removeStudent(String email) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove this student?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnector.getConnection()) {
                conn.setAutoCommit(false);

                PreparedStatement deleteReports = conn.prepareStatement(
                        "DELETE FROM reports WHERE reported_user_id IN (SELECT id FROM students WHERE student_email = ?)"
                );
                deleteReports.setString(1, email);
                deleteReports.executeUpdate();
                deleteReports.close();

                PreparedStatement deleteOrders = conn.prepareStatement(
                        "DELETE FROM orders WHERE buyer_email = ?"
                );
                deleteOrders.setString(1, email);
                deleteOrders.executeUpdate();
                deleteOrders.close();

                PreparedStatement deleteItems = conn.prepareStatement(
                        "DELETE FROM items WHERE user_email = ?"
                );
                deleteItems.setString(1, email);
                deleteItems.executeUpdate();
                deleteItems.close();

                PreparedStatement deleteStudent = conn.prepareStatement(
                        "DELETE FROM students WHERE student_email = ?"
                );
                deleteStudent.setString(1, email);
                deleteStudent.executeUpdate();
                deleteStudent.close();

                File userFolder = new File("sellingItems/" + email);
                deleteFolder(userFolder);

                conn.commit();
                loadStudentData();
                JOptionPane.showMessageDialog(this, "Student and associated data removed successfully");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error removing student: " + e.getMessage());
            }
        }
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == goBackButton) {
            this.dispose();
            new AdminDashboard();
        }
    }

    static class ButtonRenderer implements TableCellRenderer {
        private final JButton button;

        public ButtonRenderer() {
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            button.setText("Remove");
            return button;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String email;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            button.addActionListener(_ -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            button.setText("Remove");
            email = (String) table.getValueAt(row, 1);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                removeStudent(email);
            }
            isPushed = false;
            return "Remove";
        }
    }

}

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class FacultyBookSystem {
    private static HashMap<String, ArrayList<Book>> facultyBooks = new HashMap<>();
    private static HashMap<String, String> users = new HashMap<>();
    private static String currentUser = null;
    private static boolean isAdmin = false;

    public static void main(String[] args) {
        // Load existing faculty books
        loadFacultyBooks();

        // Sample users
        users.put("admin", "admin123"); // Admin user
        users.put("faculty1", "password1"); // Faculty user
        users.put("faculty2", "password2"); // Faculty user

        // Create the login frame
        JFrame loginFrame = new JFrame("Login");
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        String[] userTypes = {"Admin", "Faculty"};
        JComboBox<String> userTypeComboBox = new JComboBox<>(userTypes);

        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(500, 450);
        loginFrame.setLayout(null);
        usernameField.setBounds(100, 20, 150, 30);
        passwordField.setBounds(100, 60, 150, 30);
        userTypeComboBox.setBounds(100, 100, 150, 30);
        loginButton.setBounds(100, 140, 100, 30);

        loginFrame.add(new JLabel("Username:")).setBounds(20, 20, 80, 30);
        loginFrame.add(new JLabel("Password:")).setBounds(20, 60, 80, 30);
        loginFrame.add(userTypeComboBox);
        loginFrame.add(usernameField);
        loginFrame.add(passwordField);
        loginFrame.add(loginButton);
        loginFrame.setVisible(true);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                isAdmin = userTypeComboBox.getSelectedItem().equals("Admin");

                // Check login credentials
                if (isAdmin) {
                    if (username.equals("admin") && users.get(username).equals(password)) {
                        currentUser = username;
                        loginFrame.dispose();
                        showMainFrame();
                    } else {
                        JOptionPane.showMessageDialog(loginFrame, "Invalid admin username or password.");
                    }
                } else {
                    if (users.containsKey(username) && users.get(username).equals(password)) {
                        currentUser = username;
                        loginFrame.dispose();
                        showMainFrame();
                    } else {
                        JOptionPane.showMessageDialog(loginFrame, "Invalid faculty username or password.");
                    }
                }
            }
        });
    }

    private static void loadFacultyBooks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("facultyBooks.dat"))) {
            facultyBooks = (HashMap<String, ArrayList<Book>>) ois.readObject();
        } catch (FileNotFoundException e) {
            // File not found, will use empty facultyBooks
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void saveFacultyBooks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("facultyBooks.dat"))) {
            oos.writeObject(facultyBooks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showMainFrame() {
        JFrame frame = new JFrame(isAdmin ? "Admin Dashboard" : "Faculty Dashboard");
        DefaultTableModel model = new DefaultTableModel(new String[]{"Title", "Author", "Year"}, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // Buttons for adding, editing, deleting books
        JButton addButton = new JButton("Add Book");
        JButton editButton = new JButton("Edit Book");
        JButton deleteButton = new JButton("Delete Book");
        JButton logoutButton = new JButton("Logout");

        scrollPane.setBounds(30, 30, 400, 200);
        addButton.setBounds(30, 240, 100, 30);
        editButton.setBounds(150, 240, 100, 30);
        deleteButton.setBounds(270, 240, 100, 30);
        logoutButton.setBounds(390, 240, 100, 30);

        frame.add(scrollPane);
        frame.add(addButton);
        frame.add(editButton);
        frame.add(deleteButton);
        frame.add(logoutButton);

        frame.setSize(500, 320);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        refreshBookTable(model);

        addButton.addActionListener(e -> showAddBookForm(model));
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String title = (String) model.getValueAt(selectedRow, 0);
                String author = (String) model.getValueAt(selectedRow, 1);
                String year = (String) model.getValueAt(selectedRow, 2);
                showEditBookForm(title, author, year, selectedRow, model);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a book to edit.");
            }
        });
        deleteButton.addActionListener(e -> deleteSelectedBook(model, table));
        logoutButton.addActionListener(e -> {
            saveFacultyBooks(); // Save data on logout
            frame.dispose();
            currentUser = null;
            isAdmin = false;
            main(null);
        });
    }

    private static void refreshBookTable(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing data

        if (isAdmin) {
            // Admin can view all books from all faculty members
            for (String faculty : facultyBooks.keySet()) {
                for (Book book : facultyBooks.get(faculty)) {
                    model.addRow(new Object[]{book.title, book.author, book.publicationYear});
                }
            }
        } else {
            // Faculty can view only their own books
            ArrayList<Book> books = facultyBooks.get(currentUser);
            if (books != null) {
                for (Book book : books) {
                    model.addRow(new Object[]{book.title, book.author, book.publicationYear});
                }
            }
        }
    }

    private static void showAddBookForm(DefaultTableModel model) {
        JFrame frame = new JFrame("Add Book");

        JLabel titleLabel = new JLabel("Title:");
        JLabel authorLabel = new JLabel("Author:");
        JLabel yearLabel = new JLabel("Publication Year:");
        JTextField titleField = new JTextField(15);
        JTextField authorField = new JTextField(15);
        JTextField yearField = new JTextField(15);
        JButton saveButton = new JButton("Save");

        titleLabel.setBounds(30, 30, 150, 30);
        titleField.setBounds(200, 30, 200, 30);
        authorLabel.setBounds(30, 80, 150, 30);
        authorField.setBounds(200, 80, 200, 30);
        yearLabel.setBounds(30, 130, 150, 30);
        yearField.setBounds(200, 130, 200, 30);
        saveButton.setBounds(150, 180, 100, 30);

        saveButton.addActionListener(e -> {
            String title = titleField.getText();
            String author = authorField.getText();
            String year = yearField.getText();

            if (!title.isEmpty() && !author.isEmpty() && !year.isEmpty()) {
                Book newBook = new Book(title, author, year);
                if (isAdmin) {
                    // Admin can add a book to any faculty
                    String faculty = (String) JOptionPane.showInputDialog(frame, "Select Faculty:", "Add Book", JOptionPane.PLAIN_MESSAGE, null, users.keySet().toArray(), "faculty1");
                    if (faculty != null) {
                        facultyBooks.computeIfAbsent(faculty, k -> new ArrayList<>()).add(newBook);
                    }
                } else {
                    // Faculty can add to their own collection
                    facultyBooks.computeIfAbsent(currentUser, k -> new ArrayList<>()).add(newBook);
                }
                model.addRow(new Object[]{title, author, year});
                JOptionPane.showMessageDialog(frame, "Book added successfully.");
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
            }
        });

        frame.add(titleLabel);
        frame.add(titleField);
        frame.add(authorLabel);
        frame.add(authorField);
        frame.add(yearLabel);
        frame.add(yearField);
        frame.add(saveButton);

        frame.setSize(450, 300);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    private static void showEditBookForm(String title, String author, String year, int rowIndex, DefaultTableModel model) {
        JFrame frame = new JFrame("Edit Book");

        JLabel titleLabel = new JLabel("Title:");
        JLabel authorLabel = new JLabel("Author:");
        JLabel yearLabel = new JLabel("Publication Year:");
        JTextField titleField = new JTextField(title, 15);
        JTextField authorField = new JTextField(author, 15);
        JTextField yearField = new JTextField(year, 15);
        JButton saveButton = new JButton("Save");

        titleLabel.setBounds(30, 30, 150, 30);
        titleField.setBounds(200, 30, 200, 30);
        authorLabel.setBounds(30, 80, 150, 30);
        authorField.setBounds(200, 80, 200, 30);
        yearLabel.setBounds(30, 130, 150, 30);
        yearField.setBounds(200, 130, 200, 30);
        saveButton.setBounds(150, 180, 100, 30);

        saveButton.addActionListener(e -> {
            String newTitle = titleField.getText();
            String newAuthor = authorField.getText();
            String newYear = yearField.getText();

            if (!newTitle.isEmpty() && !newAuthor.isEmpty() && !newYear.isEmpty()) {
                Book updatedBook = new Book(newTitle, newAuthor, newYear);
                if (isAdmin) {
                    // Admin can update any book
                    model.setValueAt(newTitle, rowIndex, 0);
                    model.setValueAt(newAuthor, rowIndex, 1);
                    model.setValueAt(newYear, rowIndex, 2);
                } else {
                    // Faculty can update only their own books
                    ArrayList<Book> books = facultyBooks.get(currentUser);
                    if (books != null && rowIndex < books.size()) {
                        books.set(rowIndex, updatedBook);
                        model.setValueAt(newTitle, rowIndex, 0);
                        model.setValueAt(newAuthor, rowIndex, 1);
                        model.setValueAt(newYear, rowIndex, 2);
                    }
                }
                JOptionPane.showMessageDialog(frame, "Book updated successfully.");
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields.");
            }
        });

        frame.add(titleLabel);
        frame.add(titleField);
        frame.add(authorLabel);
        frame.add(authorField);
        frame.add(yearLabel);
        frame.add(yearField);
        frame.add(saveButton);

        frame.setSize(450, 300);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    private static void deleteSelectedBook(DefaultTableModel model, JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            if (isAdmin) {
                // Admin can delete any book
                model.removeRow(selectedRow);
            } else {
                // Faculty can delete only their own books
                ArrayList<Book> books = facultyBooks.get(currentUser);
                if (books != null) {
                    books.remove(selectedRow);
                    model.removeRow(selectedRow);
                }
            }
            JOptionPane.showMessageDialog(table, "Book deleted successfully.");
        } else {
            JOptionPane.showMessageDialog(table, "Please select a book to delete.");
        }
    }

    // Inner class to represent Book
    static class Book implements Serializable {
        String title;
        String author;
        String publicationYear;

        Book(String title, String author, String publicationYear) {
            this.title = title;
            this.author = author;
            this.publicationYear = publicationYear;
        }
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.Timer;

public class ToDoApp extends JFrame {
    private DefaultListModel<Task> listModel;
    private JList<Task> taskList;
    private JTextField taskInput, timeInput, dateInput;
    private JButton addButton, editButton, deleteButton;

    private static final String FILE_NAME = "tasks.txt";

    public ToDoApp(String username) {
        setTitle("To-Do List - Welcome " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        loadTasks();

        taskList = new JList<>(listModel);
        taskList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(taskList);

        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 5, 5));

        taskInput = new JTextField("Task description");
        dateInput = new JTextField("dd/MM/yyyy");
        timeInput = new JTextField("hh:mm a");

        addFocusListener(taskInput, "Task description");
        addFocusListener(dateInput, "dd/MM/yyyy");
        addFocusListener(timeInput, "hh:mm a");

        addButton = new JButton("Add Task");
        editButton = new JButton("Edit Task");
        deleteButton = new JButton("Delete Task");

        addButton.addActionListener(e -> addTask());
        editButton.addActionListener(e -> editSelectedTask());
        deleteButton.addActionListener(e -> deleteSelectedTask());

        inputPanel.add(taskInput);
        inputPanel.add(dateInput);
        inputPanel.add(timeInput);
        inputPanel.add(addButton);
        inputPanel.add(editButton);
        inputPanel.add(deleteButton);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks();
            }
        });

        // Start the notification timer
        startNotificationTimer();
    }

    private void addTask() {
        try {
            String description = taskInput.getText();
            String dateText = dateInput.getText();
            String timeText = timeInput.getText();

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            Date dueDate = format.parse(dateText + " " + timeText);

            if (!description.isEmpty()) {
                Task task = new Task(description, dueDate);
                listModel.addElement(task);
                clearInputs();
            } else {
                JOptionPane.showMessageDialog(this, "Task description cannot be empty!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date or time format. Please use 'dd/MM/yyyy' and 'hh:mm AM/PM'.");
        }
    }

    private void editSelectedTask() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask != null) {
            String newDescription = JOptionPane.showInputDialog(this, "Edit Task", selectedTask.getDescription());
            String newDate = JOptionPane.showInputDialog(this, "Edit Date (dd/MM/yyyy)",
                    new SimpleDateFormat("dd/MM/yyyy").format(selectedTask.getDueDate()));
            String newTime = JOptionPane.showInputDialog(this, "Edit Time (hh:mm a)",
                    new SimpleDateFormat("hh:mm a").format(selectedTask.getDueDate()));

            if (newDescription != null && newDate != null && newTime != null) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                    Date newDueDate = format.parse(newDate + " " + newTime);
                    selectedTask.setDescription(newDescription);
                    selectedTask.setDueDate(newDueDate);
                    taskList.repaint();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Invalid date or time format!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.");
        }
    }

    private void deleteSelectedTask() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask != null) {
            listModel.removeElement(selectedTask);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.");
        }
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            for (int i = 0; i < listModel.size(); i++) {
                Task task = listModel.getElementAt(i);
                writer.write(task.getDescription() + ";" + format.format(task.getDueDate()));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String description = parts[0];
                    Date dueDate = format.parse(parts[1]);
                    listModel.addElement(new Task(description, dueDate));
                }
            }
        } catch (FileNotFoundException e) {
            // No tasks to load, file does not exist
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startNotificationTimer() {
        Timer timer = new Timer(60 * 1000, e -> {
            Date now = new Date();
            for (int i = 0; i < listModel.size(); i++) {
                Task task = listModel.getElementAt(i);
                if (task.getDueDate().before(now)) {
                    playSound();
                    JOptionPane.showMessageDialog(this, "Task Overdue: " + task.getDescription());
                }
            }
        });
        timer.start();
    }

    private void playSound() {
        try {
            File soundFile = new File("/Users/vai/Documents/ToDoApp/alarm.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearInputs() {
        taskInput.setText("Task description");
        dateInput.setText("dd/MM/yyyy");
        timeInput.setText("hh:mm a");
    }

    private void addFocusListener(JTextField textField, String placeholder) {
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}

class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    public LoginPage() {
        setTitle("Login");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Background panel
        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBounds(0, 0, 400, 500);
        backgroundPanel.setBackground(new Color(60, 63, 65));
        backgroundPanel.setLayout(null);

        // Title
        JLabel titleLabel = new JLabel("Welcome Back!");
        titleLabel.setBounds(100, 50, 200, 40);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        backgroundPanel.add(titleLabel);

        // Username label and field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setBounds(50, 120, 300, 25);
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        backgroundPanel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(50, 150, 300, 40);
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        backgroundPanel.add(usernameField);

        // Password label and field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(50, 210, 300, 25);
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        backgroundPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(50, 240, 300, 40);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        backgroundPanel.add(passwordField);

        // Login button
        loginButton = new JButton("Login");
        loginButton.setBounds(50, 310, 140, 40);
        styleButton(loginButton);
        backgroundPanel.add(loginButton);

        // Register button
        registerButton = new JButton("Register");
        registerButton.setBounds(210, 310, 140, 40);
        styleButton(registerButton);
        backgroundPanel.add(registerButton);

        // Add action listeners
        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        // Add background panel to frame
        add(backgroundPanel);
        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (authenticateUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Welcome, " + username + "!");
            dispose();
            new ToDoApp(username);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
        }
    }

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username or password cannot be empty.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write(username + ";" + password);
            writer.newLine();
            JOptionPane.showMessageDialog(this, "User registered successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean authenticateUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(new Color(77, 122, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
    }
}

class Task {
    private String description;
    private Date dueDate;

    public Task(String description, Date dueDate) {
        this.description = description;
        this.dueDate = dueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        return description + " (Due: " + format.format(dueDate) + ")";
    }
}

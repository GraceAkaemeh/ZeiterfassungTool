// app/RegisterWindow.java
package app;

import service.UserService;

import javax.swing.*;
import java.awt.*;

public class RegisterWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserService userService;

    public RegisterWindow() {
        super("Register");
        userService = new UserService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> registerAction());
        add(registerButton);

        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> {
            new LoginWindow().setVisible(true);
            dispose();
        });
        add(backButton);
    }

    private void registerAction() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        boolean success = userService.register(username, password);
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful! You can login now.");
            new LoginWindow().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

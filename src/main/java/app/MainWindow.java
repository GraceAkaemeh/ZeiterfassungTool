// app/MainWindow.java
package app;

import model.User;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private User currentUser;

    public MainWindow(User user) {
        super("PKN GmbH");
        this.currentUser = user;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        add(welcomeLabel);

        JButton timeTrackingButton = new JButton("Open Time Tracking");
        timeTrackingButton.addActionListener(e -> new TimeTrackingGUI(currentUser).setVisible(true));
        add(timeTrackingButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            new LoginWindow().setVisible(true);
            dispose();
        });
        add(logoutButton);
    }
}

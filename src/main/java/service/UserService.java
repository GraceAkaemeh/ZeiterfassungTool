package service;

import dao.UserDao;
import model.User;

public class UserService {

    private final UserDao userDao = new UserDao();

    // Login
    public User login(String username, String password) {
        return userDao.findByUsernameAndPassword(username, password);
    }

    // Register
    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Benutzername und Passwort dürfen nicht leer sein!");
            return false;
        }

        // Insert user using DAO
        return userDao.insertUser(username.trim(), password.trim());
    }
}

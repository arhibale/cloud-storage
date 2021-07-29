package com.arhibale.sql;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthService {

    private final MySQLConnection connection = new MySQLConnection();

    public void connectToSQL() {
        connection.connect();
    }

    public void disconnectToSQL() {
        connection.disconnect();
    }

    public String getLoginByLoginPass(String login, String password) {
        connection.addUser(login);
        if (checkingPassword(password)) {
            return connection.getUser().getLogin();
        }
        return null;
    }

    private boolean checkingPassword(String password) {
        try {
            return connection.getUser().getPassword().equals(password);
        } catch (NullPointerException e) {
            //log.error("", e);
            return false;
        }
    }
}

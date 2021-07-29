package com.arhibale.sql;

import com.arhibale.users.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class MySQLConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("", e);
        }
    }

    private Connection mysqlConnection;
    private User user;

    public void addUser(String login) {
        try {
            PreparedStatement preparedStatement = mysqlConnection.prepareStatement("select * from users where login=?");
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (!resultSet.getString("login").isEmpty()) {
                    user = new User(
                            resultSet.getString("login"),
                            resultSet.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            log.error("", e);
        }
    }

    public void connect() {
        try {
            mysqlConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/user_cloud_storage", "root", "root");
        } catch (SQLException e) {
            log.error("", e);
        }
    }

    public void disconnect() {
        try {
            mysqlConnection.close();
        } catch (SQLException e) {
            log.error("", e);
        }
    }

    public User getUser() {
        return user;
    }
}

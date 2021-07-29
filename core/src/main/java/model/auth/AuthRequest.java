package model.auth;

import model.AbstractCommand;
import model.CommandType;

public class AuthRequest implements AbstractCommand {

    private final String login;
    private final String password;

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_REQUEST;
    }
}

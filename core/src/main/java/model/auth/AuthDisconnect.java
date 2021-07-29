package model.auth;

import model.AbstractCommand;
import model.CommandType;

public class AuthDisconnect implements AbstractCommand {

    private final String login;

    public AuthDisconnect(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public CommandType getType() {
        return CommandType.DISCONNECT;
    }
}

package model.auth;

import model.AbstractCommand;
import model.CommandType;

public class AuthResponse implements AbstractCommand {

    private final String authResponse;
    private final String login;

    public AuthResponse(String authResponse, String login) {
        this.authResponse = authResponse;
        this.login = login;
    }

    public String getAuthResponse() {
        return authResponse;
    }

    public String getLogin() {
        return login;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_RESPONSE;
    }
}

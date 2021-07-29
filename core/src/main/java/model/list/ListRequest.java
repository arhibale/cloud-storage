package model.list;

import model.AbstractCommand;
import model.CommandType;

public class ListRequest implements AbstractCommand {

    private final String upDirectory;
    private final String login;

    public ListRequest(String upDirectory, String login) {
        this.upDirectory = upDirectory;
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getUpDirectory() {
        return upDirectory;
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_REQUEST;
    }
}

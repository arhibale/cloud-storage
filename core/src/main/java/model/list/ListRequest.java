package model.list;

import model.AbstractCommand;
import model.CommandType;

public class ListRequest implements AbstractCommand {

    private final String nameDirectory;

    public ListRequest() {
        nameDirectory = "";
    }

    public ListRequest(String nameDirectory) {
        this.nameDirectory = nameDirectory;
    }

    public String getNameDirectory() {
        return nameDirectory;
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_REQUEST;
    }
}

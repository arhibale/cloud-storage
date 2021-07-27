package model.file;

import model.AbstractCommand;
import model.CommandType;

public class CreateNewFolderRequest implements AbstractCommand {

    private final String name;

    public CreateNewFolderRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public CommandType getType() {
        return CommandType.NEW_FOLDER;
    }
}

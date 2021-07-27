package model.file;

import model.AbstractCommand;
import model.CommandType;

public class FileRenameRequest implements AbstractCommand {

    private final String newName;
    private final String oldName;

    public FileRenameRequest(String newName, String oldName) {
        this.newName = newName;
        this.oldName = oldName;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_RENAME;
    }
}
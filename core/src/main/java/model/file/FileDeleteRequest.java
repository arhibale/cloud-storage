package model.file;

import model.AbstractCommand;
import model.CommandType;

public class FileDeleteRequest implements AbstractCommand {

    private final String fileName;

    public FileDeleteRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_DELETE;
    }
}

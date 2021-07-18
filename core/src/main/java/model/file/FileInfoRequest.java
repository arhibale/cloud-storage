package model.file;

import model.AbstractCommand;
import model.CommandType;

public class FileInfoRequest implements AbstractCommand {

    private final String fileName;

    public FileInfoRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_INFO_REQUEST;
    }
}

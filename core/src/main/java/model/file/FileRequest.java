package model.file;

import model.AbstractCommand;
import model.CommandType;

public class FileRequest implements AbstractCommand {

    private final String fileName;
    private final boolean fileInfo;

    public FileRequest(String fileName, boolean fileInfo ) {
        this.fileName = fileName;
        this.fileInfo = fileInfo;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isFileInfo() {
        return fileInfo;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}

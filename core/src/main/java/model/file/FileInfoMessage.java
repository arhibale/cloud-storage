package model.file;

import model.AbstractCommand;
import model.CommandType;

public class FileInfoMessage implements AbstractCommand {

    private final String fileName;
    private final long fileSize;

    public FileInfoMessage(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_INFO_MESSAGE;
    }
}

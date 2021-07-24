package model.file;

import model.AbstractCommand;
import model.CommandType;

public class FileInfo implements AbstractCommand {
    private final String fileName;
    private final long fileLength;

    public FileInfo(String fileName, long fileLength ) {
        this.fileName = fileName;
        this.fileLength = fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_INFO;
    }
}

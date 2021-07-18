package model.file;

import model.AbstractCommand;
import model.CommandType;

import java.io.File;

public class FileMessage implements AbstractCommand {

    private final File file;
    private final String fileName;
    private final long fileLength;

    public FileMessage(File file, String fileName, long fileLength) {
        this.file = file;
        this.fileName = fileName;
        this.fileLength = fileLength;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_MESSAGE;
    }
}

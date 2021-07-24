package model.list;

import model.AbstractCommand;
import model.CommandType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ListResponse implements AbstractCommand {

    private final List<String> names;
    private final String root;

    public  ListResponse(Path path) throws IOException {
        root = path.toString();
        names = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    public List<String> getNames() {
        names.sort((o1, o2) -> new Long(new File(root + "/" + o1).length() -
                new File(root + "/" + o2).length()).intValue());
        return names;
    }

    public String getRoot() {
        return root;
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_RESPONSE;
    }
}

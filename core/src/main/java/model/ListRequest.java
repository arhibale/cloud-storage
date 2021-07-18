package model;

public class ListRequest implements AbstractCommand {
    @Override
    public CommandType getType() {
        return CommandType.LIST_REQUEST;
    }
}

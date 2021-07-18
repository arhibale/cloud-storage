package model;

import java.io.Serializable;

public interface AbstractCommand extends Serializable {
    CommandType getType();
}

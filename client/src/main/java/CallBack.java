import model.AbstractCommand;
import model.Message;

import java.io.IOException;

public interface CallBack {

    void call(AbstractCommand command) throws IOException;

}

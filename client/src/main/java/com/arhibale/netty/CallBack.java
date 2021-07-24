package com.arhibale.netty;

import model.AbstractCommand;

import java.io.IOException;

public interface CallBack {

    void call(AbstractCommand command) throws IOException;

}

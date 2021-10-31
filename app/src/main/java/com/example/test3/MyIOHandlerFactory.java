package com.example.test3;

import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.ProtocolIOSession;

public class MyIOHandlerFactory implements IOEventHandlerFactory {
    @Override
    public IOEventHandler createHandler(ProtocolIOSession ioSession, Object attachment) {
        return new MyIOEventHandler();
    }
}

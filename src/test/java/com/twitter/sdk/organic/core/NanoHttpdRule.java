package com.twitter.sdk.organic.core;

import com.google.common.net.HostAndPort;
import org.junit.rules.ExternalResource;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

public class NanoHttpdRule extends ExternalResource {

    private NanoServer server;

    @Override
    protected void before() throws Throwable {
        server = new NanoServer();
    }

    @Override
    protected void after() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    @SuppressWarnings("unused")
    public NanoServer getServer() {
        return server;
    }

    public HostAndPort getSocketAddress() {
        checkState(server != null, "server not instantiated yet");
        return server.getSocketAddress();
    }

    @SuppressWarnings("UnusedReturnValue")
    public NanoHttpdRule handle(NanoServer.RequestHandler requestHandler) {
        server.handle(requestHandler);
        return this;
    }

}

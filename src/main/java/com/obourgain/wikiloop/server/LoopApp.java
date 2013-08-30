package com.obourgain.wikiloop.server;

import com.obourgain.wikiloop.server.resource.LoopResource;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class LoopApp extends Application {

    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/loop/{start}", LoopResource.class);
        return router;
    }

}

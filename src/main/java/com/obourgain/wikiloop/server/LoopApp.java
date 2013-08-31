package com.obourgain.wikiloop.server;

import com.obourgain.wikiloop.server.resource.LoopResource;
import com.obourgain.wikiloop.server.util.JSONPifier;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;

public class LoopApp extends Application {

    @Override
    public synchronized Restlet createInboundRoot() {

        Filter myFilter = new JSONPifier(getContext());
        myFilter.setNext(LoopResource.class);
        Router router = new Router(getContext());
        router.attach("/loop/{start}", myFilter);
        return router;
    }

}


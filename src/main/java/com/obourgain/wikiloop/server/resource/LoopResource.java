package com.obourgain.wikiloop.server.resource;

import com.google.gson.Gson;
import com.obourgain.wikiloop.server.domain.Loop;
import com.obourgain.wikiloop.server.model.Page;
import org.apache.log4j.Logger;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class LoopResource extends ServerResource {

    private static final Logger log = Logger.getLogger(LoopResource.class);
    static int cpt = 0;
    String start;

    @Override
    public void doInit() {
        this.start = (String) getRequestAttributes().get("start");
    }

    @Get
    public String loop() throws UnsupportedEncodingException {
        cpt++;
        log.info(cpt+":Get(raw):" + start);
        String decodedPage = URLDecoder.decode(start, "UTF-8");
        log.info(cpt+":Get:" + decodedPage);
        Loop wikiLoop = Loop.get();

        Gson gson = new Gson();
        List<Page> loop = wikiLoop.loop(decodedPage);
        for (Page p : loop) {
            log.debug(cpt+":Res:" + p);
        }
        return gson.toJson(loop);
    }
}

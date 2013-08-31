package com.obourgain.wikiloop.server.resource;

import com.google.gson.Gson;
import com.obourgain.wikiloop.server.domain.Loop;
import com.obourgain.wikiloop.server.model.Page;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
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
    public Representation loop() throws UnsupportedEncodingException {
        String decodedPage = URLDecoder.decode(start, "UTF-8");

        cpt++;
        log.info(cpt + ":Get(raw):" + start);
        log.info(cpt + ":***Get:" + decodedPage);

        Loop wikiLoop = Loop.get();
        List<Page> loop = wikiLoop.loop(decodedPage);

        if (loop.size() == 0) log.debug("Aucun résultat");
        for (Page p : loop) {
            log.debug(cpt + ":Res:" + p);
        }

        String res = new Gson().toJson(loop);
        Representation sr = new StringRepresentation(res);
        sr.setMediaType(MediaType.APPLICATION_JSON);
        return sr;
    }
}

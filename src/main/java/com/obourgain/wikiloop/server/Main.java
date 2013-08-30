package com.obourgain.wikiloop.server;

import com.obourgain.wikiloop.server.domain.Loop;
import org.apache.log4j.Logger;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        // Avant le démarrage du serveur, on charge le fichier de données.
        Loop.get();

        log.info("******  Starting server port 8012 *****");
        // Create a new Component.
        // Attach the app
        // and start
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8012);
        component.getDefaultHost().attach("/wikiphilo", new LoopApp());
        component.start();
    }

}

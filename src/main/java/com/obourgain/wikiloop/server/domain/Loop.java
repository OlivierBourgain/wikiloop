package com.obourgain.wikiloop.server.domain;

import com.obourgain.wikiloop.server.model.Page;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Loop {
    private static final Logger log = Logger.getLogger(Loop.class);

    private static Loop instance;
    private final static Map<String, String> map = new HashMap<String, String>(3000000);

    private Loop() {
    }

    public static Loop get() {
        return instance;
    }

    public static Loop init(String filename) {
        if (instance == null) {
            Loop w = new Loop();
            //String fileName = "/frwiki-20130819-successeur.txt";
            //String fileName = "/test-successeur.txt";
            try {
                File f = new File(filename);
                log.info("Chargement du fichier " + f.getAbsolutePath());
                int cpt = w.load(new FileInputStream(f));
                log.info(cpt + " lignes chargées");
            } catch (IOException e) {
                log.error("fichier non chargé", e);
            }
            instance = w;
        }
        return instance;
    }

    /**
     * Chargement des données
     */
    private int load(InputStream is) throws IOException {

        Iterator<String> it = IOUtils.lineIterator(is, "UTF-8");
        int cpt = 0;
        while (it.hasNext()) {
            cpt++;
            if (cpt % 100000 == 0) log.debug("Chargement ligne " + cpt);
            String s = it.next();
            String[] t = s.split("#");
            // On ignore les id.
            map.put(t[1], t[3].intern());
        }
        is.close();
        return cpt;
    }

    /**
     * Renvoie la liste des pages parcourues lorsqu'on clique sur le premier lien de chaque page.
     */
    public List<Page> loop(String root) {
        List<Page> res = new ArrayList<Page>();

        String tortue = map.get(root);
        if (tortue == null) return res;
        res.add(new Page(tortue));

        String lievre = map.get(tortue);
        res.add(new Page(lievre));
        if (lievre == null) return res;

        while (tortue != lievre) {
            if (lievre == null || map.get(lievre) == null) return res;
            tortue = map.get(tortue);

            lievre = map.get(lievre);
            res.add(new Page(lievre));
            lievre = map.get(lievre);
            res.add(new Page(lievre));
        }

        // Il existe une boucle. On réinitialise la Liste, 
        res = new ArrayList<Page>();
        tortue = root;

        // On va cherche le début de la boucle
        while (tortue != lievre) {
            res.add(new Page(tortue));
            tortue = map.get(tortue);
            lievre = map.get(lievre);
        }
        res.add(new Page(lievre, true));

        // Et les pages faisant partie de la boucle.
        lievre = map.get(lievre);
        while (tortue != lievre) {
            res.add(new Page(lievre, true));
            lievre = map.get(lievre);
        }
        return res;
    }
}

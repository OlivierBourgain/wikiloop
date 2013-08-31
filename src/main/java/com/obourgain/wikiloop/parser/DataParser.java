package com.obourgain.wikiloop.parser;

import org.apache.commons.io.FileUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Parse un dump de wikipedia et génère un fichier contenant la chaine de pages
 * atteintes en suivant le premier lien.
 * <p/>
 * Ce traitement se base sur un dump de wikipédia France (http://dumps.wikimedia.org/frwiki/).
 * Pour
 */
public class DataParser {

    /**
     * Emplacement et nom de l'export à traiter.
     */
    private static final String root = "../Wikipedia/data/";
    private static final String wiki = "frwiki-20130819";

    /**
     * Estimation du nombre d'articles utilisé pour tailler les structures de donneés.
     * Pour éviter les resize, prendre nombre d'articles * 1,5
     */
    private static final int NB_ARTICLES = 4500000;


    /**
     * Le traitement va utiliser les fichiers :
     * wiki + "pages-articles-multistream-index.txt"
     * wiki + "pages-articles-multistream.xml
     * <p/>
     * et générer 3 fichiers
     * wiki + "err.txt"  contenant les pages pour lesquels le premier lien n'est pas trouvé.
     * wiki + "successeurs.txt"  contenant le premier lien de chaque page.
     * wiki + "cycles.txt"  contenant le détail de la chaine de page pour chaque page.
     */

    public static final String SEP_LIEN = "#";
    public static final String SEP_CYCLE = " -> ";
    private static Map<Long, String> pages = new HashMap<Long, String>(NB_ARTICLES);
    private static Map<String, Long> dict = new HashMap<String, Long>(NB_ARTICLES);


    public static void main(String[] args) throws IOException, XMLStreamException {

        // Step 1
        // Lit l'index et renseigne les deux map
        // pages = Index -> Page
        // dict  = Page -> Index
        step1(wiki);

        System.out.println("\n\n");

        // Step 2
        // Lit le fichier principal, et extrait le premier lien de chaque page
        // Ecrit le résultat dans le fichier firstlink
        step2(wiki);

        // Libère les deux maps initiales de la mémoire, on en a plus besoin.
        pages = null;
        dict = null;

        System.out.println("\n\n");

        // Step 3
        // Lit le fichier firstlink, et génère la suite de page pour chaque page
        // Ecrit le résultat dans le fichier cycles
        step3(wiki);
    }

    private static void step1(String wiki) throws IOException {
        System.out.println("***********************************");
        System.out.println("**     Step 1 - Parsing index file ");
        System.out.println("***********************************");
        File in = new File(root + wiki + "-pages-articles-multistream-index.txt");
        System.out.println("Fichier en entrée " + in.getAbsolutePath());

        int cpt = 0;
        Iterator<String> it = FileUtils.lineIterator(in, "UTF-8");

        int ok = 0;
        int ko = 0;
        int special = 0;
        while (it.hasNext()) {
            cpt++;
            if (cpt % 100000 == 0) System.out.println("Loading line " + cpt);
            String line = it.next();
            int i1 = line.indexOf(":");
            int i2 = line.indexOf(":", i1 + 1);
            Long id = Long.parseLong(line.substring(i1 + 1, i2));
            String title = line.substring(i2 + 1);
            if (pages.containsKey(id)) {
                System.out.println("Duplicate " + title);
                ko++;
            } else if (isSpecialPage(title)) {
                special++;
            } else {
                pages.put(id, title);
                dict.put(title, id);
                ok++;
            }

        }

        System.out.println("***********************************");
        System.out.println("**     " + cpt + " lines");
        System.out.println("**     " + ok + " lines ok");
        System.out.println("**     " + ko + " lines ko");
        System.out.println("***********************************");
    }

    private static void step2(String wiki) throws XMLStreamException, IOException {
        System.out.println("***********************************");
        System.out.println("**     Step 2 - Parsing main data file ");
        System.out.println("***********************************");

        File in = new File(root + wiki + "-pages-articles-multistream.xml");
        File out = new File(root + wiki + "-firstlink.txt");
        File err = new File(root + wiki + "-err.txt");
        System.out.println("Fichier en entrée " + in.getAbsolutePath());
        System.out.println("Fichier en sortie " + out.getAbsolutePath());
        System.out.println("Fichier erreur    " + err.getAbsolutePath());

        LinkParser linkParser = new LinkParser(dict);
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        xmlif.setProperty("javax.xml.stream.isReplacingEntityReferences", Boolean.TRUE);
        XMLEventReader xmler = xmlif.createXMLEventReader(new FileReader(in));
        int cpt = 0;
        int ok = 0;
        int ko = 0;
        int nontraite = 0;
        int redirect = 0;
        long start = System.currentTimeMillis();
        long split = start;
        while (xmler.hasNext()) {

            XMLEvent event = xmler.nextEvent();
            if (event.isStartElement()) {
                String name = event.asStartElement().getName().toString();
                if (name.endsWith("}page")) {
                    cpt++;
                    if (cpt % 10000 == 0) {
                        System.out.print("Parsing article " + cpt);
                        long elapse = System.currentTimeMillis() - split;
                        System.out.println("    " + (elapse / 1000) + "s - " + (10000 * 1000L / elapse) + " articles/s");
                        split = System.currentTimeMillis();
                    }

                    //if (cpt > 100000) break;
                    Long id = null;
                    String text = null;
                    while (true) {
                        event = xmler.nextEvent();
                        if (event.isStartElement()) {
                            name = event.asStartElement().getName().toString();
                            if ((id == null) && name.endsWith("id")) {
                                event = xmler.nextEvent();
                                id = Long.parseLong(event.asCharacters().getData());
                            } else if (name.endsWith("text")) {
                                event = xmler.nextEvent();
                                text = "";
                                while (event.isCharacters()) {
                                    text += event.asCharacters().getData();
                                    event = xmler.nextEvent();
                                }
                                break;
                            }
                        }
                    }
                    String title = pages.get(id);
                    if (title == null || isSpecialPage(title)) {
                        nontraite++;
                    } else {
                        Long next = linkParser.getFirstLink(title, text);
                        if (next == null) {
                            if (linkParser.isRedirect(text)) {
                                redirect++;
                            } else {
                                FileUtils.writeStringToFile(err, "\n###########################################################\n", "UTF-8", true);
                                FileUtils.writeStringToFile(err, "Page " + id + " = " + title + " -> Next Not Found\n" + text + "\n", "UTF-8", true);
                                ko++;
                            }
                        } else {
                            FileUtils.writeStringToFile(out, id + SEP_LIEN + title + SEP_LIEN + next + SEP_LIEN + pages.get(next) + "\n", "UTF-8", true);
                            ok++;
                        }
                    }
                }
            }
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("***********************************");
        System.out.println("**     " + cpt + " articles chargés");
        System.out.println("**     " + nontraite + " lignes ignorées");
        System.out.println("**     " + redirect + " redirects ignorés");
        System.out.println("**     " + ko + " articles sans lien trouvé");
        System.out.println("**     " + ok + " liens trouvés");
        System.out.println("**     " + duration / 1000 + " sec");
        System.out.println("**     " + cpt * 1000L / duration + " lignes/s");
        System.out.println("***********************************");
    }

    public static void step3(String wiki) throws IOException {
        System.out.println("**************************************");
        System.out.println("**     Step 3 - Generating cycle file ");
        System.out.println("**************************************");

        File in = new File(root + wiki + "-firstlink.txt");
        File out = new File(root + wiki + "-cycles.txt");

        System.out.println("Fichier en entrée " + in.getAbsolutePath());
        System.out.println("Fichier en sortie " + out.getAbsolutePath());

        Map<String, String> map = getGraph(in);
        int cpt = 0;
        int philo = 0;
        int nonphilo = 0;

        long start = System.currentTimeMillis();
        long split = start;
        for (String root : map.keySet()) {
            cpt++;
            //if (cpt >= 10000) break;
            if (cpt % 10000 == 0) {
                System.out.print("Parsing article " + cpt);
                long elapse = System.currentTimeMillis() - split;
                System.out.println("    " + (elapse / 1000) + "s - " + (10000 * 1000L / elapse) + " articles/s");
                split = System.currentTimeMillis();
            }

            List<String> res = containsLoop(root, map);
            if (res.contains("Philosophie")) {
                philo++;
            } else {
                nonphilo++;
            }
            FileUtils.writeStringToFile(out, dump(res), "UTF-8", true);
            FileUtils.writeStringToFile(out, "\n", "UTF-8", true);
        }


        long duration = System.currentTimeMillis() - start;
        System.out.println("***********************************");
        System.out.println("**     " + cpt + " articles chargés");
        System.out.println("**     " + philo + " articles mènent vers Philosophie");
        System.out.println("**     " + 100. * philo / cpt + " %");
        System.out.println("**     " + nonphilo + " articles ne mènent pas vers Philosophie");
        System.out.println("**     " + 100. * nonphilo / cpt + " %");
        System.out.println("**     " + duration / 1000 + " sec");
        System.out.println("**     " + cpt * 1000L / duration + " lignes/s");
        System.out.println("***********************************");
    }

    /**
     * Renvoie true si la page est une page spéciale (qui sera ignorée)
     */
    private static boolean isSpecialPage(String line) {
        if (line.contains("Catégorie:")) return true;
        if (line.contains("Projet:")) return true;
        if (line.contains("Wikipédia:")) return true;
        if (line.contains("Modèle:")) return true;
        if (line.contains("Fichier:")) return true;
        if (line.contains("Portail:")) return true;
        if (line.contains("MediaWiki:")) return true;
        if (line.contains("Référence:")) return true;
        if (line.contains("Aide:")) return true;
        if (line.contains("Module:")) return true;
        return false;
    }


    /**
     * Charge une map contenant le premier lien de chaque page.
     */
    public static Map<String, String> getGraph(File f) throws IOException {

        Map<String, String> res = new HashMap<String, String>();

        Iterator<String> it = FileUtils.lineIterator(f, "UTF-8");
        int cpt = 0;
        while (it.hasNext()) {
            String s = it.next();
            String[] t = s.split("#");
            // On ignore les id.
            res.put(t[1], t[3]);
            cpt++;
        }

        return res;
    }

    private static String dump(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(SEP_CYCLE);
            sb.append(s);
        }
        return sb.substring(SEP_CYCLE.length()).toString();
    }


    /**
     * Vérifie s'il existe un cycle dans la liste chainée passée en paramètre.
     *
     * @param root La racine de la liste.
     * @return true si la liste contient un cycle, false sinon.
     */
    public static List<String> containsLoop(String root, Map<String, String> map) {
        List<String> res = new ArrayList<String>();
        res.add(root);

        String tortue = map.get(root);
        if (tortue == null) return res;
        res.add(tortue);

        String lievre = map.get(tortue);
        res.add(lievre);
        if (lievre == null) return res;

        while (tortue != lievre) {
            if (lievre == null || map.get(lievre) == null) return res;
            tortue = map.get(tortue);

            lievre = map.get(lievre);
            res.add(lievre);
            lievre = map.get(lievre);
            res.add(lievre);
        }

        // Il existe une boucle. On va cherche le début de la boucle
        res = new ArrayList<String>();
        tortue = root;

        while (tortue != lievre) {
            res.add(tortue);
            tortue = map.get(tortue);
            lievre = map.get(lievre);
        }

        res.add(lievre);
        lievre = map.get(lievre);
        while (tortue != lievre) {
            res.add(lievre);
            lievre = map.get(lievre);
        }

        return res;
    }

}

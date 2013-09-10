package com.obourgain.wikiloop.parser;

import java.util.Map;

public class LinkParser {
    Map<String, Long> dict;

    public LinkParser(Map<String, Long> dict) {
        this.dict = dict;
    }

    /**
     * Extraction du premier lien de l'article.
     * The rules :
     * - on ignore ce qui est entre parenthèse ou en italique
     * - on ignore les liens externes, les liens vers la page elle même et les liens rouges (liens vers une page qui n'existe pas)
     * <p/>
     * <p/>
     * Les liens apparaissent dans l'article sous la forme :
     * [[Titre page]]
     * [[Titre page|Alias]]
     * <p/>
     * Il y a de nombreux cas particuliers à ignores :
     * - les infobox et les templates, donc tout ce qui est à l'intérieur de {{}}, sauf refsou, refnec, Citation et nobr.
     * - ce qui est entre ()
     * - ce qui est italique entre '' et ''
     * - les liens vers les images : [[Image:...]]
     * - les liens externes : [[http://...]]
     * - les liens dans un commentaire HTML : <!-- -->
     * <p/>
     * Voir les TU pour tous les cas particuliers identifiés.
     * <p/>
     * Si on est dans le cas d'une redirection, on essaie d'interpréter le contenu du lien
     * en remplaçant les '_' par ' '  et '%C3%A7' par 'ç' (ce dernier cas pour traiter "Personne morale en droit français").
     */
    protected Long getFirstLink(String title, String text) {
        try {
            int l = text.length();

            // Il faut au moins 5 caractères pour faire un lien
            for (int i = 0; i < l - 5; i++) {
                char c = text.charAt(i);

                if (text.charAt(i) == '(') {
                    int nb = 1;
                    i += 1;
                    while (nb > 0 && i < l) {
                        if (text.charAt(i) == '(') nb++;
                        if (text.charAt(i) == ')') nb--;
                        i++;
                    }
                    i--;
                } else if (c == '\'' && text.charAt(i + 1) == '\'' && text.charAt(i + 2) == '\'') {
                    // Si la lettre précédente est un d ou un l, et que la lettre suivante n'est pas un espace
                    // alors on est devant un combo apostrophe + italique (' et '')
                    // Sinon c'est du gras (''')

                    if (i > 0
                            && (text.charAt(i - 1) == 'd' || text.charAt(i - 1) == 'l')
                            && (text.charAt(i + 3) != ' ')
                            && (Character.isAlphabetic(text.charAt(i + 3)) || text.charAt(i + 3) == '[')
                            ) {
                        // On traiter comme de l'italique, il faut ignorer le contenu
                        i += 2;
                        // Text italique
                        while (true) {
                            if (i > l - 3) break;
                            if (text.charAt(i) == '\'' && text.charAt(i + 1) == '\'' && text.charAt(i + 2) == '\'')
                                i += 2;
                            else if (text.charAt(i) == '\'' && text.charAt(i + 1) == '\'') break;
                            i++;
                        }
                        i++;
                    } else {
                        // On traite ça comme du gras, on passe
                        i += 2;
                    }
                } else if (c == '\'' && text.charAt(i + 1) == '\'') {
                    i += 2;
                    // Text italique
                    while (true) {
                        if (i > l - 3) break;
                        if (text.charAt(i) == '\'' && text.charAt(i + 1) == '\'' && text.charAt(i + 2) == '\'') i += 2;
                        else if (text.charAt(i) == '\'' && text.charAt(i + 1) == '\'') break;
                        i++;
                    }
                    i++;
                } else if (c == '<' && text.charAt(i + 1) == 'r') {
                    if (text.startsWith("<ref", i)) {
                        int end = text.indexOf("</ref>", i);
                        if (end < 0) end = text.indexOf("</ref >", i);
                        if (end > 0) i = end;
                    }
                } else if (c == '<' && text.charAt(i + 1) == '!') {
                    if (text.startsWith("<!--", i)) {
                        int end = text.indexOf("-->", i);
                        if (end > 0) i = end;
                    }
                } else if (c == '{' && text.charAt(i + 1) == '{') {
                    if (templateShouldBeIgnored(text, i, l)) {
                        int nb = 2;
                        i += 2;
                        while (nb > 0 && i < l) {
                            if (text.charAt(i) == '{') nb++;
                            if (text.charAt(i) == '}') nb--;
                            i++;
                        }
                        i--;
                    }
                } else if (c == '[' && text.charAt(i + 1) == '[') {
                    String link = parseLink(text, i, l);

                    if (link != null && link.startsWith("Fichier:")) {
                        i = findEndLink(text, i, l);
                    } else if (link != null && link.startsWith("Image:")) {
                        i = findEndLink(text, i, l);
                    } else if (link != null && link.startsWith("File:")) {
                        i = findEndLink(text, i, l);
                    } else if (link != null && link.compareToIgnoreCase(title) != 0) {
                        link = link.trim();
                        Long res = dict.get(link);
                        if (res != null) return res;

                        if (link.length() > 1) {

                            // On essaie de capitaliser la première lettre.
                            Character ch = link.charAt(0);
                            link = Character.toUpperCase(ch) + link.substring(1);
                            res = dict.get(link);
                            if (res != null) return res;

                            // On essaie de décapitaliser tout, sauf la première lettre
                            link = Character.toUpperCase(ch) + link.substring(1).toLowerCase();
                            res = dict.get(link);
                            if (res != null) return res;
                        }

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur de parsing pour " + title + "\n" + text.substring(0, min(text.length(), 300)));

        }

        // Rien trouvé. On essaie le cas particulier des redirections.
        if (isRedirect(text)) {
            if (text.contains("_") || text.contains("%C3%A7")) {
                String text2 = text.replace('_', ' ').replaceAll("%C3%A7", "ç");
                return getFirstLink(title, text2);
            }
        }

        return null;
    }

    private boolean templateShouldBeIgnored(String text, int i, int l) {
        String t = text.substring(i + 2, min(i + 9, l));
        // Référence souhaitée ou référence nécessaire. Le texte est souligné, mais doit
        // être pris en compte.
        if (t.equals("refsou|") || t.equals("refnec|")) return false;
        // Citation, il faut prendre en compte.
        // On devrait en théorie tester "Citation"
        if (t.equals("Citatio")) return false;

        t = text.substring(i + 2, min(i + 7, l));
        // Nobr, il faut prendre en compte.
        if (t.equals("nobr|")) return false;

        // Tout le reste est ignoré
        return true;
    }

    private int findEndLink(String text, int i, int l) {
        int nb = 1;
        i += 2;
        while (i < l - 1 && nb > 0) {
            if (text.charAt(i) == '[' && text.charAt(i + 1) == '[') {
                nb++;
                i += 2;
            } else if (text.charAt(i) == ']' && text.charAt(i + 1) == ']') {
                nb--;
                i += 2;
            } else i++;
        }
        return i;
    }


    private String parseLink(String text, int i, int l) {
        int end = text.indexOf(']', i + 1);
        int pipe = text.indexOf('|', i + 1);
        int diese = text.indexOf('#', i + 1);

        if (pipe > 0) end = min(pipe, end);
        if (diese > 0) end = min(diese, end);

        return text.substring(i + 2, end);
    }

    private int min(int a, int b) {
        return a < b ? a : b;
    }


    /**
     * Vérifie si cette page est une page de redirection.
     */
    public boolean isRedirect(String text) {
        if (text.trim().startsWith("#REDIRECT")) return true;
        if (text.trim().startsWith("#redirect")) return true;
        if (text.trim().startsWith("#Redirect")) return true;
        return false;
    }

}

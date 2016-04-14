package com.obourgain.wikiloop.server.domain;

import com.obourgain.wikiloop.server.model.Page;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Loop {
	private static final Logger log = Logger.getLogger(Loop.class);

	private static Loop instance;
	/*
	 * Le process fonctionnait initialement de faire tourner le serveur sur une instance 
	 * tiny d'EC2 (gratuite), qui est limitée à 500Mo de RAM.
	 * 
	 * Pour économiser un peu de mémoire, au lieu de stocker les données dans
	 * une Map<String, String> (Page -> Sucesseur), on utilise un tableau de
	 * String pour stocker les pages, et un tableau d'entiers qui pour chaque
	 * page donne l'index du successeur.
	 * 
	 * Le tableau de page est trié, et on retrouve la bonne page par une
	 * recherche binaire.
	 */
	private final String[] pages = new String[3150000];
	private final int[] firstLinks = new int[3150000];
	private int cpt;

	private Loop() {
	}

	public static Loop get() {
		return instance;
	}

	public static Loop init(String filename) {
		if (instance == null) {
			Loop w = new Loop();
			// String fileName = "/frwiki-20130819-successeur.txt";
			// String fileName = "/test-successeur.txt";
			try {
				File f = new File(filename);
				log.info("Chargement du fichier " + f.getAbsolutePath());
				int cpt = w.load(f);
				log.info(cpt + " lignes chargées");
			} catch (IOException e) {
				log.error("fichier non chargé", e);
			}
			instance = w;
		}
		return instance;
	}

	/**
	 * Chargement des données.
	 */
	private int load(File f) throws IOException {
		InputStream is = new FileInputStream(f);

		/* Etape 1 - On initialise la liste des pages */
		Iterator<String> it = IOUtils.lineIterator(is, "UTF-8");
		log.debug("Pass 1 - Chargement des pages");
		while (it.hasNext()) {
			if (cpt % 100000 == 0)
				log.debug("1 - Chargement ligne " + cpt);
			String s = it.next();
			String[] t = s.split("#");
			// On ignore les id.
			pages[cpt] = t[1];
			cpt++;
		}
		is.close();
		Arrays.sort(pages, 0, cpt);
		System.out.println("Pass 1 - " + cpt + " lignes chargées");

		/* Etape 2 - Initialisation du tableau des premiers liens */
		is = new FileInputStream(f);
		it = IOUtils.lineIterator(is, "UTF-8");

		int cpt2 = 0;
		log.debug("Pass 2 - Chargement des premiers liens");
		while (it.hasNext()) {
			if (cpt2 % 100000 == 0)
				log.debug("2 - Chargement ligne " + cpt2);
			String s = it.next();
			String[] t = s.split("#");
			int page = getIdx(t[1]);
			int next = getIdx(t[3]);
			firstLinks[page] = next;
			// On ignore les id.
			cpt2++;
		}
		is.close();
		System.out.println("Pass 2 - " + cpt2 + " lignes chargées");

		return cpt;
	}

	/**
	 * Recherche du successeur d'une page.
	 */
	private String get(String s) {
		int idx = getIdx(s);
		if (idx < 0) {
			log.error("Erreur d'index sur " + s);
			return null;
		}
		if (!pages[idx].equals(s))
			return null;
		return pages[firstLinks[idx]];
	}

	/**
	 * Recherche de l'index d'une page
	 */
	private int getIdx(String s) {
		return Arrays.binarySearch(pages, 0, cpt, s);
	}

	/**
	 * Renvoie la liste des pages parcourues lorsqu'on clique sur le premier
	 * lien de chaque page.
	 */
	public List<Page> loop(String root) {
		List<Page> res = new ArrayList<Page>();

		String tortue = get(root);
		if (tortue == null)
			return res;
		res.add(new Page(tortue));

		String lievre = get(tortue);
		res.add(new Page(lievre));
		if (lievre == null)
			return res;

		while (tortue != lievre) {
			if (lievre == null || get(lievre) == null)
				return res;
			tortue = get(tortue);

			lievre = get(lievre);
			res.add(new Page(lievre));
			lievre = get(lievre);
			res.add(new Page(lievre));
		}

		// Il existe une boucle. On réinitialise la Liste,
		res = new ArrayList<Page>();
		tortue = root;

		// On va cherche le début de la boucle
		while (tortue != lievre) {
			res.add(new Page(tortue));
			tortue = get(tortue);
			lievre = get(lievre);
		}
		res.add(new Page(lievre, true));

		// Et les pages faisant partie de la boucle.
		lievre = get(lievre);
		while (tortue != lievre) {
			res.add(new Page(lievre, true));
			lievre = get(lievre);
		}
		return res;
	}
}

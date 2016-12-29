package com.obourgain.wikiloop.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/**
 * Calcule le graphe inverse de wikipedia.
 * 
 * - Les vertex sont les pages
 * - Les edges sont les liens entrant vers chaque page.
 * 
 * 
 * 
 */
public class GraphInverse {
	
	private final static int BATCHSIZE = 500000;
	/**
	 * Emplacement et nom de l'export à traiter.
	 */
	private static final String root = "data/";
	private static final String wiki = "frwiki-20161220";

	public static class Node {
		String name;
		int id;
		Integer next;
		List<Node> pred = new ArrayList<>();
		boolean cycle;

		public Node(int id, String name, Integer next) {
			this.id = id;
			this.name = name;
			this.next = next;
		}

		@Override
		public String toString() {
			return id + " - " + name + " -> " + next;
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("**************************************");
		System.out.println("**     Compute inverse graph ");
		System.out.println("**************************************");

		File in = new File(root + wiki + "-firstlink.txt");
		File out = new File(root + wiki + "-cycles.txt");

		System.out.println("Fichier en entrée " + in.getAbsolutePath());
		System.out.println("Fichier en sortie " + out.getAbsolutePath());

		System.out.println("Loading data");
		Map<Integer, Node> map = getGraph(in);
		System.out.println("Starting graph");

		int cpt = 0;
		long start = System.currentTimeMillis();
		long split = start;

		for (Integer root : map.keySet()) {
			cpt++;
			if (cpt % BATCHSIZE == 0) {
				System.out.print("Parsing article " + cpt);
				long elapse = System.currentTimeMillis() - split;
				System.out.println("    " + (elapse / 1000) + "s - " + (BATCHSIZE * 1000L / elapse) + " articles/s");
				split = System.currentTimeMillis();
			}

			Node n = map.get(root);
			if (map.get(n.next) == null) {
				//System.err.println("Not found  " + n);
			} else {
				map.get(n.next).pred.add(n);

				Set<Integer> seen = new HashSet<>();
				while(n != null && n.next != null && !n.cycle) {
					seen.add(n.id);
					if (seen.contains(n.next)) {
						while(!n.cycle) {
							n.cycle = true;
							n = map.get(n.next);
						}	
					} else {
						n = map.get(n.next);
					}
				}
			}
		}
		
		Node page = map.get(235895);
		for (int i = 0; i < 10; i++) {
			if (page == null) break;
			System.out.println(page);
			System.out.println(page.pred);
			System.out.println(page.pred.size() + " entrées");
			if (page.pred.isEmpty()) break;
			page = page.pred.get(0);
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println("***********************************");
		System.out.println("**     " + cpt + " articles traités");
		System.out.println("**     " + cpt * 1000L / duration + " articles/s");
		System.out.println("***********************************");

	}

	/**
	 * Charge une map des noeuds.
	 */
	public static Map<Integer, Node> getGraph(File f) throws IOException {
		Map<Integer, Node> res = new HashMap<>();
		Iterator<String> it = FileUtils.lineIterator(f, "UTF-8");
		while (it.hasNext()) {
			String s = it.next();
			String[] t = s.split("#");
			// On ignore les id.
			int id = Integer.parseInt(t[0]);
			Node n = new Node(id, t[1], Integer.parseInt(t[2]));
			res.put(id, n);
		}
		return res;
	}
}

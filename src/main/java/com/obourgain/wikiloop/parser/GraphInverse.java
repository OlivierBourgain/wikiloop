package com.obourgain.wikiloop.parser;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * - Les vertex sont les pages - Les edges sont les liens entrant vers chaque
 * page.
 * 
 * 
 * 
 */
public class GraphInverse {

	private final static int BATCHSIZE = 1000000;
	/**
	 * Emplacement et nom de l'export à traiter.
	 */
	private static final String root = "data/";
	private static final String wiki = "frwiki-20160901";

	public static class Node {
		String name;
		int id;
		Integer next;
		List<Node> pred = new ArrayList<>();
		boolean cycle;

		int weight = 0;

		public Node(int id, String name, Integer next) {
			this.id = id;
			this.name = name;
			this.next = next;
		}

		@Override
		public String toString() {
			String w = weight > 0 ? " [w=" + weight + "]" : "";
			return id + (cycle ? "*" : "") + " - " + name + w;
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("**************************************");
		System.out.println("**     Compute inverse graph ");
		System.out.println("**************************************");

		File in = new File(root + wiki + "-firstlink.txt");

		System.out.println("Fichier en entrée " + in.getAbsolutePath());

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
				// System.err.println("Not found " + n);
			} else {
				map.get(n.next).pred.add(n);

				Set<Integer> seen = new HashSet<>();
				while (n != null && n.next != null && !n.cycle) {
					seen.add(n.id);
					if (seen.contains(n.next)) {
						while (!n.cycle) {
							n.cycle = true;
							n = map.get(n.next);
						}
					} else {
						n = map.get(n.next);
					}
				}
			}
		}
		long duration = System.currentTimeMillis() - start;
		System.out.println("***********************************");
		System.out.println("**     " + cpt + " articles traités");
		System.out.println("**     " + cpt * 1000L / duration + " articles/s");
		System.out.println("***********************************");

		System.out.println("Updating weight");
		cpt = 0;
		start = System.currentTimeMillis();
		split = start;

		// Compute weights
		for (Node n : map.values()) {
			cpt++;
			if (cpt % BATCHSIZE == 0) {
				System.out.print("Parsing article " + cpt);
				long elapse = System.currentTimeMillis() - split;
				System.out.println("    " + (elapse / 1000) + "s - " + (BATCHSIZE * 1000L / elapse) + " articles/s");
				split = System.currentTimeMillis();
			}
			if (n.weight > 0) continue;
			updateWeight(n);
		}

		duration = System.currentTimeMillis() - start;
		System.out.println("***********************************");
		System.out.println("**     " + cpt + " articles traités");
		System.out.println("**     " + cpt * 1000L / duration + " articles/s");
		System.out.println("***********************************");

		Node p = map.get(17836);
		int total = 0;
		do {
			tracePage(p, "", cpt);
			total += p.weight;
			p = map.get(p.next);
		} while (p.id != 17836);

		double percentage = total * 100. / cpt;
		System.out.println("Total : " + FORMATTER.format(percentage) + "%");
	}

	public static final NumberFormat FORMATTER = new DecimalFormat("#0.00");

	private static void tracePage(Node page, String prefix, int total) {
		if (page == null) return;
		double percentage = page.weight * 100.0 / total;
		System.out.println(prefix + page.name + " [" + FORMATTER.format(percentage) + "%]");
		Collections.sort(page.pred, nodeComparator);
		for (Node pred : page.pred) {
			if (pred.cycle) continue;
			String newpref = "";
			if (prefix.length() > 2) newpref = prefix.substring(0, prefix.length()-2) + "  |--";
			else newpref = prefix + "|--";
			if (pred.weight > 400000) tracePage(pred, newpref, total);
		}

		return;
	}

	private static Node firstOutOfCycle(Node page) {
		for (Node n : page.pred)
			if (!n.cycle) return n;
		return page.pred.get(0);
	}

	private static Comparator<Node> nodeComparator = new Comparator<Node>() {
		@Override
		public int compare(Node o1, Node o2) {
			return o2.weight - o1.weight;
		}
	};

	public static int updateWeight(Node n) {
		if (n.pred.size() == 0) {
			n.weight = 1;
			return 1;
		}

		int sum = 1; // Pour la page en cours
		for (Node pred : n.pred) {
			if (!pred.cycle) sum += updateWeight(pred);
		}
		n.weight = sum;
		return sum;
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

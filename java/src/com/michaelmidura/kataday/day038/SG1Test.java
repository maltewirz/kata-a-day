package com.michaelmidura.kataday.day038;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

public class SG1Test {

	@Test
	public void testNoSolution() {
		String existingWires = "SX.\n" +
				"XX.\n" +
				"..G";

		String solution = "Oh for crying out loud...";

		assertEquals(solution, SG1.wireDHD(existingWires));
	}

	@Test
	public void test3x3() {
		String existingWires = "SX.\n" +
				"X..\n" +
				"XXG";

		String solution = "SX.\n" +
				"XP.\n" +
				"XXG";

		assertEquals(solution, SG1.wireDHD(existingWires));
	}

	@Test
	public void test5x5() {
		String existingWires = ".S...\n" +
				"XXX..\n" +
				".X.XX\n" +
				"..X..\n" +
				"G...X";

		String solution = ".SP..\n" +
				"XXXP.\n" +
				".XPXX\n" +
				".PX..\n" +
				"G...X";

		assertEquals(solution, SG1.wireDHD(existingWires));
	}

	@Test
	public void test10x10() {
		String existingWires = "XX.S.XXX..\n" +
				"XXXX.X..XX\n" +
				"...X.XX...\n" +
				"XX...XXX.X\n" +
				"....XXX...\n" +
				"XXXX...XXX\n" +
				"X...XX...X\n" +
				"X...X...XX\n" +
				"XXXXXXXX.X\n" +
				"G........X";

		String solution = "XX.S.XXX..\n" +
				"XXXXPX..XX\n" +
				"...XPXX...\n" +
				"XX.P.XXX.X\n" +
				"...PXXX...\n" +
				"XXXXPP.XXX\n" +
				"X...XXP..X\n" +
				"X...X..PXX\n" +
				"XXXXXXXXPX\n" +
				"GPPPPPPP.X";

		assertEquals(solution, SG1.wireDHD(existingWires));
	}

	@Test
	public void randomTests() {
		for (int i = 3; i < 51; i++) {
			String existingWires = "";
			for (int x = 0; x < i; x++) {
				for (int y = 0; y < i; y++)
					if (ThreadLocalRandom.current().nextFloat() < 0.55)
						existingWires += "X";
					else
						existingWires += ".";
				existingWires += "\n";
			}
			int startPos = ThreadLocalRandom.current().nextInt(0, existingWires.length() - 2);
			if (existingWires.charAt(startPos) == '\n')
				startPos++;
			int goalPos = ThreadLocalRandom.current().nextInt(0, existingWires.length() - 2);
			if (existingWires.charAt(goalPos) == '\n')
				goalPos++;
			if (goalPos == startPos)
				goalPos++;
			existingWires = existingWires.substring(0, startPos) + 'S' + existingWires.substring(startPos + 1);
			existingWires = existingWires.substring(0, goalPos) + 'G' + existingWires.substring(goalPos + 1);
			existingWires = existingWires.substring(0, existingWires.length() - 1);
			assertEquals(CompleteSolution.wireDHD(existingWires), SG1.wireDHD(existingWires));
		}
	}

	public static class CompleteSolution {

		private static Node[][] nodes;
		private static ArrayList<Node> open;
		private static ArrayList<Node> closed;
		private static Node start, goal;

		public static String wireDHD(String existingWires) {

			String[] splitWiring = existingWires.split("\n");

			nodes = new Node[splitWiring.length][splitWiring.length];
			open = new ArrayList<>();
			closed = new ArrayList<>();
			start = null;
			goal = null;

			for (int x = 0; x < splitWiring.length; x++)
				for (int y = 0; y < splitWiring.length; y++) {
					nodes[x][y] = new Node(x, y, splitWiring[x].split("")[y].equals("X"));
					if (splitWiring[x].split("")[y].equals("S"))
						start = new Node(nodes[x][y]);
					else if (splitWiring[x].split("")[y].equals("G"))
						goal = new Node(nodes[x][y]);
				}

			start.g = 0;
			start.f = start.g + heuristic(start, goal);
			open.add(start);

			while (!open.isEmpty()) {

				Node current = open.stream().min(Comparator.comparingDouble(Node::getF)).get();

				if (current.x == goal.x && current.y == goal.y)
					return constructPath(current);

				open.remove(current);
				closed.add(current);

				for (Node successor : successors(current)) {
					if (getDuplicate(closed, successor) == null) {
						successor.f = current.g + heuristic(successor, goal);
						if (getDuplicate(open, successor) == null)
							open.add(successor);
						else {
							Node duplicate = getDuplicate(open, successor);
							if (successor.g < duplicate.g) {
								duplicate.g = successor.g;
								duplicate.parent = successor.parent;
							}
						}
					}
				}
			}

			return "Oh for crying out loud...";
		}

		private static ArrayList<Node> successors(Node node) {
			ArrayList<Node> successors = new ArrayList<>();
			for (int x = node.x - 1; x <= node.x + 1; x++)
				for (int y = node.y - 1; y <= node.y + 1; y++)
					if (!(x == node.x && y == node.y) && x >= 0 && y >= 0 && x < nodes.length && y < nodes.length && !nodes[x][y].blocked)
						successors.add(new Node(nodes[x][y]));
			for (Node successor : successors) {
				if (successor.x != node.x && successor.y != node.y)
					successor.g = node.g + 1.414;
				else
					successor.g = node.g + 1;
				successor.parent = node;
			}
			return successors;
		}

		private static double heuristic(Node node1, Node node2) {
			double maxD = Math.max(Math.abs(node1.x - node2.x), Math.abs(node1.y - node2.y));
			double minD = Math.min(Math.abs(node1.x - node2.x), Math.abs(node1.y - node2.y));
			return (1.414 * minD) + (maxD - minD);
		}

		private static Node getDuplicate(ArrayList<Node> nodes, Node node) {
			for (Node n : nodes)
				if (samePosition(n, node))
					return n;
			return null;
		}

		private static boolean samePosition(Node node1, Node node2) {
			return node1.x == node2.x && node1.y == node2.y;
		}

		private static String constructPath(Node node) {
			ArrayList<Node> path = new ArrayList<>();
			while (node.parent != null) {
				path.add(node);
				node = node.parent;
			}

			String ret = "";
			for (int x = 0; x < nodes.length; x++) {
				for (int y = 0; y < nodes.length; y++) {
					Node check = nodes[x][y];
					if (check.blocked)
						ret += "X";
					else if (samePosition(check, start))
						ret += "S";
					else if (samePosition(check, goal))
						ret += "G";
					else if (path.stream().filter(n -> samePosition(n, check)).count() > 0)
						ret += "P";
					else
						ret += ".";
				}
				ret += "\n";
			}
			ret = ret.substring(0, ret.length() - 1);
			return ret;
		}
	}

	private static class Node {

		public Node parent;
		public boolean blocked;
		public int x, y;
		public double f, g;

		private Node(int x, int y, boolean blocked) {
			this.x = x;
			this.y = y;
			this.blocked = blocked;
			f = 0;
			g = 0;
		}

		private Node(Node node) {
			this.parent = node.parent;
			this.blocked = node.blocked;
			this.x = node.x;
			this.y = node.y;
			this.f = node.f;
			this.g = node.g;
		}

		public double getF() {
			return f;
		}
	}
}

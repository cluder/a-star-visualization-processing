package ch.coredump.astar;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

/**
 * see Coding Challenge 51.1: A* Pathfinding Algorithm
 * https://www.youtube.com/watch?v=aKYlikFAV4k
 */
public class AStarMain extends PApplet {
	static final int ROWS = 20;
	static final int COLS = 20;
	final int SCHREEN_WIDTH = 800;
	final int SCREEN_HEIGHT = 600;

	int cellWidth = SCHREEN_WIDTH / COLS;
	int cellHeight = SCREEN_HEIGHT / ROWS;

	boolean diagonalMoveAllowed = true;
	float blockChance = 0.4f; // 0.0 - 1.0

	Node[][] grid;

	List<Node> openSet = new ArrayList<>();
	List<Node> closedSet = new ArrayList<>();
	List<Node> solution = new ArrayList<>();

	Node start;
	Node end;

	boolean pause = false;
	private String infoText = "";

	public static void main(String[] args) {
		PApplet.main(AStarMain.class, args);
	}

	@Override
	public void settings() {
		size(SCHREEN_WIDTH, SCREEN_HEIGHT, JAVA2D);
	}

	@Override
	public void setup() {
		frameRate(10);

		initialize();
	}

	private void reset() {
		pause = true;
		openSet.clear();
		closedSet.clear();
		solution.clear();

		// initialize the open set (closed set is empty)
		openSet.add(start);
		setInfoText("");
	}

	private void initialize() {
		pause = true;

		// initialize grid
		grid = new Node[COLS][ROWS];
		for (int col = 0; col < COLS; col++) {
			for (int row = 0; row < ROWS; row++) {
				// create Nodes and set positions
				final Node n = grid[col][row];
				grid[col][row] = new Node(row, col, (col + 0.5f) * width / COLS, ((row + 0.5f) * height / ROWS));
			}
		}

		// initialize start / end
		start = grid[0][0];
		end = grid[COLS - 1][ROWS - 1];

		// initialize the open set (closed set is empty)
		openSet.add(start);
	}

	private void blockRandomNodes() {
		for (int col = 0; col < COLS; col++) {
			for (int row = 0; row < ROWS; row++) {
				final Node node = grid[col][row];
				node.blocked = false;
				// block random nodes
				if (random(1) < blockChance) {
					node.blocked = true;
				}
			}
		}

		start.blocked = false;
		end.blocked = false;
	}

	/**
	 * return a list of neighbor Nodes, which are not in the closedSet.<br>
	 * Diagonal neighbors are skipped.
	 */
	List<Node> getNeighbors(int r, int c) {
		List<Node> neighbors = new ArrayList<>();

		for (int col = max(c - 1, 0); col <= min(c + 1, COLS - 1); col++) {
			for (int row = max(r - 1, 0); row <= min(r + 1, ROWS - 1); row++) {
				final Node n = grid[col][row];
				if (n.blocked) {
					continue;
				}
				if (closedSet.contains(n)) {
					continue;
				}
				if (diagonalMoveAllowed == false && abs(r - row) == 1 && abs(c - col) == 1) {
					// skip diagonal neighbors
					continue;
				} else {
					neighbors.add(n);
				}
			}
		}

		return neighbors;
	}

	/**
	 * draw is the main loop, which ends, when openSet is empty
	 */
	@Override
	public void draw() {
		diagonalMoveAllowed = true;
		background(0);
		frameRate(15);

		if (pause == false) {
			setInfoText("");
			findPath();
		}

		// draw start / end
		fill(Color.YELLOW.getRGB());
		ellipse(start.posX, start.posY, 10, 10);
		ellipse(end.posX, end.posY, 10, 10);

		// draw all nodes
		for (int col = 0; col < COLS; col++) {
			for (int row = 0; row < ROWS; row++) {
				final Node n = grid[col][row];
				if (openSet.contains(n) || closedSet.contains(n)) {
					continue;
				}
				n.draw(this, new Color(250, 250, 250, 100));
			}
		}

		// draw open / closed sets
		for (Node n : openSet) {
			n.draw(this, new Color(50, 50, 255));
		}
		for (Node n : closedSet) {
			n.draw(this, new Color(255, 50, 50));
		}

		// draw solution
		drawSolution();

		drawText();
	}

	private void drawText() {
		if (pause == false) {
			return;
		}

		// text background
		stroke(255, 255, 0, 100);
		rectMode(CORNER);
		fill(0, 180);
		rect(10, height * 0.76f, 270, 130);

		textSize(15);
		fill(255, 200, 0, 250);
		textAlign(LEFT);
		text("SPACE - reset\n"//
				+ "P - toggle PAUSE\n" //
				+ "B - block/unblock all cells\n" //
				+ "R - block random cells\n" //
				+ "Mouse Btn - block/unblock cells\n", //
				20, height * 0.8f);

		stroke(255, 255, 0, 100);
		rectMode(CENTER);
		fill(0, 150);
		rect(width / 2, 50, 250, 65);

		textAlign(CENTER, CENTER);
		textSize(25);
		fill(255, 200, 0, 250);
		text(infoText, width / 2, height * 0.05f);
		text("paused", width / 2, height * 0.1f);
	}

	/**
	 * returns false, if no solution possible. <br>
	 * returns true if solution is found or not done yet.
	 */
	private boolean findPath() {
		if (openSet.isEmpty()) {
			// no possible solution
			pause = true;
			return false;
		} else {
			// Nodes in open set have to be checked
			// find node with lowest f-score
			Node current = getLowestFScore(openSet);

			if (current == end) {
				// finished
				// add end node to solution
				trackSolution(current);
				pause = true;
				return true;
			}

			// add this node with the lowest f-score to the closed set
			closedSet.add(current);
			openSet.remove(current);

			// get neighbors of current, which are not in the closed Set
			final List<Node> neighbors = getNeighbors(current.row, current.col);
			for (Node n : neighbors) {
				// calculate g score for neighbor (distance from current to neighbor)
				float tempG = current.g + distanceToNeighbor(current, n);

				if (openSet.contains(n) == false) {
					// we found a new neighbor - add it to the open set
					n.g = tempG;
					openSet.add(n);
				} else {
					// neighbor has already been visited
					if (tempG < n.g) {
						// new g is lower than existing
						n.g = tempG;
					} else {
						// not a better path
						continue;
					}
				}

				// calculate heuristic value between neighbor and the end
				n.h = heuristic(n, end);
				// f-score is the previous score g + the heuristic score h to the end
				n.f = n.g + n.h;

				// save previous neighbor
				n.previous = current;
			}

			// track current solution
			trackSolution(current);
		}

		// openset is empty - finished / no solution yet
		return true;
	}

	private void trackSolution(Node current) {
		solution.clear();
		Node n = current;
		while (n.previous != null) {
			solution.add(n);
			n = n.previous;
		}
		solution.add(start);
	}

	private Node getLowestFScore(List<Node> list) {
		Node lowestF = list.get(0);
		for (Node n : list) {
			if (n.f < lowestF.f) {
				lowestF = n;
			}
		}
		return lowestF;
	}

	private void drawSolution() {
		Node prev = null;
		for (Node n : solution) {
			if (prev != null) {
				strokeWeight(1);
				stroke(255, 255, 0);
				line(prev.posX, prev.posY, n.posX, n.posY);
			}
			n.draw(this, Color.YELLOW);
			prev = n;
		}
		if (openSet.isEmpty()) {
			textSize(50);
			textAlign(CENTER, CENTER);
			stroke(255, 100);
			setInfoText("No solution found");
		}
	}

	private void setInfoText(String string) {
		infoText = string;
	}

	/**
	 * Calculates the distance from one node to its neighbor.
	 */
	private float distanceToNeighbor(Node a, Node b) {
		return dist(a.posX, a.posY, b.posX, b.posY);
	}

	/**
	 * calculates the heuristics for the current node to the end.
	 */
	private float heuristic(Node current, Node end) {
		return dist(current.posX, current.posY, end.posX, end.posY);
	}

	@Override
	public void keyPressed() {
		if (key == 'P' || key == 'p') {
			pause = !pause;
		}
		if (key == ' ') {
			// reset everything
			reset();
		}
		if (key == 'B' || key == 'b') {
			reset();
			blockAll();
		}
		if (key == 'r' || key == 'R') {
			reset();
			blockRandomNodes();
		}
	}

	private void blockAll() {
		boolean toBlock = !grid[1][1].blocked;
		for (int col = 0; col < COLS; col++) {
			for (int row = 0; row < ROWS; row++) {
				final Node node = grid[col][row];

				if (node == start || node == end) {
					continue;
				}
				node.blocked = toBlock;
			}
		}
	}

	@Override
	public void mouseDragged() {
		blockUnblockSelectedNode();
	}

	@Override
	public void mouseClicked() {
		blockUnblockSelectedNode();
	}

	private void blockUnblockSelectedNode() {
		Node n = getSelectedNode();
		if (n == null) {
			return;
		}
		if (mouseButton == LEFT) {
			n.blocked = true;
		} else {
			n.blocked = false;
		}
	}

	@Override
	public void mouseMoved() {
		Node n = getSelectedNode();
		if (n == null) {
			return;
		}
		strokeWeight(1);
		noFill();
		stroke(255, 255, 0, 255);
		rectMode(CENTER);
		rect(n.posX, n.posY, cellHeight, cellWidth);
	}

	/**
	 * returns the currently selected cell, or null if not allowed.
	 */
	private Node getSelectedNode() {
		final int gridX = mouseX / cellWidth;
		final int gridY = mouseY / cellHeight;

		if (gridX >= ROWS || gridY >= COLS) {
			System.out.println(String.format("x:%s, y:%s, gridx:%s, gridy:%s", mouseX, mouseY, gridX, gridY));
		}

		Node n = grid[min(gridX, COLS - 1)][min(gridY, ROWS - 1)];

		if (openSet.contains(n) || closedSet.contains(n) || n == start || n == end) {
			// only allow block / onblock if not in use
			return null;
		}
		return n;
	}
}

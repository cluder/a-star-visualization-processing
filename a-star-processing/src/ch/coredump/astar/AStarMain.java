package ch.coredump.astar;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import processing.core.PApplet;

/**
 * see Coding Challenge 51.1: A* Pathfinding Algorithm
 * https://www.youtube.com/watch?v=aKYlikFAV4k
 */
public class AStarMain extends PApplet {
	static final int ROWS = 22;
	static final int COLS = 22;

	boolean diagonalMoveAllowed = true;
	float blockChance = 0.4f; // 0.0 - 1.0

	Node[][] grid;

	List<Node> openSet = new ArrayList<>();
	List<Node> closedSet = new ArrayList<>();
	List<Node> solution = new ArrayList<>();

	Node start;
	Node end;

	public static void main(String[] args) {
		PApplet.main(AStarMain.class, args);
	}

	@Override
	public void settings() {
		size(800, 600);
	}

	@Override
	public void setup() {
		frameRate(10);

		initialize();
	}

	private void initialize() {
		openSet = new ArrayList<>();
		closedSet = new ArrayList<>();
		solution = new ArrayList<>();

		// initialize grid
		grid = new Node[ROWS][COLS];
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				// create Nodes and set positions
				grid[row][col] = new Node(row, col, (col + 0.5f) * width / COLS, ((row + 0.5f) * height / ROWS));
				if (random(1) < blockChance) {
					grid[row][col].blocked = true;
				}
			}
		}

		// initialize start / end
		start = grid[0][0];
		end = grid[ROWS - 1][COLS - 1];

		start.blocked = false;
		end.blocked = false;

		// initialize the open set (closed set is empty)
		openSet.add(start);

		loop();
	}

	/**
	 * return a list of neighbor Nodes, which are not in the closedSet.<br>
	 * Diagonal neighbors are skipped.
	 */
	List<Node> getNeighbors(int r, int c) {
		List<Node> neighbors = new ArrayList<>();

		for (int row = max(r - 1, 0); row <= min(r + 1, ROWS - 1); row++) {
			for (int col = max(c - 1, 0); col <= min(c + 1, COLS - 1); col++) {
				final Node n = grid[row][col];
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
		boolean noSolution = false;
		background(0);

		if (openSet.isEmpty() == false) {
			// Nodes in open set have to be checked
			// find node with lowest f-score
			Node current = openSet.stream().min(Comparator.comparing(Node::getF)).get();

			if (current == end) {
				// finished
				System.out.println("DONE");
				noLoop();
			}

			// add this node with the lowest f-score to the closed set
			closedSet.add(current);
			openSet.remove(current);

			// get neighbors of current, which are not in the closed Set
			final List<Node> neighbors = getNeighbors(current.getRow(), current.getCol());
			for (Node n : neighbors) {
				// calculate g score for neighbor (distance from current to neighbor)
				float tempG = current.g + distanceToNeighbor(current, n);

				if (openSet.contains(n) == false) {
					// we found a new neighbor - add it to the open set
					n.g = tempG;
					openSet.add(n);
				} else {
					// neighbor has already been visited
					if (tempG < n.getG()) {
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
			if (noSolution == false) {
				solution.clear();
				Node n = current;
				while (n.previous != null) {
					solution.add(n);
					n = n.previous;
				}
				solution.add(start);
			}

		} else {
			// openset is empty - finished / no solution
			System.out.println("openset empty - no solution found");
			noSolution = true;
			noLoop();
		}

		// draw nodes
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				grid[row][col].draw(this, new Color(250, 250, 250, 20));
			}
		}

		// draw open / closed sets
		for (Node n : openSet) {
			n.draw(this, new Color(0, 120, 255));
		}
		for (Node n : closedSet) {
			n.draw(this, Color.RED);
		}

		// draw solution
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
			text("No solution found", width / 2, height / 2);
		}

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
		if (key != ' ') {
			return;
		}
		// reset everything
		noLoop();
		initialize();
	}
}

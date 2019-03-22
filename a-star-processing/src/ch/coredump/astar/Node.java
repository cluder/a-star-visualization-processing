package ch.coredump.astar;

import java.awt.Color;

import processing.core.PApplet;
import processing.core.PConstants;

public class Node {
	// position
	int row;
	int col;
	float posX;
	float posY;
	boolean blocked;

	float size = 10;
	// cost
	float f = 0;
	// cost from start
	float g = 0;
	// cost till end
	float h = 0;
	// the node we came from, when searching the path
	public Node previous;

	public Node(int row, int col, float x, float y) {
		this.posX = x;
		this.posY = y;
		this.row = row;
		this.col = col;
	}

	public void draw(PApplet p, Color c) {
		final float cellWidth = p.width / AStarMain.ROWS;
		final float cellheight = p.height / AStarMain.COLS;

		p.push();

		p.rectMode(PConstants.CENTER);
		if (blocked) {
			// simply a black rectangle
			p.fill(170);
			p.stroke(90);
			p.rect(posX, posY, cellWidth * 0.9f, cellheight * 0.9f);
		} else {
//			colored circle
			p.strokeWeight(2);
			p.stroke(c.getRGB(), 100);
//			p.noFill();
			p.fill(c.getRGB(), 70);
			p.circle(posX, posY, cellWidth / 2);
		}

		p.pop();
	}

	public float getF() {
		return f;
	}

	public float getG() {
		return g;
	}

	public float getH() {
		return h;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	@Override
	public String toString() {
		return String.format("Node row:%s, col:%s, f:%s g:%s h:%s", row, col, f, g, h);
	}
}

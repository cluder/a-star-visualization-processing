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
		final float cellWidth = p.width / AStarMain.COLS;
		final float cellheight = p.height / AStarMain.ROWS;

		p.push();

		p.rectMode(PConstants.CENTER);
		if (blocked) {
			// simply a black rectangle
			p.noStroke();
			p.fill(255, 150);
			p.rect(posX, posY, cellWidth * 0.8f, cellheight * 0.8f);
			p.fill(255, 50);
			p.rect(posX, posY, cellWidth * 0.95f, cellheight * 0.95f);
		} else {
			p.strokeWeight(1);
			p.stroke(c.getRGB());
			p.fill(c.getRGB(), 100);
			p.circle(posX, posY, cellWidth / 2);
		}

		p.pop();
	}

	@Override
	public String toString() {
		return String.format("Node row:%s, col:%s, f:%s g:%s h:%s", row, col, f, g, h);
	}
}

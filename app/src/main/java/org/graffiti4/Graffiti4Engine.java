package org.graffiti4;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * single stroke gesture recognizer based on the thin algorithm described at
 * https://jackschaedler.github.io/handwriting-recognition/
 */
public class Graffiti4Engine {
	public static final char NULL_CHAR = ' ';

	/**
	 * Graffiti dimensions class
	 */
	public static class Dimension {
		// Dimensions of the graffiti
		public int minX, minY, maxX, maxY;
	}

	public Dimension getDimension() {
		Dimension dimension = new Dimension();
		getDimension(dimension, pointsX, pointsY, pointsCount);
		return dimension;
	}

	/**
	 * @return count of currently registered points for a Gesture
	 */
	public int getPointsCount() {
		return pointsCount;
	}

	/**
	 * @param ix      index of point between 0 and {@link #getPointsCount()}
	 * @param pointXY int[2] array to set points [x,y]
	 */
	public void getPoint(int ix, int[] pointXY) {
		pointXY[0] = pointsX[ix];
		pointXY[1] = pointsY[ix];
	}

	/**
	 * start gesture with point (x,y)
	 *
	 * @param x
	 * @param y
	 */
	public void gestureStart(int x, int y) {
		pointsCount = 0;
		pointsX[pointsCount] = x;
		pointsY[pointsCount] = y;
		pointsCount++;
	}

	/**
	 * add next gesture point (x,y)
	 *
	 * @param x
	 * @param y
	 */
	public void gestureAdd(int x, int y) {
		if (pointsCount >= POINTS_SIZE) {
			// no more place for dots
			return;
		}
		pointsX[pointsCount] = x;
		pointsY[pointsCount] = y;
		pointsCount++;
	}

	/**
	 * finish gesture and create graffiti
	 */
	public void gestureEnd(int x, int y) {
		gestureAdd(x, y);
		thinPoints();
		lastGraffiti = calculateDirections(pointsX, pointsY, pointsCount);
	}

	public void registerLastGraffiti(char c) {
		if (lastGraffiti != null) {
			registerGraffiti(c, lastGraffiti);
		}
	}

	public String getGGraffitiString() {
		return lastGraffiti == null ? "" : lastGraffiti.toString();
	}

	public char getGraffitiChar() {
		if (lastGraffiti.size() < 1) {
			return '.';
		}
		if (lastGraffiti.size() == 1 && lastGraffiti.get(0) == -1) {
			return '.';
		}
		char result = NULL_CHAR;
		Node current = root;
		for (int i : lastGraffiti) {
			if (current == null) {
				return NULL_CHAR;
			}
			if (current.c != NULL_CHAR) {
				result = current.c;
			}
			current = current.next[i];
		}
		if (current != null && current.c != NULL_CHAR) {
			result = current.c;
		}
		return result;
	}

	public void loadFromReader(Reader reader) {
		Scanner scanner = new Scanner(reader);
		Arrays.fill(root.next, null);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] parts = line.split("=");
			if (parts.length != 2) {
				continue;
			}
			parts[0] = parts[0].trim();
			if (parts[0].length() != 1) {
				continue;
			}
			char c = parts[0].charAt(0);
			if (parts[1].trim().length() > 2) {
				parts[1] = parts[1].trim();
				parts[1] = parts[1].substring(1, parts[1].length() - 1);
			}
			String[] numbers = parts[1].split(",");
			List<Integer> list = new ArrayList<>(numbers.length);
			for (String s : numbers) {
				list.add(Integer.parseInt(s.trim(), 10));
			}
			registerGraffiti(c, list);
		}
		scanner.close();
	}

	public void saveToWriter(Writer writer) throws IOException {
		List<Integer> list = new ArrayList<>(100);
		saveRecursively(root, writer, list);
		writer.close();
	}

	// private area ------------------------------------------------------

	private static final int GUESTURE_LENGTH_TRESHOLD = 20;

	private static final int POINTS_SIZE = 500;

	private int pointsCount = 0;

	private final int[] pointsX = new int[POINTS_SIZE];

	private final int[] pointsY = new int[POINTS_SIZE];

	private static final int GUESTURE_MAX = 20;

	private List<Integer> lastGraffiti = null;

	private static final Node root = new Node(NULL_CHAR);

	private static class Node {

		char c;

		Node[] next = new Node[8];

		Node(char c) {
			this.c = c;
		}
	}

	private static class State {
		// current tracing direction
		int x, y, direction;
	}

	private void thinPoints() {
		if (pointsCount <= 2) {
			// nothing to thin
			return;
		}
		int t = GUESTURE_LENGTH_TRESHOLD >> 1;
		// arrayX, arrayY, arrayLength
		int ix = 0;
		for (int p = 2; p < pointsCount; p++) {
			int dx = pointsX[ix] - pointsX[p];
			int dy = pointsY[ix] - pointsY[p];
			if (dx > t || dx < -t || dy > t || dy < -t) {
				ix++;
				pointsX[ix] = pointsX[p];
				pointsY[ix] = pointsY[p];
			}
		}
		pointsCount = ix + 1;
	}

	private List<Integer> calculateDirections(int[] arrX, int[] arrY, int len) {
		// up to GUESTURE_MAX directions
		ArrayList<Integer> result = new ArrayList<>(GUESTURE_MAX);
		if (len < 3) {
			// dot
			result.add(-1);
			return result;
		}

		Dimension dimension = new Dimension();
		getDimension(dimension, arrX, arrY, len);
		{// first direction will be the starting position
			result.add(getDirection(arrX[0], arrY[0], dimension));
			// second direction will be the ending position
			result.add(getDirection(arrX[len - 1], arrY[len - 1], dimension));
		}

		State lastState = new State();
		lastState.x = arrX[0];
		lastState.y = arrY[0];
		lastState.direction = -1;
		// trace fluctuations
		int oldDiection = lastState.direction;
		for (int i = 1; i < len; i++) {
			calculateState(lastState, arrX[i], arrY[i]);
			int newDirection = lastState.direction;
			if (oldDiection != newDirection) {
				result.add(newDirection);
				if (result.size() >= GUESTURE_MAX) {
					return result;
				}
			}
			oldDiection = newDirection;
		}
		return result;
	}

	private int getDirection(int x, int y, Dimension d) {
		if (y <= d.minY + GUESTURE_LENGTH_TRESHOLD) {
			return UP;
		} else if (y >= d.maxY - GUESTURE_LENGTH_TRESHOLD) {
			return DOWN;
		} else if (x <= d.minX + GUESTURE_LENGTH_TRESHOLD) {
			return LEFT;
		} else if (x >= d.maxX - GUESTURE_LENGTH_TRESHOLD) {
			return RIGHT;
		} else {
			return UP;// consider center as UP_LEFT
		}
	}

	private final static int UP = 0;

	private final static int RIGHT = 1;

	private final static int DOWN = 2;

	private final static int LEFT = 3;

	private void calculateState(State state, int x, int y) {
		int dx = state.x - x;
		int dy = state.y - y;
		int ax = dx < 0 ? -dx : dx;
		int ay = dy < 0 ? -dy : dy;

		if (ax >= ay) {
			// Horizontal movement
			state.direction = (dx < 0) ? RIGHT : LEFT;
		} else {
			// vertical movement
			state.direction = (dy < 0) ? DOWN : UP;
		}
		state.x = x;
		state.y = y;
	}

	private void registerGraffiti(char c, List<Integer> lastGesture) {
		Node current = root;
		Node next = null;
		for (int i : lastGesture) {
			next = current.next[i];
			if (next == null) {
				next = current.next[i] = new Node(NULL_CHAR);
			}
			current = next;
		}
		if (next != null) {
			next.c = c;
		}
	}

	private static void getDimension(Dimension dimension, int[] arrayX, int[] arrayY, int len) {
		if (len > 0) {
			dimension.minX = dimension.maxX = arrayX[0];
			dimension.minY = dimension.maxY = arrayY[0];
			for (int i = 1; i < len; i++) {
				{
					int x = arrayX[i];
					if (dimension.minX > x) {
						dimension.minX = x;
					}
					if (dimension.maxX < x) {
						dimension.maxX = x;
					}
				}
				{
					int y = arrayY[i];

					if (dimension.minY > y) {
						dimension.minY = y;
					}
					if (dimension.maxY < y) {
						dimension.maxY = y;
					}
				}
			}
		}
	}

	private static void saveRecursively(Node node, Writer writer, List<Integer> list) throws IOException {
		for (int i = 0; i < node.next.length; i++) {
			if (node.next[i] != null) {
				list.add(i);
				saveRecursively(node.next[i], writer, list);
				list.remove(list.size() - 1);
			}
		}
		if (node.c != NULL_CHAR) {
			writer.write(node.c + "=" + list.toString() + "\n");
		}
	}
}
package qh.q3d;

import qh.math.Vector;

/**
 * A Class used to store the order in which vertices shall be drawn
 * @author jackl_000
 *
 */
public class Triangle {
	public int A, B, C;
	public Vector normal;
	public Vector center;
	/**
	 * @param a
	 * @param b
	 * @param c
	 */
	public Triangle(int a, int b, int c) {
		A = a;
		B = b;
		C = c;
		normal = null;
		center = null;
	}
	
	/**
	 * Normal is defined as the cross product of (b-a) and (c-a)
	 * @param a Vertex a
	 * @param b Vertex b
	 * @param c Vertex c
	 */
	public void calcNormal(Vector a, Vector b, Vector c) {
		normal = Vector.sub(b, a).cross(Vector.sub(c,a));
		normal.W = 0;
	}
	
	public void calcCenter(Vector a, Vector b, Vector c) {
		center = Vector.add(a,b).add(c).mul(0.3333333333333);
	}
}

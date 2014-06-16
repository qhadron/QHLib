/**
 * 
 */
package qh.q3d.objects;

import qh.q3d.Triangle;
import qh.q3d.Vertex;

/**
 * @author jackl_000
 *
 */
public class Mountain extends Object3D {

	public Mountain(int peaks, double baseHeight, double height) {
		super("Mountain", peaks + 4, peaks + 2);
		vertices[0] = new Vertex(-1, 0,0);
		vertices[1] = new Vertex(-0.9, baseHeight,0);
		for (int i = 2; i < vertices.length - 2; ++i) {
			vertices[i] = new Vertex( ((double)1.8 / peaks) * i - 1.1, baseHeight + Math.random() * height, 0);
		}
		vertices[peaks+1] = new Vertex(0.9, baseHeight,0);
		vertices[peaks+2] = new Vertex(1, 0, 0);
		for (int i = 0; i < peaks + 2; ++i) {
			triangles[i] = new Triangle (0,i,i+1);
		}
	}
}
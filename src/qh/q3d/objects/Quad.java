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
public class Quad extends Object3D {
	/**
	 * @param name
	 * @param verticiesCount
	 * @param facesCount
	 */
	public Quad() {
		super("quad", 4, 2);
		vertices[0] = new Vertex(-1, 0, 1);
		vertices[1] = new Vertex(1, 0, 1);
		vertices[2] = new Vertex(1, 0, -1);
		vertices[3] = new Vertex(-1, 0, -1);
		triangles[0] = new Triangle(2, 3, 0);
		triangles[1] = new Triangle(0, 1, 2);
	}

}

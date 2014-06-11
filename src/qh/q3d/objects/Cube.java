package qh.q3d.objects;

import qh.q3d.Object3D;
import qh.q3d.Triangle;
import qh.q3d.Vertex;


public class Cube extends Object3D {
	
	public Cube() {
		super("Cube",8, 12);
		vertices[0] = new Vertex(-1, 1, 1);
		vertices[1] = new Vertex(1, 1, 1);
		vertices[2] = new Vertex(-1, -1, 1);
		vertices[3] = new Vertex(1, -1, 1);
		vertices[4] = new Vertex(-1, 1, -1);
		vertices[5] = new Vertex(1, 1, -1);
		vertices[6] = new Vertex(1, -1, -1);
		vertices[7] = new Vertex(-1, -1, -1);
		triangles[0] = new Triangle ( 0, 1, 2 );
		triangles[1] = new Triangle ( 1, 2, 3 );
		triangles[2] = new Triangle ( 1, 3, 6 );
		triangles[3] = new Triangle ( 1, 5, 6 );
		triangles[4] = new Triangle ( 0, 1, 4 );
		triangles[5] = new Triangle ( 1, 4, 5 );

		triangles[ 6] = new Triangle ( 2, 3, 7 );
		triangles[ 7] = new Triangle ( 3, 6, 7 );
		triangles[ 8] = new Triangle ( 0, 2, 7 );
		triangles[ 9] = new Triangle ( 0, 4, 7 );
		triangles[10] = new Triangle ( 4, 5, 6 );
		triangles[11] = new Triangle ( 4, 6, 7 );
	}
}

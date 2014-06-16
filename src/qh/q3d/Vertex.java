/**
 * 
 */
package qh.q3d;

import java.awt.Color;
import java.awt.Container;

import qh.math.Matrix;
import qh.math.Vector;

/**
 * @author jackl_000
 *
 */
public class Vertex extends Vector {
	public static final Color DEFAULT_COLOR = new Color(1.0f,0.3f,0.0f,1.0f);
	public Color color;
	
	public Vertex() {
		color = DEFAULT_COLOR;
	}
	
	public Vertex(Color c) {
		color = c;
	}
	
	public Vertex(Vector vec, Color c) {
		super(vec);
		color =c;
	}
	
	public Vertex(double x, double y, double z) {
		super(x,y,z);
		color = DEFAULT_COLOR;
	}
	
	public Vertex(double x, double y, double z, double w) {
		super(x,y,z,w);
		color = DEFAULT_COLOR;
	}
	
	public Vertex(double x, double y, double z, Color c) {
		super(x,y,z);
		color = c;
	}
	
	public Vertex(double x, double y, double z, double w, Color c) {
		super(x,y,z,w);
		color = c;
	}
	
	public Vertex(Vertex src) {
		super(src);
		color = src.color;
	}

	public static Vertex mul(Matrix mat, Vertex vec) {
		return new Vertex(vec).mul2(mat);
	}
	
	public Vertex mul2(Matrix mat) {
		double tx, ty, tz, tw;
		tx = mat.m[ 0] * X + mat.m[ 1] * Y + mat.m[ 2] * Z + mat.m[ 3] * W;
		ty = mat.m[ 4] * X + mat.m[ 5] * Y + mat.m[ 6] * Z + mat.m[ 7] * W;
		tz = mat.m[ 8] * X + mat.m[ 9] * Y + mat.m[10] * Z + mat.m[11] * W;
		tw = mat.m[12] * X + mat.m[13] * Y + mat.m[14] * Z + mat.m[15] * W;
		X = tx;
		Y = ty;
		Z = tz;
		W = tw;
		return this;
	}
	

	public static Vertex transformCoords(Vertex vec, Matrix mat) {
		Vertex res = Vertex.mul(mat,vec);
		res.X /= res.W;
		res.Y /= res.W;
		res.Z /= res.W;
		return res;
	}

	public static Vertex add(Vertex p1, Vertex p2) {
		return new Vertex(p1).add2(p2);
	}
	
	public Vertex add2(Vertex other) {
		super.add(other);
		return this;
	}
}

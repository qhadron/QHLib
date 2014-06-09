package qh.q3d;

public class Vector {
	public static final double INF = 1e10;
	public static final Vector unitX = new Vector(1.0,0.0,0.0,0.0);
	public static final Vector unitY = new Vector(0.0,1.0,0.0,0.0);
	public static final Vector unitZ = new Vector(0.0,0.0,1.0,0.0);

	public static Vector cross(Vector a, Vector b) {
		return new Vector(a.Y * b.Z - a.Z * b.Y, // X
				a.Z * b.X - a.X * b.Z, // Y
				a.X * b.Y - a.Y * b.X, // Z
				Math.max(a.W, b.W));
	}

	public static double dot(Vector a, Vector b) {
		return a.X * b.X + a.Y * b.Y + a.Z * b.Z;
	}

	public static double length(Vector vec) {
		return Math.sqrt(vec.X * vec.X + vec.Y * vec.Y + vec.Z * vec.Z);
	}

	public static Vector normal(Vector src) {
		return new Vector(src).divide(src.length());
	}

	public static Vector mul(Matrix mat, Vector vec) {
		return new Vector(vec).mul(mat);
	}
	
	public static Vector mul(Vector vec, double x) {
		return new Vector(vec).mul(x);
	}
	
	/**
	 * Column major matrix <br>
	 * |_00__04__08__12_| <br>
	 * |_01__05__09__13_| <br>
	 * |_02__06__10__14_| <br>
	 * |_03__07__11__15_| .
	 * 
	 * @author jackl_000
	 * 
	 */
	public Vector mul(Matrix mat) {
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

	public double X, Y, Z, W;

	public Vector() {
		X = Y = Z = 0;
		W = 1.0;
	}

	public Vector(double x, double y, double z, double w) {
		X = x;
		Y = y;
		Z = z;
		W = w;
	}
	
	public Vector(double x, double y, double z) {
		X = x;
		Y = y;
		Z = z;
		W = 1.0;
	}
	
	public Vector(Vector src) {
		X = src.X;
		Y = src.Y;
		Z = src.Z;
		W = src.W;
	}

	public Vector add(double x) {
		this.X += x;
		this.Y += x;
		this.Z += x;
		this.W += x;
		return this;
	}

	public Vector add(Vector other) {
		this.X += other.X;
		this.Y += other.Y;
		this.Z += other.Z;
		this.W = other.W;
		return this;
	}

	public Vector cross(Vector other) {
		double tx, ty, tz;
		tx = Y * other.Z - Z * other.Y;
		ty = Z * other.X - X * other.Z;
		tz = X * other.Y - Y * other.X;
		X = tx;
		Y = ty;
		Z = tz;
		return this;
	}
	
	public Vector add(double x, double y, double z) {
		X += x;
		Y += y;
		Z += z;
		return this;
	}
	
	public Vector sub(double x, double y, double z) {
		X -= x;
		Y -= y;
		Z -= z;
		return this;
	}
	
	public Vector divide(double x) {
		if (x == 0) {
			X *= INF;
			Y *= INF;
			Z *= INF;
			W *= INF;
			return this;
		}
		X /= x;
		Y /= x;
		Z /= x;
		W /= x;
		return this;
	}

	public Vector divide(Vector other) {
		if (other.X == 0)
			X *= INF;
		else
			X /= other.X;
		if (other.Y == 0)
			Y *= INF;
		else
			Y /= other.Y;
		if (other.Z == 0)
			Z *= INF;
		else
			Z /= other.Z;
		if (other.W == 0)
			W *= INF;
		else
			W /= other.W;
		return this;
	}

	public double dot(Vector other) {
		return X * other.X + Y * other.Y + Z * other.Z;
	}

	public double length() {
		return Vector.length(this);
	}

	public Vector mul(double x) {
		X *= x;
		Y *= x;
		Z *= x;
		return this;
	}

	public Vector mul(Vector other) {
		X *= other.X;
		Y *= other.Y;
		Z *= other.Z;
		W *= other.W;
		return this;
	}

	public Vector normalize() {
		double length = this.length();
		if (length != 0) {
			X /= length;
			Y /= length;
			Z /= length;
		}
		return this;
	}

	public void set(double x, double y, double z) {
		X = x;
		Y = y;
		Z = z;
	}
	
	public void set(double x, double y, double z, double w) {
		X = x;
		Y = y;
		Z = z;
		W = w;
	}

	public Vector sub(double x) {
		this.X -= x;
		this.Y -= x;
		this.Z -= x;
		this.W -= x;
		return this;
	}

	public Vector sub(Vector other) {
		this.X -= other.X;
		this.Y -= other.Y;
		this.Z -= other.Z;
		return this;
	}

	@Override
	public String toString() {
		return String.format("Vector (%.2f, %.2f, %.2f, %.2f)", X, Y, Z, W);
	}

	public static Vector transformCoords(Vector vec, Matrix mat) {
		Vector res = Vector.mul(mat,vec);
		res.X /= res.W;
		res.Y /= res.W;
		res.Z /= res.W;
		return res;
	}

	public void set(Vector pos) {
		this.X = pos.X;
		this.Y = pos.Y;
		this.Z = pos.Z;
		this.W = pos.W;
	}

	public static Vector sub(Vector a, Vector b) {
		return new Vector(a).sub(b);
	}
	

}

package qh.math;

import qh.q3d.MathHelper;

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
public class Matrix {
	public double[] m;

	/**
	 * PI/180
	 */
	private static final double DEGTORAD = Math.PI / 180.0;
	/**
	 * 180/PI
	 */
	private static final double RADTODEG = 180.0 / Math.PI;

	/**
	 * Constructs an identity matrix
	 */
	public Matrix() {
		m = new double[16];
		for (int i = 0; i < 16; ++i)
			m[i] = (i == 0 || i == 5 || i == 10 || i == 15) ? 1 : 0;
	}

	public Matrix(Matrix src) {
		m = new double[16];
		for (int i = 0; i < 16; ++i) {
			m[i] = src.m[i];
		}
	}

	public static Matrix identity() {
		return new Matrix();
	}
	
	public static Matrix translate(double x, double y, double z) {
		Matrix matrix = new Matrix();
		matrix.m[12] = x;
		matrix.m[13] = y;
		matrix.m[14] = z;
		return matrix;
	}

	public static Matrix translate(Vector pos) {
		Matrix matrix = new Matrix();
		matrix.m[3] = pos.X;
		matrix.m[7] = pos.Y;
		matrix.m[11] = pos.Z;
		return matrix;
	}

	public static Matrix scale(double x, double y, double z) {
		Matrix matrix = new Matrix();
		matrix.m[0] = x;
		matrix.m[5] = y;
		matrix.m[10] = z;
		return matrix;
	}

	public static Matrix scale(Vector size) {
		Matrix matrix = new Matrix();
		matrix.m[0] = size.X;
		matrix.m[5] = size.Y;
		matrix.m[10] = size.Z;
		return matrix;
	}

	public static Matrix rotationX(double radians) {
		Matrix matrix = new Matrix();
		matrix.m[5] = Math.cos(radians);
		matrix.m[9] = Math.sin(radians);
		matrix.m[6] = -matrix.m[9];
		matrix.m[10] = matrix.m[5];
		return matrix;
	}

	public static Matrix rotationY(double radians) {
		Matrix matrix = new Matrix();
		matrix.m[0] = Math.cos(radians);
		matrix.m[2] = Math.sin(radians);
		matrix.m[8] = -matrix.m[2];
		matrix.m[10] = matrix.m[0];
		return matrix;
	}

	public static Matrix rotationZ(double radians) {
		Matrix matrix = new Matrix();
		matrix.m[0] = Math.cos(radians);
		matrix.m[4] = Math.sin(radians);
		matrix.m[1] = -matrix.m[4];
		matrix.m[5] = matrix.m[0];
		return matrix;
	}

	public static Matrix rotationYXZ(double yaw, double pitch, double roll) {
		return Matrix.rotationY(yaw).mul(
				Matrix.rotationX(pitch).mul(Matrix.rotationZ(roll)));
	}

	public static Matrix rotationYXZ(Vector rotation) {
		return Matrix.rotationYXZ(rotation.Y, rotation.X, rotation.Z);
	}
	
	public static Matrix rotationZXY(double roll, double pitch, double yaw) {
		return Matrix.rotationZ(roll).mul(
				Matrix.rotationX(pitch).mul(Matrix.rotationY(yaw)));
	}

	public static Matrix rotationZXY(Vector rotation) {
		return Matrix.rotationZXY(rotation.Z, rotation.X, rotation.Y);
	}
	
	public static Matrix rotationYZX(double yaw, double roll, double pitch) {
		return Matrix.rotationY(yaw).mul(
				Matrix.rotationZ(roll).mul(Matrix.rotationX(pitch)));
	}

	public static Matrix rotationYZX(Vector rotation) {
		return Matrix.rotationYZX(rotation.Y, rotation.Z, rotation.X);
	}

	public static Matrix rotationZYX(double roll, double yaw, double pitch) {
		return Matrix.rotationZ(roll).mul(
				Matrix.rotationY(yaw).mul(Matrix.rotationX(pitch)));
	}

	public static Matrix rotationZYX(Vector rotation) {
		return Matrix.rotationZYX(rotation.Z, rotation.Y, rotation.X);
	}
	
	
	public static Matrix rotationXYZ(double pitch, double yaw, double roll) {
		return Matrix.rotationX(pitch).mul(
				Matrix.rotationY(yaw).mul(Matrix.rotationZ(roll)));
	}

	public static Matrix rotationXYZ(Vector rotation) {
		return Matrix.rotationXYZ(rotation.X, rotation.Y, rotation.Z);
	}

	
	public Matrix mul(Matrix other) {
		this.setData(Matrix.mul(this, other));
		return this;
	}
	
	public static Matrix projectFrustum(double left, double right,
			double bottom, double top, double near, double far) {
		Matrix res = new Matrix();
		double x = 2 * near / (right - left);
		double y = 2 * near / (top - bottom);

		double a = (right + left) / (right - left);
		double b = (top + bottom) / (top - bottom);
		double c = -(far + near) / (far - near);
		double d = -2 * far * near / (far - near);
		res.m[0] = x;
		res.m[4] = 0;
		res.m[8] = a;
		res.m[12] = 0;
		res.m[1] = 0;
		res.m[5] = y;
		res.m[9] = b;
		res.m[13] = 0;
		res.m[2] = 0;
		res.m[6] = 0;
		res.m[10] = c;
		res.m[14] = d;
		res.m[3] = 0;
		res.m[7] = 0;
		res.m[11] = -1;
		res.m[15] = 0;
		return res;
	}

	/**
	 * @param near
	 *            near clipping space
	 * @param far
	 *            far clipping space
	 * @param fov
	 *            field of view in degrees
	 * @param aspect
	 *            aspect ratio (width/height)
	 * @return a perspective projection matrix
	 */
	public static Matrix projectPerspectiveRH(double fov, double aspect,
			double near, double far) {
		double ymax = near * Math.tan((fov) * 0.5);
		double ymin = -ymax;
		double xmin = ymin * aspect;
		double xmax = ymax * aspect;
		return projectFrustum(xmin, xmax, ymin, ymax, near, far);
	}

	/**
	 * Creates a perspective matrix
	 * 
	 * @param fov
	 *            field of view in radians
	 * @param aspect
	 *            screen width / screen height
	 * @param znear
	 *            near clipping plane
	 * @param zfar
	 *            far clipping plane
	 * @return a new perspective matrix
	 */
	public static Matrix projectPerspectiveLH(double fov, double aspect,
			double znear, double zfar) {
		Matrix matrix = new Matrix();
		double tan = 1.0 / (Math.tan((fov) * 0.5));
		matrix.m[0] = tan / aspect;
		matrix.m[5] = tan;
		matrix.m[10] = -zfar / (znear - zfar);
		matrix.m[14] = 1.0;
		matrix.m[11] = (znear * zfar) / (znear - zfar);
		matrix.m[15] = 0;
		return matrix;
	}

	public static double toDegrees(double radians) {
		return radians * RADTODEG;
	}

	public static double toRadians(double degrees) {
		return degrees * DEGTORAD;
	}

	public static Matrix mul(Matrix a, Matrix b) {
		// Hard-coded for speed;
		Matrix result = new Matrix();
		// Fisrt Column
		result.m[0] = a.m[0] * b.m[0] + a.m[4] * b.m[1] + a.m[8] * b.m[2]
				+ a.m[12] * b.m[3];
		result.m[1] = a.m[1] * b.m[0] + a.m[5] * b.m[1] + a.m[9] * b.m[2]
				+ a.m[13] * b.m[3];
		result.m[2] = a.m[2] * b.m[0] + a.m[6] * b.m[1] + a.m[10] * b.m[2]
				+ a.m[14] * b.m[3];
		result.m[3] = a.m[3] * b.m[0] + a.m[7] * b.m[1] + a.m[11] * b.m[2]
				+ a.m[15] * b.m[3];

		// Second Column
		result.m[4] = a.m[0] * b.m[4] + a.m[4] * b.m[5] + a.m[8] * b.m[6]
				+ a.m[12] * b.m[7];
		result.m[5] = a.m[1] * b.m[4] + a.m[5] * b.m[5] + a.m[9] * b.m[6]
				+ a.m[13] * b.m[7];
		result.m[6] = a.m[2] * b.m[4] + a.m[6] * b.m[5] + a.m[10] * b.m[6]
				+ a.m[14] * b.m[7];
		result.m[7] = a.m[3] * b.m[4] + a.m[7] * b.m[5] + a.m[11] * b.m[6]
				+ a.m[15] * b.m[7];

		// Third Column
		result.m[8] = a.m[0] * b.m[8] + a.m[4] * b.m[9] + a.m[8] * b.m[10]
				+ a.m[12] * b.m[11];
		result.m[9] = a.m[1] * b.m[8] + a.m[5] * b.m[9] + a.m[9] * b.m[10]
				+ a.m[13] * b.m[11];
		result.m[10] = a.m[2] * b.m[8] + a.m[6] * b.m[9] + a.m[10] * b.m[10]
				+ a.m[14] * b.m[11];
		result.m[11] = a.m[3] * b.m[8] + a.m[7] * b.m[9] + a.m[11] * b.m[10]
				+ a.m[15] * b.m[11];

		// Fourth Column
		result.m[12] = a.m[0] * b.m[12] + a.m[4] * b.m[13] + a.m[8] * b.m[14]
				+ a.m[12] * b.m[15];
		result.m[13] = a.m[1] * b.m[12] + a.m[5] * b.m[13] + a.m[9] * b.m[14]
				+ a.m[13] * b.m[15];
		result.m[14] = a.m[2] * b.m[12] + a.m[6] * b.m[13] + a.m[10] * b.m[14]
				+ a.m[14] * b.m[15];
		result.m[15] = a.m[3] * b.m[12] + a.m[7] * b.m[13] + a.m[11] * b.m[14]
				+ a.m[15] * b.m[15];
		return result;
	}

	public static Matrix lookAtLH(Vector eye, Vector target, Vector up) {
		Matrix res = new Matrix();
		Vector zaxis = Vector.sub(target, eye).normalize();
		if (zaxis.length() == 0)
			zaxis.Z = 1;
		Vector xaxis = Vector.cross(up, zaxis).normalize();
		if (xaxis.length() == 0) {
			zaxis.X += 0.000001;
			xaxis = Vector.cross(up, zaxis).normalize();
		}
		Vector yaxis = Vector.cross(zaxis, xaxis);

		res.m[0] = xaxis.X;
		res.m[1] = xaxis.Y;
		res.m[2] = xaxis.Z;
		res.m[3] = -Vector.dot(xaxis, eye);

		res.m[4] = yaxis.X;
		res.m[5] = yaxis.Y;
		res.m[6] = yaxis.Z;
		res.m[7] = -Vector.dot(yaxis, eye);

		res.m[8] = zaxis.X;
		res.m[9] = zaxis.Y;
		res.m[10] = zaxis.Z;
		res.m[11] = -Vector.dot(zaxis, eye);

		// already set during initialization of matrix
		/*
		 * res.m[12] = 0; res.m[13] = 0; res.m[14] = 0; res.m[15] = 1;
		 */
		return res;
	}

	/**
	 * Generates a view matrix for a first person camera looking towards positive-Z by default
	 * <br>(Rotations in order Y-X-Z)
	 * @param eye position of the camera
	 * @param rotation rotation of the camera
	 * @return a view matrix representing the camera transformations
	 */
	public static Matrix FPViewLH(Vector eye, Vector rotation) {
		Vector rot = new Vector(rotation);
		rot.Y = -rot.Y;
		return Matrix.translate(Vector.mul(eye, -1)).mul(Matrix.rotationZYX(rot));
	}

	/**
	 * Generates a view matrix for a first person camera looking towards positive-Z by default
	 * <br>(Rotations in order Y-X-Z)
	 * @param eye position of the camera
	 * @param pitch rotation around X
	 * @param yaw rotation around Y
	 * @param roll rotation around Z
	 * @return a view matrix representing the camera transformations
	 */
	public static Matrix FPViewLH(Vector eye, double pitch, double yaw, double roll) {
		return  Matrix.translate(Vector.mul(eye, -1)).mul(Matrix.rotationYXZ(-yaw, pitch, roll));
	}
	
	public Matrix setData(Matrix src) {
		for (int i = 0; i < src.m.length; ++i) {
			m[i] = src.m[i];
		}
		return this;
	}

	public Matrix setData(double[] src) {
		for (int i = 0; i < src.length; ++i) {
			m[i] = src[i];
		}
		return this;
	}

	public Matrix setData(double[][] src) {
		for (int column = 0, cnt = 0; column < 4; ++column) {
			for (int row = 0; row < 4; ++row) {
				m[cnt++] = src[column][row];
			}
		}
		return this;
	}

	public String toString() {
		String str = "";
		for (int x = 0; x < 4; x++) 
		for (int y = 0; y < 4; y++){
				str +=  MathHelper.round(m[y + x * 4],2) + "\t";
				if (y == 3)
					str += "\n";
			}
		return str;
	};
	
	public static Matrix transpose(Matrix src) {
		return new Matrix(src).transpose();
	}
	
	public Matrix transpose() {
		double temp;
		temp = m[1];
		m[1] = m[4];
		m[4] = temp;
		
		temp = m[2];
		m[2] = m[8];
		m[8] = temp;
		
		temp = m[6];
		m[6] = m[9];
		m[9] = temp;
		
		temp  = m[ 7];
		m[ 7] = m[13];
		m[13] = temp;
		
		temp  = m[ 3];
		m[ 3] = m[12];
		m[12] = temp;
		
		temp  = m[11];
		m[11] = m[14];
		m[14] = temp;

		return this;
	}


}

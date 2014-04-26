package qh.qmath;

public class vec3 {
	public double x, y, z;

	public vec3() {
		x = 0;
		y = 0;
		z = 0;
	}

	public vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public vec3 mul(double other) {
		x *= other;
		y *= other;
		z *= other;
		return this;
	}

	public vec3 add(vec3 other) {
		x += other.x;
		y += other.y;
		z += other.z;
		return this;
	}

	public vec3 sub(vec3 other) {
		x -= other.x;
		y -= other.y;
		z -= other.z;
		return this;
	}

	public vec3 mul(mat3 other) {
		return new vec3(x * other.m00 + y * other.m01 + z * other.m02, x
				* other.m10 + y * other.m11 + z * other.m12, x * other.m20 + y
				* other.m21 + z * other.m22);
	}

	@Override
	public String toString() {
		return "x: " + x + "\ty:" + y + "\tz:" + z;
	}
}

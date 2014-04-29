package qh.qmath;

/**
 * @author jackl_000
 *	Row major matrix
 */
public class mat3 {
	public double m00,m01,m02,m10,m11,m12,m20,m21,m22;
	
	public mat3(mat3 other) {
		m00 = other.m00;
		m01 = other.m01;
		m02 = other.m02;
		m10 = other.m10;
		m11 = other.m11;
		m12 = other.m12;
		m20 = other.m20;
		m21 = other.m21;
		m22 = other.m22;
	}
	
	public mat3() {
		m00 = m11 = m22 = 1;
		m01 = m02 = m10 = m12 = m20 = m21 = 0;
	}
	
	public mat3(double a, double b, double c, double d) {
		m00 = a; m01 = b; m10 = c; m11 = d;
		m02 = m12 = m20 = m21 = 0;
		m22 = 1;
	}
	
	
	
	public mat3(double m00, double m01, double m02, double m10, double m11,
			double m12, double m20, double m21, double m22) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
	}

	public static mat3 getIdentity() {
		return new mat3();
	}
	
	public static mat3 getTranslation(double x, double y) {
		return new mat3(1,0,x,0,1,y,0,0,1);
	}
	
	public static mat3 getRotatation(double angle) {
		return new mat3(Math.cos(angle),-Math.sin(angle),Math.sin(angle),Math.cos(angle));
	}
	
	public static mat3 getScale(double valx, double valy) {
		return new mat3(valx,0,0,valy);
	}
	
	public mat3 setTranslation(double x, double y) {
		m02 = x;
		m12 = y;
		return this;
	}
	
	public mat3 setScale(double valx, double valy) {
		m00 = valx;
		m11 = valy;
		return this;
	}
	
	public mat3 setRotation(double angle) {
		double cos = Math.cos(angle), sin = Math.sin(angle);
		m01 = m00 *(-sin);
		m00 = m00 * cos;
		m10 = m11 * sin;
		m11 = m11 * cos;
		return this;
	}
	
	public mat3 mul(mat3 other) {
		 mat3 result = new mat3();
		 result.m00 = m00 * other.m00 + m01*other.m10 + m02 * other.m20;
		 result.m01 = m00 * other.m01 + m01*other.m11 + m02 * other.m21;
		 result.m02 = m00 * other.m02 + m01*other.m12 + m02 * other.m22;
		 
		 result.m10 = m10 * other.m00 + m11*other.m10 + m12 * other.m20;
		 result.m11 = m10 * other.m01 + m11*other.m11 + m12 * other.m21;
		 result.m12 = m10 * other.m02 + m11*other.m12 + m12 * other.m22;
		 
		 result.m20 = m20 * other.m00 + m21*other.m10 + m22 * other.m20;
		 result.m21 = m20 * other.m01 + m21*other.m11 + m22 * other.m21;
		 result.m22 = m20 * other.m02 + m21*other.m12 + m22 * other.m22;
		 
		 return result;
	}
	
	
	public mat3 mul(double x) {
		m00*=x;m01*=x;m10*=x;m11*=x;
		return this;
	}
	
	public String toString() {
		return String.format("%.4f %.4f %.4f\n%.4f %.4f %.4f\n%.4f %.4f %.4f", m00,m01,m02,m10,m11,m12,m20,m21,m22);
	}

	public void clear() {
		m00 = m11 = m22 = 1;
		m01 = m02 = m10 = m12 = m20 = m21 = 0;
	}

}

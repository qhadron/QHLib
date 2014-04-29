/**
 * 
 */
package qh.qmath;

/**
 * @author jackl_000
 *
 */
public class Camera2D {
	public mat3 data;
	double width, height;
	double xcenter,ycenter;
	public Camera2D(double width, double height) {
		this.width = width;
		this.height = height;
		xcenter = width/2;
		ycenter = height/2;
		data = new mat3();
	}
	
	public void translate(double x, double y) {
		data.m02 = -x;
		data.m12 = -y;
	}
	
	public void scale(double x, double y) {
		data.m00 = x;
		data.m11 = y;
	}
	
	public void scale(double n) {
		data.m00 = data.m11 = n;
	}
	
	public void rotate(double alpha) {
		double cos = Math.cos(-alpha), sin = Math.sin(-alpha);
		mat3 temp = new mat3(data);
		data.m00 = cos * temp.m00 - sin * temp.m10;
		data.m01 = cos * temp.m01 - sin * temp.m11;
		data.m02 = cos * temp.m02 - sin * temp.m12;
		
		data.m10 = sin * temp.m00 + cos * temp.m10;
		data.m11 = sin * temp.m01 + cos * temp.m11;
		data.m12 = sin * temp.m02 + cos * temp.m12;
		
		data.m20 = temp.m20;
		data.m21 = temp.m21;
		data.m22 = 1;
		
	}
}

/**
 * 
 */
package qh.q3d.objects;

import qh.math.Vector;

/**
 * @author jackl_000
 *
 */
public class Light extends Vector {
	
	public Vector color;
	
	/**
	 * default constructor
	 */
	public Light() {
		color = new Vector(1,1,1,1);
	}

	/**Creates a light at these coordinates
	 */
	public Light(double x, double y, double z, double w) {
		super(x, y, z, w);
		color = new Vector(1,1,1,1);
	}

	/**Creates a light at these coordinates
	 */
	public Light(double x, double y, double z) {
		super(x, y, z);
		color = new Vector(1,1,1,1);
	}

	/**Copy constructor
	 * @param src the source light
	 */
	public Light(Light src) {
		super(src);
		color = new Vector(src.color);
	}
	
	public Light(Vector pos) {
		super(pos);
		color = new Vector(1,1,1,1);
	}

	public Light setColor(double r, double g, double b) {
		color.set(r,g,b);
		return this;
	}
	
	public Light setColor(Vector src) {
		color.set(src);
		return this;
	}
	
}

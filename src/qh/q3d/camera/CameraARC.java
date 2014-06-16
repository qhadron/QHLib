/**
 * 
 */
package qh.q3d.camera;

import qh.math.Matrix;
import qh.math.Vector;

/**
 * @author jackl_000
 *
 */
public class CameraARC extends Camera{


	private Vector rotation;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CameraFP [position=" + position + ", rotation="
				+ rotation + ", velocity=" + velocity + "]";
	}

	public Matrix getViewMatrix() {
		if (viewChanged) {
			view = Matrix.rotationYXZ(rotation).mul(Matrix.lookAtLH(position, target, Vector.unitY));
			viewChanged = false;
		}
		return new Matrix(view);
	}

	/*public double ymax,ymin,xmax,xmin;*/
	public CameraARC(Vector position, Vector target, Vector rotation) {
		this.position = position;
		this.target = target;
		this.rotation = rotation;
		this.velocity = new Vector();
	}
	
	public CameraARC() {
		position = new Vector();
		target = new Vector();
		rotation = new Vector();
		velocity = new Vector();
	}

	/**
	 * @return the rotation
	 */
	public Vector getRotation() {
		return new Vector(rotation);
	}

	/**
	 * @param rotation the rotation to set
	 */
	public void setRotation(Vector rotation) {
		this.rotation.set(rotation);
		this.viewChanged = true;
	}
	
	public void setRotation(double x, double y, double z) {
		this.rotation.set(x,y,z);
		this.viewChanged = true;
	}
	
	public void rotate(Vector rotation) {
		this.rotation.add(rotation);
		this.viewChanged = true;
	}
	
	public void rotate(double x, double y, double z) {
		this.rotation.X += x;
		this.rotation.Y += y;
		this.rotation.Z += z;
		this.viewChanged = true;
	}
	
}
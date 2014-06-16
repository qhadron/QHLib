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
public class Camera {


	protected Vector position;
	protected Vector target;
	protected double fov, near, far, aspect;
	protected Matrix view, perspective;
	protected boolean viewChanged = true;
	protected boolean screenChanged = true;
	protected boolean velocityChanged = true;
	protected double speed;
	protected Vector velocity;
	

	/*public double ymax,ymin,xmax,xmin;*/
	public Camera(Vector position, Vector target) {
		this.position = position;
		this.target = target;
		this.velocity = new Vector();
	}
	
	public Camera() {
		position = new Vector();
		target = new Vector();
		velocity = new Vector();
	}

	public void setPosition(double x, double y, double z) {
		position.set(x, y, z);
		viewChanged = true;
	}
	
	public void translate(double x, double y, double z) {
		position.X += x;
		position.Y += y;
		position.Z += z;
		viewChanged = true;
	}
	
	public void setPosition(Vector pos) {
		position.set(pos);
		viewChanged = true;
	}
	
	public void setTarget(Vector pos) {
		target.set(pos);
		viewChanged = true;
		velocityChanged = true;
	}
	public void setTarget(double x, double y, double z) {
		target.set(x, y, z);
		viewChanged = true;
		velocityChanged = true;
	}
	
	public void translate(Vector pos) {
		position.add(pos);
		viewChanged = true;
	}
	
	public Vector getPosition() {
		return new Vector(position);
	}
	
	public Matrix getViewMatrix() {
		if (viewChanged) {
			view = Matrix.lookAtLH(position, target, Vector.unitY);
			viewChanged = false;
		}
		return new Matrix(view);
	}
	
	public Matrix getPerspectiveMatrix() {
		if (screenChanged) {
			perspective = Matrix.projectPerspectiveLH(fov, aspect, near, far);
			/*ymax = near * Math.tan((fov) * 0.5);
			ymin = -ymax;
			xmin = ymin * aspect;
			xmax = ymax * aspect;*/
			screenChanged = false;
		}
		return new Matrix(perspective);
	}

	/**
	 * @return the fov
	 */
	public double getFov() {
		return fov;
	}

	/**
	 * @param fov the fov to set
	 */
	public void setFov(double fov) {
		this.fov = fov;
		screenChanged = true;
	}

	/**
	 * @return the near
	 */
	public double getNear() {
		return near;
	}

	/**
	 * @param near the near to set
	 */
	public void setNear(double near) {
		this.near = near;
		screenChanged = true;
	}

	/**
	 * @return the far
	 */
	public double getFar() {
		return far;
	}

	/**
	 * @param far the far to set
	 */
	public void setFar(double far) {
		this.far = far;
		screenChanged = true;
	}
	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
		velocityChanged = true;
	}
	
	public void addSpeed(double val) {
		this.speed += val;
		velocityChanged = true;
	}
	
	public boolean update(double dt) {
		if (velocityChanged)
			velocity = Vector.sub(target,position).normalize().mul(speed);
		if (Math.abs(this.speed) > 1e-8)
			this.translate(Vector.mul(velocity, dt));
		return aspect != 0;
	}

	/**
	 * @return the aspect
	 */
	public double getAspect() {
		return aspect;
	}

	/**
	 * @param aspect the aspect to set
	 */
	public void setAspect(double aspect) {
		this.aspect = aspect;
		this.screenChanged = true;
	}
	
}
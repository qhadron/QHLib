/**
 * 
 */
package qh.q3d;

/**
 * @author jackl_000
 *
 */
public class Camera {


	private Vector position, target, rotation, targetRotation;
	private double fov, near, far;
	private Matrix view, perspective;
	private boolean viewChanged = true;
	private boolean screenChanged = true;
	private boolean velocityChanged = true;
	private double speed;
	private Vector velocity;
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Camera [position=" + position + ", target=" + target
				+ ", rotation=" + rotation + "]";
	}

	/*public double ymax,ymin,xmax,xmin;*/
	public Camera(Vector position, Vector target, Vector rotation) {
		this.position = position;
		this.target = target;
		this.rotation = rotation;
		this.velocity = new Vector();
		this.targetRotation = new Vector();
	}
	
	public Camera() {
		position = new Vector();
		target = new Vector();
		rotation = new Vector();
		velocity = new Vector();
		this.targetRotation = new Vector();
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
			view = Matrix.rotationYXZ(rotation).mul(Matrix.lookAt(position, Vector.mul(Matrix.rotationYXZ(targetRotation), target), Vector.unitY));
			viewChanged = false;
		}
		return new Matrix(view);
	}
	
	public Matrix getPerspectiveMatrix(double aspect) {
		if (screenChanged) {
			perspective = Matrix.projectPerspectiveLH(fov, aspect, near, far);
			/*ymax = near * Math.tan((fov) * 0.5);
			ymin = -ymax;
			xmin = ymin * aspect;
			xmax = ymax * aspect;*/
			screenChanged = false;
		}
		return perspective;
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
	
	/**
	 * @param rotation the rotation to set
	 */
	public void setTargetRotation(Vector rotation) {
		this.targetRotation.set(rotation);
		this.viewChanged = true;
		velocityChanged = true;
		
	}
	
	public void setTargetRotation(double x, double y, double z) {
		this.targetRotation.set(x,y,z);
		this.viewChanged = true;
		velocityChanged = true;
	}
	
	public void rotateTarget(Vector rotation) {
		this.targetRotation.add(rotation);
		this.viewChanged = true;
		velocityChanged = true;
	}
	
	public void rotateTarget(double x, double y, double z) {
		this.targetRotation.X += x;
		this.targetRotation.Y += y;
		this.targetRotation.Z += z;
		this.viewChanged = true;
		velocityChanged = true;
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
	
	public void update(double dt) {
		if (velocityChanged)
			velocity = Vector.mul(Vector.sub(target, position).normalize(), speed);
		if (Math.abs(this.speed) > 1e-8)
			this.translate(Vector.mul(velocity, dt));
	}
	
}
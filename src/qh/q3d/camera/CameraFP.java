/**
 * 
 */
package qh.q3d.camera;

import qh.math.Matrix;
import qh.math.Vector;
import qh.q3d.MathHelper;
import qh.q3d.objects.Light;

/**
 * A class for a "first person" camera
 * @author jackl_000
 *
 */
public class CameraFP {

	/**
	 * Position of this camera
	 */
	protected Vector position;
	/**
	 * Field of view
	 */
	protected double fov;
	/**
	 * Near clipping plane
	 */
	protected double near;
	/**
	 * Far clipping plane
	 */
	protected double far;
	/**
	 * Aspect ratio
	 */
	protected double aspect;
	/**
	 * Matrix used for transformations.
	 * Cached for performance
	 */
	protected Matrix view, perspective;
	/**
	 * Variables used to detect changes in the transformation matrices
	 */
	protected volatile boolean viewChanged, screenChanged,headingChanged;
	/**
	 * The speed of this camera
	 */
	protected double speed;
	/**
	 * The rotation of this camera
	 */
	protected Vector rotation;
	/**
	 * The direction this camera is pointing to (normalized)
	 */
	protected Vector heading;
	/**
	 * The light this camera gives
	 */
	protected Light light;

	/**
	 * Create a new Camera
	 */
	public CameraFP() {
		position = new Vector();
		heading = new Vector();
		this.rotation = new Vector();
		light = new Light();
	}
	
	/**
	 * Create a new camera with the given position and rotation
	 * @param position
	 * @param rotation
	 */
	public CameraFP(Vector position, Vector rotation) {
		this.position = new Vector(position);
		this.heading = new Vector();
		this.rotation = new Vector(rotation);
		light = new Light();
	}
	
	public void addSpeed(double val) {
		this.speed += val;
	}
	
	
	/**
	 * @return the aspect
	 */
	public double getAspect() {
		return aspect;
	}
	
	/**
	 * @return the far
	 */
	public double getFar() {
		return far;
	}
	
	/**
	 * @return the fov
	 */
	public double getFov() {
		return fov;
	}
	
	/**
	 * @return the camera's light
	 */
	public Light getLight() {
		setLight();
		return new Light(light);
	}
	
	/**
	 * @return reference to the camera's light
	 */
	public Light getLightReference() {
		setLight();
		return light;
	}
	
	/**
	 * @return the near
	 */
	public double getNear() {
		return near;
	}
	
	/**
	 * Returns a copy of the current perspective matrix, re-calculating if needed
	 * @return a copy of the perspective matrix
	 */
	public Matrix getPerspectiveMatrix() {
		if (screenChanged) {
			perspective = Matrix.projectPerspectiveLH(fov, aspect, near, far);
			screenChanged = false;
		}
		return new Matrix(perspective);
	}

	/**
	 * @return A copy of the current position
	 */
	public Vector getPosition() {
		return new Vector(position);
	}

	/**
	 * @return A reference of the current position object
	 */
	public Vector getPositionReference() {
		return position;
	}

	/**
	 * @return the rotX
	 */
	public double getRotX() {
		return rotation.X;
	}

	/**
	 * @return the rotY
	 */
	public double getRotY() {
		return rotation.Y;
	}

	public double getRotZ() {
		return rotation.Z;
	}

	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}
	/**
	 * Returns a copy of the current view matrix, re-calculating if needed
	 * @return a copy of the view matrix
	 */
	public Matrix getViewMatrix() {
		if (viewChanged) {
			view = Matrix.FPViewLH(position,rotation);
			viewChanged = false;
		}
		return new Matrix(view);
	}

	/**
	 * Movement only on the X-Z plane
	 * @param dist the distance to move
	 */
	public void horizontalMove(double dist) {
		if (headingChanged) 
			setHeading();
		if (viewChanged)
			this.getViewMatrix();
		this.translate(new Vector(-heading.Z, 0, heading.X).mul(Matrix.rotationZ(rotation.Z).transpose()).mul(dist));
	}
	
	/**
	 * Move forward (towards the point the camera is pointing to)
	 * @param dist the distance to move
	 */
	public void move(double dist) {
		if (headingChanged) 
			setHeading();
		this.translate(Vector.mul(heading, dist));
	}
	
	/**
	 * Rotate about the x-axis (add to current rotation)
	 * @param val the angle to rotate (in radians)
	 */
	public void rotateX(double val) {
		rotation.X += val;
		viewChanged = true;
		headingChanged = true;
	}
	
	/**
	 * Rotate about the y-axis (add to current rotation)
	 * @param val the angle to rotate (in radians)
	 */
	public void rotateY(double val) {
		rotation.Y += val;
		viewChanged = true;
		headingChanged = true;
	}
	
	/**
	 * Rotate about the z-axis (add to current rotation)
	 * @param val the angle to rotate (in radians)
	 */
	public void rotateZ(double val) {
		rotation.Z += val;
		viewChanged = true;
		headingChanged = true;
	}
	
	/**
	 * @param aspect the aspect to set
	 */
	public void setAspect(double aspect) {
		this.aspect = aspect;
		this.screenChanged = true;
	}
	
	/**
	 * @param far the far to set
	 */
	public void setFar(double far) {
		this.far = far;
		screenChanged = true;
	}
	
	/**
	 * @param fov the fov to set
	 */
	public void setFov(double fov) {
		this.fov = fov;
		screenChanged = true;
	}
	
	private void setHeading() {
		heading.set(0, 0, 1, 0);
		heading.mul(Matrix.transpose(getViewMatrix()));
		heading.W = 0;
		headingChanged = false;
	}
	
	private void setLight() {
		if (headingChanged)
			setHeading();
		if (viewChanged)
			getViewMatrix();
		light.set(Vector.mul(heading, 1).add(position));
	}

	/**
	 * @param near the near to set
	 */
	public void setNear(double near) {
		this.near = near;
		screenChanged = true;
	}
	/**
	 * Set the position of this camera
	 * @param x x coordinates
	 * @param y y coordinates
	 * @param z z coordinates
	 */
	public void setPosition(double x, double y, double z) {
		position.set(x, y, z);
		viewChanged = true;
	}
	
	/**
	 * Set the position
	 * @param pos the position
	 */
	public void setPosition(Vector pos) {
		position.set(pos);
		viewChanged = true;
	}
	
	/**
	 * Set the rotation about the X axis
	 * @param val the rotation to set
	 */
	public void setRotationX(double val) {
		rotation.X = val;
		viewChanged = true;
		headingChanged = true;
	}
	
	/**
	 * Set the rotation about the Y axis
	 * @param val the rotation to set
	 */
	public void setRotationY(double val) {
		rotation.Y = val;
		viewChanged = true;
		headingChanged = true;
	}
	
	/**
	 * Set the rotation about the Z axis
	 * @param val the rotation to set
	 */
	public void setRotationZ(double val) {
		rotation.Z = val;
		viewChanged = true;
		headingChanged = true;
	}
	
	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Returns a string containing the position, heading, and rotation of this camera
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "P:" + position + "H:" + heading + "R:(" + MathHelper.round(MathHelper.toDegrees(rotation.X),2) + "," + MathHelper.round(MathHelper.toDegrees(rotation.Y),2) + "," + MathHelper.round(MathHelper.toDegrees(rotation.Z),2) + ")";
	}

	/**
	 * Translate the camera (add to its coordinates)
	 * @param x x distance
	 * @param y y distance
	 * @param z z distance
	 */
	public void translate(double x, double y, double z) {
		position.X += x;
		position.Y += y;
		position.Z += z;
		viewChanged = true;
	}
	
	/**
	 * Translate the camera (add to its coordinates)
	 * @param pos the distance to translate
	 */
	public void translate(Vector pos) {
		position.add(pos);
		viewChanged = true;
	}
	
	/**
	 * Update the position and/or heading given the time passed
	 * @param dt the number of seconds elapsed
	 * @return if every variable is correctly set or not
	 */
	public void update(double dt) {
		if (headingChanged) 
			setHeading();
		headingChanged = false;
		if (Math.abs(this.speed) > 1e-8)
			this.translate(Vector.mul(heading, dt * speed));
	}
}
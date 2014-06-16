/**
 * 
 */
package qh.q3d.camera;

import qh.math.Matrix;
import qh.math.Vector;
import qh.q3d.MathHelper;
import qh.q3d.objects.Light;

/**
 * @author jackl_000
 *
 */
public class CameraFP {


	protected Vector position;
	protected double fov, near, far, aspect;
	protected Matrix view, perspective;
	protected volatile boolean viewChanged = true;
	protected volatile boolean screenChanged = true;
	protected volatile boolean headingChanged = true;
	protected double speed;
	protected Vector rotation;
	protected Vector heading;
	protected Light light;

	/*public double ymax,ymin,xmax,xmin;*/
	public CameraFP(Vector position, Vector target) {
		this.position = position;
		this.heading = new Vector();
		this.rotation = new Vector();
		light = new Light();
	}
	
	public CameraFP() {
		position = new Vector();
		heading = new Vector();
		this.rotation = new Vector();
		light = new Light().setColor(0,0,0);
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
	
	public void translate(Vector pos) {
		position.add(pos);
		viewChanged = true;
	}
	
	public Vector getPosition() {
		return new Vector(position);
	}
	
	public Vector getPositionReference() {
		return position;
	}
	
	public Matrix getViewMatrix() {
		if (viewChanged) {
			view = Matrix.FPViewLH(position,rotation);
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
	}
	
	public void addSpeed(double val) {
		this.speed += val;
	}
	
	public boolean update(double dt) {
		if (headingChanged) 
			setHeading();
		headingChanged = false;
		if (Math.abs(this.speed) > 1e-8)
			this.translate(Vector.mul(heading, dt * speed));
		return aspect != 0;
	}
	
	public void move(double dist) {
		if (headingChanged) 
			setHeading();
		this.translate(Vector.mul(heading, dist));
	}
	
	public void horizontalMove(double dist) {
		if (headingChanged) 
			setHeading();
		this.translate(new Vector(-heading.Z, heading.Y, heading.X).mul(dist));
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
	
	public Light getLight() {
		setLight();
		return new Light(light);
	}
	
	public Light getLightReference() {
		setLight();
		return light;
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
	public void rotateX(double val) {
		rotation.X += val;
		viewChanged = true;
		headingChanged = true;
	}
	
	public void rotateY(double val) {
		rotation.Y += val;
		viewChanged = true;
		headingChanged = true;
	}
	
	public void rotateZ(double val) {
		rotation.Z += val;
		viewChanged = true;
		headingChanged = true;
	}
	
	public void setRotationX(double val) {
		rotation.X = val;
		viewChanged = true;
		headingChanged = true;
	}
	
	public void setRotationY(double val) {
		rotation.Y = val;
		viewChanged = true;
		headingChanged = true;
	}
	
	public void setRotationZ(double val) {
		rotation.Z = val;
		viewChanged = true;
		headingChanged = true;
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
	
	public String toString() {
		return "P:" + position + "H:" + heading + "R:(" + MathHelper.round(MathHelper.toDegrees(rotation.X),2) + "," + MathHelper.round(MathHelper.toDegrees(rotation.Y),2) + "," + MathHelper.round(MathHelper.toDegrees(rotation.Z),2) + ")";
		//return "P:"+position + " R:" + MathHelper.toDegrees(rotX) + "," + MathHelper.toDegrees(rotY);
	}
}
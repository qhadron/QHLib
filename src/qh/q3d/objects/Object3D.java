package qh.q3d.objects;

import qh.math.Matrix;
import qh.math.Vector;
import qh.q3d.Triangle;
import qh.q3d.Vertex;

public abstract class Object3D {
	/**
	 * Name of the object
	 */
	public String name;
	/**
	 * Number of vertices
	 */
	public Vertex[] vertices;
	/**
	 * The triangles of this object
	 */
	public Triangle[] triangles;
	/**
	 * Global position of this object
	 */
	private Vector position;
	/**
	 * Current rotation of this object
	 */
	private Vector rotation;
	/**
	 * Velocity of this object
	 */
	private Vector velocity;
	
	/**
	 * Scaling of this object
	 */
	private Vector scale;
	
	private boolean modelChanged;
	private Matrix modelMatrix;
	
	/**
	 * Constructs the object:
	 * <ol>
	 * <li> Initializes the vertex array </li>
	 * <li> Initializes all the positional vectors</li>
	 * </ol>
	 * @param verticiesCount the number of vertices of this object
	 */
	public Object3D(int verticiesCount, int facesCount) {
		this.name = "Object";
		this.vertices = new Vertex[verticiesCount];
		for (int i = 0; i < vertices.length; ++i)
			vertices[i] = new Vertex();
		this.triangles = new Triangle[facesCount];
		position = new Vector();
		rotation = new Vector();
		velocity = new Vector();
		scale = new Vector(1,1,1);
		modelChanged = true;
		this.getModelMatrix();
	}
	
	/**
	 * Constructs the object:
	 * <ol>
	 * <li> Sets the name </li>
	 * <li> Initializes the vertex array </li>
	 * <li> Initializes all the positional vectors</li>
	 * </ol>
	 * @param name Name of this object, used in {@link #toString()}
	 * @param verticiesCount the number of vertices of this object
	 */
	public Object3D(String name, int verticiesCount, int facesCount) {
		this.name = name;
		this.vertices = new Vertex[verticiesCount];
		for (int i = 0; i < vertices.length; ++i)
			vertices[i] = new Vertex();
		this.triangles = new Triangle[facesCount];
		position = new Vector();
		rotation = new Vector();
		velocity = new Vector();
		scale = new Vector(1,1,1);
		modelChanged = true;
		this.getModelMatrix();
	}
		
	public void setPosition(double x, double y, double z) {
		position.set(x, y, z);
		modelChanged = true;
	}

	public void setRotation(double x, double y, double z) {
		rotation.set(x, y, z);
		modelChanged = true;
	}
	
	public void setVelocity(double x, double y, double z) {
		velocity.set(x,y,z);
		modelChanged = true;
	}
	
	public void setScale(double x, double y, double z) {
		scale.set(x, y, z);
		modelChanged = true;
	}
	
	
	public void setPosition(Vector val) {
		position.set(val);
		modelChanged = true;
	}
	
	public void setRotation(Vector val) {
		rotation.set(val);
		modelChanged = true;
	}
	
	public void setScale(Vector val) {
		scale.set(val);
		modelChanged = true;
	}
	
	public void setVeloctiy(Vector val) {
		velocity.set(val);
		modelChanged = true;
	}
	/**
	 * Updates the object whenever it is drawn. Moves the object by its {@link #velocity} by default.
	 * @param dt change of time in seconds
	 */
	public void update(double dt) {
		position.add(Vector.mul(velocity, dt));
		modelMatrix.mul(Matrix.translate(velocity));
	}
	
	/** 
	 * Prints the name and position of this object
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String classname = this.getClass().getName();
		int pos = classname.lastIndexOf('.');
		if (pos != -1)
			classname = classname.substring(++pos);
		return classname + "[name=" + name + ", position=" + position + "]";
	}

	/**
	 * @return a copy of the model matrix of this object
	 */
	public Matrix getModelMatrix() {
		if (modelChanged)
			modelMatrix = Matrix.scale(scale).mul(Matrix.rotationYXZ(rotation.Y, rotation.X,
				rotation.Z)).mul(Matrix.translate(position));
		return new Matrix(modelMatrix);
	}
	
	public void calculateNormals() {
		for (int i =0; i < triangles.length; ++i) {
			triangles[i].calcNormal(vertices[triangles[i].A], vertices[triangles[i].B], vertices[triangles[i].C]);
		}
	}
	
	public void calculateCenters() {
		for (int i =0; i < triangles.length; ++i) {
			triangles[i].calcCenter(vertices[triangles[i].A], vertices[triangles[i].B], vertices[triangles[i].C]);
		}
	}
}

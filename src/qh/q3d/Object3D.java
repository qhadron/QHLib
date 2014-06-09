package qh.q3d;

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
	public Vector position;
	/**
	 * Current rotation of this object
	 */
	public Vector rotation;
	/**
	 * Velocity of this object
	 */
	public Vector velocity;
	
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
		setFaces(triangles);
		position = new Vector();
		rotation = new Vector();
		velocity = new Vector();
	}
	
	protected abstract void setFaces(Triangle[] arrayOfFaces);
	
	public void setPosition(double x, double y, double z) {
		position.set(x, y, z);
	}

	public void setRotation(double x, double y, double z) {
		rotation.set(x, y, z);
	}
	
	public void setVelocity(double x, double y, double z) {
		velocity.set(x,y,z);
	}
	
	/**
	 * Updates the object whenever it is drawn. Moves the object by its {@link #velocity} by default.
	 * @param dt change of time in seconds
	 */
	public void update(double dt) {
		position.add(Vector.mul(velocity, dt));
	}
	
	/** 
	 * Prints the name and position of this object
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[name=" + name + ", position=" + position + "]";
	}
}

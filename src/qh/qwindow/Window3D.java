package qh.qwindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import qh.q3d.Camera;
import qh.q3d.Matrix;
import qh.q3d.Object3D;
import qh.q3d.Vector;
import qh.q3d.Vertex;

public class Window3D extends JPanel implements ComponentListener {
	/**
	 * Generated serial version UID for JComponent
	 */
	private static final long serialVersionUID = 2230112262025881448L;
	protected static final boolean DEBUG = false;
	private volatile BufferedImage mbf;
	private Thread calcThread;
	private double[][] zbuffer;

	private boolean[] keysBuffer;

	public Camera camera;

	private LinkedList<Object3D> worldObjects;

	private int windowWidth, windowHeight;
	private int screenWidth, screenHeight;

	private String title;

	private boolean needUpdate;

	private JFrame mainFrame;

	private Graphics2D g2d;

	public Window3D() {
		super(false);
		screenWidth = 640;
		screenHeight = 480;
		title = "New Window";
		init();
	}

	public Window3D(String title) {
		super(false);
		this.title = title;
		this.screenWidth = 640;
		this.screenHeight = 480;
		init();
	}

	public Window3D(int width, int height) {
		super(false);
		this.screenWidth = width;
		this.screenHeight = height;
		this.title = "New Window";
		init();
	}

	public Window3D(String title, int width, int height) {
		super(false);
		this.title = title;
		this.screenWidth = width;
		this.screenHeight = height;
		init();
	}

	private void init() {
		windowWidth = screenWidth + 6;
		windowHeight = screenHeight + 29;

		zbuffer = new double[screenWidth][screenHeight];
		keysBuffer = new boolean[KeyEvent.KEY_PRESSED]; // arbitrarily large
														// size

		mbf = new BufferedImage(screenWidth, screenHeight,
				BufferedImage.TYPE_4BYTE_ABGR);
		g2d = mbf.createGraphics();

		worldObjects = new LinkedList<Object3D>();

		camera = new Camera();
		camera.setPosition(0, 0, 10);
		camera.setTarget(0, 0, 0);
		camera.setFov((0.78));
		camera.setFar(1.0);
		camera.setNear(0.01);
		// initialize the JFrame
		try {
			JFrame.setDefaultLookAndFeelDecorated(true);
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err.println("Windows default look and feel not supported: ");
			System.err.println(e.getMessage());
		}
		mainFrame = new JFrame(title);
		mainFrame.setMinimumSize(new Dimension(10, 10));
		mainFrame.setResizable(false);
		mainFrame.setAutoRequestFocus(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// initialize the canvas
		setFocusable(true);
		setDoubleBuffered(false);
		addComponentListener(this);
		setBackground(Color.white);
		mainFrame.add(this, BorderLayout.CENTER);
		mainFrame.setSize(windowWidth, windowHeight);

		setSize(windowWidth, windowHeight);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);

		this.start();
	}

	private void start() {
		calcThread = new Thread(new Runnable() {
			public void run() {
				long lastTime = System.currentTimeMillis();
				while (true) {
					render((System.currentTimeMillis() - lastTime) / 1e3);
					lastTime = System.currentTimeMillis();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						if (DEBUG)
							System.out.println("[not an error]: How dare you interrupt me!");
					}
				}
			}
		}, "Rendering Calculations Thread");
		calcThread.start();
	}

	/**
	 * puts pixels into backbuffer
	 */
	private synchronized void render(double dt) {
		Matrix view = camera.getViewMatrix();
		Matrix projection = camera.getPerspectiveMatrix((double) screenWidth
				/ screenHeight);
		Iterator<Object3D> it = worldObjects.iterator();
		while (it.hasNext()) {
			Object3D cur = it.next();
			cur.update(dt);
			Matrix model = Matrix.scale(cur.scale).mul(Matrix.rotationYXZ(cur.rotation.Y, cur.rotation.X,
					cur.rotation.Z)).mul(Matrix.translate(cur.position));
			Matrix MV = model.mul(view);
			Matrix MVP = MV.mul(projection);
			Vertex[] pixels = new Vertex[cur.vertices.length];
			boolean visible[] = new boolean[pixels.length];
			// calculate vertices
			Vector temp;
			for (int i = 0; i < pixels.length; ++i) {
				temp = Vertex.mul(MV, cur.vertices[i]);
				visible[i] = !(temp.Z < camera.getNear());
				pixels[i] = project(cur.vertices[i], MVP);
			}
			// draw faces
			for (int i = 0, a, b, c; i < cur.triangles.length; ++i) {
				a = cur.triangles[i].A;
				b = cur.triangles[i].B;
				c = cur.triangles[i].C;

				if (visible[a] && visible[b])
					drawLine(pixels[a], pixels[b]);
				if (visible[b] && visible[c])
					drawLine(pixels[b], pixels[c]);
				if (visible[c] && visible[a])
					drawLine(pixels[c], pixels[a]);
			}

		}
	}

	/**
	 * Maps coordinates ranging from -1 to 1 to screen coordinates, inverting y
	 * coordinates in the process
	 * 
	 * @param vec
	 *            vector
	 * @param mat
	 *            matrix
	 * @return projected screen coordinates
	 */
	public Vector project(Vector vec, Matrix mat) {
		Vector res = Vector.transformCoords(vec, mat);
		res.X = res.X * screenWidth + screenWidth / 2.0;
		res.Y = -res.Y * screenHeight + screenHeight / 2.0;
		return res;
	}

	/**
	 * Maps coordinates ranging from -1 to 1 to screen coordinates, inverting y
	 * coordinates in the process
	 * 
	 * @param vec
	 *            vector
	 * @param mat
	 *            matrix
	 * @return projected screen coordinates
	 */
	public Vertex project(Vertex vec, Matrix mat) {
		Vertex res = Vertex.transformCoords(vec, mat);
		res.X = res.X * screenWidth + screenWidth / 2.0;
		res.Y = -res.Y * screenHeight + screenHeight / 2.0;
		return res;
	}

	public void update() {
		needUpdate = true;
		this.repaint();
	}

	public void clear() {
		g2d.setColor(this.getBackground());
		g2d.fillRect(0, 0, screenWidth, screenHeight);
		calcThread.interrupt();
		render(0);
		update();
	}

	private void putPixel(int x, int y, int rgb) {
		mbf.setRGB(x, y, rgb);
	}

	public void drawPoint(Vector point, int rgb) {
		if (point.X >= 0 && point.Y >= 0 && point.X < screenWidth
				&& point.Y < screenHeight) {
			putPixel((int) point.X, (int) point.Y, rgb);
		}
	}

	public void drawPoint(double x, double y, int rgb) {
		if (x >= 0 && y >= 0 && x < screenWidth && y < screenHeight) {
			putPixel((int) x, (int) y, rgb);
		}
	}

	/**
	 * Draws a line between two points.<br>
	 * This function uses Brensenham's Algorithm, as described <a href =
	 * "http://en.wikipedia.org/wiki/Bresenham's_line_algorithm"> here </a>.
	 * 
	 * @param a
	 *            Starting point
	 * @param b
	 *            Ending point
	 * @param rgb
	 *            color
	 * 
	 * @see #drawLine(double, double, double, double, int)
	 */
	public void drawLine(Vector a, Vector b, int rgb) {
		int x0 = (int) a.X;
		int y0 = (int) a.Y;
		int x1 = (int) b.X;
		int y1 = (int) b.Y;

		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		int sx = (x0 < x1) ? 1 : -1;
		int sy = (y0 < y1) ? 1 : -1;
		int err = dx - dy;
		int e2;
		while (true) {
			drawPoint(x0, y0, rgb);
			if ((x0 == x1) && (y0 == y1))
				break;
			e2 = err << 1;
			if (e2 > -dy) {
				err -= dy;
				x0 += sx;
			}
			if (e2 < dx) {
				err += dx;
				y0 += sy;
			}
		}
	}

	/**
	 * Draws a line between two vertices.<br>
	 * This function uses Brensenham's Algorithm, as described <a href =
	 * "http://en.wikipedia.org/wiki/Bresenham's_line_algorithm"> here </a>.
	 * 
	 * @param a
	 *            Starting point
	 * @param b
	 *            Ending point
	 * @param rgb
	 *            color
	 * 
	 * @see #drawLine(double, double, double, double, int)
	 */
	public void drawLine(Vertex a, Vertex b) {
		int x0 = (int) a.X;
		int y0 = (int) a.Y;
		int x1 = (int) b.X;
		int y1 = (int) b.Y;

		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		int sx = (x0 < x1) ? 1 : -1;
		int sy = (y0 < y1) ? 1 : -1;
		int err = dx - dy;
		int e2;

		int r1 = a.color.getRed(), g1 = a.color.getGreen(), b1 = a.color
				.getBlue(), a1 = a.color.getAlpha();
		int r2 = b.color.getRed(), g2 = b.color.getGreen(), b2 = b.color
				.getBlue(), a2 = b.color.getAlpha();

		int rgb = rgb((r1 + r2 >> 1), (g1 + g2 >> 1), (b1 + b2) >> 1,
				(a1 + a2) >> 1);
		while (true) {
			drawPoint(x0, y0, rgb);
			if ((x0 == x1) && (y0 == y1))
				break;
			e2 = err << 1;
			if (e2 > -dy) {
				err -= dy;
				x0 += sx;
			}
			if (e2 < dx) {
				err += dx;
				y0 += sy;
			}
		}
	}

	/**
	 * Draws a line between two points.<br>
	 * This function uses Brensenham's Algorithm, as described <a href =
	 * "http://en.wikipedia.org/wiki/Bresenham's_line_algorithm"> here </a>.
	 * 
	 * @param x0
	 *            x-coordinate of the first starting point
	 * @param y0
	 *            y-coordinate of the first starting point
	 * @param x1
	 *            x-coordinate of the second starting point
	 * @param y1
	 *            y-coordinate of the second starting point
	 * @param rgb
	 *            color of the point
	 * @see #drawLine(Vector, Vector, int)
	 */
	public void drawLine(double x0, double y0, double x1, double y1, int rgb) {
		double dx = Math.abs(x1 - x0);
		double dy = Math.abs(y1 - y0);
		double sx = (x0 < x1) ? 1 : -1;
		double sy = (y0 < y1) ? 1 : -1;
		double err = dx - dy;

		while (true) {
			drawPoint(x0, y0, rgb);
			if ((x0 == x1) && (y0 == y1))
				break;
			double e2 = 2 * err;
			if (e2 > -dy) {
				err -= dy;
				x0 += sx;
			}
			if (e2 < dx) {
				err += dx;
				y0 += sy;
			}
		}
	}

	public static int rgb(int r, int g, int b, int a) {
		return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8)
				| ((b & 0xff));
	}

	public static int rgbf(double r, double g, double b, double a) {
		return (((int) (a * 0xff) & 0xff) << 24)
				| (((int) (r * 0xff) & 0xff) << 16)
				| (((int) (g * 0xff) & 0xff) << 8) | ((int) (b * 0xff) & 0xff);
	}

	long lastTime = System.currentTimeMillis();
	private boolean _updateFPS = true;
	public double FPS;

	@Override
	public void paint(Graphics g) {
		if (needUpdate) {
			g.drawImage(mbf, 0, 0, null);
			g.setColor(inverseColor(getBackground()));
			if (_updateFPS) {
				FPS = 2000.0 / (System.currentTimeMillis() - lastTime);
				lastTime = System.currentTimeMillis();
				_updateFPS = false;
			} else {
				_updateFPS = true;
			}
			g.drawString(String.format("FPS: %6.2f", FPS), 10, 10);
			needUpdate = false;
		}
	}

	private Color inverseColor(Color c) {
		return new Color(255 - c.getRed(), 255 - c.getGreen(),
				255 - c.getBlue(), 255);
	}

	public void addObject(Object3D mesh) {
		if (mesh != null)
		worldObjects.add(mesh);
	}

	public void removeObject(Object3D mesh) {
		worldObjects.remove(mesh);
	}

	public void addObjects(Collection<? extends Object3D> src) {
		worldObjects.addAll(src);
	}

	public void removeObjects(Collection<? extends Object3D> src) {
		worldObjects.removeAll(src);
	}

	public void setObjects(LinkedList<Object3D> src) {
		worldObjects.clear();
		worldObjects.addAll(src);
	}

	/**
	 * returns a reference to all the objects rendered
	 * <p>
	 * <b> CHANGES TO THE REFERENCE ITSELF IS NOT CHECKED!</b>
	 * 
	 * @return a reference to the objects in the world
	 */
	public LinkedList<Object3D> getObjects() {
		return worldObjects;
	}

	@Override
	public void componentResized(ComponentEvent e) {

		// TODO decide whether or not window size is useful
		/*
		 * this.windowWidth = this.screenWidth + 16; this.windowHeight =
		 * this.screenHeight + 39;
		 */
	}

	/**
	 * unused
	 */
	@Override
	public void componentMoved(ComponentEvent e) {

	}

	/**
	 * unused
	 */
	@Override
	public void componentShown(ComponentEvent e) {
		repaint();
	}

	/**
	 * unused
	 */
	@Override
	public void componentHidden(ComponentEvent e) {

	}

}

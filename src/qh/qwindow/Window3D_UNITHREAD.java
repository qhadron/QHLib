package qh.qwindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import qh.math.Matrix;
import qh.math.Vector;
import qh.q3d.MathHelper;
import qh.q3d.Vertex;
import qh.q3d.camera.CameraFP;
import qh.q3d.objects.Light;
import qh.q3d.objects.Object3D;

public class Window3D_UNITHREAD extends JPanel implements ComponentListener, KeyListener,
		MouseMotionListener, MouseListener, MouseWheelListener {

	private static final boolean DEBUG = false;
	private static final boolean USE_MOUSE = false;
	/** codes for computing the regions a line belongs to */
	private static final int OUTCODE_LEFT = 0b0001, OUTCODE_RIGHT = 0b0010,
			OUTCODE_TOP = 0b0100, OUTCODE_BOTTOM = 0b1000;

	/** varaibles for defaut input handling */
	private static final long INPUT_DELAY = 10;
	private static final double ACCEL = 0.5;
	private static final double ROTATION_SPEED = 0.01;
	private static final double MOVEMENT_SPEED = 1;
	private static final double ZOOM_SPEED = 0.1;

	/** varaibles regarding the crosshair in the center of the screen */
	private static final int LINE_HEIGHT = 15;
	private static final int CROSSHAIR_SIZE = 5;

	/** Thread used for parsing input */
	private Thread inputThread;

	/** z-buffer for proper drawing */
	private double[][] zbuffer;

	/** variables for buffered input */
	private volatile boolean[] keysBuffer;
	private boolean mouseLeftDown, mouseRightDown;

	/**
	 * Stores whether the window is fullscreen or not
	 */
	private boolean fullscreen;

	/** whether or not to draw in wireframe mode */
	private volatile boolean WIREFRAME = true;

	/**
	 * Render distance, defaults to POSITIVE_INFINITY (render all objects)
	 */
	private volatile double RENDER_DISTANCE = Double.POSITIVE_INFINITY;

	/**
	 * A list of all the lights
	 */
	private LinkedList<Light> lights;

	/**
	 * The current camera used
	 */
	public CameraFP camera;

	/**
	 * A list of all the objects to render
	 */
	private LinkedList<Object3D> worldObjects;

	/** information about the window */
	private int windowWidth, windowHeight;

	/** information about the screen */
	private int screenWidth, screenHeight;

	private String title;

	private JFrame mainFrame;

	/**
	 * Graphics used for drawing
	 */
	private Graphics drawingGraphics;

	private PrintStream logger;

	/** used for debugging */
	double viewprojecttime = 0;
	double mvptime = 0;
	double projecttime = 0;
	double drawTime = 0;
	long rendercnt = 0;

	/**
	 * Creates a 640x480 window with the title "New Window"
	 */
	public Window3D_UNITHREAD() {
		super(false);
		screenWidth = 640;
		screenHeight = 480;
		title = "New Window";
		init();
	}

	/**
	 * Creates a 640x480 window with the specified title
	 * 
	 * @param title
	 *            the title of the window
	 */
	public Window3D_UNITHREAD(String title) {
		super(false);
		this.title = title;
		this.screenWidth = 640;
		this.screenHeight = 480;
		init();
	}

	/**
	 * Creates a window with the specified height and width
	 * 
	 * @param width
	 *            the width of the window
	 * @param height
	 *            the height of the window
	 */
	public Window3D_UNITHREAD(int width, int height) {
		super(false);
		this.screenWidth = width;
		this.screenHeight = height;
		this.title = "New Window";
		init();
	}

	/**
	 * Creates a window with the specified parameters
	 * 
	 * @param title
	 *            the title of the window
	 * @param width
	 *            the width of the window
	 * @param height
	 *            the height of the window
	 */
	public Window3D_UNITHREAD(String title, int width, int height) {
		super(false);
		this.title = title;
		this.screenWidth = width;
		this.screenHeight = height;
		init();
	}

	/**
	 * Initializing the window
	 */
	private void init() {
		// debug output
		if (DEBUG)
			try {
				File file = new File(
						new SimpleDateFormat("HH.mm.ss.ddMMyyyy")
								.format(new Date()) + ".log").getAbsoluteFile();
				file.createNewFile();
				logger = new PrintStream(new FileOutputStream(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		// simple hack to make the screen the specified dimensions
		windowWidth = screenWidth + 6;
		windowHeight = screenHeight + 29;

		// initializing local variables
		zbuffer = new double[screenWidth][screenHeight];
		// keys buffer initialized to arbitrarily large size
		keysBuffer = new boolean[KeyEvent.KEY_PRESSED];

		worldObjects = new LinkedList<Object3D>();

		lights = new LinkedList<Light>();

		camera = new CameraFP();
		camera.setPosition(0, 0, 10);
		camera.setFov((MathHelper.HALF_PI));
		camera.setFar(1);
		camera.setNear(0.999);
		camera.setAspect((double) screenWidth / screenHeight);
		mainFrame = new JFrame(title);
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

		// initialize the canvas (this)
		setFocusable(true);
		setDoubleBuffered(false);
		addComponentListener(this);
		setBackground(Color.white);
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		setSize(windowWidth, windowHeight);

		initMainFrame();
	}

	/**
	 * Initializes the main frame (needed to go to fullscreen mode)
	 */
	private void initMainFrame() {
		mainFrame.setMinimumSize(new Dimension(10, 10));
		mainFrame.setResizable(true);
		mainFrame.setAutoRequestFocus(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.add(this, BorderLayout.CENTER);
		mainFrame.setSize(windowWidth, windowHeight);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}

	public void start() {

		inputThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					parseInput();
					try {
						Thread.sleep(INPUT_DELAY);
					} catch (InterruptedException e) {

					}
				}
			}

		}, "Input Parsing Thread");
		inputThread.start();

		repaint();
	}

	private void parseInput() {
		double rotX = camera.getRotX(), rotY = camera.getRotY(), rotZ = camera
				.getRotZ();
		boolean reverseY = false;
		if (rotX > MathHelper.TWO_PI) {
			camera.rotateX(-MathHelper.TWO_PI);
		}
		if (rotX < 0) {
			camera.rotateX(MathHelper.TWO_PI);
		}
		// add a tiny angel to avoid rounding errors
		if (rotX == 0) {
			camera.rotateX(0.0001);
		}
		if (rotY > MathHelper.TWO_PI) {
			camera.rotateY(-MathHelper.TWO_PI);
		}
		if (rotY < 0) {
			camera.rotateY(MathHelper.TWO_PI);
		}
		// add a tiny angel to avoid rounding errors
		if (rotY == 0) {
			camera.rotateY(0.0001);
		}
		if (rotZ > MathHelper.TWO_PI) {
			camera.rotateZ(-MathHelper.TWO_PI);
		}
		if (rotZ < 0) {
			camera.rotateZ(MathHelper.TWO_PI);
		}
		// add a tiny angel to avoid rounding errors
		if (rotZ == 0) {
			camera.rotateZ(0.0001);
		}
		reverseY = MathHelper.between(rotX, MathHelper.HALF_PI,
				MathHelper.THREEHALVES_PI);

		if (keysBuffer[KeyEvent.VK_UP]) {
			camera.addSpeed(ACCEL);
		} else if (keysBuffer[KeyEvent.VK_DOWN]) {
			camera.addSpeed(-ACCEL);
		}
		if (keysBuffer[KeyEvent.VK_LEFT]) {
			if (reverseY)
				camera.rotateY(ROTATION_SPEED);
			else
				camera.rotateY(-ROTATION_SPEED);
		} else if (keysBuffer[KeyEvent.VK_RIGHT]) {
			if (reverseY)
				camera.rotateY(-ROTATION_SPEED);
			else
				camera.rotateY(ROTATION_SPEED);
		}
		if (keysBuffer[KeyEvent.VK_Z]) {
			camera.rotateX(ROTATION_SPEED);
		} else if (keysBuffer[KeyEvent.VK_X]) {
			camera.rotateX(-ROTATION_SPEED);
		}
		if (keysBuffer[KeyEvent.VK_Q] || mouseLeftDown) {
			camera.rotateZ(-ROTATION_SPEED);
		} else if (keysBuffer[KeyEvent.VK_E] || mouseRightDown) {
			camera.rotateZ(ROTATION_SPEED);
		}
		if (keysBuffer[KeyEvent.VK_SPACE]) {
			camera.translate(0, MOVEMENT_SPEED, 0);
		} else if (keysBuffer[KeyEvent.VK_SHIFT]) {
			camera.translate(0, -MOVEMENT_SPEED, 0);
		}
		if (keysBuffer[KeyEvent.VK_W]) {
			camera.move(MOVEMENT_SPEED);
		} else if (keysBuffer[KeyEvent.VK_S]) {
			camera.move(-MOVEMENT_SPEED);
		}
		if (keysBuffer[KeyEvent.VK_A]) {
			camera.horizontalMove(MOVEMENT_SPEED);
		} else if (keysBuffer[KeyEvent.VK_D]) {
			camera.horizontalMove(-MOVEMENT_SPEED);
		}

		if (keysBuffer[KeyEvent.VK_C]) {
			camera.setSpeed(0);
		}
		if (keysBuffer[KeyEvent.VK_V]) {
			camera.setRotationX(0);
			camera.setRotationY(0);
			camera.setRotationZ(0);
			camera.setFov(MathHelper.HALF_PI);
		}
		if (keysBuffer[KeyEvent.VK_R]) {
			camera.setSpeed(0);
			camera.setPosition(0, 0, 0);
			camera.setRotationX(0);
			camera.setRotationY(0);
			camera.setRotationZ(0);
			camera.setFov(MathHelper.HALF_PI);
		}
		if (keysBuffer[KeyEvent.VK_ESCAPE]) {
			this.exit();
		}
		if (keysBuffer[KeyEvent.VK_F11]) {
			toggleFullscreen();
			keysBuffer[KeyEvent.VK_F11] = false;
		}
	}

	public void close() {
		try {
			inputThread.join(100);
		} catch (InterruptedException e) {
			System.out.println("Error while exiting: ");
			e.printStackTrace();
		}
		mainFrame.dispose();
		System.gc();
	}

	public void exit() {
		close();
		System.exit(0);
	}

	private void clear() {
		drawingGraphics.setColor(getBackground());
		drawingGraphics.fillRect(0, 0, screenWidth, screenHeight);
		// clear depth buffer
		for (int i = 0; i < zbuffer.length; ++i)
			for (int j = 0; j < zbuffer[0].length; ++j)
				zbuffer[i][j] = Double.POSITIVE_INFINITY;
	}

	private void render(double dt) {
		long temptime, st;
		;
		if (DEBUG) {
			++rendercnt;
			logger.println("--------------------cnt: " + rendercnt
					+ "--------------------");
			st = System.currentTimeMillis();

		}
		camera.update(dt);
		Matrix view = camera.getViewMatrix();
		Matrix perspective = camera.getPerspectiveMatrix();
		Matrix VP = view.mul(perspective);
		if (DEBUG) {
			viewprojecttime += (temptime = System.currentTimeMillis() - st);
			logger.println("VP time = " + temptime);
		}
		Iterator<Object3D> it = worldObjects.iterator();
		Vector cameraPos = camera.getPosition();
		clear();
		while (it.hasNext()) {
			Object3D cur = it.next();
			if (cur.isMovable())
				cur.update(dt);
			if (Vector.sub(cur.getPosition(), cameraPos).lengthSquared() > RENDER_DISTANCE) {
				continue;
			}

			Vertex[] pixels = new Vertex[cur.vertices.length];
			boolean visible[] = new boolean[pixels.length];
			Matrix model = cur.getModelMatrix();
			Matrix MVP = Matrix.mul(model, VP);
			if (DEBUG) {
				mvptime += (temptime = System.currentTimeMillis() - st);
				logger.println("MVP time = " + (temptime));
				st = System.currentTimeMillis();
			}
			// calculate vertices
			Vertex temp;
			for (int i = 0; i < pixels.length; ++i) {
				temp = Vertex.mul(MVP, cur.vertices[i]);
				visible[i] = !(temp.Z < -0.001);
				if (visible[i])
					pixels[i] = projectHomogeneous(temp);
			}

			// System.exit(0);
			if (DEBUG) {
				projecttime += (temptime = System.currentTimeMillis() - st);
				logger.println("projection time = " + (temptime));
				st = System.currentTimeMillis();
			}
			if (WIREFRAME) {
				// draw faces
				for (int i = 0; i < cur.triangles.length; ++i) {
					if (cur.triangles[i].normal == null) {
						cur.triangles[i].calcNormal(
								cur.vertices[cur.triangles[i].A],
								cur.vertices[cur.triangles[i].B],
								cur.vertices[cur.triangles[i].C]);
					}
					if (cur.triangles[i].center == null) {
						cur.triangles[i].calcCenter(
								cur.vertices[cur.triangles[i].A],
								cur.vertices[cur.triangles[i].B],
								cur.vertices[cur.triangles[i].C]);
					}
					if (visible[cur.triangles[i].A]
							&& visible[cur.triangles[i].B])
						drawLine(pixels[cur.triangles[i].A],
								pixels[cur.triangles[i].B]);
					if (visible[cur.triangles[i].B]
							&& visible[cur.triangles[i].C])
						drawLine(pixels[cur.triangles[i].B],
								pixels[cur.triangles[i].C]);
					if (visible[cur.triangles[i].C]
							&& visible[cur.triangles[i].A])
						drawLine(pixels[cur.triangles[i].C],
								pixels[cur.triangles[i].A]);
				}

			} else {
				// normals of the surfaces, used for shading
				Vector transformedNormal, transformedCenter;
				// draw faces
				for (int i = 0; i < cur.triangles.length; ++i) {
					if (cur.triangles[i].normal == null) {
						cur.triangles[i].calcNormal(
								cur.vertices[cur.triangles[i].A],
								cur.vertices[cur.triangles[i].B],
								cur.vertices[cur.triangles[i].C]);
					}
					if (cur.triangles[i].center == null) {
						cur.triangles[i].calcCenter(
								cur.vertices[cur.triangles[i].A],
								cur.vertices[cur.triangles[i].B],
								cur.vertices[cur.triangles[i].C]);
					}
					transformedNormal = Vector.transformCoords(
							cur.triangles[i].normal, MVP);
					transformedCenter = Vector.transformCoords(
							cur.triangles[i].center, model);
					if (visible[cur.triangles[i].A]
							&& visible[cur.triangles[i].B]
							&& visible[cur.triangles[i].C]) {
						drawTriangle(pixels[cur.triangles[i].A],
								pixels[cur.triangles[i].B],
								pixels[cur.triangles[i].C],
								Vector.transformCoords(cur.triangles[i].normal,
										model), transformedCenter);
					}
				}
			}

			if (DEBUG) {
				drawTime = (temptime = System.currentTimeMillis() - st);
				logger.println("draw time = " + (temptime));
			}
		}
		if (DEBUG) {
			logger.println("Totals: ");
			logger.println("VP  time: \t" + (viewprojecttime) + " ms");
			logger.println("MVP time: \t" + (mvptime) + " ms");
			logger.println("PRO time: \t" + (projecttime) + " ms");
			logger.println("DRA time: \t" + (drawTime) + " ms");
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
		res.Z = vec.Z;
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
		res.Z = vec.Z;
		return res;
	}

	public Vertex projectHomogeneous(Vertex vec) {
		Vertex res = new Vertex(vec);
		res.X /= res.W;
		res.Y /= res.W;
		res.X = res.X * screenWidth + screenWidth / 2.0;
		res.Y = -res.Y * screenHeight + screenHeight / 2.0;
		return res;
	}

	public void update() {
		this.repaint();
	}

	private void putPixel(int x, int y, int rgb) {
		drawingGraphics.setColor(new Color(rgb));
		drawingGraphics.drawLine(x, y, x, y);
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
		drawLine(a.X, a.Y, b.X, b.Y, rgb);
	}

	/**
	 * Draws a line between two vertices, naively interpolating the color.<br>
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
		int r1 = a.color.getRed(), g1 = a.color.getGreen(), b1 = a.color
				.getBlue(), a1 = a.color.getAlpha();
		int r2 = b.color.getRed(), g2 = b.color.getGreen(), b2 = b.color
				.getBlue(), a2 = b.color.getAlpha();

		int rgb = MathHelper.rgb((r1 + r2 >> 1), (g1 + g2 >> 1),
				(b1 + b2) >> 1, (a1 + a2) >> 1);
		drawLine(a.X, a.Y, b.X, b.Y, rgb);
	}

	private int calcOutCode(double x, double y) {
		int code = 0;
		if (x < 0)
			code |= OUTCODE_LEFT;
		else if (x > screenWidth)
			code |= OUTCODE_RIGHT;
		if (y < 0)
			code |= OUTCODE_BOTTOM;
		else if (y > screenHeight)
			code |= OUTCODE_TOP;
		return code;
	}

	/**
	 * Draws a line between two points.<br>
	 * This function uses <a href =
	 * "http://en.wikipedia.org/wiki/Cohen%E2%80%93Sutherland"> Cohen�CSutherland
	 * algorithm </a> for clipping the line, and <a href =
	 * "http://en.wikipedia.org/wiki/Bresenham's_line_algorithm"> Brensenham's
	 * Algorithm </a> for drawing the line.
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
		/* clipping */
		int codea = calcOutCode(x0, y0);
		int codeb = calcOutCode(x1, y1);
		boolean visible = false;
		while (true) {
			if ((codea | codeb) == 0) { // bitwise OR is 0, so in clipping
										// rectangle
				visible = true;
				break;
			} else if ((codea & codeb) != 0) { // both are out of viewing range
				break;
			} else { // real deal, where endpoints need to be recalculated
				// new coordinates to clip from outside point to intersection
				// with clip edge
				double x = 0, y = 0;
				// getting the point with the code outside of clipping rectangle
				int codeOut = (codea != 0) ? codea : codeb;

				// Find intersection points of line and clipping rectangle using
				// the slope
				if ((codeOut & OUTCODE_TOP) > 0) { // point is above the clip
													// rectangle
					x = x0 + (x1 - x0) * (screenHeight - y0) / (y1 - y0);
					y = screenHeight;
				} else if ((codeOut & OUTCODE_BOTTOM) > 0) { // point is below
																// the clip
																// rectangle
					x = x0 + (x1 - x0) * (0 - y0) / (y1 - y0);
					y = 0;
				} else if ((codeOut & OUTCODE_RIGHT) > 0) { // point is to the
															// right of clip
															// rectangle
					y = y0 + (y1 - y0) * (screenWidth - x0) / (x1 - x0);
					x = screenWidth;
				} else if ((codeOut & OUTCODE_LEFT) > 0) { // point is to the
															// left of clip
															// rectangle
					y = y0 + (y1 - y0) * (0 - x0) / (x1 - x0);
					x = 0;
				}
				/*
				 * replace points and get ready for additional passes (when
				 * middle of line seemingly passes through the clipping
				 * rectangle)
				 */
				if (codeOut == codea) { // (x0,y0) is the one outside
					x0 = x;
					y0 = y;
					codea = calcOutCode(x0, y0);
				} else { // (x1,y1) is the one outside
					x1 = x;
					y1 = y;
					codeb = calcOutCode(x1, y1);
				}

			}
		}
		if (!visible)
			return; // skip rendering if points arn't visible
		/* line algorithm */
		double dx = Math.abs(x1 - x0);
		double dy = Math.abs(y1 - y0);
		double sx = (x0 < x1) ? 1 : -1;
		double sy = (y0 < y1) ? 1 : -1;
		double err = dx - dy;

		while (true) {
			drawPoint(x0, y0, rgb);
			if ((Math.abs(x0 - x1) < 1) && (Math.abs(y0 - y1) < 1))
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

	/**
	 * Uses Java's incomplete pass-by-value system to change the vectors passed
	 * in
	 * 
	 * @param a
	 *            point a
	 * @param b
	 *            point b
	 * @return whether or not a or b is visible
	 */
	public boolean clipCoords(Vector a, Vector b) {
		/* clipping */
		int codea = calcOutCode(a.X, a.Y);
		int codeb = calcOutCode(b.X, b.Y);
		boolean visible = false;
		while (true) {
			if ((codea | codeb) == 0) { // bitwise OR is 0, so in clipping
										// rectangle
				visible = true;
				break;
			} else if ((codea & codeb) != 0) { // both are out of viewing range
				break;
			} else { // real deal, where endpoints need to be recalculated
				// new coordinates to clip from outside point to intersection
				// with clip edge
				double x = 0, y = 0;
				// getting the point with the code outside of clipping rectangle
				int codeOut = (codea != 0) ? codea : codeb;
				// Find intersection points of line and clipping rectangle using
				// the slope
				if ((codeOut & OUTCODE_TOP) > 0) { // point is above the clip
													// rectangle
					x = a.X + (b.X - a.X) * (screenHeight - a.Y) / (b.Y - a.Y);
					y = screenHeight;
				} else if ((codeOut & OUTCODE_BOTTOM) > 0) { // point is below
																// the clip
																// rectangle
					x = a.X + (b.X - a.X) * (0 - a.Y) / (b.Y - a.Y);
					y = 0;
				} else if ((codeOut & OUTCODE_RIGHT) > 0) { // point is to the
															// right of clip
															// rectangle
					y = a.Y + (b.Y - a.Y) * (screenWidth - a.X) / (b.X - a.X);
					x = screenWidth;
				} else if ((codeOut & OUTCODE_LEFT) > 0) { // point is to the
															// left of clip
															// rectangle
					y = a.Y + (b.Y - a.Y) * (0 - a.X) / (b.X - a.X);
					x = 0;
				}
				/*
				 * replace points and get ready for additional passes (when
				 * middle of line seemingly passes through the clipping
				 * rectangle)
				 */
				if (codeOut == codea) { // (x0,y0) is the one outside
					a.X = x;
					a.Y = y;
					codea = calcOutCode(a.X, a.Y);
				} else { // (x1,y1) is the one outside
					b.X = x;
					b.Y = y;
					codeb = calcOutCode(b.X, b.Y);
				}

			}
		}
		return visible;
	}

	public void swingDrawTriangle(Vertex p1, Vertex p2, Vertex p3) {
		int[] x = new int[3], y = new int[3];
		x[0] = (int) p1.X;
		x[1] = (int) p2.X;
		x[2] = (int) p3.X;
		y[0] = (int) p1.Y;
		y[1] = (int) p2.Y;
		y[2] = (int) p3.Y;
		drawingGraphics.setColor(new Color((int) MathHelper.interpolateColor(
				MathHelper.interpolateColor(p1, p2, 0.5), p3.color.getRGB(),
				0.5)));
		drawingGraphics.fillPolygon(x, y, 3);
	}

	public boolean checkCoords(Vector vec) {
		return (vec.X >= 0 && vec.X < screenWidth && vec.Y >= 0 && vec.Y < screenHeight);
	}

	// draws a triangle p1 p2 p3
	public void drawTriangle(Vertex p1, Vertex p2, Vertex p3, Vector normal,
			Vector center) {
		// triangle not visible
		if (!(checkCoords(p1) || checkCoords(p2) || checkCoords(p3)))
			return;

		// sort the points in ascending Y order
		if (p1.Y > p2.Y) {
			Vertex temp = p2;
			p2 = p1;
			p1 = temp;
		}

		if (p2.Y > p3.Y) {
			Vertex temp = p2;
			p2 = p3;
			p3 = temp;
		}

		if (p1.Y > p2.Y) {
			Vertex temp = p2;
			p2 = p1;
			p1 = temp;
		}

		// TODO implement lighting
		/*
		 * Vector brightness =
		 * MathHelper.calcBrightness(center,normal,camera.getLightReference());
		 * Iterator<Light> lit = lights.iterator(); while(lit.hasNext()) {
		 * brightness.add(MathHelper.calcBrightness(center, normal,
		 * lit.next())); }
		 */
		Vector brightness = new Vector(1, 1, 1, 1);
		// inverse slopes
		double is12, is13;
		// http://en.wikipedia.org/wiki/Slope
		// Computing inverse slopes
		if (p2.Y - p1.Y > 0)
			is12 = (p2.X - p1.X) / (p2.Y - p1.Y);
		else
			is12 = 0;

		if (p3.Y - p1.Y > 0)
			is13 = (p3.X - p1.X) / (p3.Y - p1.Y);
		else
			is13 = 0;
		// check direction to draw points
		if (is12 > is13) {
			for (int y = (int) p1.Y; y <= (int) p3.Y; ++y) {
				if (y < p2.Y) {
					scanLine(y, p1, p3, p1, p2, brightness);
				} else {
					scanLine(y, p1, p3, p2, p3, brightness);
				}
			}
		} else {
			for (int y = (int) p1.Y; y <= (int) p3.Y; ++y) {
				if (y < p2.Y) {
					scanLine(y, p1, p2, p1, p3, brightness);
				} else {
					scanLine(y, p2, p3, p1, p3, brightness);
				}
			}
		}
	}

	private void scanLine(int y, Vertex pa, Vertex pb, Vertex pc, Vertex pd,
			Vector brightness) {
		if (y < 0 || y >= screenHeight)
			return;
		/*
		 * compute the gradient between the points to get starting and ending
		 * points
		 */
		// if y is equal, force value to be 1 to avoid div by 0
		double gradient1 = (pa.Y != pb.Y) ? (y - pa.Y) / (pb.Y - pa.Y) : 1;
		double gradient2 = (pc.Y != pd.Y) ? (y - pc.Y) / (pd.Y - pc.Y) : 1;

		// starting and ending x coord
		int xstart = (int) MathHelper.interpolate(pa.X, pb.X, gradient1);
		int xend = (int) MathHelper.interpolate(pc.X, pd.X, gradient2);

		if ((xstart < 0 && xend < 0)
				|| (xstart >= screenWidth && xend >= screenWidth))
			return;

		int rgb1 = MathHelper.interpolateColor(pa, pb, gradient1);
		int rgb2 = MathHelper.interpolateColor(pc, pd, gradient2);

		// starting and ending z coord (interpolated across y)
		double z1 = MathHelper.interpolate(pa.Z, pb.Z, gradient1);
		double z2 = MathHelper.interpolate(pc.Z, pd.Z, gradient1);
		int rgb;
		for (int x = xstart; x < xend; ++x) {
			if (x < 0 || x >= screenWidth)
				continue;
			double gradient3 = (double) (x - xstart) / (xend - xstart);
			double z = MathHelper.interpolate(z1, z2, gradient3);
			if (zbuffer[x][y] < z)
				continue;
			zbuffer[x][y] = z;
			rgb = MathHelper.mulColor(
					MathHelper.interpolateColor(rgb1, rgb2, gradient3),
					brightness);
			putPixel(x, y, rgb);
		}
	}

	private long lastTime = System.currentTimeMillis();
	private double lastFPS;
	public double FPS;
	long dt;

	@Override
	public void paint(Graphics g) {
		drawingGraphics = g;
		int curLine = 0;
		//System.out.println(dt);
		render(dt / 1000.0);
		dt = (System.currentTimeMillis() - lastTime);
		FPS = 1000.0 / dt;
		FPS = FPS * 0.5 + lastFPS * 0.5;
		lastFPS = FPS;
		g.setColor(inverseColor(getBackground()));
		g.drawString("FPS:\t" + MathHelper.round(FPS), 10,
				curLine += LINE_HEIGHT);
		g.drawString(camera.toString(), 10, curLine += LINE_HEIGHT);
		g.drawString("Velocity: " + MathHelper.round(camera.getSpeed()), 10,
				curLine += LINE_HEIGHT);
		g.drawString((WIREFRAME) ? "Wireframe mode" : "Shading mode", 10,
				curLine += LINE_HEIGHT);
		g.drawLine((screenWidth >> 1) - CROSSHAIR_SIZE, screenHeight >> 1,
				(screenWidth >> 1) + CROSSHAIR_SIZE, screenHeight >> 1);
		g.drawLine(screenWidth >> 1, (screenHeight >> 1) + CROSSHAIR_SIZE,
				screenWidth >> 1, (screenHeight >> 1) - CROSSHAIR_SIZE);
		lastTime = System.currentTimeMillis();
	}

	public Color inverseColor(Color c) {
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

	public void addObjects(Object3D[] src) {
		for (int i = 0; i < src.length; ++i)
			worldObjects.add(src[i]);
	}

	public void addLight(Light pos) {
		lights.add(pos);
	}

	public void removeLight(Vector pos) {
		worldObjects.remove(pos);
	}

	public void addLights(Collection<Light> src) {
		lights.addAll(src);
	}

	public void removeLights(Collection<Light> src) {
		lights.removeAll(src);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color bg) {
		if (drawingGraphics != null)
			drawingGraphics.setColor(bg);
		super.setBackground(bg);
	}

	/**
	 * @return the render distance
	 */
	public double getRenderDistance() {
		return Math.sqrt(RENDER_DISTANCE);
	}

	/**
	 * @param distance
	 *            the distance to set
	 */
	public void setRenderDistance(double distance) {
		RENDER_DISTANCE = distance * distance;
	}

	/**
	 * Interrupts rendering and creates resized images
	 * 
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentResized(ComponentEvent e) {
		screenWidth = getWidth();
		screenHeight = getHeight();
		zbuffer = new double[screenWidth][screenHeight];
		camera.setAspect((double) screenWidth / screenHeight);
		// free up any old resources
		System.gc();
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

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseLeftDown = true;
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			mouseRightDown = true;
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseLeftDown = false;
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			mouseRightDown = false;
		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!USE_MOUSE)
			return;
		camera.setRotationY(MathHelper.HALF_PI
				* ((double) e.getX() / screenWidth - .5));
		camera.setRotationX(MathHelper.HALF_PI
				* (.5 - (double) e.getY() / screenHeight));

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!USE_MOUSE)
			return;
		camera.setRotationY(MathHelper.HALF_PI
				* ((double) e.getX() / screenWidth - .5));
		camera.setRotationX(MathHelper.HALF_PI
				* (.5 - (double) e.getY() / screenHeight));
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == '1') {
			this.WIREFRAME = !this.WIREFRAME;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		keysBuffer[e.getKeyCode()] = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keysBuffer[e.getKeyCode()] = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		camera.setFov(MathHelper.clamp(camera.getFov() + e.getWheelRotation()
				* ZOOM_SPEED, 0.05, Math.PI - 0.1));
	}

	public void toggleFullscreen() {
		fullscreen = !fullscreen;
		if (fullscreen) {
			GraphicsEnvironment env = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			GraphicsDevice device = env.getDefaultScreenDevice();
			// reopen mainframe
			mainFrame.dispose();
			mainFrame.setUndecorated(true);
			initMainFrame();
			device.setFullScreenWindow(mainFrame);
			if (!device.isFullScreenSupported())
				System.out
						.println("We've been tricked! Your device does not support fullscreen");
		} else {
			GraphicsEnvironment env = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			GraphicsDevice device = env.getDefaultScreenDevice();
			mainFrame.dispose();
			mainFrame.setUndecorated(false);
			initMainFrame();
			device.setFullScreenWindow(null);
		}
	}

	public void setFullscreen(boolean isFullscreen) {
		if (this.fullscreen != isFullscreen) {
			toggleFullscreen();
		}
	}
}

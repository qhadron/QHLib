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

import qh.q3d.Camera;
import qh.q3d.MathHelper;
import qh.q3d.Matrix;
import qh.q3d.Object3D;
import qh.q3d.Vector;
import qh.q3d.Vertex;

public class Window3D extends JPanel implements ComponentListener {
	/**
	 * Generated serial version UID for JComponent
	 */
	private static final long serialVersionUID = 2230112262025881448L;
	private static final boolean DEBUG = false;
	private static final boolean WIREFRAME = false;
	/** codes for computing the regions a line belongs to */
	private static final int OUTCODE_LEFT = 0b0001;
	private static final int OUTCODE_RIGHT = 0b0010;
	private static final int OUTCODE_TOP = 0b0100;
	private static final int OUTCODE_BOTTOM = 0b1000;
	private BufferedImage mbf;
	private Thread calcThread;
	private double[][] zbuffer;

	private boolean[] keysBuffer;

	public Camera camera;

	private LinkedList<Object3D> worldObjects;

	private int windowWidth, windowHeight;
	private int screenWidth, screenHeight;

	private String title;

	private JFrame mainFrame;

	private Graphics2D g2d;

	private PrintStream logger;

	private Object renderlock = new Object();

	/**
	 * puts pixels into backbuffer
	 */
	double viewprojecttime = 0;
	/**
	 * puts pixels into backbuffer
	 */
	double mvptime = 0;
	/**
	 * puts pixels into backbuffer
	 */
	double projecttime = 0;
	/**
	 * puts pixels into backbuffer
	 */
	double drawTime = 0;
	long rendercnt = 0;

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
		camera.setNear(0.1);
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
				long dt;
				while (true) {
					render((dt = (System.currentTimeMillis() - lastTime)) / 1e3);
					lastTime = System.currentTimeMillis();
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						if (DEBUG)
							System.out
									.println("[not an error]: How dare you interrupt me!");
					}
					if (DEBUG)
						if (dt > 20)
							System.out.println("Behind! -> \t" + dt);

				}
			}
		}, "Rendering Calculations Thread");
		calcThread.start();
	}

	private void clear() {
		g2d.fillRect(0, 0, screenWidth, screenHeight);
		// clear depth buffer
		for (int i = 0; i < zbuffer.length; ++i)
			for (int j = 0; j < zbuffer[0].length; ++j)
				zbuffer[i][j] = Double.POSITIVE_INFINITY;
	}

	private synchronized void render(double dt) {
		if (!DEBUG) {
			camera.update(dt);
			Matrix view = camera.getViewMatrix();
			Matrix perspective = camera
					.getPerspectiveMatrix((double) screenWidth / screenHeight);
			Iterator<Object3D> it = worldObjects.iterator();
			synchronized (renderlock) {
				clear();
				synchronized (worldObjects) {
					while (it.hasNext()) {
						Object3D cur = it.next();
						cur.update(dt);
						Vertex[] pixels = new Vertex[cur.vertices.length];
						if (WIREFRAME) {
							Matrix model = cur.getModelMatrix();
							Matrix MV = model.mul(view);
							Matrix MVP = MV.mul(perspective);
							boolean visible[] = new boolean[pixels.length];
							// calculate vertices
							Vector temp;
							for (int i = 0; i < pixels.length; ++i) {
								temp = Vertex.mul(MV, cur.vertices[i]);
								visible[i] = !(temp.Z < camera.getNear());
								pixels[i] = project(cur.vertices[i], MVP);
							}
							// draw faces
							for (int i = 0; i < cur.triangles.length; ++i) {
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
							Matrix model = cur.getModelMatrix();
							Matrix MV = model.mul(view);
							Matrix MVP = MV.mul(perspective);
							boolean visible[] = new boolean[pixels.length];
							// calculate vertices
							Vector temp;
							for (int i = 0; i < pixels.length; ++i) {
								temp = Vertex.mul(MV, cur.vertices[i]);
								visible[i] = !(temp.Z < camera.getNear());
								pixels[i] = project(cur.vertices[i], MVP);
							}
							// draw faces
							for (int i = 0; i < cur.triangles.length; ++i) {
								if (visible[cur.triangles[i].A]
										&& visible[cur.triangles[i].B]
										&& visible[cur.triangles[i].C]) {
									drawTriangle(pixels[cur.triangles[i].A],
											pixels[cur.triangles[i].B],
											pixels[cur.triangles[i].C]);
								}
							}
							/*
							 * boolean visible[] = new boolean[pixels.length];
							 * Matrix MVP = cur.getModelMatrix().mul(view)
							 * .mul(perspective); for (int i = 0; i <
							 * cur.vertices.length; ++i) { pixels[i] =
							 * project(cur.vertices[i], MVP); } for (int i = 0;
							 * i < cur.triangles.length; ++i) { double color =
							 * 0.25 + (i % cur.triangles.length) * 0.75 /
							 * cur.triangles.length;
							 * drawTriangle(pixels[cur.triangles[i].A],
							 * pixels[cur.triangles[i].B],
							 * pixels[cur.triangles[i].C], rgbf(color, color,
							 * color, 1)); }
							 */
						}
					}
					renderlock.notify();
				}
			}
		} else {
			++rendercnt;
			logger.println("--------------------cnt: " + rendercnt
					+ "--------------------");
			long temptime;
			long st = System.currentTimeMillis();
			camera.update(dt);
			Matrix view = camera.getViewMatrix();
			Matrix perspective = camera
					.getPerspectiveMatrix((double) screenWidth / screenHeight);
			viewprojecttime += (temptime = System.currentTimeMillis() - st);
			logger.println("VP time = " + temptime);
			Iterator<Object3D> it = worldObjects.iterator();
			synchronized (renderlock) {
				g2d.fillRect(0, 0, screenWidth, screenHeight);
				synchronized (worldObjects) {
					while (it.hasNext()) {
						Object3D cur = it.next();
						cur.update(dt);
						logger.println(cur);
						st = System.currentTimeMillis();
						Matrix model = cur.getModelMatrix();
						Matrix MV = model.mul(view);
						Matrix MVP = MV.mul(perspective);
						mvptime += (temptime = System.currentTimeMillis() - st);
						logger.println("MVP time = " + (temptime));
						st = System.currentTimeMillis();
						Vertex[] pixels = new Vertex[cur.vertices.length];
						boolean visible[] = new boolean[pixels.length];
						// calculate vertices
						Vector temp;
						for (int i = 0; i < pixels.length; ++i) {
							temp = Vertex.mul(MV, cur.vertices[i]);
							visible[i] = !(temp.Z < camera.getNear());
							pixels[i] = project(cur.vertices[i], MVP);
						}
						projecttime += (temptime = System.currentTimeMillis()
								- st);
						logger.println("projection time = " + (temptime));
						st = System.currentTimeMillis();
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
						drawTime = (temptime = System.currentTimeMillis() - st);
						logger.println("draw time = " + (temptime));
					}
				}
				renderlock.notify();
			}
			logger.println("Totals: ");
			logger.println("VP  time" + ((double) viewprojecttime));
			logger.println("MVP time: \t" + (mvptime / 1000.0));
			logger.println("PRO time: \t" + (projecttime / 1000.0));
			logger.println("DRA time: \t" + (drawTime / 1000.0));
			// System.out.println("Rendering done");
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
		return res;
	}

	public void update() {
		synchronized (renderlock) {
			try {
				renderlock.wait();
			} catch (InterruptedException e) {
				System.out.println("How dare you interrupt me!");
				e.printStackTrace();
			}
			this.repaint();
		}
	}

	private void putPixel(int x, int y, double z, int rgb) {
		if (zbuffer[x][y] < z)
			return;
		zbuffer[x][y] = z;
		mbf.setRGB(x, y, rgb);
	}

	public void drawPoint(double x, double y, double z, int rgb) {
		if (x >= 0 && y >= 0 && x < screenWidth && y < screenHeight) {
			putPixel((int) x, (int) y, z, rgb);
		}
	}

	private void putPixel(int x, int y, int rgb) {
		mbf.setRGB(x, y, rgb);
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
	 * "http://en.wikipedia.org/wiki/Cohen%E2%80%93Sutherland"> Cohen¨CSutherland
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

	/*
	 * // draws a line between papb -> pcpd private void scanLine(int y, Vector
	 * pa, Vector pb, Vector pc, Vector pd, int rgb) {
	 * 
	 * 
	 * // compute the gradient between the points to get starting and ending //
	 * points
	 * 
	 * // if y is equal, force value to be 1 to avoid div by 0 double gradient1
	 * = (pa.Y != pb.Y) ? (y - pa.Y) / (pb.Y - pa.Y) : 1; double gradient2 =
	 * (pc.Y != pd.Y) ? (y - pc.Y) / (pd.Y - pc.Y) : 1;
	 * 
	 * //starting and ending x coord int xstart = (int)
	 * MathHelper.interpolate(pa.X, pb.X, gradient1); int xend = (int)
	 * MathHelper.interpolate(pc.X, pd.X, gradient2);
	 * 
	 * //starting and ending z coord (interpolated across y) double z1 =
	 * MathHelper.interpolate(pa.Z, pb.Z, gradient1); double z2 =
	 * MathHelper.interpolate(pc.Z, pd.Z, gradient1);
	 * 
	 * for (int x = xstart; x < xend; ++x) { double gradient3 = (double) (x -
	 * xstart) / ( xend - xstart); double z = MathHelper.interpolate(z1, z2,
	 * gradient3); drawPoint(x, y, z, rgb); } }
	 */
	// draws a line between papb -> pcpd
	private void scanLine(int y, Vertex pa, Vertex pb, Vertex pc, Vertex pd) {

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

		int rgb1 = MathHelper.interpolateColor(pa, pb, gradient1);
		int rgb2 = MathHelper.interpolateColor(pc, pd, gradient2);

		// starting and ending z coord (interpolated across y)
		double z1 = MathHelper.interpolate(pa.Z, pb.Z, gradient1);
		double z2 = MathHelper.interpolate(pc.Z, pd.Z, gradient1);
		int rgb;
		for (int x = xstart; x < xend; ++x) {
			double gradient3 = (double) (x - xstart) / (xend - xstart);
			double z = MathHelper.interpolate(z1, z2, gradient3);
			rgb = MathHelper.interpolateColor(rgb1, rgb2, gradient3);
			drawPoint(x, y, z, rgb);
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
	private boolean clipCoords(Vector a, Vector b) {
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

	public void drawTriangle(Vertex p1, Vertex p2, Vertex p3) {
		// TODO add triangle clipping
		// http://www.arcsynthesis.org/gltut/Positioning/Tut05%20Boundaries%20and%20Clipping.html
		if (!(clipCoords(p1, p2) && clipCoords(p2, p3) && clipCoords(p1, p3)))
			return;
		// Sorting the points in ascending y
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
		if (is12 > is13) {
			for (int y = (int) p1.Y; y <= (int) p3.Y; ++y) {
				if (y < p2.Y) {
					scanLine(y, p1, p3, p1, p2);
				} else {
					scanLine(y, p1, p3, p2, p3);
				}
			}
		} else {
			for (int y = (int) p1.Y; y <= (int) p3.Y; ++y) {
				if (y < p2.Y) {
					scanLine(y, p1, p2, p1, p3);
				} else {
					scanLine(y, p2, p3, p1, p3);
				}
			}
		}
	}

	long lastTime = System.currentTimeMillis();
	private boolean _updateFPS = true;
	public double FPS;

	@Override
	public void paint(Graphics g) {
		g.drawImage(mbf, 0, 0, this);
		g.setColor(inverseColor(getBackground()));
		if (_updateFPS) {
			FPS = 2000.0 / (System.currentTimeMillis() - lastTime);
			lastTime = System.currentTimeMillis();
			_updateFPS = false;
		} else {
			_updateFPS = true;
		}
		g.drawString(String.format("FPS: %6.2f", FPS), 10, 10);
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

	public void addObjects(Object3D[] src) {
		for (int i = 0; i < src.length; ++i)
			worldObjects.add(src[i]);
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
		if (g2d != null)
			g2d.setColor(bg);
		super.setBackground(bg);
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

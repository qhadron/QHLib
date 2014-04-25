package qh.qwindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class QWindow implements KeyListener, MouseListener, MouseMotionListener {
	
	String title;
	private int maxx, maxy;
	public volatile boolean[] keysDown;
	public volatile int mouseButton;
	public volatile boolean mouseDown;
	public volatile int mouseX,mouseY;
	
	private JFrame mainFrame;
	private QCanvas canvas;
	
	private KeyListener keyListener = this;
	private MouseListener mouseListener = this;
	private MouseMotionListener mouseMotionListener = this;
	
	public QWindow() {
		title = "New Window";
		maxx = 640;
		maxy = 480;
		init();
	}
	
	public QWindow(String title) {
		this.title = title;
		maxx = 640;
		maxy = 480;
		init();
	}
	
	public QWindow(int WIDTH, int HEIGHT) {
		maxx = WIDTH;
		maxy = HEIGHT;
		init();
	}
	
	public QWindow(String title, int width, int height) {
		this.title = title;
		this.maxx = width;
		this.maxy = height;
		init();
	}
	
	public QWindow(String title, KeyListener kl, MouseListener ml, MouseMotionListener mml) {
		this.title = title;
		maxx = 640;
		maxy = 480;
		this.keyListener = kl;
		this.mouseListener = ml;
		this.mouseMotionListener = mml;
		init();
	}
	
	public QWindow(String title, int width, int height, KeyListener kl, MouseListener ml, MouseMotionListener mml) {
		this.title = title;
		maxx = width;
		maxy = height;
		this.keyListener = kl;
		this.mouseListener = ml;
		this.mouseMotionListener = mml;
		init();
	}
	
	void init() {
		keysDown = new boolean[256];
		maxx += 16;
		maxy += 39;
		
		//initialize the JFrame
		try {
			JFrame.setDefaultLookAndFeelDecorated(true);
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err.println("Windows default look and feel not supported: ");
			System.err.println(e.getMessage());
		}
		mainFrame = new JFrame(title);
		mainFrame.setMinimumSize(new Dimension(0,0));
		mainFrame.setResizable(true);
		mainFrame.setAutoRequestFocus(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		//initialize the canvas
		canvas = new QCanvas(maxx,maxy,this);
		canvas.addKeyListener(keyListener);
		canvas.addMouseListener(mouseListener);
		canvas.addMouseMotionListener(mouseMotionListener);
		mainFrame.add(canvas,BorderLayout.CENTER);
		mainFrame.pack();
		canvas.setSize(maxx, maxy);
		mainFrame.setSize(canvas.getPreferredSize());
		
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
		canvas.start();
	}
	
	public void drawLine(int x1, int y1, int  x2,int y2) {
		canvas.drawLine(x1,y1,x2,y2);
	}
	
	public void drawRect(int x1,int y1, int x2, int y2) {
		canvas.drawRect(x1, y1, x2-x1, y2-y1);
	}
	
	public void drawOval(int x, int y, int width, int height) {
		canvas.drawOval(x, y, width, height);
	}
	
	public void drawPolygon(int[] xpoints, int[] ypoints, int npoints) {
		canvas.drawPolygon(xpoints, ypoints, npoints);
	}
	public void fillRect(int x1,int y1, int x2, int y2) {
		canvas.fillRect(x1, y1, x2-x1, y2-y1);
	}
	
	public void drawRect2(int x1,int y1, int width, int height) {
		canvas.drawRect(x1, y1, width, height);
	}
	
	public void fillRect2(int x1,int y1, int width, int height) {
		canvas.fillRect(x1, y1, width, height);
	}
	
	public void fillOval(int x, int y, int width, int height) {
		canvas.fillOval(x, y, width, height);
	}
	
	public void fillPolygon(int[] xpoints, int[] ypoints, int npoints) {
		canvas.fillPolygon(xpoints, ypoints, npoints);
	}
	
	public void drawPolyline(int[] xpoints, int[] ypoints, int npoints) {
		canvas.drawPolyline(xpoints, ypoints, npoints);
	}
	
	public void setColor(Color c) {
		canvas.setColor(c);
	}
	
	public void clear() {
		canvas.clear();
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		keysDown[e.getKeyCode()] = true;
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			System.exit(0);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keysDown[e.getKeyCode()] = false;
	}

	public boolean isKeyDown(int key) {
		return keysDown[key];
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseDown = true;
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	
	public int getWidth() {
		return canvas.getWidth();
	}
	
	public int getHeight() {
		return canvas.getHeight();
	}


	public void addKeyListener(KeyListener kl) {
		canvas.removeKeyListener(this.keyListener);
		canvas.addKeyListener(kl);
	}
	
	public void addMouseListener(MouseListener ml) {
		canvas.removeMouseListener(this.mouseListener);
		canvas.addMouseListener(ml);
	}
	
	public void addMouseMotionListener(MouseMotionListener mml) {
		canvas.removeMouseMotionListener(this.mouseMotionListener);
		canvas.addMouseMotionListener(mml);
	}
	
	public void addMouseWheelListener (MouseWheelListener mwl) {
		canvas.addMouseWheelListener(mwl);
	}
	
	public void addComponentListener (ComponentListener cl) {
		canvas.removeComponentListener(canvas);
		canvas.addComponentListener(cl);
	}
	
	public void removeComponentListener (ComponentListener cl) {
		canvas.removeComponentListener(cl);
	}
	
	public void removeKeyListener(KeyListener kl) {
		canvas.removeKeyListener(kl);
	}
	
	public void removeMouseListener(MouseListener ml) {
		canvas.removeMouseListener(ml);
	}
	
	public void removeMouseMotionListener(MouseMotionListener mml) {
		canvas.removeMouseMotionListener(mml);
	}
	
	public void removeMouseWheelListener(MouseWheelListener mwl) {
		canvas.removeMouseWheelListener(mwl);
	}
	
	public void setTitle(String newTitle) {
		title = newTitle;
		mainFrame.setTitle(title);
	}
	
	public String getTitle() {
		return title;
	}
	
	public void update() {
		canvas.update();
	}
	
	public int getFps() {
		return canvas.getFps();
	}
	public void setFps(int fps) {
		canvas.setFps(fps);;
	}
	public boolean isDoubleBuffered() {
		return canvas.isDoubleBuffered();
	}
	public void setDoubleBuffered(boolean isDoubleBuffered) {
		canvas.setDoubleBuffered(isDoubleBuffered);
	}
	
	public Color getBackgroundColor() {
		return canvas.getBackgroundColor();
	}
	
	public void setBackgroundColor(Color val) {
		canvas.setBackgroundColor(val);
	}
}

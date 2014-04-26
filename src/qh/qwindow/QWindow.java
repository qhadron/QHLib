package qh.qwindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class QWindow extends JPanel implements KeyListener, MouseListener, MouseMotionListener,ActionListener, ComponentListener {
	
	/**
	 * 	Generated serial version UID
	 */
	private static final long serialVersionUID = 6588150114177176857L;
	String title;
	private int maxx, maxy;
	public volatile boolean[] keysDown;
	public volatile int mouseButton;
	public volatile boolean mouseDown;
	public volatile int mouseX,mouseY;
	
	private JFrame mainFrame;
	
	private KeyListener keyListener = this;
	private MouseListener mouseListener = this;
	private MouseMotionListener mouseMotionListener = this;
	

	Timer timer;
	private int fps = 30;
	private int delay;
	
	private Graphics2D g2d;
	private Image image;
	private Color curColor = Color.black;
	private Color backgroundColor = Color.white;
	private boolean doubleBuffered;
	private volatile boolean needUpdate;
	
	public QWindow() {
		super(false);
		title = "New Window";
		maxx = 640;
		maxy = 480;
		init();
	}
	
	public QWindow(String title) {
		super(false);
		this.title = title;
		maxx = 640;
		maxy = 480;
		init();
	}
	
	public QWindow(int WIDTH, int HEIGHT) {
		super(false);
		maxx = WIDTH;
		maxy = HEIGHT;
		init();
	}
	
	public QWindow(String title, int width, int height) {
		super(false);
		this.title = title;
		this.maxx = width;
		this.maxy = height;
		init();
	}
	
	public QWindow(String title, KeyListener kl, MouseListener ml, MouseMotionListener mml) {
		super(false);
		this.title = title;
		maxx = 640;
		maxy = 480;
		this.keyListener = kl;
		this.mouseListener = ml;
		this.mouseMotionListener = mml;
		init();
	}
	
	public QWindow(String title, int width, int height, KeyListener kl, MouseListener ml, MouseMotionListener mml) {
		super(false);
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
		setFocusable(true);
		setDoubleBuffered(false);
		addComponentListener(this);
		delay = (int)((double)1000/fps);
		timer = new Timer(delay,this);
		doubleBuffered = false;
		setBackground(Color.white);
		this.addKeyListener(keyListener);
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseMotionListener);
		mainFrame.add(this,BorderLayout.CENTER);
		mainFrame.pack();
		setSize(maxx, maxy);
		mainFrame.setSize(getPreferredSize());
		
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
		repaint();
		start();
	}
	
	public void start() {
		timer.start();
		if (image == null) {
			image = createVolatileImage(getWidth(), getHeight());
			g2d = (Graphics2D) image.getGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
		clear();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.repaint();
	}
	
	@Override
	public Dimension getPreferredSize() {
        return new Dimension(maxx,maxy);
    }

	@Override
	public void paint(Graphics g) {
		Rectangle r = g.getClipBounds();
		if (!doubleBuffered||needUpdate) {
			g.drawImage(image, r.x, r.y, r.x+r.width, r.y + r.height, r.x, r.y, r.x + r.width, r.y + r.height, Color.white, null);
			needUpdate = false;
		}
	}

	public void clear() {
		g2d.setColor(backgroundColor);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(curColor);
	}
	
	public void update() {
		needUpdate = true;
	}

	public void drawLine(int x1, int y1, int  x2,int y2) {
		g2d.drawLine(x1,y1,x2,y2);
	}
	
	public void drawRect(int x,int y, int width, int height) {
		g2d.drawRect(x, y, width, height);
	}
	
	public void drawRect2(int x1,int y1, int x2, int y2) {
		g2d.drawRect(x1, y1, Math.abs(x2-x1), Math.abs(y2-y1));
	}
	
	public void drawOval(int x, int y, int width, int height) {
		g2d.drawOval(x, y, width, height);
	}
	
	public void drawPolygon(int[] xpoints, int[] ypoints, int npoints) {
		g2d.drawPolygon(xpoints, ypoints, npoints);
	}
	public void fillRect(int x,int y, int width, int height) {
		g2d.fillRect(x, y, width, height);
	}
	
	public void fillRect2(int x1,int y1, int x2, int y2) {
		g2d.fillRect(x1, y1, Math.abs(x2-x1), Math.abs(y2-y1));
	}
	
	public void fillOval(int x, int y, int width, int height) {
		g2d.fillOval(x, y, width, height);
	}
	
	public void fillPolygon(int[] xpoints, int[] ypoints, int npoints) {
		g2d.fillPolygon(xpoints, ypoints, npoints);
	}
	
	public void drawPolyline(int[] xpoints, int[] ypoints, int npoints) {
		g2d.drawPolyline(xpoints, ypoints, npoints);
	}
	
	public int getFps() {
		return fps;
	}
	public void setFps(int fps) {
		this.fps = fps;
	}
	public void setColor(Color c) {
		curColor = c;
		g2d.setColor(c);
	}

	public boolean isDoubleBuffered() {
		return doubleBuffered;
	}
	public void setDoubleBuffered(boolean isDoubleBuffered) {
		this.doubleBuffered = isDoubleBuffered;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	
	public void setTitle(String newTitle) {
		title = newTitle;
		mainFrame.setTitle(title);
	}

	public String getTitle() {
		return title;
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
		mouseButton = e.getButton();
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

	
	@Override
	public void componentResized(ComponentEvent e) {
		BufferedImage temp;
		try {
			 temp = new BufferedImage(maxx, maxy, BufferedImage.TYPE_4BYTE_ABGR);
		} catch (IllegalArgumentException exp) {
			temp = new BufferedImage(maxx+1,maxy+1,BufferedImage.TYPE_4BYTE_ABGR);
		}
		temp.createGraphics().drawImage(image,0,0,null);
		maxx = getWidth();
		maxy = getHeight();
		image = createVolatileImage((maxx==0)?1:maxx, (maxy==0)?1:maxy);
		if (image!=null) {
			g2d = (Graphics2D) image.getGraphics();
			g2d.setBackground(Color.white);
			g2d.clearRect(0, 0, maxx, maxy);
			g2d.drawImage(temp, 0, 0, null);
			g2d.setColor(curColor);
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		repaint();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		
	}
	
}

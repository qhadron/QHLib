package qh.qwindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class QCanvas extends JPanel implements ActionListener, ComponentListener{
	private static final long serialVersionUID = -7588566643786634253L;
	
	JFrame frame;
	
	Timer timer;
	
	private int fps = 30;
	private int delay;
	private int maxx,maxy;
	
	private Graphics2D g2d;
	private Image image;
	private Color curColor = Color.white;
	
	QCanvas(int width, int height, QWindow window) {
		super(true);
		setFocusable(true);
		setDoubleBuffered(false);
		addComponentListener(this);
		delay = (int)((double)1000/fps);
		timer = new Timer(delay,this);
		maxx = width;
		maxy = height;
		setBackground(Color.white);
		repaint();
	}
	public void start() {
		timer.start();
		if (image == null) {
			image = createVolatileImage(getWidth(), getHeight());
			g2d = (Graphics2D) image.getGraphics();
		}
		clear();
	};
	
	
	@Override
	public void paint(Graphics g) {
		Rectangle r = g.getClipBounds();
		g.drawImage(image, r.x, r.y, r.x+r.width, r.y + r.height, r.x, r.y, r.x + r.width, r.y + r.height, Color.white, null);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		this.repaint();
	}
	
	@Override
	public Dimension getPreferredSize() {
        return new Dimension(maxx,maxy);
    }


	public void clear() {
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.setColor(curColor);
	}
	
	public void drawLine(int x1, int y1, int  x2,int y2) {
		g2d.drawLine(x1,y1,x2,y2);
	}
	
	public void drawRect(int x1,int y1, int x2, int y2) {
		g2d.drawRect(x1, y1, x2, y2);
	}
	
	public void drawOval(int x, int y, int width, int height) {
		g2d.drawOval(x, y, width, height);
	}
	
	public void drawPolygon(int[] xpoints, int[] ypoints, int npoints) {
		g2d.drawPolygon(xpoints, ypoints, npoints);
	}
	public void fillRect(int x1,int y1, int x2, int y2) {
		g2d.fillRect(x1, y1, x2, y2);
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
	
	public void setColor(Color c) {
		curColor = c;
		g2d.setColor(c);
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
		image = createVolatileImage(maxx, maxy);
		g2d = (Graphics2D) image.getGraphics();
		g2d.setBackground(Color.white);
		g2d.clearRect(0, 0, maxx, maxy);
		g2d.drawImage(temp, 0, 0, null);
		g2d.setColor(curColor);
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

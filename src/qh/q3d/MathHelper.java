package qh.q3d;


public class MathHelper {
	public static double clamp(double value) {
		return Math.max(0, Math.min(value, 1));
	}

	// Interpolating the value between 2 numbers
	// min is the starting point, max the ending point
	// and gradient the % between the 2 numbers
	public static double interpolate(double min, double max, double gradient) {
		return min + (max - min) * clamp(gradient);
	}

	public static int interpolateColor(Vertex a, Vertex b, double gradient) {
		int r1 = a.color.getRed(), g1 = a.color.getGreen(), b1 = a.color
				.getBlue();
		int r2 = b.color.getRed(), g2 = b.color.getGreen(), b2 = b.color
				.getBlue();
		int R = (int) interpolate(r1, r2, gradient);
		int G = (int) interpolate(g1, g2, gradient);
		int B = (int) interpolate(b1, b2, gradient);
		return rgb(R, G, B, 255);
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

	public static int interpolateColor(int rgb1, int rgb2, double gradient) {
		int r1 = 0xff & ((rgb1) >> 16), r2 = 0xff & ((rgb2) >> 16);
		int g1 = 0xff & ((rgb1) >>  8), g2 = 0xff & ((rgb2) >>  8);
		int b1 = 0xff & ((rgb1) >>  0), b2 = 0xff & ((rgb2) >>  0);
		int R = (int) interpolate(r1, r2, gradient);
		int G = (int) interpolate(g1, g2, gradient);
		int B = (int) interpolate(b1, b2, gradient);
		return rgb(R, G, B, 255);
	}

}

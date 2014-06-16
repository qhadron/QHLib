package qh.q3d;

import qh.math.Vector;
import qh.q3d.objects.Light;

public class MathHelper {
	private static final double EPSILON = 0.1;
	public static final double TWO_PI = Math.PI * 2;
	public static final double HALF_PI = Math.PI / 2;
	public static final double THREEHALVES_PI = Math.PI + HALF_PI;
	public static final double RAD_TO_DEG_RATIO = 180 / Math.PI;
	public static final double DEG_TO_RAD_RATIO = Math.PI / 180;

	public static double clamp(double val, double min, double max) {
		return Math.max(min, Math.min(val, max));
	}

	public static double clamp(double value) {
		return clamp(value, 0, 1);
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

	/**
	 * Return a long in case of negative numbers
	 * 
	 * @param rgb1
	 *            color 1
	 * @param rgb2
	 *            color 2
	 * @param gradient
	 *            % between the colors
	 * @return a blend of the two colors
	 */
	public static int interpolateColor(int rgb1, int rgb2, double gradient) {
		int r1 = 0xff & ((rgb1) >> 16), r2 = 0xff & ((rgb2) >> 16);
		int g1 = 0xff & ((rgb1) >> 8), g2 = 0xff & ((rgb2) >> 8);
		int b1 = 0xff & ((rgb1)), b2 = 0xff & ((rgb2));
		int R = (int) interpolate(r1, r2, gradient);
		int G = (int) interpolate(g1, g2, gradient);
		int B = (int) interpolate(b1, b2, gradient);
		return rgb(R, G, B, 255);
	}

	/**
	 * Floating point modulo operation (a mod b). Defined as x - y * round(x/y).
	 * 
	 * @param x
	 *            left operand
	 * @param y
	 *            right operand
	 * @return "remainder" of division
	 */
	public static double mod(double x, double y) {
		return x - y * Math.round(x / y);
	}

	public static boolean between(double x, double low, double high) {
		return (x > low) && (x < high);
	}

	public static boolean around(double x, double target) {
		return Math.abs(x - target) < EPSILON;
	}

	public static boolean around(double x, double target, double epsilon) {
		return Math.abs(x - target) < epsilon;
	}

	public static double toDegrees(double radians) {
		return radians * RAD_TO_DEG_RATIO;
	}

	public static double toRadians(double degrees) {
		return degrees * DEG_TO_RAD_RATIO;
	}

	public static long fast10pow(int p) {
		if (p == 0)
			return 1;
		long num = 10;
		while (p > 0) {
			if ((p & 1) == 0) {
				num *= num;
				p >>= 1;
			} else {
				num *= 10;
				--p;
			}
		}
		return num;
	}

	public static String round(double x, int digits) {
		double t = Math.pow(10, digits);
		x *= t;
		x = (long) x / t;
		char[] s = String.valueOf(x).toCharArray();
		int i = 0, l;
		for (; i < s.length && s[i] != '.'; ++i)
			;
		l = i += digits + 1;
		if (l > s.length) {
			String res = new String(s);
			while (l > res.length())
				res += '0';
			return res;
		}
		if (i < s.length && s[i--] >= '5') {
			do {
				if (s[i] == '.')
					--i;
				else {
					s[i] += 1;
					if (s[i] > '9')
						s[i--] = '0';
					else
						break;
					if (i == -1)
						return "1" + new String(s, 0, l);
				}
			} while (i >= 0);
		}
		return new String(s, 0, l);
	}

	public static Vector calcBrightness(Vector vertex, Vector normal,
			Light light) {
		return Vector.mul(
				light.color,
				Math.max(
						0,
						Vector.sub(light, vertex).normalize()
								.dot(Vector.normal(normal))));
	}

	public static int mulColor(int rgb, double val) {
		return rgb((int) ((0xff & ((rgb) >> 16)) * val),
				(int) (((0xff & (rgb) >> 8)) * val),
				(int) ((0xff & rgb) * val), 255);
	}

	public static int mulColor(int rgb, Vector val) {
		return rgb((int) ((0xff & ((rgb) >> 16)) * val.X),
				(int) (((0xff & (rgb) >> 8)) * val.Y),
				(int) ((0xff & rgb) * val.Z), 255);
	}
}

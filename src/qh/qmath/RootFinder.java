package qh.qmath;

public class RootFinder {
	public double sqrt(double x) {
		double result = x;
		if (x > 0) {
			int cnt = 0;
			while ((result /= 100) > 100)
				cnt++;
			if (result > 10)
				result *= 6;
			else
				result *= 2;
			for (int i = 1; i <= cnt; i++)
				result *= 10;
			//result = x / 2;
			for (int i = 0; i < 6; i ++)
				result = 0.5 * (result + x / result);
		} else {
			result = Double.NaN;
		}
		return result;
	}
	
	public static void main(String[] args){
		final double[] x = {579096231,806762548,718644768,881988823.0,417012049};
		double[] resulta = new double[5];
		double[] resultb = new double[5];
		double timea = 0, timeb = 0;
		long t1,t2=0;
		RootFinder rf = new RootFinder();
		for (int i = 0; i < 10e6; i ++)
			if (i % 2 == 0)
				resulta[i%5]=rf.sqrt(x[i % 5]);
			else
				resultb[i%5]=Math.sqrt(x[i % 5]);

		int tempint;
		for (int i = 0; i < 10e6 ; i ++){
			tempint = i % 5;
			t1 = System.nanoTime();
			 resultb[tempint]=Math.sqrt(x[tempint]);
			t2 = System.nanoTime();
			timeb += t2-t1;
		}
		for (int i = 0; i < 10e6 ; i ++){
			tempint = i % 5;
			t1 = System.nanoTime();
			resulta[tempint]=rf.sqrt(x[tempint]);
			t2 = System.nanoTime();
			timea += t2-t1;

		}
		
		timea /= 10e6;
		timeb /= 10e6;
		for (int i = 0; i < 5; i ++){
			System.out.println("[A]sqrt(" + x[i] + "): " +resulta[i] );
			System.out.println("[B]sqrt(" + x[i] + "): " + resultb[i]);
		}
		System.out.println("[A] " + timea + " [B] " + timeb);
	}
}

package qh.qmath;
import java.util.ArrayList;

/**
 * @author jackl_000
 *
 */

public class FactorHelper {

	ArrayList<Long> allFactors;

	/**
	 * @param numberToCheck
	 * @return if the number is prime or not
	 */
	public static boolean checkIsPrime(long numberToCheck) {

		if (numberToCheck == 2) {
			return true;
		} else {

			if (numberToCheck % 2 == 0) {
				return false;
			}
			
			if (numberToCheck == 1) {
				return false;
			}

			if (numberToCheck == 0) {
				return false;
			}

			if (numberToCheck < 0) {
				return false;
			}
		}

		long limit = Math.round(Math.sqrt(numberToCheck));

		for (long i = 3; i <= limit; i += 2) {
			if (numberToCheck % i == 0) {
				return false;
			}
		}
		return true;

	}

	/**
	 * @param numberToCheck
	 * @return all prime factors of the number
	 */
	public static ArrayList<Long> getPrimeFactors(long numberToCheck) {
		ArrayList<Long> result = new ArrayList<Long>();
		if (!(checkIsPrime(numberToCheck))) {
			
			long x = 2;

			while (x * x <= numberToCheck) {
				if (numberToCheck % x == 0) {
					result.add(x);
					numberToCheck = numberToCheck / x;
				} else {
					x++;
				}
			}
			result.add(numberToCheck);
			return result;
		} else {
			result.add(numberToCheck);
			return result;
		}
	}
	//get factors from prime factors (slower than needed, but nice to have)
	/*private void findFactors(Long[] primeFactors, int[] count,
			int currentIndex, long curResult) {
		if (currentIndex == primeFactors.length) {
			allFactors.add(curResult);
			return;
		}

		for (int i = 0; i <= count[currentIndex]; ++i) {
			findFactors(primeFactors, count, currentIndex + 1, curResult);
			curResult *= primeFactors[currentIndex];
		}

	}

	public ArrayList<Long> getFactors(long numberToCheck) {
		ArrayList<Long> primeFactors = getPrimeFactors(numberToCheck);
		ArrayList<Long> factors = new ArrayList<Long>();
		int[] primeFactorCount = new int[primeFactors.size()];

		for (int i = 0; i < primeFactorCount.length; i++)
			primeFactorCount[i] = 1;

		for (int i = 0; i < primeFactors.size(); i++) {
			if (!factors.contains(primeFactors.get(i)))
				factors.add(primeFactors.get(i));
			else
				primeFactorCount[factors.size() - 1]++;
		}
		
		allFactors = new ArrayList<Long>();
		
		findFactors(factors.toArray(new Long[factors.size()]),
				primeFactorCount, 0, 1);
		
		factors = allFactors;

		Collections.sort(factors);
		return factors;
	}*/
	/**
	 * @param num - the number to check
	 * @return all the factors of num
	 */
	public static ArrayList<Long> getFactors(long num) {
		ArrayList<Long> result = new ArrayList<Long>();
		for (long i= 1; i <= num; i++){
			if (num%i==0)
				result.add(i);
		}
		return result;
	}
	/**
	 * @param a - the first number
	 * @param b - the second number
	 * @return a long[2] containing the two numbers with all common factors removed 
	 */
	public static long[] removeCommonFactors(long a, long b) {
		ArrayList<Long> aCF = getFactors(a);
		ArrayList<Long> bCF = getFactors(b);
		aCF.remove(new Long(1));
		bCF.remove(new Long(1));
		for (int i = 0; i < aCF.size(); i++) {
			for (int j = 0; j < bCF.size(); j++) {
				if (aCF.get(i) == bCF.get(j)) {
					return removeCommonFactors(a / aCF.get(i), b / aCF.get(i));
				}
			}
		}
		return new long[] { a, b };

	}
	
	/**
	 * @param a 
	 * @param b
	 * @return the smallest common multiple between a and b
	 */
	public static  long getSmallestCommonMultiple(long a, long b) {
		ArrayList<Long> pf1;
		ArrayList<Long> pf2;
		ArrayList<Long> pf3 = new ArrayList<Long>();
		long result = 1;
		pf1 = getPrimeFactors(a);
		pf2 = getPrimeFactors(b);
		int smallersize =(pf1.size()<pf2.size())?pf1.size():pf2.size();
		for (int i = 0; i < smallersize ; i++) {
			if (pf1.get(i) == pf2.get(i)) {
				pf3.add(pf1.get(i));
			} else {
				pf3.add(pf1.get(i));
				pf3.add(pf2.get(i));
			}
		}
		
		for (int i = 0; i < pf3.size(); i ++)
			result *= pf3.get(i);
		return result;
	}
	
	/**
	 * @param a
	 * @param b
	 * @return returns the greatest common factor of a and b
	 */
	public static long getGreatestCommonFactor(long a,long b) {
		ArrayList<Long> pf1;
		ArrayList<Long> pf2;
		pf1 = getFactors(a);
		pf2 = getFactors(b);
		Long largestFactor = 1L;
		for (int i = 0; i < pf1.size() ; i++) {
			for (int j = 0; j < pf2.size(); j++) {
				if (pf1.get(i) == pf2.get(j))
					if (pf1.get(i) > largestFactor)
						largestFactor = pf1.get(i);
			}
		}
		
		return largestFactor;
	}
	
	/**
	 * Finds primes using a sieve algorithm
	 * 
	 * @param limit the upper limit of numbers to check
	 * @return an ArrayList containing the primes
	 */
	public static ArrayList<Long> getPrimes(int limit) {
		ArrayList<Long> primes = new ArrayList<Long>((int)(limit/(Math.log10(limit)-1)));
		boolean[] notPrime = new boolean[limit];
		int n;
		for (int i = 2; i <= limit; i++) {
			if (!notPrime[i-2]) {
				primes.add((long) i);
				n = i+i;
				while (n<limit+2) {
					notPrime[n-2] = true;
					n+=i;
					
				}
			}
		}
		
		return primes;
	}
	
	/**
	 * Finds primes by testing against previous primes
	 * 
	 * !!!REALLY SLOW!!!
	 * 
	 * @param limit the upper limit of numbers to check
	 * @return an ArrayList containing the primes
	 */
	@Deprecated
	public static ArrayList<Long> getPrimes(long limit) {
		ArrayList<Long> primes = new ArrayList<Long>((int)(limit/(Math.log10(limit)-1)));
		boolean p = false;
		int sqrt = 0;
		primes.add(2L);
		for (long i = 3; i <= limit; i+=2,p=true,sqrt=(int)Math.sqrt(i)) {
			for (long j:primes) {
				if (j > sqrt)
					break;
				if (i%j==0) {
					p=false;
					break;
				}
			}
			if (p)
				primes.add(i);
		}
		return primes;
	}
}
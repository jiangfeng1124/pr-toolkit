package edlin.sequence;


/***
 * This class implements Hamming Loss function
 * @author Kuzman Ganchev and Georgi Georgiev
 * <A HREF="mailto:georgiev@ontotext.com>georgi.georgiev@ontotext.com</A>
 * <A HREF="mailto:ganchev@ontotext.com>kuzman.ganchev@ontotext.com</A>
 * Date: Thu Feb 26 12:27:56 EET 2009
 */

public class HammingLoss implements Loss {

	public double calculate(int[] truth, int[] guess) {

		double result = 0;

		for (int i = 0; i < guess.length; i++) {
			if (guess[i]!=truth[i]) {
				result++;
			}
		}

		return result;
	}



}

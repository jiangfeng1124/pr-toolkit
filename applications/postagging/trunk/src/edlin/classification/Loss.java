package edlin.classification;

/***
 * This class defines the contract we hava for loss function
 * @author Kuzman Ganchev and Georgi Georgiev
 * <A HREF="mailto:georgiev@ontotext.com>georgi.georgiev@ontotext.com</A>
 * <A HREF="mailto:ganchev@ontotext.com>kuzman.ganchev@ontotext.com</A>
 * Date: Thu Feb 26 12:27:56 EET 2009
 */

public interface Loss {

	public double calculate(int truth, int guess);

}

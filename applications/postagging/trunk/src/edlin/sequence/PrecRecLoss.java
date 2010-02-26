package edlin.sequence;

import edlin.types.Evaluation;


/***
 * This class implements a loss function for high precision or recall.
 * @author Kuzman Ganchev and Georgi Georgiev
 * <A HREF="mailto:georgiev@ontotext.com>georgi.georgiev@ontotext.com</A>
 * <A HREF="mailto:ganchev@ontotext.com>kuzman.ganchev@ontotext.com</A>
 * Date: Thu Feb 26 12:27:56 EET 2009
 */

public class PrecRecLoss implements Loss {
	
	int tagOfInterest;
	double precWeight; 
	double recWeight;
	
	public PrecRecLoss(int tagOfInterest, double precWeight){
		this.tagOfInterest = tagOfInterest;
		if (Double.isInfinite(precWeight)){
			recWeight = 0;
			this.precWeight = 1;
		} else{
			recWeight = 1;
			this.precWeight = precWeight;
		}
	}

	public double calculate(int[] truth, int[] guess) {
		Evaluation eval = Evaluate.eval(truth, guess, tagOfInterest);
		// this ensures that we don't make an update if fn = 0.
		if (precWeight==0 && eval.fn == 0) 
			return -1e10;
		return eval.fp*precWeight + eval.fn*recWeight;
	}



}

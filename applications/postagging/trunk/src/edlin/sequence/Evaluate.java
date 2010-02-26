package edlin.sequence;

import java.util.ArrayList;
import java.util.HashSet;

import edlin.types.Evaluation;


public class Evaluate {
	
	/**
	 * Compute evaluation of entire instance list, with respect to a single tag.  
	 * @param h
	 * @param data
	 * @param tagOfInterest tag wrt which we compute tp,fp,fn
	 * @return
	 */
	public static Evaluation eval(LinearTagger h, ArrayList<SequenceInstance> data, int tagOfInterest){
		Evaluation eval = new Evaluation(0,0,0,0,0);
		for (SequenceInstance inst : data) {
			int[] hx = h.label(inst.x);
			updateEvaluation(eval, inst.y, hx, tagOfInterest);
		}
		return eval;		
	}
	
	/**
	 * Compute evaluation of entire instance list, with respect to a single tag.  
	 * @param h
	 * @param data
	 * @param tagOfInterest tag wrt which we compute tp,fp,fn
	 * @return
	 */
	public static Evaluation eval(LinearTagger h, SequenceInstance inst, int tagOfInterest){
		Evaluation eval = new Evaluation(0,0,0,0,0);
		int[] hx = h.label(inst.x);
		updateEvaluation(eval, inst.y, hx, tagOfInterest);
		return eval;
	}
	
	public static Evaluation eval(int[] truth, int[] guess, int tagOfInterest){
		Evaluation eval = new Evaluation(0,0,0,0,0);
		updateEvaluation(eval, truth, guess, tagOfInterest);
		return eval;
	}

	private static void updateEvaluation(Evaluation eval, int[] truth, int[] guess, int tagOfInterest){
		HashSet<String> truespans = getSpans(truth, tagOfInterest);
		HashSet<String> guessedspans = getSpans(guess, tagOfInterest);
		for (int t = 0; t < truth.length; t++) {
			eval.total++;
			if (truth[t] == guess[t]) eval.correct++;
		}
		for (String t:truespans){
			if (guessedspans.contains(t)) eval.tp++;
			else eval.fn++;
		}
		for (String g:guessedspans){
			if (!truespans.contains(g)) eval.fp++;
		}	
	}
	

	
	private static HashSet<String> getSpans(int[] y, int tagOfInterest) {
		HashSet<String> res = new HashSet<String>();
		for (int t=0; t<y.length; t++){
			if (y[t] == tagOfInterest){
				int start = t;
				while(t<y.length && y[t] == tagOfInterest) t++;
				res.add(start+"-"+(t-1));
			}
		}
		return res;
	}



}

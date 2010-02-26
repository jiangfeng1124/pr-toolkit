package edlin.classification;

import java.util.ArrayList;

import edlin.types.ClassificationInstance;
import edlin.types.Evaluation;
import edlin.types.LinearClassifier;

public class Evaluate {
	
	/**
	 * Compute evaluation of entire instance list, with respect to a single tag.  
	 * @param h
	 * @param data
	 * @param tagOfInterest tag wrt which we compute tp,fp,fn
	 * @return
	 */
	public static Evaluation eval(LinearClassifier h, ArrayList<ClassificationInstance> data, int tagOfInterest){
		Evaluation eval = new Evaluation(0,0,0,0,0);
		for (ClassificationInstance inst : data) {
			int hx = h.label(inst.x);
			updateEvaluation(eval, inst.y, hx, tagOfInterest);
		}
		return eval;		
	}
	
	public static Evaluation[] eval(LinearClassifier h, ArrayList<ClassificationInstance> data){
		Evaluation[] res = new Evaluation[data.get(0).yAlphabet.size()];
		for (int y = 0; y < data.get(0).yAlphabet.size(); y++) {
			res[y] = new Evaluation(0,0,0,0,0);
		}
		for (ClassificationInstance inst : data) {
			int hx = h.label(inst.x);
			for (int y = 0; y < data.get(0).yAlphabet.size(); y++) {
				updateEvaluation(res[y], inst.y, hx, y);
			}
		}
		return res;
	}
	
	/**
	 * Compute evaluation of entire instance list, with respect to a single tag.  
	 * @param h
	 * @param data
	 * @param tagOfInterest tag wrt which we compute tp,fp,fn
	 * @return
	 */
	public static Evaluation eval(LinearClassifier h, ClassificationInstance inst, int tagOfInterest){
		Evaluation eval = new Evaluation(0,0,0,0,0);
		int hx = h.label(inst.x);
		updateEvaluation(eval, inst.y, hx, tagOfInterest);
		return eval;
	}
	
	public static Evaluation eval(int truth, int guess, int tagOfInterest){
		Evaluation eval = new Evaluation(0,0,0,0,0);
		updateEvaluation(eval, truth, guess, tagOfInterest);
		return eval;
	}
	
	private static void updateEvaluation(Evaluation eval, int truth, int guess, int tagOfInterest){
		eval.total++;
		if (truth == tagOfInterest && truth == guess) eval.tp++;
		if (truth == tagOfInterest && truth != guess) eval.fn++;
		if (guess == tagOfInterest && truth != guess) eval.fp++;
	}
	




}

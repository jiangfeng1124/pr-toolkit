package model.distribution.trainer;

import gnu.trove.TIntArrayList;
import model.distribution.AbstractMultinomial;

/**
 * Finds the best parameters using variational Bayes with a Dirichlet Prior
 * @author javg
 *
 */
public class MultinomialVariationalBayesTrainer implements AbstractMultinomialTrainer{
	
	double prior;
	
	public MultinomialVariationalBayesTrainer(double prior) {
		this.prior = prior;
	}
	
	
	public AbstractMultinomial update(AbstractMultinomial counts){
		AbstractMultinomial params = counts.clone();
		return update(counts,params);
	}
	
	public AbstractMultinomial update(AbstractMultinomial countsTable, AbstractMultinomial paramsTable){
		for(int variable = 0; variable < countsTable.numVariables(); variable++){
			double sum=0;
			TIntArrayList states = countsTable.getAvailableStates(variable);
			
			for(int stateInd = 0; stateInd < states.size(); stateInd++){
				int state = states.getQuick(stateInd);
				double counts = countsTable.getCounts(variable, state);
				sum += counts;
			}
			double newSum  = util.DigammaFunction.expDigamma(sum+countsTable.numStates()*prior);
			if(newSum < 1.E-100 || Double.isNaN(newSum) || Double.isInfinite(newSum)){
				System.out.println("Probem with variational sum after digamma function");
			}else{
				for(int stateInd = 0; stateInd < states.size(); stateInd++){
					int state = states.getQuick(stateInd);
					double counts = countsTable.getCounts(variable, state);
					double newCounts = util.DigammaFunction.expDigamma(counts+prior);
					paramsTable.setCounts(variable, state, newCounts/newSum);
				}
			}
		}		
		return paramsTable;
	}
}

package model.distribution.trainer;

import gnu.trove.TIntArrayList;
import model.distribution.AbstractMultinomial;

/**
 * Finds the best parameters using variational Bayes with a Dirichlet Prior
 * @author javg
 *
 */
public class TableNormalizerMultinomialTrainer implements AbstractMultinomialTrainer{
	
	
	public AbstractMultinomial update(AbstractMultinomial counts){
		AbstractMultinomial params = counts.clone();
		return update(counts,params);
	}
	
	public AbstractMultinomial update(AbstractMultinomial countsTable, AbstractMultinomial paramsTable){
		paramsTable.copyAndNormalize(countsTable);
		return paramsTable;
	}
}

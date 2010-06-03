package model.distribution.trainer;

import gnu.trove.TIntArrayList;
import model.distribution.AbstractMultinomial;

/**
 * Finds the best parameters using variational Bayes with a Dirichlet Prior
 * @author javg
 *
 */
public interface AbstractMultinomialTrainer {
	
	public AbstractMultinomial update(AbstractMultinomial counts);	
	public AbstractMultinomial update(AbstractMultinomial countsTable, AbstractMultinomial paramsTable);	
}

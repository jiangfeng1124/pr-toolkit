package model.chain.hmmFinalState;

import model.AbstractCountTable;
import model.chain.hmm.HMMCountTable;
import model.distribution.Multinomial;

public class HMMFinalStateCountTable extends HMMCountTable{

	
	
	/**
	 * Creates count tables for hmm with final state.
	 * nr-states includes the spurious state
	 * so initial counts and observation do not need to have them.
	 * @param nrObservations
	 * @param nrStates
	 */
	public HMMFinalStateCountTable(int nrObservations, int nrStates){
		initialCounts = new Multinomial(1,nrStates-1);
		transitionCounts = new Multinomial(nrStates,nrStates);
		observationCounts = new Multinomial(nrStates-1,nrObservations);
	}
	
}

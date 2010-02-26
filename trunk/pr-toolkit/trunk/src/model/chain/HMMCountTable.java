package model.chain;

import model.AbstractCountTable;
import model.distribution.Multinomial;

public class HMMCountTable extends AbstractCountTable{

	
	public Multinomial initialCounts;
	public Multinomial transitionCounts;
	public Multinomial observationCounts;
	
	protected HMMCountTable(){
		
	}
	
	public HMMCountTable(int nrObservations, int nrStates){
		initialCounts = new Multinomial(1,nrStates);
		transitionCounts = new Multinomial(nrStates,nrStates);
		observationCounts = new Multinomial(nrStates,nrObservations);
	}
	
	
	@Override
	public void fill(double value) {
		initialCounts.fill(value);
		transitionCounts.fill(value);
		observationCounts.fill(value);
		
	}

	@Override
	public void fill(AbstractCountTable value) {
		if (value instanceof HMMCountTable) {
			initialCounts.fill(((HMMCountTable) value).initialCounts);
			transitionCounts.fill(((HMMCountTable) value).transitionCounts);
			observationCounts.fill(((HMMCountTable) value).observationCounts);
		}
	}
	
	public void print(){
		initialCounts.print("Initial Counts",null,null);
		transitionCounts.print("Transition Counts",null,null);
		observationCounts.print("Observatiob Counts",null,null);
	}

}

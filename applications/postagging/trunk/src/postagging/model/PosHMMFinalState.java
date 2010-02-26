package postagging.model;




import model.chain.hmmFinalState.HMMFinalState;
import postagging.data.PosCorpus;

public class PosHMMFinalState extends HMMFinalState{
	
	
	public PosHMMFinalState(PosCorpus c, int nrTags) {
		super(c,c.getNrWordTypes(),nrTags);
	}
		
	public String getName(){
		return "POSTAG HMM Final State";
	}
	
	

	
	
	
	
	
	
}

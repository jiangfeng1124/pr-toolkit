package postagging.model;





import model.chain.hmmFinalState.ReverseHMMFinalState;
import postagging.data.PosCorpus;

public class PosReverseHMMFinalState extends ReverseHMMFinalState{
	
	
	public PosReverseHMMFinalState(PosCorpus c, int nrTags) {
		super(c,c.getNrWordTypes(),nrTags);
	}
		
	public String getName(){
		return "POSTAG Reverse HMM Final State";
	}

	
	
	
	
	
	
}

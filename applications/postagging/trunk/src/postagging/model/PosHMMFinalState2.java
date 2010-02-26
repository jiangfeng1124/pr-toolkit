package postagging.model;




import model.chain.hmmFinalState2.HMMFinalState2;
import postagging.data.PosCorpus;

public class PosHMMFinalState2 extends HMMFinalState2{
	
	
	public PosHMMFinalState2(PosCorpus c, int nrTags) {
		super(c,c.getNrWordTypes(),nrTags);
	}
		
	public String getName(){
		return "POSTAG HMM Final State";
	}
	
	

	
	
	
	
	
	
}

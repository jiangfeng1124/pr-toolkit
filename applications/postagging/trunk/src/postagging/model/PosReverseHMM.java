package postagging.model;




import model.chain.hmm.ReverseHMM;
import postagging.data.PosCorpus;

public class PosReverseHMM extends ReverseHMM{
	
	
	public PosReverseHMM(PosCorpus c, int nrTags) {
		super(c,c.getNrWordTypes(),nrTags);
	}
		
	public String getName(){
		return "POSTAG Reverse HMM";
	}

	
	
	
	
	
	
}

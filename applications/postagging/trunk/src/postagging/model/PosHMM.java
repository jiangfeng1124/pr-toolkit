package postagging.model;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import data.Corpus;
import model.chain.hmm.HMM;
import model.distribution.Multinomial;
import postagging.data.PosCorpus;

public class PosHMM extends HMM{
	
	
	public PosHMM(PosCorpus c, int nrStates) {
			super(c,c.getNrWordTypes(),nrStates);
	}
		
	public String getName(){
		return "POSTAG HMM";
	}

	public static PosHMM loadModel(Corpus corpus, String directory) throws IOException {
		System.out.println(corpus.getName());
		System.out.println(directory);
		if (!corpus.checkDescription(directory)) {
			System.out.println("Corpus is not the same");
			System.exit(1);
		}
		Properties properties = new Properties();
		InputStream input = new FileInputStream(directory+ "properties");
		properties.load(input);
		int nrStates = (Integer) properties.get("nrStates");
		PosHMM model = new PosHMM((PosCorpus)corpus,nrStates);
		model.initialProbabilities = Multinomial.load(directory+"/initProb");
		model.observationProbabilities = Multinomial.load(directory+"/obsProb");
		model.observationProbabilities = Multinomial.load(directory+"/transProb");
		return model;
	}
	
	
	
	
	
}

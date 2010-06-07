package learning;





import java.io.IOException;
import java.io.UnsupportedEncodingException;

import learning.stats.TrainStats;
import model.AbstractCountTable;
import model.AbstractModel;
import model.AbstractSentenceDist;


public class EM {

	public final AbstractModel model;
	
	
	protected int iter;
	
	
	
	public EM(AbstractModel model){
		this.model = model;
		
	}
	
	
	/**
	 * Iterates E step and M step numIters times.
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public void em(int numIters, TrainStats stats) throws UnsupportedEncodingException, IOException {
		stats.emStart(this.model,this);
		
	
		AbstractSentenceDist[] sentenceDists = model.getSentenceDists();
		
		AbstractCountTable counts = model.getCountTable();
		
		for(iter = 0; iter < numIters; iter++) {
			System.out.println("\nEM Iteration " + (iter+1));
			System.out.flush();
			stats.emIterStart(this.model, this);		
		
			
			corpusEStep(counts, sentenceDists, stats);
			//System.exit(-1);
			
			// M step
			stats.mStepStart(this.model, this);
			mStep(counts);
			stats.mStepEnd(this.model, this);
			System.out.print(stats.printEndMStep(this.model,this));
			
			stats.emIterEnd(this.model, this);
			System.out.print(stats.printEndEMIter(this.model,this));
		}
		stats.emEnd(this.model, this);
		System.out.print(stats.printEndEM(this.model,this));
		System.out.println();
	}
	
	
	public void sentenceEStep(AbstractSentenceDist sd, AbstractCountTable counts, TrainStats stats) {
		sd.initSentenceDist();
		stats.eStepSentenceStart(model,this,sd);
		model.computePosteriors(sd);
		sd.clearCaches();
		model.addToCounts(sd,counts);	
		stats.eStepSentenceEnd(model,this,sd);
		sd.clearPosteriors();
		stats.printEndSentenceEStep(model,this);
		
	}

	public void corpusEStep(AbstractCountTable counts, AbstractSentenceDist[] sentenceDists, TrainStats stats) {	
		// Clear model for accumulating E step counts
		counts.fill(0);
		// Corpus E step
		stats.eStepStart(this.model, this);
		System.out.println("Using senteces on training:" + sentenceDists.length);
		for(AbstractSentenceDist sd : sentenceDists){			
			sentenceEStep(sd, counts,stats);
		}
		stats.eStepEnd(this.model, this);
		System.out.print(stats.printEndEStep(this.model,this));
	}
	
	public void mStep(AbstractCountTable counts) {
		model.updateParameters(counts);
	}
	
	public int getCurrentIterationNumber() {
		return iter; 
	}
}

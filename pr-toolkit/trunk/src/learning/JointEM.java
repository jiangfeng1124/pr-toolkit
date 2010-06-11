package learning;





import learning.stats.JointTrainStats;
import model.AbstractCountTable;
import model.AbstractModel;
import model.AbstractSentenceDist;

/**
 * This is a test to train several models at the same time
 * Ideally there will be just a EM model. But for now
 * we will have this two until the abstractions come off.
 * 
 * @author javg
 *
 */
public class JointEM {

	public final AbstractModel[] models;
	
	
	protected int iter;
	
	
	
	public JointEM(AbstractModel[] models){
		this.models = models;
		
	}
	
	
	/**
	 * Iterates E step and M step numIters times.
	 */
	public void em(int numIters, JointTrainStats stats) {
		stats.emStart(this.models,this);
		
		//Create models and sentence dists 
		AbstractSentenceDist[][] sentenceDists = new AbstractSentenceDist[models.length][];
		AbstractCountTable[] counts = new AbstractCountTable[models.length];
		for (int i = 0; i < sentenceDists.length; i++) {
			sentenceDists[i] = models[i].getSentenceDists();
			counts[i] = models[i].getCountTable();
		}
		
		

		
		for(iter = 0; iter < numIters; iter++) {
			System.out.println("\nEM Iteration " + (iter+1));
			System.out.flush();
			stats.emIterStart(this.models, this);		
		
			// Clear model for accumulating E step counts
			for (int i = 0; i < models.length; i++) {
				counts[i].clear();
			}
			// Corpus E step
			stats.eStepStart(this.models, this);
			corpusEStep(counts, sentenceDists, stats);
			stats.eStepEnd(this.models, this);
			System.out.print(stats.printEndEStep(this.models,this));
			
			// M step
			stats.mStepStart(this.models, this);
			mStep(counts);
			stats.mStepEnd(this.models, this);
			System.out.print(stats.printEndMStep(this.models,this));
			
			stats.emIterEnd(this.models, this);
			System.out.print(stats.printEndEMIter(this.models,this));
		}
		stats.emEnd(this.models, this);
		System.out.print(stats.printEndEM(this.models,this));
		System.out.println();
	}
	
	
	public void sentenceEStep(AbstractSentenceDist[] sd, AbstractCountTable[] counts, JointTrainStats stats) {
		for (int i = 0; i < sd.length; i++) {
			sd[i].initSentenceDist();
		}
		stats.eStepSentenceStart(models,this,sd);
		for (int i = 0; i < sd.length; i++) {
			models[i].computePosteriors(sd[i]);
			models[i].addToCounts(sd[i],counts[i]);
		}			
		stats.eStepSentenceEnd(models,this,sd);
		stats.printEndSentenceEStep(models,this);
	}
	
	


	public void corpusEStep(AbstractCountTable[] counts, AbstractSentenceDist[][] sentenceDists, JointTrainStats stats) {	
		System.out.println("Calling corpusEStep");
		AbstractSentenceDist[] pair = new AbstractSentenceDist[2];
		for(int i = 0; i < sentenceDists[0].length; i++){
			pair[0]=sentenceDists[0][i];
			pair[1]=sentenceDists[1][i];
			sentenceEStep(pair, counts,stats);
		}
		
	}
	
	public void mStep(AbstractCountTable[] counts) {
		for (int i = 0; i < counts.length; i++) {
			models[i].updateParameters(counts[i]);
		}

	}
	
	public int getCurrentIterationNumber() {
		return iter; 
	}
}

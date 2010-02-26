package learning;

import learning.stats.TrainStats;
import model.AbstractCountTable;
import model.AbstractModel;
import model.AbstractSentenceDist;
import constraints.CorpusConstraints;

public class CorpusPR extends EM {

	final CorpusConstraints cstraints; 

	
	
	public CorpusPR(AbstractModel model,CorpusConstraints cstraints) {
		super(model);
		this.cstraints = cstraints;
	}

	@Override
	/**
	 * Its the constraints responsability to computer all the data and leave
	 * the posteriors well for the end of the corpus...
	 */
	public void corpusEStep(AbstractCountTable counts, AbstractSentenceDist[] sentenceDists, TrainStats stats) {
		
		
		stats.eStepBeforeConstraints(model,this,cstraints,sentenceDists);
		cstraints.project(counts, sentenceDists,stats,this);
		stats.eStepAfterConstraints(model,this,cstraints,sentenceDists);
		
		
//		for(AbstractSentenceDist sd : sentenceDists){	
//			stats.eStepSentenceStart(model,this,sd);
//			sd.clearCaches();
//			model.addToCounts(sd, counts);
//			stats.eStepSentenceEnd(model,this,sd);
//			sd.clearPosteriors();
//		}		
	}
	
}


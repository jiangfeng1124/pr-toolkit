package learning;

import learning.stats.JointTrainStats;
import learning.stats.TrainStats;
import model.AbstractCountTable;
import model.AbstractModel;
import model.AbstractSentenceDist;
import constraints.CorpusConstraints;
import constraints.JointCorpusConstraints;

public class JointCorpusPR extends JointEM {

	final JointCorpusConstraints  cstraints; 

	
	
	public JointCorpusPR(AbstractModel[] models,JointCorpusConstraints cstraints) {
		super(models);
		this.cstraints = cstraints;
	}

	@Override
	public void corpusEStep(AbstractCountTable[] counts, AbstractSentenceDist[][] sentenceDists, JointTrainStats stats) {
		for (int i = 0; i < sentenceDists[0].length; i++) {
				sentenceDists[0][i].initSentenceDist();
				sentenceDists[1][i].initSentenceDist();
				models[0].computePosteriors(sentenceDists[0][i]);
				models[1].computePosteriors(sentenceDists[1][i]);
		}
		
		stats.eStepBeforeConstraints(models,this,cstraints,sentenceDists);
		cstraints.project(sentenceDists);
		stats.eStepAfterConstraints(models,this,cstraints,sentenceDists);
		
		AbstractSentenceDist[] pair = new AbstractSentenceDist[2];
		for(int i = 0; i < sentenceDists[0].length; i++){
			pair[0]=sentenceDists[0][i];
			pair[1]=sentenceDists[1][i];
			stats.eStepSentenceStart(models,this,pair);
			models[0].addToCounts(pair[0], counts[0]);
			models[1].addToCounts(pair[1], counts[1]);
			stats.eStepSentenceEnd(models,this,pair);
		}				
	}
	
}


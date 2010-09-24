package constraints;


import learning.CorpusPR;
import learning.stats.TrainStats;
import model.AbstractCountTable;
import model.AbstractSentenceDist;


public interface CorpusConstraints {

	/**
	 * Receives the posterios to projects and the counts table.
	 * It is required to return the updated count table this way
	 * we do not need to keep all the posteriors in memory at once.
	 * 
	 * Also need to keep stats up to date for last EM iteration.
	 * @param counts
	 * @param posteriors
	 */
	void project(AbstractCountTable counts, AbstractSentenceDist[] posteriors, TrainStats stats, CorpusPR pr);

}

package constraints;


import model.AbstractSentenceDist;


public interface JointCorpusConstraints {

	void project(AbstractSentenceDist[][] posteriors);

}

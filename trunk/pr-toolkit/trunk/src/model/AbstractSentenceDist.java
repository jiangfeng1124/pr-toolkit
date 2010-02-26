package model;

public abstract class AbstractSentenceDist {
	public abstract double getLogLikelihood();
	public abstract void initSentenceDist();
	public abstract void clearCaches();
	public abstract void clearPosteriors();
}

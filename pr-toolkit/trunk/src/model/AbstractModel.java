package model;

import data.InstanceList;



public abstract class AbstractModel {

	public abstract void computePosteriors(AbstractSentenceDist dist);
	public abstract void addToCounts(AbstractSentenceDist sd, AbstractCountTable counts);
	public abstract void updateParameters(AbstractCountTable counts);
	
	public abstract AbstractCountTable getCountTable();
	public abstract AbstractSentenceDist[] getSentenceDists();
	public abstract AbstractSentenceDist[] getSentenceDists(InstanceList list);
}

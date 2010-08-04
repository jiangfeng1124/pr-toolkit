package edlin.io;

import java.util.ArrayList;

import postagging.data.PosCorpus;


import model.distribution.trainer.ObservationMultinomialFeatureFunction;

import data.InstanceList;
import data.WordInstance;
import edlin.sequence.SequenceInstance;
import edlin.types.Alphabet;
import edlin.types.SparseVector;


public class Corpus2POSFeatures {
	Alphabet xAlphabet;
	Alphabet yAlphabet;
	PosCorpus corpus;
	ObservationMultinomialFeatureFunction fx;
	
	public Corpus2POSFeatures(Alphabet xAlphabet, Alphabet yAlphabet, PosCorpus c, ObservationMultinomialFeatureFunction fx) {
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		this.corpus = c;
		this.fx =fx;
	}


	public ArrayList<SequenceInstance> getTrain() {
		return ids2seqenceInstances(corpus.trainInstances);
	}

	public ArrayList<SequenceInstance> getTest() {
		if (true ) throw new UnsupportedOperationException("not implemented!");
		return ids2seqenceInstances(corpus.testInstances.get(0));
	}
	
	public ArrayList<SequenceInstance> ids2seqenceInstances(InstanceList in){
		ArrayList<SequenceInstance> result = new ArrayList<SequenceInstance>();
		for (int i = 0; i < in.instanceList.size(); i++) {
			WordInstance instance = in.instanceList.get(i);
			SparseVector[] x = new SparseVector[instance.words.length];
			String[] y = new String[instance.words.length];
			for (int position = 0; position < instance.words.length; position++) {
				y[position] = corpus.tagAlphabet.lookupIndex(instance.getTagId(position));
				
				util.SparseVector tmp = fx.apply(instance.getTagId(position), instance.getWordId(position));
				SparseVector sv = new SparseVector();
				for (int j = 0; j < tmp.numEntries(); j++) {
					sv.add(xAlphabet.lookupObject(fx.al.lookupIndex(tmp.getIndexAt(j))), tmp.getValueAt(j));
				}
				x[position] = sv;
			}
			SequenceInstance inst = new SequenceInstance(xAlphabet, yAlphabet, x,y);
			result.add(inst);
		}
		return result;
	}
	
	

}

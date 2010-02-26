package util.pipes;

import java.io.IOException;

import util.Alphabet;
import util.SparseVector;
import data.Corpus;

public abstract class Pipe{
	public Corpus c;
	public abstract void process(int wordId, String word, Alphabet<String> alphabet,SparseVector sv);
	
	//Pre-Processing steps.
	public void init(Corpus c) throws IOException{
		this.c = c;
	}
	
	public abstract String getName();
}

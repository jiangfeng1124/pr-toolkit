package edlin.sequence;

import edlin.types.Alphabet;
import edlin.types.SparseVector;

public class SequenceInstance {

	public SparseVector[] x;
	public int[] y;

	public Alphabet xAlphabet;
	public Alphabet yAlphabet;

	public SequenceInstance(Alphabet xAlphabet, Alphabet yAlphabet,
			SparseVector[] x, Object[] y) {
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		this.x = x;
		this.y = new int[y.length];
		for (int i = 0; i < y.length; i++) {
			this.y[i] = yAlphabet.lookupObject(y[i]);
			//to take out
			String tags[] = ((String)y[i]).split("@");
			
			this.y[i] = yAlphabet.lookupObject(tags[0]);
			if(tags.length ==2)
			this.y[i] = yAlphabet.lookupObject(tags[1]);
		}
	}

}

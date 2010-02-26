package edlin.types;

public class ClassificationInstance {

	public SparseVector x;
	public int y;

	public Alphabet xAlphabet;
	public Alphabet yAlphabet;

	public ClassificationInstance(Alphabet xAlphabet, Alphabet yAlphabet,
			SparseVector x, Object y) {
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		this.x = x;
		this.y = yAlphabet.lookupObject(y);
	}

	@Override
	public String toString() {
		return y + ": " + x.toString();
	}

}

package edlin.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import edlin.types.Alphabet;
import edlin.types.ClassificationInstance;
import edlin.types.SparseVector;


public class InternetAdReader {

	Alphabet xAlphabet;
	Alphabet yAlphabet;

	public InternetAdReader(Alphabet xAlphabet, Alphabet yAlphabet) {
		this.xAlphabet = xAlphabet;
		this.yAlphabet = yAlphabet;
		// just so the ordering is the identity look up indices 3..1560
		for (int x = 3; x < 1560; x++)
			xAlphabet.lookupObject(x);
	}

	public ArrayList<ClassificationInstance> readFile(String dataLoc)
			throws IOException {
		ArrayList<ClassificationInstance> result = new ArrayList<ClassificationInstance>();
		BufferedReader reader = new BufferedReader(new FileReader(dataLoc));
		String ln = reader.readLine();
		while (ln != null) {
			String[] feats = ln.split(",");
			SparseVector x = new SparseVector();
			Object y = feats[feats.length - 1];
			for (int i = 3; i < feats.length - 1; i++) {
				if (feats[i].equals("1"))
					x.add(xAlphabet.lookupObject(i), 1.0);
			}
			result.add(new ClassificationInstance(xAlphabet, yAlphabet, x, y));
			ln = reader.readLine();
		}
		return result;
	}

}

package util.pipes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import util.Alphabet;
import util.SparseVector;
import data.Corpus;

public class MorphChan extends Pipe {

	boolean self = false;
	boolean allSubsets2 = false;
	boolean allSubsets3 = false;
	boolean allSubsets4 = false;
	boolean selfAndAllSubsets1 = false;
	boolean selfAndAllSubsets2 = false;
	boolean selfAndAllSubsets3 = false;
	boolean selfAndAllSubsets4 = false;
	String fname = null;
	Corpus c;
	private Morphology[] morphology;
	
	private class Morphology{
		String[] transformations;
		String thisTransform;
		private Morphology(String input){
			transformations = input.split("-");
			if (transformations.length!=2) throw new IllegalArgumentException("can't parse "+input);
			thisTransform = transformations[1].intern();
			transformations = transformations[0].split("_");
			for (int i = 0; i < transformations.length; i++) {
				transformations[i] = transformations[i].intern();
			}
		}
	}
	
	public MorphChan(String[] args) {
		fname = args[0];
		for (int i = 1; i < args.length; i++) {
			String arg = args[i];
			if(arg.charAt(0)=='#') break;
			else if(arg.equals("self")) self = true;
			else if(arg.equals("allSubsets2")) allSubsets2 = true;
			else if(arg.equals("allSubsets3")) allSubsets3 = true;
			else if(arg.equals("allSubsets4")) allSubsets4 = true;
			else if(arg.equals("selfAndAllSubsets1")) selfAndAllSubsets1 = true;
			else if(arg.equals("selfAndAllSubsets2")) selfAndAllSubsets2 = true;
			else if(arg.equals("selfAndAllSubsets3")) selfAndAllSubsets3 = true;
			else if(arg.equals("selfAndAllSubsets4")) selfAndAllSubsets4 = true;
			else throw new IllegalArgumentException("unknown argument "+arg);
		}
		
	}

	@Override
	public String getName() {
		StringBuilder res = new StringBuilder();
		StringBuilder off = new StringBuilder();
		res.append("MorphChan ");
		if(self) res.append("self ");
		else off.append("self ");
		if (allSubsets2) res.append("allSubsets2 ");
		else off.append("allSubsets2 ");
		if (allSubsets3) res.append("allSubsets3 ");
		else off.append("allSubsets3 ");
		if (allSubsets4) res.append("allSubsets4 ");
		else off.append("allSubsets4 ");
		if (selfAndAllSubsets1) res.append("selfAndAllSubsets1 ");
		else off.append("selfAndAllSubsets1 ");
		if (selfAndAllSubsets2) res.append("selfAndAllSubsets2 ");
		else off.append("selfAndAllSubsets2 ");
		if (selfAndAllSubsets3) res.append("selfAndAllSubsets3 ");
		else off.append("selfAndAllSubsets3 ");
		if (selfAndAllSubsets4) res.append("selfAndAllSubsets4 ");
		else off.append("selfAndAllSubsets4 ");
		res.append(" # ");
		res.append(off);
		return res.toString();
	}

	public void init(Corpus c) throws IOException{
		this.c = c;
		c.wordAlphabet.stopGrowth();
		morphology = new Morphology[c.wordAlphabet.size()];
		System.out.println("Reading "+fname+" as MorphChan morphology");
		int numLines = 0;
		int numUsed = 0;
		BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(fname),"UTF8"));
		for(String line = reader.readLine(); line != null; line = reader.readLine()){
			numLines++;
			String[] tmp = line.split("  *");
			int index = c.wordAlphabet.lookupObject(tmp[0]);
			if (index < 0) continue;
			numUsed ++;
			morphology[index] = new Morphology(tmp[1]);
		}
		System.out.println("  .. done reading "+fname+" used "+numUsed+"/"+numLines+" vocab = "+c.wordAlphabet.size());
	}
	
	@Override
	public void process(int wordId, String word, Alphabet<String> a,
			SparseVector sv) {
		Morphology m = morphology[wordId];
		if (m==null) return;
		if (self){
			sv.add(a.lookupObject("morph=s"+m.thisTransform), 1);
		}
		//System.out.println(c.wordAlphabet.lookupIndex(wordId));
		if (selfAndAllSubsets1 || selfAndAllSubsets2 || selfAndAllSubsets3 || selfAndAllSubsets4 || allSubsets2 || allSubsets3 || allSubsets4){
			String selfAnd = "morph=all"+m.thisTransform+"_";
			for (int i = 0; i < m.transformations.length; i++) {
				String selfAndi = selfAnd+m.transformations[i];
				String justi = "morph=just"+m.transformations[i];
				double streni = 1.0/m.transformations.length;
				if(selfAndAllSubsets1) sv.add(a.lookupObject(selfAndi),1);
				if(selfAndAllSubsets2 || selfAndAllSubsets3 || selfAndAllSubsets4 || allSubsets2 || allSubsets3 || allSubsets4){
					for (int j = 0; j < i; j++) {
						String selfAndij=selfAndi+m.transformations[j];
						String justij = justi+m.transformations[j];
						double strenj = streni/m.transformations.length;
						if(selfAndAllSubsets2)
							sv.add(a.lookupObject(selfAndij),strenj);
						if(allSubsets2)
							sv.add(a.lookupObject(justij),1);
						if(selfAndAllSubsets3 || selfAndAllSubsets4 || allSubsets3 || allSubsets4){
							for (int k = 0; k < j; k++) {
								String selfAndijk = selfAndij+m.transformations[k];
								String justijk = justij+m.transformations[k];
								double strenk = strenj/m.transformations.length;
								if (selfAndAllSubsets3)
									sv.add(a.lookupObject(selfAndijk), strenk);
								if (allSubsets3)
									sv.add(a.lookupObject(justijk), strenk);
								if(selfAndAllSubsets4 || allSubsets4){
									for (int l = 0; l < k; l++) {
										String selfAndijkl = selfAndijk + m.transformations[l];
										String justijkl = selfAndijk + m.transformations[l];
										double strenl = strenk/m.transformations.length;
										if (selfAndAllSubsets4)
											sv.add(a.lookupObject(selfAndijkl), strenl);
										if (allSubsets4)
											sv.add(a.lookupObject(justijkl),strenl);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	public String getFeaturePrefix(){
		return "morph";
	}
}

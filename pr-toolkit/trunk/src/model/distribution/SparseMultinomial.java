package model.distribution;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntDoubleIterator;

import java.util.Random;



/**
 * Contains a multivariate multinomial distribution
 * Sum over all states for a given variable = 1
 * Implements a sparse version of the Multinomial
 * For each variable we will only have a small number 
 * of states
 * @author javg
 *
 */
public class SparseMultinomial  implements AbstractMultinomial{
	TIntDoubleHashMap[] values;
	int variables;
	int states;
	
	public SparseMultinomial(int variables, int states, TIntArrayList[] sparsityPattern){
		this.variables = variables;
		this.states = states;
		values = new TIntDoubleHashMap[variables];
		for (int i = 0; i < values.length; i++) {
			TIntDoubleHashMap entry = new TIntDoubleHashMap();
			TIntArrayList sparsity = sparsityPattern[i];
			for(int j = 0; j < sparsity.size(); j++){
				entry.put(sparsity.get(j), 0);
			}
			entry.compact();
			values[i] = entry;
		}
	}
	
	public int numStates(){
		return states;
	}
	
	public int numVariables(){
		return states;
	}
	
	
	public  TIntArrayList[] getAvailableStates(){
		TIntArrayList[] results = new TIntArrayList[variables];
		for (int i = 0; i < variables; i++) {
			results[i] = new TIntArrayList(values[i].keys());
		}
		return results;
	}
	
	/**
	 * In this case this method does not make a lot of sense
	 * since all values are valid for each position.
	 */
	public  TIntArrayList getAvailableStates(int position){
		return new TIntArrayList(values[position].keys());
	}
	
	public void print(String name, String[] labels1, String[] labels2){
		System.out.println(name);
		for(int variable =0; variable < variables; variable++){
			if(labels1 != null){
				System.out.print(labels1[variable]+" - ");
			}else{
				System.out.print(variable+" - ");
			}
			TIntDoubleIterator iter = values[variable].iterator();
			while(iter.hasNext()){
				iter.advance();
				System.out.print(iter.key()+":"+iter.value()+" ");
					
			}
			System.out.println();
		}
	}
	
	public String toString(String name, String[] labels1, String[] labels2){
		StringBuffer sb = new StringBuffer();
		sb.append(name+"\n");
		for(int variable =0; variable < variables; variable++){
			if(labels1 != null){
				sb.append(labels1[variable]+" - ");
			}else{
				sb.append(variable+" - ");
			}
			TIntDoubleIterator iter = values[variable].iterator();
			while(iter.hasNext()){
				iter.advance();
				sb.append(iter.key()+":"+iter.value()+" ");
					
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
		
	public void fill(double value){
		for(int variable =0; variable < variables; variable++){
			TIntDoubleIterator iter = values[variable].iterator();
			while(iter.hasNext()){
				iter.advance();
				iter.setValue(value);
				
			}
		}
	}
	
	public void fill(AbstractMultinomial other){
		fill(other);
	}
	
	public void fill(SparseMultinomial other){
		for(int variable =0; variable < variables; variable++){
			values[variable] = (TIntDoubleHashMap) other.values[variable].clone();
		}
	}
	
	public final void addCounts(int variable, int state, double value){
		if(values[variable].contains(state)){
			values[variable].put(state, values[variable].get(state)+value);
		}else{
			throw new RuntimeException("Sparse multinomial does not contain that entry \n"+
					"variable: " + variable + " state: " +state);
		}
	}
	public final void setCounts(int variable, int state, double value){
		if(values[variable].contains(state)){
			values[variable].put(state, value);
		}else{
			throw new RuntimeException("Sparse multinomial does not contain that entry \n"+
					"variable: " + variable + " state: " +state);
		}
	}
	public final double getCounts(int variable, int state){
		if(values[variable].contains(state)){
			return values[variable].get(state);
		}else{
			throw new RuntimeException("Sparse multinomial does not contain that entry \n"+
					"variable: " + variable + " state: " +state);
		}
	}
	
	public void initializeRandom(Random r, double jitter){
		for(int i = 0; i < variables; i++){
			double sum = 0;
			TIntDoubleIterator iter = values[i].iterator();
			while(iter.hasNext()){
				iter.advance();
				double value =  1 + jitter*r.nextDouble();
				iter.setValue(value);
				sum+=value;
				
			}
			iter = values[i].iterator();
			while(iter.hasNext()){
				iter.advance();
				sum+=iter.setValue(iter.value()/sum);
				
			}
		}
	}

	public void copyAndNormalize(AbstractMultinomial other){
		copyAndNormalize((SparseMultinomial)other);
	}
	
	public SparseMultinomial() {
	
	}
	
	/**
	 * Creates a copy of this sparseMultinomial
	 * @return
	 */
	public SparseMultinomial clone(){
		SparseMultinomial table = new SparseMultinomial();
		table.variables = variables;
		table.states = states;
		table.values = new TIntDoubleHashMap[variables];
		for (int i = 0; i < variables; i++) {
			table.values[i] = (TIntDoubleHashMap) values[i].clone();
		}
		return table;
	}
	
	public void copyAndNormalize(SparseMultinomial other){
		for(int i = 0; i < variables; i++){
			double sum = 0;
			if(values[i].size() != other.values[i].size()){
				throw new RuntimeException("SparseMultinomail: CopyAndNormalize: "+ 
						"sizes are not the same for variabe " + i);
			}
			TIntDoubleIterator iter = values[i].iterator();
			TIntDoubleIterator otherIter = other.values[i].iterator();
			while(iter.hasNext()){
				iter.advance();
				otherIter.advance();
				double value =otherIter.value(); 
				iter.setValue(value);
				sum+=value;	
			}
			iter = values[i].iterator();
			while(iter.hasNext()){
				iter.advance();
				iter.setValue(iter.value()/sum);
				
			}
		}
	}
	
	public double sum(int variable) {
		double sum = 0;
		TIntArrayList availableStates = getAvailableStates(variable);
		for (int i = 0; i < availableStates.size(); i++) {
			sum +=getCounts(variable, availableStates.getQuick(i));
		}
		return sum;
	}
	
	public void saveTable(String name){
//		try {
//			System.out.println("------------Saving Multinomial Table");
//			String outFilename = name + ".gz";
//			DataOutputStream out = new DataOutputStream(new GZIPOutputStream(
//					new FileOutputStream(outFilename)));
//			out.writeInt(variables);
//			out.writeInt(states);
//			for(int variable =0; variable < variables; variable++){
//				for(int state =0; state < states; state++){
//					out.writeDouble(getCounts(variable, state));
//				}
//			}
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("Error saving multinomial Table");
//			System.exit(1);
//		}
//		System.out.println("------------Saving Multinomial Table END");

	}
//
//	private SparseMultinomial(){
//		
//	}
//	
//	public static SparseMultinomial load(String name){
//		SparseMultinomial table = new SparseMultinomial();
//		System.out.println("------------Loading Multinomial Table");
//		String inFilename = name + ".gz";
//		try {
//			DataInputStream data_in = new DataInputStream(new GZIPInputStream(
//					new FileInputStream(inFilename)));
//			table.variables = data_in.readInt();
//			table.states = data_in.readInt();
//			table.values = new double[table.variables][table.states];
//			for(int variable =0; variable < table.variables; variable++){
//				for(int state =0; state < table.states; state++){
//					table.values[variable][state] = data_in.readDouble();
//				}
//			}
//			data_in.close();
//		} catch (FileNotFoundException e) {
//			System.out.println("Multinomial Table does not exits " + name + ".gz" );
//			System.exit(1);
//		} catch (IOException e) {
//			System.out.println("Error reading Multinomial Table");
//			System.exit(1);
//		}
//		System.out.println("------------Loading Multinomial Table END");
//		return table;
//	
//	}
		
	public boolean equals(Object table){
		if(table instanceof SparseMultinomial){
			return equals(((SparseMultinomial) table));
		}else return false;
	}
	
	public boolean equals(SparseMultinomial table){
		if(variables != table.variables) return false;
		if(states != table.states) return false;
		for(int variable =0; variable < variables; variable++){
			if(!values[variable].equals(table.values[variable])){
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		TIntArrayList[] sparsityPattern = new TIntArrayList[4];
		for (int i = 0; i < sparsityPattern.length; i++) {
			sparsityPattern[i] = new TIntArrayList();
		}
		sparsityPattern[0].add(1);
		sparsityPattern[0].add(3);
		sparsityPattern[1].add(1);
		sparsityPattern[1].add(2);
		sparsityPattern[2].add(0);
		sparsityPattern[2].add(3);
		sparsityPattern[3].add(0);
		sparsityPattern[3].add(1);
		SparseMultinomial multinomial = new SparseMultinomial(4, 4, sparsityPattern);
		multinomial.print("test", null, null);
		multinomial.setCounts(1, 2, 5);
		multinomial.setCounts(1, 1, 3);
		multinomial.setCounts(0, 3, 2);
		multinomial.setCounts(0, 1, 1);
		multinomial.setCounts(2, 0, 1);
		multinomial.setCounts(2, 3, 0.5);
		multinomial.setCounts(3, 0, 1);
		multinomial.setCounts(3, 1, 2);
		multinomial.print("test", null, null);
		multinomial.addCounts(1, 2, 5);
		multinomial.addCounts(1, 1, 3);
		multinomial.addCounts(0, 3, 2);
		multinomial.addCounts(0, 1, 1);
		multinomial.addCounts(2, 0, 1);
		multinomial.addCounts(2, 3, 0.5);
		multinomial.addCounts(3, 0, 1);
		multinomial.addCounts(3, 1, 2);
		multinomial.print("test", null, null);
		SparseMultinomial multinomial2 = new SparseMultinomial(4, 4, sparsityPattern);
		multinomial2.copyAndNormalize(multinomial);
		multinomial.print("test", null, null);
		multinomial2.print("test2", null, null);
		
		SparseMultinomial multinomial3 = multinomial2.clone();
		multinomial3.print("test3", null, null);
		multinomial3.addCounts(1, 1, 3);
		multinomial2.print("test2", null, null);
		multinomial3.print("test3", null, null);
		
	}
	
}

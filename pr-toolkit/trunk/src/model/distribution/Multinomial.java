package model.distribution;

import gnu.trove.TIntArrayList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;



/**
 * Contains a multivariate multinomial distribution
 * Sum over all states for a given variable = 1
 * 
 * @author javg
 *
 */
public class Multinomial implements AbstractMultinomial{
	double[][] values;
	int variables;
	int states;
	
	public Multinomial(int variables, int states){
		this.variables = variables;
		this.states = states;
		values = new double[variables][states];
	}
	
	public int numStates(){
		return states;
	}
	
	public void print(String name, String[] labels1, String[] labels2){
		util.Printing.printDoubleArray(values,labels1,labels2, name);
	}
	
	public String toString(String name, String[] labels1, String[] labels2){
		return util.Printing.doubleArrayToString(values,labels1,labels2, name);
	}
	
	public void fill(double value){
		for(int variable =0; variable < variables; variable++){
			java.util.Arrays.fill(values[variable],value);
		}
	}
	
	public void fill(AbstractMultinomial other){
		fill(other);
	}
	
	public Multinomial clone(){
		Multinomial other = new Multinomial();
		other.variables = variables;
		other.states = states;
		other.values = new double[variables][states];
		for(int variable =0; variable < variables; variable++){
			for(int state =0; state < states; state++){
				other.values[variable][state] = values[variable][state];
			}
		}
		return other;
	}
	
	public void fill(Multinomial other){
		for(int variable =0; variable < variables; variable++){
			for(int state =0; state < states; state++){
				values[variable][state] = other.values[variable][state];
			}
		}
	}
	
	/**
	 * In this case this method does not make a lot of sense
	 * since all values are valid for each position.
	 */
	public  TIntArrayList[] getAvailableStates(){
		TIntArrayList[] results = new TIntArrayList[variables];
		int[] values = new int[states];
		for (int i = 0; i < states; i++) {
			values[i]=i;
		}
		for (int i = 0; i < variables; i++) {
			results[i] = new TIntArrayList(values);
		}
		return results;
	}
	
	/**
	 * In this case this method does not make a lot of sense
	 * since all values are valid for each position.
	 */
	public  TIntArrayList getAvailableStates(int position){
		int[] values = new int[states];
		for (int i = 0; i < states; i++) {
			values[i]=i;
		}
		return new TIntArrayList(values);
	}
	
	public final void addCounts(int variable, int state, double value){
		values[variable][state]+=value;
	}
	public final void setCounts(int variable, int state, double value){
		values[variable][state]=value;
	}
	public final double getCounts(int variable, int state){
		return values[variable][state];
	}
	
	public void initializeRandom(Random r, double jitter){
		for(int i = 0; i < variables; i++){
			double sum = 0;
			for(int j = 0; j < states; j++){
				double value =  1 + jitter*r.nextDouble();
				setCounts(i, j, value);
				sum+=value;
			}
			for(int j = 0; j < states; j++){
				setCounts(i, j, getCounts(i, j)/sum);
			}
		}
	}

	public void copyAndNormalize(AbstractMultinomial other){
		if(other instanceof Multinomial){
			copyAndNormalize((Multinomial)other);
		}
	}
	
	public void copyAndNormalize(Multinomial other){
		for(int i = 0; i < variables; i++){
			double sum = 0;
			for(int j = 0; j < states; j++){
				double value = other.getCounts(i, j);
				setCounts(i, j, value);
				sum+=value;
			}
			if(sum == 0) continue;
			if(Double.isInfinite(sum) || Double.isNaN(sum)){
				System.out.println("Sum is zero or infinity or NAN for multinomial");
				System.exit(-1);
			}
			for(int j = 0; j < states; j++){
				setCounts(i, j, getCounts(i, j)/sum);
			}
		}	
	}
	
	public void saveTable(String name){
		try {
			System.out.println("------------Saving Multinomial Table");
			String outFilename = name + ".gz";
			DataOutputStream out = new DataOutputStream(new GZIPOutputStream(
					new FileOutputStream(outFilename)));
			out.writeInt(variables);
			out.writeInt(states);
			for(int variable =0; variable < variables; variable++){
				for(int state =0; state < states; state++){
					out.writeDouble(getCounts(variable, state));
				}
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error saving multinomial Table");
			System.exit(1);
		}
		System.out.println("------------Saving Multinomial Table END");

	}

	private Multinomial(){
		
	}
	
	public static Multinomial load(String name){
		Multinomial table = new Multinomial();
		System.out.println("------------Loading Multinomial Table");
		String inFilename = name + ".gz";
		try {
			DataInputStream data_in = new DataInputStream(new GZIPInputStream(
					new FileInputStream(inFilename)));
			table.variables = data_in.readInt();
			table.states = data_in.readInt();
			table.values = new double[table.variables][table.states];
			for(int variable =0; variable < table.variables; variable++){
				for(int state =0; state < table.states; state++){
					table.values[variable][state] = data_in.readDouble();
				}
			}
			data_in.close();
		} catch (FileNotFoundException e) {
			System.out.println("Multinomial Table does not exits " + name + ".gz" );
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Error reading Multinomial Table");
			System.exit(1);
		}
		System.out.println("------------Loading Multinomial Table END");
		return table;
	
	}
		
	public boolean equals(Object table){
		if(table instanceof Multinomial){
			return equals(((Multinomial) table));
		}else return false;
	}
	
	public boolean equals(Multinomial table){
		if(variables != table.variables) return false;
		if(states != table.states) return false;
		for(int variable =0; variable < variables; variable++){
			for(int state =0; state < states; state++){
				if(getCounts(variable, state) != table.getCounts(variable, state)){
					return false;
				}
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		
	}
	
}

package edlin.types;

import java.text.DecimalFormat;

public class Evaluation {
	
	public int tp, fp, fn, tn;
	public int correct, total;
	
	public Evaluation(int correct, int total, int tp, int fp,
			int fn) {
		this.correct = correct;
		this.total = total;
		this.tp = tp;
		this.fp = fp;
		this.fn = fn;
	}
	
	public double accuracy(){ return ((double)correct)/total; }
	public double precision(){ return tp+fp==0? 1 : tp/((double)tp+fp); }
	public double recall(){ return tp+fn==0? 1 : tp/((double)tp+fn); }
	public double fscore(){ return 2*precision()*recall()/(precision()+recall()); }
	
	@Override
	public String toString(){
		DecimalFormat nf = new DecimalFormat("%");
		DecimalFormat df = new DecimalFormat("000");
		return " "+df.format(tp+fp)+" "+df.format(tp+fn)+" ("+df.format(tp)+")"+"   "+"P:"+nf.format(precision())+"  R:"+nf.format(recall())+"  F1:"+nf.format(fscore());
	}

	public void add(Evaluation e) {
		tp+=e.tp;
		fp+=e.fp;
		fn+=e.fn;
		tn+=e.tn;
		correct+=e.correct;
		total+=e.total;
	}
	

}

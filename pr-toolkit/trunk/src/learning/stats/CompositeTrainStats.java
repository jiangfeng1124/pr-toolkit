package learning.stats;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import learning.EM;
import model.AbstractModel;
import model.AbstractSentenceDist;
import constraints.CorpusConstraints;

public class CompositeTrainStats<K extends AbstractModel, J extends AbstractSentenceDist> extends TrainStats<K,J> {

        ArrayList<TrainStats<K,J>> stats = new ArrayList<TrainStats<K,J>>();
        
        public void addStats(TrainStats<K,J> stat){
                stats.add(stat);
        }
        
        
        public String getPrefix(){
                return "";
        }
        
        @Override
        public String printEndEM(K model,EM em){
                StringBuffer sb = new StringBuffer();
                for(TrainStats<K,J> stat: stats){
                        String s = stat.printEndEM( model,em);
                        if(s!=""){
                        	s= s.replace("\n", "\n"+stat.getPrefix());
                                sb.append(stat.getPrefix()+s+"\n");
                        }
                }
                return sb.toString();
        }
        @Override
        public String printEndEMIter(K model,EM em){
                StringBuffer sb = new StringBuffer();
                for(TrainStats<K,J> stat: stats){
                        String s = stat.printEndEMIter( model,em);
                        if(s!=""){
                        	s= s.replace("\n", "\n"+stat.getPrefix());
                                sb.append(stat.getPrefix()+s+"\n");
                        }
                        
                }
                return sb.toString();
        }
        
        @Override
        public String printEndEStep(K model,EM em){
                StringBuffer sb = new StringBuffer();
                for(TrainStats<K,J> stat: stats){
                        String s = stat.printEndEStep(model,em);
                        if(s!=""){
                        	s=  s.replace("\n", "\n"+stat.getPrefix());
                                sb.append(stat.getPrefix()+s+"\n");
                        }
                }
                return sb.toString();
        }
        
        @Override
        public String printEndSentenceEStep(K model,EM em){
                StringBuffer sb = new StringBuffer();
                for(TrainStats<K,J> stat: stats){
                        String s = stat.printEndSentenceEStep(model,em);
                        if(s!=""){
                        	s= s.replace("\n", "\n"+stat.getPrefix());
                                sb.append(stat.getPrefix()+s+"\n");
                        }
                }
                return sb.toString();
        }
        
        @Override
        public String printEStepEndConstraints(K model,EM em,
        		CorpusConstraints constraints){
                StringBuffer sb = new StringBuffer();
                for(TrainStats<K,J> stat: stats){
                        String s = stat.printEStepEndConstraints(model,em,constraints);
                        if(s!=""){
                        	s= s.replace("\n", "\n"+stat.getPrefix());
                                sb.append(stat.getPrefix()+s+"\n");
                        }
                }
                return sb.toString();
        }
        
        @Override
        public String printEndMStep(K model,EM em) throws UnsupportedEncodingException, IOException{
                StringBuffer sb = new StringBuffer();
                for(TrainStats<K,J> stat: stats){
                        String s = stat.printEndMStep(model,em);
                        if(s!=""){
                        	s= s.replace("\n", "\n"+stat.getPrefix());
                                sb.append(stat.getPrefix()+s+"\n");
                        }
                }
                return sb.toString();
        }
        @Override
        public void emStart(K model,EM em){
                for(TrainStats<K,J> stat: stats){
                        stat.emStart(model,em);
                }
        }
        @Override
        public void emEnd(K model,EM em){
                for(TrainStats<K,J> stat: stats){
                        stat.emEnd(model,em);
                }
        }
        @Override
        public void emIterStart(K model,EM em){
                for(TrainStats<K,J> stat: stats){
                        stat.emIterStart(model,em);
                }
        }
        
        @Override
        public void emIterEnd(K model,EM em){
                for(TrainStats<K,J> stat: stats){
                        stat.emIterEnd(model,em);
                }
        }
        
        @Override
        public void eStepStart(K model,EM em){
                for(TrainStats<K,J> stat: stats){
                        stat.eStepStart(model,em);
                }
        }
        
        @Override
        public void eStepEnd(K model,EM em){
                for(TrainStats<K,J> stat: stats){
                        stat.eStepEnd(model,em);
                }
        }
        
        @Override
        public void eStepSentenceStart(K model,EM em,J sd){
                for(TrainStats<K,J> stat: stats){
                        stat.eStepSentenceStart(model,em,sd);
                }
        }
        
        @Override
        public void eStepSentenceEnd(K model,EM em,J sd){
                for(TrainStats<K,J> stat: stats){
                        stat.eStepSentenceEnd(model,em,sd);
                }
        }
        
        @Override
        public void mStepStart(K model,EM em){
                for(TrainStats<K,J> stat: stats){
                        stat.mStepStart(model,em);
                }
        }
        @Override
        public void mStepEnd(K model,EM em){
                for(TrainStats<K,J> stat: stats){
                        stat.mStepEnd(model,em);
                }
        }
        
        @Override
        public void eStepBeforeConstraints(K model,EM em,
        		CorpusConstraints constraints,
        		J[] sentenceDists){
                for(TrainStats<K,J> stat: stats){
                        stat.eStepBeforeConstraints(model,em,constraints,sentenceDists);
                }
        }
        @Override
        public void eStepAfterConstraints(K model,EM em,
        		CorpusConstraints constraints,
        		J[] sentenceDists){
                for(TrainStats<K,J> stat: stats){
                        stat.eStepAfterConstraints(model,em,constraints,sentenceDists);
                }
        }
        
        //Implements the simple factory design pattern to read from the file
        public static <K extends AbstractModel, J extends AbstractSentenceDist>
        TrainStats<K,J> buildTrainStats(String fileName)
        throws IOException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
                CompositeTrainStats<K,J> stats = new CompositeTrainStats<K,J>();
                if(fileName.equals("")){        
                      //  stats.addStats(new GlobalEMTimeCounter());
                       // stats.addStats(new L1LMaxStats("10"));
                } else {
                        BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"UTF8"));
                        String line = reader.readLine();
                        while(line != null){
                                if(!line.startsWith("#")){
                                        String[] tokens = line.split(" "); 
                                         try {
                                               ArrayList<String> ctorargs = new ArrayList<String>(tokens.length-1);
                                               @SuppressWarnings("unchecked")
                                               Class name = Class.forName("em.stats."+tokens[0]);
                                               for (int ix = 1; ix < tokens.length; ix++)
                                                 ctorargs.add(ix-1, tokens[ix]);
                                               @SuppressWarnings("unchecked")
                                               Constructor ctor = name.getConstructors()[0];  // hack? existe um...
                                               stats.addStats((TrainStats<K,J>)ctor.newInstance(ctorargs.toArray()));
                                             }
                                             catch (ClassNotFoundException e) {  // forName
                                               System.err.println("Stats " + tokens[0]);
                                               System.err.println(e);
                                               return null;
                                             }                                  
                                        
                                }
                                line = reader.readLine();
                        }
                }
                return stats;
        }
        
    
}
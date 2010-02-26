package learning.stats;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import learning.EM;
import model.AbstractModel;
import model.AbstractSentenceDist;
import constraints.CorpusConstraints;



public class JointCompositeTrainStats extends JointTrainStats{

        ArrayList<TrainStats> stats = new ArrayList<TrainStats>();
        
        public void addStats(TrainStats stat){
                stats.add(stat);
        }
        
        
        public String getPrefix(){
                return "";
        }
        
//        @Override
//        public String printEndEM(AbstractModel model,EM em){
//                StringBuffer sb = new StringBuffer();
//                for(TrainStats stat: stats){
//                        String s = stat.printEndEM( model,em);
//                        if(s!=""){
//                                sb.append(stat.getPrefix()+s);
//                        }
//                }
//                return sb.toString();
//        }
//        @Override
//        public String printEndEMIter(AbstractModel model,EM em){
//                StringBuffer sb = new StringBuffer();
//                for(TrainStats stat: stats){
//                        String s = stat.printEndEMIter( model,em);
//                        if(s!=""){
//                                sb.append(stat.getPrefix()+s);
//                        }
//                        
//                }
//                return sb.toString();
//        }
//        
//        @Override
//        public String printEndEStep(AbstractModel model,EM em){
//                StringBuffer sb = new StringBuffer();
//                for(TrainStats stat: stats){
//                        String s = stat.printEndEStep(model,em);
//                        if(s!=""){
//                                sb.append(stat.getPrefix()+s);
//                        }
//                }
//                return sb.toString();
//        }
//        
//        @Override
//        public String printEndSentenceEStep(AbstractModel model,EM em){
//                StringBuffer sb = new StringBuffer();
//                for(TrainStats stat: stats){
//                        String s = stat.printEndSentenceEStep(model,em);
//                        if(s!=""){
//                                sb.append(stat.getPrefix()+s);
//                        }
//                }
//                return sb.toString();
//        }
//        
//        @Override
//        public String printEStepEndConstraints(AbstractModel model,EM em,
//        		CorpusConstraints constraints){
//                StringBuffer sb = new StringBuffer();
//                for(TrainStats stat: stats){
//                        String s = stat.printEStepEndConstraints(model,em,constraints);
//                        if(s!=""){
//                                sb.append(stat.getPrefix()+s);
//                        }
//                }
//                return sb.toString();
//        }
//        
//        @Override
//        public String printEndMStep(AbstractModel model,EM em){
//                StringBuffer sb = new StringBuffer();
//                for(TrainStats stat: stats){
//                        String s = stat.printEndMStep(model,em);
//                        if(s!=""){
//                                sb.append(stat.getPrefix()+s);
//                        }
//                }
//                return sb.toString();
//        }
//        @Override
//        public void emStart(AbstractModel model,EM em){
//                for(TrainStats stat: stats){
//                        stat.emStart(model,em);
//                }
//        }
//        @Override
//        public void emEnd(AbstractModel model,EM em){
//                for(TrainStats stat: stats){
//                        stat.emEnd(model,em);
//                }
//        }
//        @Override
//        public void emIterStart(AbstractModel model,EM em){
//                for(TrainStats stat: stats){
//                        stat.emIterStart(model,em);
//                }
//        }
//        
//        @Override
//        public void emIterEnd(AbstractModel model,EM em){
//                for(TrainStats stat: stats){
//                        stat.emIterEnd(model,em);
//                }
//        }
//        
//        @Override
//        public void eStepStart(AbstractModel model,EM em){
//                for(TrainStats stat: stats){
//                        stat.eStepStart(model,em);
//                }
//        }
//        
//        @Override
//        public void eStepEnd(AbstractModel model,EM em){
//                for(TrainStats stat: stats){
//                        stat.eStepEnd(model,em);
//                }
//        }
//        
//        @Override
//        public void eStepSentenceStart(AbstractModel model,EM em,AbstractSentenceDist sd){
//                for(TrainStats stat: stats){
//                        stat.eStepSentenceStart(model,em,sd);
//                }
//        }
//        
//        @Override
//        public void eStepSentenceEnd(AbstractModel model,EM em,AbstractSentenceDist sd){
//                for(TrainStats stat: stats){
//                        stat.eStepSentenceEnd(model,em,sd);
//                }
//        }
//        
//        @Override
//        public void mStepStart(AbstractModel model,EM em){
//                for(TrainStats stat: stats){
//                        stat.mStepStart(model,em);
//                }
//        }
//        @Override
//        public void mStepEnd(AbstractModel model,EM em){
//                for(TrainStats stat: stats){
//                        stat.mStepEnd(model,em);
//                }
//        }
//        
//        @Override
//        public void eStepBeforeConstraints(AbstractModel model,EM em,
//        		CorpusConstraints constraints,
//        		AbstractSentenceDist[] sentenceDists){
//                for(TrainStats stat: stats){
//                        stat.eStepBeforeConstraints(model,em,constraints,sentenceDists);
//                }
//        }
//        @Override
//        public void eStepAfterConstraints(AbstractModel model,EM em,
//        		CorpusConstraints constraints,
//        		AbstractSentenceDist[] sentenceDists){
//                for(TrainStats stat: stats){
//                        stat.eStepAfterConstraints(model,em,constraints,sentenceDists);
//                }
//        }
//        
//        //Implements the simple factory design pattern to read from the file
//        public static TrainStats buildTrainStats(String fileName) throws IOException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
//                JointCompositeTrainStats stats = new JointCompositeTrainStats();
//                if(fileName.equals("")){        
//                      //  stats.addStats(new GlobalEMTimeCounter());
//                       // stats.addStats(new L1LMaxStats("10"));
//                } else {
//                        BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"UTF8"));
//                        String line = reader.readLine();
//                        while(line != null){
//                                if(!line.startsWith("#")){
//                                        String[] tokens = line.split(" "); 
//                                         try {
//                                               ArrayList<String> ctorargs = new ArrayList<String>(tokens.length-1);
//                                               @SuppressWarnings("unchecked")
//                                               Class name = Class.forName("em.stats."+tokens[0]);
//                                               for (int ix = 1; ix < tokens.length; ix++)
//                                                 ctorargs.add(ix-1, tokens[ix]);
//                                               @SuppressWarnings("unchecked")
//                                               Constructor ctor = name.getConstructors()[0];  // hack? existe um...
//                                               stats.addStats((TrainStats)ctor.newInstance(ctorargs.toArray()));
//                                             }
//                                             catch (ClassNotFoundException e) {  // forName
//                                               System.err.println("Stats " + tokens[0]);
//                                               System.err.println(e);
//                                               return null;
//                                             }                                  
//                                        
//                                }
//                                line = reader.readLine();
//                        }
//                }
//                return stats;
//        }
//        
        
}
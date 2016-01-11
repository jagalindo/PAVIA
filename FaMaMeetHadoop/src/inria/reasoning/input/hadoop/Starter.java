package inria.reasoning.input.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;

public class Starter {

    public static void main(String[] args) throws Exception {
        
    	String modelname=args[0];
    	String pathToModel=args[1];
    	String pathToModel2=args[2];
    	int numFeatures=0;
    	//generate combinations of 1 element+ key
    	System.out.println("Processing model "+modelname);

//		String[] features= null;
		String path=pathToModel2+modelname;
		try {
	    	XMLReader reader = new XMLReader();
	    	FAMAFeatureModel parseFile = (FAMAFeatureModel) reader.parseFile(path);
			numFeatures = parseFile.getFeatures().size();
			
		} catch (WrongFormatException e) {
			e.printStackTrace();
		}
    	//get the first iteration; a simple list with the list of features
    	System.out.println("Processing output");
        
	    Configuration conf = new Configuration();
        conf.set("modelname", modelname);
        conf.set("path",pathToModel + modelname);
        
        Job job = Job.getInstance(conf,"Permutation");
        FileInputFormat.setInputPaths(job, new Path(pathToModel+"zerokey.txt"));

        //job.addCacheFile(new URI("inputs/fm-source/"+modelname));
        job.setJarByClass(Starter.class);
        job.setMapperClass(PermutationMapInit.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileOutputFormat.setOutputPath(job, new Path(modelname+"/output1/"));
        job.waitForCompletion(true);

    	//check this is the number of features
    	System.out.println("There are "+numFeatures+" features");
	   
    	conf = new Configuration();
        //permito partir por linea
        conf.set("mapreduce.input.fileinputformat.split.minsize", "1");
     
        //compression for communication
        conf.set("mapreduce.output.fileoutputformat.compress.type","BLOCK");
        conf.set("mapreduce.output.fileoutputformat.compress.codec","org.apache.hadoop.io.compress.DefaultCodec");

        //jvm mem for large models 2gb per mapper
        conf.set("mapred.child.java.opts","-Xmx2000m");
   //     int maxsize=250000;
    	//get the permutations for more than one feature
    	for(int i=2;i<=numFeatures;i++){
    		
   // 		String splitsize=(maxsize/(numFeatures-i+1))+"";
    		
    //		System.out.println("Processing output"+(i-1)+" with split "+splitsize);
            
    		
    		conf.set("modelname", modelname);
            conf.set("path",pathToModel + modelname);
       //     conf.set("mapreduce.input.fileinputformat.split.maxsize", splitsize ); 
            job = Job.getInstance(conf,"Permutation");           
            job.setJarByClass(Starter.class);
            job.setInputFormatClass(KeyValueTextInputFormat.class);
            //conf.set("key.value.separator.in.input.line", "\t"); 

            job.setMapperClass(PermutationMap.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            //FileInputFormat.setInputDirRecursive(job, true);
            FileInputFormat.setInputPaths(job, new Path(modelname+"/output"+(i-1)));
            FileOutputFormat.setOutputPath(job, new Path(modelname+"/output"+i+"/"));
            job.waitForCompletion(true);
    	}
    	
    	
    }
}

package inria.distributed.reasoning.deadFeatures;

import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.FAMAfeatureModel.transformations.FeatureModelTransform;
import es.us.isa.Sat4jReasoner.Sat4jReasoner;

public class Starter {

	public static void main(String[] args) throws Exception {

		// Syso the process

		String modelname = args[0];
		String pathToModel = args[1];

		System.out.println("Processing model " + modelname + " with Sat");

		// Load the model to extract the info we need for execution
		XMLReader reader = new XMLReader();
		FAMAFeatureModel parseFile = (FAMAFeatureModel) reader
				.parseFile(pathToModel + modelname);
		Sat4jReasoner reasoner = new Sat4jReasoner();
		FeatureModelTransform transform = new FeatureModelTransform();
		transform.transform(parseFile, reasoner);

		// data required for the process
		int numberOfFeatures = reasoner.getVariables().size();

		String cnfmodel = reasoner.getPartialCNF(1);
		int numclauses = reasoner.clauses.size();
		String[] features = new String[numberOfFeatures];
		String[] cnfvalues = new String[numberOfFeatures];

		int i = 0;
		for (Entry<String, String> entry : reasoner.getVariables().entrySet()) {
			features[i] = entry.getKey();
			cnfvalues[i] = entry.getValue();
			i++;
		}

		// Prepare the configuration
		Configuration conf = new Configuration();
		conf.set("cnf", cnfmodel);
		conf.setStrings("features", features);
		conf.setStrings("cnfvalues", cnfvalues);
		conf.set("num", numclauses + "");

		//Get the features ina  file
		Job job = Job.getInstance(conf, "Permutation");
		FileInputFormat.setInputPaths(job, new Path("zerokey.txt"));
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setJarByClass(inria.distributed.reasoning.deadFeatures.Starter.class);
		job.setMapperClass(inria.distributed.reasoning.deadFeatures.PermutationMapInit.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		Path outputpath = new Path(modelname + "/output1/");
		FileOutputFormat.setOutputPath(job, outputpath);
		job.waitForCompletion(true);

		System.out.println("There are " + numberOfFeatures + " features");
		conf = new Configuration();
		cnfmodel = reasoner.getPartialCNF(i);
		conf.set("cnf", cnfmodel);
		conf.setStrings("features", features);
		conf.setStrings("cnfvalues", cnfvalues);

		//Check how may are dead
		job = Job.getInstance(conf, "Permutation");
		job.setJarByClass(inria.distributed.reasoning.deadFeatures.Starter.class);
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setMapperClass(inria.distributed.reasoning.deadFeatures.PermutationMap.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.setInputPaths(job, new Path(modelname + "/output1"));
		FileOutputFormat.setOutputPath(job, new Path(modelname + "/output2"));
		job.waitForCompletion(true);

	}

	
}

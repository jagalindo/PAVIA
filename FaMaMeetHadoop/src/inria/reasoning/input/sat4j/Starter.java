package inria.reasoning.input.sat4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
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

		// Prepare the writing of the first iteration (taken 1 one 1)
		Job job = Job.getInstance(conf, "Permutation");
		FileInputFormat.setInputPaths(job, new Path("zerokey.txt"));
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setJarByClass(inria.reasoning.input.sat4j.Starter.class);
		job.setMapperClass(inria.reasoning.input.sat4j.PermutationMapInit.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		Path outputpath = new Path(modelname + "/output1/");
		FileOutputFormat.setOutputPath(job, outputpath);

		job.waitForCompletion(true);
		boolean validmodel = hassize(conf,modelname + "/output1/" );
		//This happens when someone doesnt like the crappy ternary operator
		System.out.println("Is there data to keep on generating permutations?"+validmodel);

		if (validmodel) {
			// check this is the number of features
			System.out.println("There are " + numberOfFeatures + " features");
			//int maxsize = 128000000;
			// get the permutations for more than one feature
			for (i = 2; i <= numberOfFeatures && validmodel; i++) {
				//String splitsize = (maxsize / (numberOfFeatures - i + 1)) + "";

				System.out.println("Processing output" + (i - 1));
					//	+ " with split " + splitsize);

				conf = new Configuration();

				//conf.set("mapreduce.input.fileinputformat.split.maxsize",splitsize);

				cnfmodel = reasoner.getPartialCNF(i);
				conf.set("cnf", cnfmodel);
				conf.setStrings("features", features);
				conf.setStrings("cnfvalues", cnfvalues);


				job = Job.getInstance(conf, "Permutation");
				job.setJarByClass(inria.reasoning.input.sat4j.Starter.class);
				job.setInputFormatClass(KeyValueTextInputFormat.class);

				job.setMapperClass(inria.reasoning.input.sat4j.PermutationMap.class);
				job.setOutputKeyClass(Text.class);
				job.setOutputValueClass(Text.class);
				// FileInputFormat.setInputDirRecursive(job, true);
				FileInputFormat.setInputPaths(job, new Path(modelname
						+ "/output" + (i - 1)));
				FileOutputFormat.setOutputPath(job, new Path(modelname
						+ "/output" + i + "/"));
				 job.waitForCompletion(true);
				 validmodel = hassize(conf,modelname
							+ "/output" + i + "/" );

				System.out.println("Is there data to keep on generating permutations?"+validmodel);
			}
		}
	}

	
	public static boolean hassize(Configuration conf, String path) {

		try {
			FileSystem fs = FileSystem.get(conf);
			FileStatus[] status = fs.listStatus(new Path(path));
			for (int j = 0; j < status.length; j++) {
				if (status[j].getLen() > 0) {
					return true;
				}
			}
		} catch (IllegalArgumentException | IOException e) {
			throw new IllegalStateException();
		}
		return false;
	}
}

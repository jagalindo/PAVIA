package inria.reasoning.products;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;

public class Starter {

    public static void main(String[] args) throws Exception {
        BufferedWriter output = new BufferedWriter(new FileWriter("file1.txt"));
    	XMLReader reader = new XMLReader();
    	FAMAFeatureModel parseFile = (FAMAFeatureModel) reader.parseFile("inputs/fm-source/aircraft_fm.xml");
    	Collection<Feature> features = parseFile.getFeatures();
    	for(Feature f: features){
    		output.write(f+" ");
    	}
    	output.close();
    	
        JobConf conf = new JobConf(Starter.class);
        conf.setJobName("permutation");

        conf.setOutputKeyClass(IntWritable.class);
        conf.setOutputValueClass(Text.class);

        conf.setMapperClass(ProductsMap.class);
   //   conf.setReducerClass(ProductsReduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path("inputs/combinations/aircraft_fm.csv"));
        FileOutputFormat.setOutputPath(conf, new Path("output"));

        JobClient.runJob(conf);
    }
}

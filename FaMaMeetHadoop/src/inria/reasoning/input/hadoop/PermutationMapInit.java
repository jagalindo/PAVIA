package inria.reasoning.input.hadoop;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import es.us.isa.FAMA.Reasoner.FeatureModelReasoner;
import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.FAMAfeatureModel.transformations.FeatureModelTransform;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import es.us.isa.Sat4jReasoner.Sat4jReasoner;
import es.us.isa.Sat4jReasoner.questions.Sat4jValidConfigurationQuestion;
import es.us.isa.Sat4jReasoner.questions.Sat4jValidProductQuestion;


public class PermutationMapInit extends 
		Mapper<LongWritable, Text, Text, Text> {
	Log log = LogFactory.getLog(PermutationMapInit.class);
	
	String[] array;
	String modelname;
	String path;
	
	Text key= new Text();
	Text value= new Text();
	
	Sat4jReasoner reasoner= new Sat4jReasoner();
	FeatureModelTransform transform = new FeatureModelTransform();
	
	@Override
	protected void setup(
			org.apache.hadoop.mapreduce.Mapper<LongWritable, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {

		Configuration conf = context.getConfiguration();
		//array = conf.getStrings("features");
		
		modelname = conf.get("modelname").trim();
		path=conf.get("path");

		
		try {
			XMLReader reader = new XMLReader();
			FileSystem fs = FileSystem.get(context.getConfiguration());
			FSDataInputStream open = fs.open(new Path(path));
			
			FAMAFeatureModel fm = (FAMAFeatureModel) reader.parseStream(open);
			if(fm==null){
				throw new IllegalStateException("El modelo no se ha cargado bien");
			}
			array= new String[fm.getFeatures().size()];
			transform.transform(fm, reasoner);
			array= new String[fm.getFeatures().size()];
			int i=0;
			for(Feature f: fm.getFeatures()){
	        	array[i]=f.getName().trim();
	        	i++;
	    	}
			conf.set("numberOfFeatures", fm.getFeatures().size()+"");
		} catch (WrongFormatException e) {
			e.printStackTrace();
		}
		super.setup(context);
	}
	
	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		
		int i=1;
	    for(String f: array){
	    	if (isValidConfiguration(reasoner, f)) {//this will prevent the combination of dead features
		    	this.key.set(i+"");
		    	this.value.set(f);
				context.write(this.key,this.value);
				//if (isValidProduct(reasoner, f)) {
				//	context.write(new Text("VP"), new Text(f));
				//}
			} 
	    	i++;
		} 
    	
    	
	}
	
	boolean isValidProduct(FeatureModelReasoner reasoner, String productString) {

		Sat4jValidProductQuestion valid = new Sat4jValidProductQuestion();
		valid.setProduct(createProduct(productString));
		reasoner.ask(valid);
		return valid.isValid();

	}

	boolean isValidConfiguration(FeatureModelReasoner reasoner,
			String prodString) {

		Sat4jValidConfigurationQuestion valid = new Sat4jValidConfigurationQuestion();
		valid.setConfiguration((createConfguration(prodString)));
		reasoner.ask(valid);
		return valid.isValid();

	}
	
	private es.us.isa.FAMA.stagedConfigManager.Configuration createConfguration(
			String prodString) {
		es.us.isa.FAMA.stagedConfigManager.Configuration conf = new es.us.isa.FAMA.stagedConfigManager.Configuration();
		String[] oldRowData = prodString.split("\\s");
		for (String s : oldRowData) {
			conf.addElement(new Feature(s.trim()), 1);
		}
		return conf;
	}

	private Product createProduct(String string) {
		Product p = new Product();
		String[] oldRowData = string.split("\\s");
		for (String s : oldRowData) {
			p.addFeature(new Feature(s.trim()));
		}
		return p;
	}

}

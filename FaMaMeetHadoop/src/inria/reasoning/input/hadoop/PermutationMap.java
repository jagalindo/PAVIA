package inria.reasoning.input.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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

public class PermutationMap extends Mapper<Text, Text, Text, Text> {
	Log log = LogFactory.getLog(PermutationMap.class);

	String[] array;
	String modelname;
	String path;
	FAMAFeatureModel fm;
	ArrayList<Integer> splitList =null;
	String[] split=null;
	Text key = new Text();
	Text value = new Text();
	Sat4jReasoner reasoner= new Sat4jReasoner();
	FeatureModelTransform transform = new FeatureModelTransform();

	@Override
	protected void setup(
			org.apache.hadoop.mapreduce.Mapper<Text, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {

		Configuration conf = context.getConfiguration();
		//array = conf.getStrings("features");
		
		modelname = conf.get("modelname").trim();
		path=conf.get("path");
		try {
			XMLReader reader = new XMLReader();
			FileSystem fs = FileSystem.get(context.getConfiguration());
			FSDataInputStream open = fs.open(new Path(path));
			
			fm = (FAMAFeatureModel) reader.parseStream(open);
			
			
			if(fm==null){
				throw new IllegalStateException("El modelo no se ha cargado bien");
			}
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
	public void map(Text key, Text value, Context context)
			throws IOException, InterruptedException {

		// check if its a valid product from previous pass
		if (!key.toString().trim().equals("VP")) {
			if(key.toString().trim().contains("-")){
				split= key.toString().trim().split("-");}
			else{
				split= new String[1];
				split[0]=key.toString().trim();
			}
			splitList=new ArrayList<Integer>();
			for (String s : split) {
				int parseInt= 0;
				try{
					parseInt=Integer.parseInt(s);
				}catch(Exception e){
					throw new IllegalStateException(key.toString()+"---"+value.toString());
				}
				
				if (splitList.contains(parseInt)) {
					throw new IllegalStateException("Duplicated elements");
				}
				splitList.add(parseInt);
			}

			//keep on going with the filtering
			
			
			
			String[] rowLine = value.toString().split("\\s");
			List<String> list = Arrays.asList(rowLine);
			
			for (int i = splitList.get(splitList.size() - 1); i < array.length; i++) {
				String string = getString(list);
				if (!string.contains(array[i])) {

					String configuration = string + array[i];
					
					if (isValidConfiguration(reasoner, configuration)) {
						this.key.set(generatekey(splitList, (i + 1)));
						this.value.set(configuration);
						context.write(this.key,this.value);
//						if (isValidProduct(reasoner, configuration)) {
//							context.write(new Text("VP"), new Text(configuration));
//						}
					} 
				}
			}
		}
	}

	private Text generatekey(List<Integer> oldkeys, int newKey) {

		String res = "";
		Collections.sort(oldkeys);
		for (Integer i : oldkeys) {
			res += i + "-";
		}
		res += newKey;
		return new Text(res);
	}

	private String getString(List<String> list) {

		String res = "";
		boolean first = true;
		for (String s : list) {
			if (!first) {
				res += s.trim() + " ";
			} else {
				first = false;
			}
		}

		return res;
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

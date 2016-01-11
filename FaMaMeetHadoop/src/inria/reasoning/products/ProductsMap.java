package inria.reasoning.products;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;
import es.us.isa.Sat4jReasoner.Sat4jReasoner;
import es.us.isa.Sat4jReasoner.questions.Sat4jValidProductQuestion;

public class ProductsMap extends MapReduceBase implements
		Mapper<LongWritable, Text, IntWritable, Text> 
	{
	Log log = LogFactory.getLog(ProductsMap.class);
	
	private final static IntWritable one = new IntWritable(1);
	//serial consecutive blablabla..
	private Text word = new Text();

	public void map(LongWritable key, Text value,
			OutputCollector<IntWritable, Text> output, Reporter reporter)
			throws IOException {
		this.word = value;

		XMLReader reader = new XMLReader();
		try {
			FAMAFeatureModel parseFile = (FAMAFeatureModel) reader.parseFile("./inputs/fm-source/aircraft_fm.xml");
			Sat4jReasoner reasoner = new Sat4jReasoner();
			parseFile.transformTo(reasoner);
			Sat4jValidProductQuestion valid = new Sat4jValidProductQuestion();
			valid.setProduct(createProduct(value.toString()));
			reasoner.ask(valid);
			if(valid.isValid()){
				output.collect(one, word);
			}
		} catch (WrongFormatException e) {
			e.printStackTrace();
		}
	}

	private Product createProduct(String string) {
		Product p = new Product();
        String[] oldRowData = string.split(";");
        for(String s:oldRowData){
        	p.addFeature(new Feature(s.replace(";", "")));
        }
		return p;
	}

}

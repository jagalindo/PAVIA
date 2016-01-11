package inria.reasoning.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import es.us.isa.FAMA.models.FAMAfeatureModel.FAMAFeatureModel;
import es.us.isa.FAMA.models.FAMAfeatureModel.Feature;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLReader;
import es.us.isa.FAMA.models.variabilityModel.parsers.WrongFormatException;

public class Generator {

	
	
	public static void generate(String origPath,String destPath) throws WrongFormatException, IOException{
		 XMLReader reader = new XMLReader();
	      FAMAFeatureModel in = (FAMAFeatureModel) reader.parseFile(origPath);
	      Collection<Feature> features = in.getFeatures();
	      
          BufferedWriter output = new BufferedWriter(new FileWriter(destPath));
	      
	      
	      OrderedPowerSet<Feature> powerset= new  OrderedPowerSet<>((List<Feature>)features);
	      //This for generaets all the possible permutations
	      for(int i=1;i<features.size()+1;i++){
	    	  System.out.println(i);
		      List<LinkedHashSet<Feature>> permutationsList = powerset.getPermutationsList(i);

		      for(LinkedHashSet<Feature> product:permutationsList){
		    	  for(Feature f:product){
		    		  output.write(f.getName()+";");
		    	  }
		    	  output.write("\r\n");
		      }
	      }
          output.close();

	}
	
	public static void main(String[] args) throws IOException, WrongFormatException {
        BufferedWriter output = new BufferedWriter(new FileWriter("inputs/time.csv"));

		File f= new File("./inputs/fm-source/");
		File[] listFiles = f.listFiles();
		for(File path:listFiles){
			long start = System.currentTimeMillis();
			generate(path.getAbsolutePath(),path.getAbsolutePath().replace(".xml", ".csv").replace("fm-source", "combinations"));
			long time=System.currentTimeMillis()-start;
			output.write(path+";"+time);
		}
		output.close();
	}

}

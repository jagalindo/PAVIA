package inria.reasoning.input.chainmapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;


public class PermutationMap extends Mapper<Text, Text, Text, Text> {
	Log log = LogFactory.getLog(PermutationMap.class);

	Text key, value;
	String cnfmodel;
	String[] features, cnfvalues, configurationKeys;
	ISolver solver;
	Reader reader;
	int[] array;
	IVecInt assump;

	@Override
	protected void setup(
			org.apache.hadoop.mapreduce.Mapper<Text, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {

		Configuration conf = context.getConfiguration();
	
		key = new Text();
		value = new Text();
		cnfmodel = conf.get("cnf");
		features = conf.getStrings("features");
		cnfvalues = conf.getStrings("cnfvalues");
		
		solver = SolverFactory.newDefault();
		//solver.setKeepSolverHot(true);
		reader = new DimacsReader(solver);
		
		try {
			reader.parseInstance(new ByteArrayInputStream((cnfmodel).getBytes(StandardCharsets.UTF_8)));
		} catch (ParseFormatException | ContradictionException e) {
			e.printStackTrace();
		}
		try {
			solver.isSatisfiable();
		} catch (TimeoutException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		super.setup(context);
	}

	@Override
	public void map(Text key, Text value, Context context) throws IOException,	InterruptedException {
	//	boolean res=false;

		//Split hold an array with the keys of each configuration
		if (key.toString().trim().contains("-")) {
			configurationKeys = key.toString().trim().split("-");
		} else {
			configurationKeys = new String[1];
			configurationKeys[0] = key.toString().trim();
		}
		
		parseInt(configurationKeys);
		assump=new VecInt(array);
		
	
		
		//get the last int in keys to start over
		for (int i=array[configurationKeys.length-1]+1; i < features.length; i++) {
				if(!contains(array, i)){
					if (isValidConfiguration(i)) {
						this.key.set(key.toString()+"-"+(i));
						this.value.set(value.toString()+" "+features[i]);
						context.write(this.key, this.value);
					} 
				}
		}

	}

	
	class myRun implements Runnable  {
		Integer i=null;
		boolean exec=false;
		public myRun(int i){
			this.i=i;
		}
		public void run() {
			
			if(!contains(array, i)){
				if (isValidConfiguration(i)) {
					exec= true;
				}
			}
		}
	}
	private boolean contains(int[] array2, int i) {
			for(int a:array){
				if(a==i){return true;}
			}
		return false;
	}

	private void parseInt(String[] configurationKeys2) {
		array = new int[configurationKeys2.length];
		for(int i=0;i<configurationKeys2.length;i++){
			array[i]= Integer.parseInt(configurationKeys2[i]);
		}

	}

	public static <T> boolean contains(final T[] array, final T v) {
	    if (v == null) {
	        for (final T e : array)
	            if (e == null)
	                return true;
	    } else {
	        for (final T e : array)
	            if (e == v || v.equals(e))
	                return true;
	    }

	    return false;
	}
	
	private boolean isValidProduct(int configuration){
		boolean res=false;
		
		Collection<Integer> mycol = new LinkedList<Integer>();
		mycol.add(configuration);
		
		for(int nottoinclude:){
			
		}
		
		
		
		// check the validity
		try {
			assump.push(configuration);
			res= solver.isSatisfiable(assump);			
			assump.remove(configuration);

			return res;
		} catch (IllegalArgumentException|TimeoutException e) {
			assump.remove(configuration);
			throw new IllegalStateException(cnfmodel+"\n"+assump.toString()+" size:"+assump.size());
		} 
	}
	private boolean isValidConfiguration(int configuration) {
		
		boolean res=false;
		
			
		// check the validity
		try {
			assump.push(configuration);
			res= solver.isSatisfiable(assump);			
			assump.remove(configuration);

			return res;
		} catch (IllegalArgumentException|TimeoutException e) {
			assump.remove(configuration);
			throw new IllegalStateException(cnfmodel+"\n"+assump.toString()+" size:"+assump.size());
		} 

	}
	
}

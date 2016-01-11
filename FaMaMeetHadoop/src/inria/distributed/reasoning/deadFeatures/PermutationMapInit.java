package inria.distributed.reasoning.deadFeatures;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

public class PermutationMapInit extends Mapper<Text, Text, Text, Text> {
	Log log = LogFactory.getLog(PermutationMapInit.class);

	Text key, value;
	String cnfmodel;
	String[] features, cnfvalues;
	ISolver solver;
	Reader reader;
	int numclauses;

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
		numclauses = Integer.parseInt(conf.get("num"));

		solver = SolverFactory.newDefault();
		// solver.setKeepSolverHot(true);
		reader = new DimacsReader(solver);
		
		try {
			reader.parseInstance(new ByteArrayInputStream((cnfmodel).getBytes(StandardCharsets.UTF_8)));
		} catch (ParseFormatException | ContradictionException e) {
			e.printStackTrace();
		}

		super.setup(context);
	}

	@Override
	public void map(Text key, Text value, Context context) throws IOException,
			InterruptedException {

		for (int i = 0; i < features.length; i++) {
			this.key.set((i + 1) + "");
			this.value.set(features[i]);
			context.write(this.key, this.value);
		}

	}

}

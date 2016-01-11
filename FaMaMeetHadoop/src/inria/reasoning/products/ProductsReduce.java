package inria.reasoning.products;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class ProductsReduce extends MapReduceBase implements
		Reducer<IntWritable, Text, NullWritable, Text> {
	public void reduce(IntWritable key, Iterator<Text> values,
			OutputCollector<NullWritable, Text> output, Reporter reporter)
			throws IOException {

		while (values.hasNext() ) {
			Text next = values.next();
			output.collect(NullWritable.get(), next);
		}

	}
}

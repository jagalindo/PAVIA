package inria.reasoning.valid;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Created by malawito on 12/5/14.
 */
public class ValidDriver extends Configured implements Tool {
    public int run(String[] args) throws Exception
    {

        if(args.length !=2) {
            System.err.println("Usage: temperatureExample.MaxTemperatureDriver <input path> <outputpath>");
            System.exit(-1);
        }

        Job job = new Job();
        job.setJarByClass(ValidDriver.class);
        job.setJobName("Max Temperature");

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(ValidMapper.class);
        job.setReducerClass(ValidReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        System.exit(job.waitForCompletion(true) ? 0:1);
        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        ValidDriver driver = new ValidDriver();
        int exitCode = ToolRunner.run(driver, args);
        System.exit(exitCode);
    }
}
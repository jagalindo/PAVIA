package inria.reasoning.input.hadoop;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.jobcontrol.Job;
import org.apache.hadoop.mapred.jobcontrol.JobControl;

public class Permutation {

	    public static void main(String[] args) throws Exception {

	        int start = 1;
	        int cycles = 1;

	        JobControl control = new JobControl("Permutation");

	        ArrayList<Job> dependingJobs = null;
	        Job lastJob = null;

	        for (int i = 1; i <= cycles; i++) {

	            dependingJobs = new ArrayList<Job>();
	            if (lastJob != null) {
	                dependingJobs.add(lastJob);
	            }

	            Configuration defaults = new Configuration();
	            JobConf conf = new JobConf(defaults, Permutation.class);

	            Path input = new Path("job" + (start+i-1) + "/part-00000");
	            Path output = new Path("job" + (start+i));

	            FileInputFormat.setInputPaths(conf, input);
	            FileOutputFormat.setOutputPath(conf, output);

	            conf.setOutputKeyClass(Text.class);
	            conf.setOutputValueClass(Text.class);

	            //conf.setCombinerClass(PermutationReduce.class);

	            conf.setInputFormat(TextInputFormat.class);
	            conf.setOutputFormat(TextOutputFormat.class);

	            conf.setNumMapTasks((start+i+1)*4);
	            conf.setNumReduceTasks(start+i+1);

	            Job actualJob = new Job(conf, dependingJobs);
	            actualJob.setJobName("PermutationGen" + (start+i));

	            control.addJob(actualJob);

	            lastJob = actualJob;
	        }


	        Thread controller = new Thread(control);
	        controller.start();

	        while (!control.allFinished()) {
	            try {
	                /*
	                System.out.println("Jobs in waiting state: " + control.getWaitingJobs().size());
	                System.out.println("Jobs in ready state: " + control.getReadyJobs().size());
	                System.out.println("Jobs in running state: " + control.getRunningJobs().size());
	                System.out.println("Jobs in success state: " + control.getSuccessfulJobs().size());
	                System.out.println("Jobs in failed state: " + control.getFailedJobs().size());
	                System.out.println("\n");
	                 
	                Thread.sleep(2000);
	                 * */
	            } catch (Exception e) {
	            }
	        }
	        control.stop();
	        System.out.println("done.");
	    }
}

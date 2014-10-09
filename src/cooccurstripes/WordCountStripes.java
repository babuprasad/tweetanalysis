package cooccurstripes;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.MapWritable;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordCountStripes {

	private static final transient Logger LOG = LoggerFactory.getLogger(WordCountStripes.class);

	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration();		

		LOG.info("HDFS Root Path: {}", conf.get("fs.defaultFS"));
		LOG.info("MR Framework: {}", conf.get("mapreduce.framework.name")); 
		
		/* Set the Input/Output Paths on HDFS */
		if(args.length < 2)
		{
			LOG.error("No input/output path specified. Format $ hadoop jar <jar_name> <input_path> <output_path>");
			System.exit(0);
		}
		String inputPath = args[0];
		String outputPath = args[1];
		LOG.info("Input : " + inputPath);
		LOG.info("Output : " + outputPath);
		
		/* FileOutputFormat wants to create the output directory itself.
		 * If it exists, delete it:
		 */
		deleteFolder(conf,outputPath);	
		
		Job job = Job.getInstance(conf);
		job.setJarByClass(WordCountStripes.class);
		job.setMapperClass(TokenizerMapperStripes.class);
		//job.setCombinerClass(MapSumReducer.class);
		job.setReducerClass(MapSumReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(MapWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
	
	/**
	 * Delete a folder on the HDFS. This is an example of how to interact
	 * with the HDFS using the Java API. You can also interact with it
	 * on the command line, using: hdfs dfs -rm -r /path/to/delete
	 * 
	 * @param conf a Hadoop Configuration object
	 * @param folderPath folder to delete
	 * @throws IOException
	 */
	private static void deleteFolder(Configuration conf, String folderPath ) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(folderPath);
		if(fs.exists(path)) {
			LOG.info("Output path exists. Deleting it...");
			fs.delete(path,true);
		}
	}
}
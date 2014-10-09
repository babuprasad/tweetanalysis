package graph;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortestPath {

	private static final transient Logger LOG = LoggerFactory.getLogger(ShortestPath.class);
	private static String inputPath = "";
	private static String outputPath = "";
	private static String tempPath = "";
	public static boolean isShortestPathFound = false;
	
	public static enum DISTANCE{
		MIN (0),
		PREVMIN (0);

		@SuppressWarnings("unused")
		private int value;

		DISTANCE(int val) {
			this.value = val; 
		}
	};

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
		inputPath = args[0];
		outputPath = args[1];
		if(args.length == 3)
			tempPath = args[2];
		else
			tempPath = "/temp";
		
		LOG.info("Input : " + inputPath);
		LOG.info("Output : " + outputPath);
		int i = 0;
		long prevmin = -1;
		/* FileOutputFormat wants to create the output directory itself.
		 * If it exists, delete it:
		 */
		while(!isShortestPathFound)
		{
			conf = new Configuration();
			System.out.println(" Deleting output directory... " + tempPath);
			deleteFolder(conf,tempPath);
			Job job = Job.getInstance(conf);
			
			System.out.println("*****************");	
			System.out.println("finding minimum distance...");
			System.out.println("Iteration : " + i);
			System.out.println(" ");
			job.setJarByClass(ShortestPath.class);
			job.setMapperClass(PathMapper.class);
			//job.setCombinerClass(ShortestDistanceReducer.class);
			job.setReducerClass(ShortestDistanceReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(Text.class);			
			FileInputFormat.addInputPath(job, new Path(inputPath));
			//FileOutputFormat.setOutputPath(job, new Path(inputPath));
			FileOutputFormat.setOutputPath(job, new Path(tempPath));			
			
			job.waitForCompletion(true);			
			
			Counters counters = job.getCounters();					
			long min = counters.findCounter(DISTANCE.MIN).getValue();
			System.out.println("Previous Min Value " + prevmin);
			System.out.println("Current Min Value " + min);
			if(prevmin == min)
			{			
				System.out.println("Shortest path found and the minimum distance  " + i + " is  :" +counters.findCounter(DISTANCE.MIN).getValue());
				deleteFolder(conf, outputPath);
				FileOutputFormat.setOutputPath(job, new Path(outputPath));				
				isShortestPathFound = true;				
				System.exit(job.waitForCompletion(true) ? 0 : 1);
			}			
			else
			{				
				prevmin = min;
				System.out.println("Minimum distance in iteration  " + i + " is  :" +counters.findCounter(DISTANCE.MIN).getValue());
				//FileOutputFormat.setOutputPath(job, new Path(inputPath));
				System.out.println("Before Swap inputPath : " + inputPath + " outputPath " + tempPath );
				String temp = inputPath;
				inputPath = tempPath;
				tempPath = temp;
				counters.findCounter(DISTANCE.MIN).increment(-min);		
				System.out.println("After Swap inputPath : " + inputPath + " outputPath " + tempPath );				
			}
			i++;
				
		}
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
	public static void deleteFolder(Configuration conf, String folderPath ) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(folderPath);
		if(fs.exists(path)) {
			fs.delete(path,true);
		}
	}
	
	
	public static void createFolder(Configuration conf, String folderPath ) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(folderPath);		
		if(!fs.exists(path)) {
			fs.create(path,true);
		}
	}
}
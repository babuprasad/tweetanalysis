package kmeans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMeansCluster {

	public static enum COUNTER {
		LOW(1), MEDIUM(2), HIGH(3);

		@SuppressWarnings("unused")
		private int value;

		COUNTER(int val) {
			this.value = val;
		}
	};


	private static final transient Logger LOG = LoggerFactory
			.getLogger(KMeansCluster.class); 

	public static void main(String[] args) throws Exception {
		
		int count = 0;
		String inputPath ="/user/naren/kmeans";
		String outputPath = "/user/naren/kmeansoutput";
		
		Configuration conf1 = new Configuration();
		deleteFolder(conf1, outputPath);
		writeFile("657", inputPath + "/low.txt",conf1);
		writeFile("2386", inputPath + "/medium.txt",conf1);
		writeFile("4115", inputPath + "/high.txt",conf1);
		boolean isConverged = true;
		while (isConverged) {
			count++;
		
			Configuration conf = new Configuration();

	
			LOG.info("HDFS Root Path: {}", conf.get("fs.defaultFS"));
			LOG.info("MR Framework: {}", conf.get("mapreduce.framework.name"));
			/* Set the Input/Output Paths on HDFS */


			Job job = Job.getInstance(conf);

			job.setJarByClass(KMeansCluster.class);
			job.setMapperClass(TokenizerMapper.class);
			job.setCombinerClass(IntSumReducer.class);
			job.setReducerClass(IntSumReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);
			FileInputFormat.addInputPath(job, new Path(inputPath));
			FileOutputFormat.setOutputPath(job, new Path(outputPath));

			
			job.waitForCompletion(true);
			

			LOG.info("*******","About to read: line 74");
			String low = readFile(inputPath + "/low.txt",conf);
			String medium = readFile(inputPath + "/medium.txt",conf);
			String high = readFile(inputPath + "/high.txt",conf);

			String currentLow = readFile(inputPath + "/currentLow.txt",conf);
			String currentMedium = readFile(inputPath + "/currentMedium.txt",conf);
			String currentHigh = readFile(inputPath + "/currentHigh.txt",conf);
			
			System.out.println("*******Prev Centroids - Low " + low);
			System.out.println("*******Prev Centroids - Medium " + medium);
			System.out.println("*******Prev Centroids - High " + high);
			
			System.out.println("*******Current Centroids - Low " + currentLow);
			System.out.println("*******Current Centroids - Medium " + currentMedium);
			System.out.println("*******Current Centroids - High " + currentHigh);

			if (low.equals(currentLow) && medium.equals(currentMedium)
					&& high.equals(currentHigh)) {
				isConverged = false;
				System.exit(job.waitForCompletion(true) ? 0 : 1);
			} else {
				System.out.println("*******Not merged, writing, line 96. Path: " + inputPath);
				writeFile(currentLow, inputPath + "/low.txt",conf);
				writeFile(currentMedium, inputPath + "/medium.txt",conf);
				writeFile(currentHigh, inputPath + "/high.txt",conf);
				deleteFolder(conf, outputPath);

			}
			System.out.println("***Counter: " + count);

		}

	}

	/**
	 * Delete a folder on the HDFS. This is an example of how to interact with
	 * the HDFS using the Java API. You can also interact with it on the command
	 * line, using: hdfs dfs -rm -r /path/to/delete
	 * 
	 * @param conf
	 *            a Hadoop Configuration object
	 * @param folderPath
	 *            folder to delete
	 * @throws IOException
	 */
	private static void deleteFolder(Configuration conf, String folderPath)
			throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(folderPath);
		if (fs.exists(path)) {
			fs.delete(path, true);
		}
	}

	public static void writeFile(String text, String path,Configuration conf) {
		try {
			Path pt = new Path(path);
			FileSystem fs = FileSystem.get(conf);
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
					fs.create(pt, true)));
			br.write(text);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readFile(String path,Configuration conf) {
		String line = "";
		try {
			Path pt = new Path(path);
			FileSystem fs = FileSystem.get(conf);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fs.open(pt)));

			line = br.readLine();

			return line;
		} catch (Exception e) {
			System.out.println("file not found/some other error");
			e.printStackTrace();
		}
		return line;
	}
}
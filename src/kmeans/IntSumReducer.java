package kmeans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import kmeans.KMeansCluster.COUNTER;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntSumReducer extends
		Reducer<Text, IntWritable, Text, IntWritable> {
	private IntWritable result = new IntWritable();
	private static final transient Logger LOG = LoggerFactory
			.getLogger(KMeansCluster.class);

	List<String> list = new ArrayList<String>();

	public void reduce(Text key, Iterable<IntWritable> values, Context context)
			throws IOException, InterruptedException {
		int sum = 0;
		int count = 0;
		String inputPath = "/user/naren/kmeans";
		String k = key.toString();
 
		for (IntWritable val : values) {
			list.add(k + " " + val.toString());
			sum += val.get();
			count++;
		}
		System.out.println("List: " + k + "::" + list);

		// System.out.println("List 2 element"+list.get(2));
		int average = sum / count;

		result.set(average);

		System.out.println("***Inside reducer, Line 38, input: " + inputPath
				+ ", Key: " + key + ", average: " + average);
		Configuration conf = context.getConfiguration();
		// conf.addResource(new Path("/"));

		if (k.equals("low")) {
			System.out.println("*****inside low*****");
			context.getCounter(COUNTER.LOW).increment(average);
			writeFile(String.valueOf(average),
					inputPath +"/currentLow.txt", conf);
		}

		if (k.equals("medium")) {

			System.out.println("*****inside medium*****");
			context.getCounter(COUNTER.MEDIUM).increment(average);
			writeFile(String.valueOf(average),
					inputPath +"/currentMedium.txt", conf);
		}
		if (k.equals("high")) {
			System.out.println("*****inside high*****");
			context.getCounter(COUNTER.HIGH).increment(average);
			writeFile(String.valueOf(average),
					inputPath +"/currentHigh.txt", conf);
		}
		LOG.info("*******", "Inside reducer, Line 55, input: " + inputPath);
		String currentLow = readFile(inputPath +"/currentLow.txt",
				context.getConfiguration());
		String currentMedium = readFile(inputPath +"/currentMedium.txt",
				context.getConfiguration());
		String currentHigh = readFile(inputPath +"/currentHigh.txt",
				context.getConfiguration());

		System.out.println("Low value from red: " + currentLow);
		System.out.println("Medium value from red: " + currentMedium);
		System.out.println("High value from red: " + currentHigh);

		for (int i = 0; i < list.size(); i++) {
			System.out.println("inside list print######");
		
			String[] l = list.get(i).split(" ");
			System.out.println(l[0] +","+l[1]);
			context.write(new Text(l[0]),
					new IntWritable(Integer.parseInt(l[1])));
		}

	}

	public void writeFile(String text, String path, Configuration conf) {
		try {
			System.out.println("*** inside writeFile of reducer, text: " + text
					+ ", path: " + path);
			Path pt = new Path(path);
			FileSystem fs = FileSystem.get(conf);
			BufferedWriter br = new BufferedWriter(new OutputStreamWriter(
					fs.create(pt, true)));

			br.write(text);
			br.close();
		} catch (Exception e) {
			System.out.println("inside reducer some error in write");
			e.printStackTrace();
		}
	}

	public String readFile(String path, Configuration conf) {
		String line = "";
		System.out.println("********* inside readFile of reducer. Line 78");
		try {
			Path pt = new Path(path);
			FileSystem fs = FileSystem.get(conf);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fs.open(pt)));

			line = br.readLine();

			return line;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return line;
	}
}

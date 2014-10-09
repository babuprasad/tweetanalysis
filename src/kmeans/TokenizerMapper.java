package kmeans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

	private Text word = new Text();

	public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException {

		String input = "/user/naren/kmeans";
		System.out.println("*******Inside mapper, Line 30, path: " + input);
		int low = Integer.parseInt(readFile(input + "/low.txt",
				context.getConfiguration()));
		int medium = Integer.parseInt(readFile(input + "/medium.txt",
				context.getConfiguration()));
		int high = Integer.parseInt(readFile(input + "/high.txt",
				context.getConfiguration())); 

		System.out.println("******** Current low, med and high: " + low + ", "
				+ medium + "," + high);
		StringTokenizer itr = new StringTokenizer(value.toString());
		while (itr.hasMoreTokens()) {
			word.set(itr.nextToken());
			int followersCount = Integer.parseInt(word.toString());

			
			String classifier = getClassifier(followersCount, low, medium, high);

			System.out.println("******Followers Count: "+followersCount + " Classifier: " + classifier);
			context.write(new Text(classifier), new IntWritable(followersCount));

		}
	}

	public static void writeFile(String text, String path, Configuration conf) {
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

	public static String readFile(String path, Configuration conf) {
		String line = "";
		System.out.println("****** Path: " + path);
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

	public static String getClassifier(int input, int low, int medium, int high) {

		String classifier = "";
		int l = Math.abs(low - input);
		int m = Math.abs(medium - input);
		int h = Math.abs(high - input);

		if (l < m && l < h)
			classifier = "low";
		else if (m < l && m < h)
			classifier = "medium";
		else
			classifier = "high";

		return classifier;

	}
}
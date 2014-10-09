package sample;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntSumReducer 
extends Reducer<Text,IntWritable,Text,IntWritable> {
	private IntWritable result = new IntWritable();
	private static final transient Logger LOG = LoggerFactory.getLogger(WordCount.class);
	
	public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
		int sum = 0;
		System.out.println("reduce -- sample wordcount");
		LOG.info("reduce -- sample wordcount ");
		for (IntWritable val : values) {
			sum += val.get(); 
		}
		result.set(sum);
		context.write(key, result);
	}
}

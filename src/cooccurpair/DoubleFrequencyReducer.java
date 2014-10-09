package cooccurpair;

import java.io.IOException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class DoubleFrequencyReducer extends Reducer<Text,DoubleWritable,Text,DoubleWritable> {
	
	private static DoubleWritable result = new DoubleWritable();
	private static MapWritable singleKeyMap = new MapWritable(); 
	
	public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {		
		try { 
			int sum = 0;
			for (DoubleWritable val : values) {
				
				sum += val.get();
			}
			result.set(sum);
			
			//Check whether we are single key, in that case add to local hashmap
			String[] keyPair = key.toString().trim().split(",");
			if(keyPair.length == 1)
			{				
				singleKeyMap.put(new Text(keyPair[0].trim()), new Text(String.valueOf(result.get())));			
			}
			
			// Calculate Relative frequency of A,B with respect to A
			else 
			{					
				Text singleKeyFreq =  (Text) singleKeyMap.get(new Text(keyPair[0].trim()));				
				if(singleKeyFreq != null)
				{					
					result.set(result.get()/Double.parseDouble(singleKeyFreq.toString()) );
				}			
				context.write(key, result);
			}
			
		} catch (Exception e) {
			System.out.println("Reducer  ERROR - " + e.getMessage());
			e.printStackTrace();
		}
	}
}

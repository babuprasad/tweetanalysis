package cooccurstripes;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

public class MapSumReducer extends Reducer<Text,MapWritable,Text,DoubleWritable> {
	private DoubleWritable result = new DoubleWritable();
	private MapWritable mapResult = new MapWritable();
	private int mapCount = 1;	

	public void reduce(Text key, Iterable<MapWritable> values, Context context) throws IOException, InterruptedException {
		
		//String keyString = ""; 
		System.out.println("key - " + key);
		System.out.println("Reduce Values  : " + values);
		for (MapWritable valueMap : values) {
			Set<Writable> keySet =  valueMap.keySet();			
			for (Writable keyPair : keySet)			
			{	Text tmpKeyPair = (Text) keyPair;
				Text keyPairText = null;
			
				if(key.toString().compareTo(tmpKeyPair.toString()) != 0)				
					keyPairText = new Text(key.toString() + "," + tmpKeyPair.toString());				
				else
					keyPairText = key;
				
				System.out.println("Key Map : " + keyPairText);
				if(!mapResult.containsKey(keyPairText))
				{	
					System.out.println("Key doesnt exist. so initiating 1 for keymap : " + keyPairText);
					mapCount = 1;
					mapResult.put(keyPairText, new Text(""+mapCount));
				}
				else
				{				
					Text mapCountText = (Text) mapResult.get(keyPairText);
					mapCount = Integer.parseInt(mapCountText.toString());
					mapCount += 1;					
					mapResult.put(keyPairText, new Text(""+mapCount));
					System.out.println("Key exist. so incremented map count for Keymap : " + keyPairText + " cnt : "+mapCount);
				}				
			}
		}
		
		// Calculating relative frequency by getting the pairs frequency and single key frequency from above mapResult HashMap		
		for (Entry<Writable, Writable> mapResultEntry : mapResult.entrySet()) 
		{			
			Text keyFromResult = (Text) mapResultEntry.getKey();
			Text valFromResult = (Text) mapResultEntry.getValue();
			
			if(keyFromResult.toString().compareTo(key.toString()) != 0)
			{
				Text singleKeyFrequency = (Text) mapResult.get(key);
				System.out.println("Single Key : " + key + " freq : " + singleKeyFrequency);
				System.out.println("Pair Key : " + keyFromResult + " freq : " + valFromResult);
				System.out.println("Relative frequency : " + Double.parseDouble(valFromResult.toString()) / Double.parseDouble(singleKeyFrequency.toString()));
				result.set(Double.parseDouble(valFromResult.toString()) / Double.parseDouble(singleKeyFrequency.toString()));				
				System.out.println("result pair - " + keyFromResult);
				context.write(keyFromResult, result);  
			}			
			
			// To sort based to ignore the order across pairs, eg A,B =B,A  
			/*if(key.toString().compareTo(mapResultEntry.getKey().toString()) < 0)
			{				
				result.set(((IntWritable) mapResultEntry.getValue()).get());
				cooccurKeyText.set(key.toString() + "-" +mapResultEntry.getKey().toString()); 
				context.write(cooccurKeyText, result);
			}
			else
			{
				result.set(((IntWritable) mapResultEntry.getValue()).get());
				cooccurKeyText.set(mapResultEntry.getKey().toString() + "-" +key.toString()); 
				context.write(cooccurKeyText, result);
			}*/				
		}		
	}
}

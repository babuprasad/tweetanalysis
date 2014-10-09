package cooccurstripes;

import java.io.IOException;
//import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map.Entry;

import java.util.StringTokenizer;
//import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;

import common.UtilityClass;

public class TokenizerMapperStripes extends Mapper<Object, Text, Text, MapWritable>{
	
	

	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		StringTokenizer itr = new StringTokenizer(value.toString());		
		String currentToken = "";
		ArrayList<String> tokenList = new ArrayList<String>(); 
		System.out.println("Value in Mapper : " + value.toString());
		while (itr.hasMoreTokens())  
		{				
			currentToken = itr.nextToken().trim();
			if(!UtilityClass.isStopWord(currentToken))			
				tokenList.add(UtilityClass.cleanText(currentToken));			
		}
		
		// Forming co-occurring token from all possible combinations
		// using 'Stripes approach' 
		int j = 0;
		String nextToken = "";		
		for (int i = 0; i< tokenList.size(); i++) 
		{		
			MapWritable cooccur = new MapWritable();
			
			
			Text word = new Text();
			j = 0;
			currentToken = tokenList.get(i);
			word.set(currentToken);	
			while(j != tokenList.size()) // But here we are allowing duplicate key in stripe as
										// we need to calculate the single key frequency in reducer
			{				
				nextToken = tokenList.get(j);										
				cooccur.put(new Text(nextToken), new Text(""));								
				j++;
			}	
			context.write(word, cooccur);			
			System.out.println("Word Context to set in HDFS - " + word);
			
			for (Entry<Writable, Writable> set : cooccur.entrySet()) {
				System.out.println("map " + set.getKey());
			}
			
						
		}
	}
}
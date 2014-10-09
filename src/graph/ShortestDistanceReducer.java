package graph;

import graph.ShortestPath.DISTANCE;																																																				

import java.io.IOException;


import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ShortestDistanceReducer 
extends Reducer<Text,Text,Text,Text> {
//	private IntWritable result = new IntWritable();
	//private static final transient Logger LOG = LoggerFactory.getLogger(ShortestPath.class);
	
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		try
		{
			int minDistance = 10000;
			String minDistanceNode = "";
			  
			String node = "";		
			String path = "";			
			
			System.out.println("reduce key " + key);
			for (Text val : values) 
			{
				System.out.println("reduce value: " + val.toString());
				String[] valArray = val.toString().split(",");				
				if(valArray[0].compareTo("node") == 0)
				{
					node = valArray[1];
					System.out.println("node " + node);
				}				
				
				
				else if(valArray[0].compareTo("distance") == 0)
				{				
					if(Integer.parseInt(valArray[1]) < minDistance)
					{
						minDistance = Integer.parseInt(valArray[1]);						
						minDistanceNode = valArray[2];
					}
					path = valArray[2];
					System.out.println("min distance " + minDistance + " " + path);
				}
				else
					System.out.println("ERROR - Invalid key type");
			}			
			
			String[] nodeArray = node.split(" ");
			System.out.println("incrementing counter .... ");
			context.getCounter(DISTANCE.MIN).increment(minDistance);			
			node = nodeArray[0] + " " + minDistance + " " + nodeArray[2] + " " + path + "-" + minDistanceNode;			
			System.out.println("writing reducer output -- " + node);			
			System.out.println("Output path before writing to disk - " + FileOutputFormat.getOutputPath(context).toString());
			context.write(new Text(""), new Text(node));
		}
		catch(Exception e)
		{
			System.out.println("ERROR - " + e.getMessage());
			e.printStackTrace();
		}
	}
}

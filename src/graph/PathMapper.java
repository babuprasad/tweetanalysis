package graph;

import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class PathMapper extends Mapper<Object, Text, Text, Text>{

	
	
	//private static final transient Logger LOG = LoggerFactory.getLogger(ShortestPath.class);

	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

		try { 
			System.out.println("Mapper  value - " + value);
			String[] graphNodeDetails = value.toString().trim().split(" ");		
			System.out.println("graph node length " + graphNodeDetails.length);
			String node = graphNodeDetails[0];
			int distance = Integer.parseInt(graphNodeDetails[1]);
			String path = "";
			if(graphNodeDetails.length == 4)
				path = graphNodeDetails[3] + "-";
			context.write(new Text(node), new Text("node," + value.toString().trim()));			
			String[] adjacencyList = graphNodeDetails[2].split(":");
			System.out.println("adjacency list length " + adjacencyList.length);
			for (String adjacentNode : adjacencyList) 
			{			
				context.write(new Text(adjacentNode), new Text("distance," + String.valueOf((distance + 1)) + "," + path + adjacentNode));			
			}
		} catch (NumberFormatException e) {
			System.out.println("ERROR - " + e.getMessage());
			e.printStackTrace();
		}		
	}
}
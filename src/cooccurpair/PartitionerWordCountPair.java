package cooccurpair;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class PartitionerWordCountPair extends Partitioner<Text, DoubleWritable>{

	@Override
	public int getPartition(Text key, DoubleWritable value, int numReduceTasks) {
		int partition = 0;		
		 
		if(key.toString().toLowerCase().compareTo("g") < 0 )
			partition = 0;
		else if(key.toString().toLowerCase().compareTo("m") < 0 && key.toString().toLowerCase().compareTo("g") > 0 )
			partition = 1;
		else if(key.toString().toLowerCase().compareTo("s") < 0 && key.toString().toLowerCase().compareTo("m") > 0  )
			partition = 2;
		else
			partition = 3;			
		return partition;
	}

}


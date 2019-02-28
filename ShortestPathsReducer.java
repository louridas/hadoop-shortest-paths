import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Counter;

import org.apache.log4j.Logger;

public class ShortestPathsReducer
    extends Reducer<Text, Text, Text, Text> {
    
    private Logger logger = Logger.getLogger(this.getClass());

    public void reduce(Text key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {

        Text result = new Text();
        long minDistance = Long.MAX_VALUE;
        String pathFromSource = null;
        String neighbours = null;
        Counter updated =
            context.getCounter(ShortestPaths.MoreIterations.numUpdated);
        long existingDistance = Long.MAX_VALUE;

        for (Text val : values) {
            logger.debug("Input: " + key + " " + val);
            String[] curNodeData = val.toString().split("\\s+");
            if (curNodeData.length == 2) {
                long distance = Long.parseLong(curNodeData[0]);
                if (distance < minDistance) {
                    minDistance = distance;
                    pathFromSource = curNodeData[1];
                }
            } else {
                existingDistance = Long.parseLong(curNodeData[1]);
                if (existingDistance < minDistance) {
                    minDistance = existingDistance;
                    pathFromSource = curNodeData[2];
                }
                neighbours = curNodeData[0];
            }
        }

        if (minDistance < existingDistance) {
            updated.increment(1);
        }
        
        result.set(String.join(" ", neighbours,
                Long.toString(minDistance),
                pathFromSource));
        logger.debug(key + " " + result);
        context.write(key, result);
    }
}

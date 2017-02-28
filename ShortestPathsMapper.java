import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;

import org.apache.log4j.Logger;


public class ShortestPathsMapper
    extends Mapper<Object, Text, Text, Text>{

    private Logger logger = Logger.getLogger(this.getClass());
    
    private static final String NULL_NODE = "null";

    @Override
    public void map(Object key, Text value, Context context)
        throws IOException, InterruptedException {
            
        Configuration conf = context.getConfiguration();
        String source = conf.get("source");
        String line = value.toString();
        String[] nodes = line.split("\\s+");
        if (nodes.length < 2) {
            return;
        }
        
        String from = nodes[0];
        String partPath = source;
        long distance = Long.MAX_VALUE;
        String previous = NULL_NODE;
        String zero = Long.toString(0);
        String inf = Long.toString(Long.MAX_VALUE);
        Text outKey = new Text(from);
        Text outValue = new Text();
        
        if (nodes.length == 2) {
            if (from.equals(source)) {
                distance = 0;
                outValue.set(String.join(" ", nodes[1], zero, from));
            } else {
                outValue.set(String.join(" ", nodes[1], inf, NULL_NODE));
            }
        } else {
            distance = Long.parseLong(nodes[2]);
            partPath = nodes[3];
            outValue.set(String.join(" ", nodes[1], nodes[2], nodes[3]));
        }
        logger.debug(outKey + " " + outValue);        
        context.write(outKey, outValue);
            
        for (String neighbour : nodes[1].split(",")) {
            String pathFromSource = source;
            if (neighbour.equals(NULL_NODE)) {
                return;
            }
            outKey.set(neighbour);
            long newDistance = distance;
            if (newDistance != Long.MAX_VALUE) {
                newDistance += 1;
                if (!partPath.equals(from)) {
                    pathFromSource = from + "," + partPath;
                }
            
                outValue.set(String.join(" ", Long.toString(newDistance),
                        pathFromSource));
                logger.debug(outKey + " " + outValue);
                context.write(outKey, outValue);
            }
        }
    }
}

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class AdjacencyListCreator {

    public static class EdgeMapper
        extends Mapper<Object, Text, Text, Text>{

        @Override
        public void map(Object key, Text value, Context context)
            throws IOException, InterruptedException {
            String line = value.toString();
            if (line.startsWith("#")) {
                return;
            }
            String[] nodes = line.split("\\s+");
            Text source = new Text(nodes[0]);
            Text dest = new Text(nodes[1]);
            context.write(source, dest);
        }
    }

    public static class AdjacencyListReducer
        extends Reducer<Text, Text, Text, Text> {

        @Override
        public void reduce(Text node,
            Iterable<Text> neighbours,
            Context context) throws IOException, InterruptedException {
            StringBuffer adjacencyList = new StringBuffer();
            Text result = new Text();

            for (Text neighbour : neighbours) {
                adjacencyList.append(neighbour);
                adjacencyList.append(",");
            }
            String resultString =
                adjacencyList.length() > 0 ?
                adjacencyList.substring(0, adjacencyList.length() - 1) :
                "";
            result.set(resultString);
            context.write(node, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "adjacency list creator");
        job.setJarByClass(AdjacencyListCreator.class);
        job.setMapperClass(EdgeMapper.class);
        job.setReducerClass(AdjacencyListReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

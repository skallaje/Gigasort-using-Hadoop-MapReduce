import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MapReduceGigaSort {

  public static class Map
       extends Mapper <Object, Text, Text, Text> {

    private Text word1 = new Text();
    private Text word2 = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
    String line = value.toString(); // converts the entire line to a string
        word1.set(line.substring(0,10)); // extracts first 10 characters to be used as key
    word2.set(line.substring(10,98)); // extracts remaining characters to be used as value
        context.write(word1, word2); // key value pairs to be used for reduce function
      }
  }


  public static class Reduce
       extends Reducer <Text,Text,Text,Text> {

    public void reduce(Text key, Iterable<Text> values,
                       Context context) throws IOException, InterruptedException {
    // Reduce functions internally does the shuffling and sorting of data. No explicit sorting code required.
    // Loop through every value array and write it to Context object

          for (Text val : values) {
        context.write(key, val);
          }
      
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    conf.set("mapred.textoutputformat.separator", " "); // defaults the separator to 2 spaces

    Job job = Job.getInstance(conf, "TeraSort");
    job.setJarByClass(MapReduceTeraSort.class);

    job.setMapperClass(Map.class); // calls the mapper class
    job.setCombinerClass(Reduce.class); // calls the combiner class for shuffling
    job.setReducerClass(Reduce.class); // calls the reducer class

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    FileInputFormat.addInputPath(job, new Path(args[0])); // sets input path
    FileOutputFormat.setOutputPath(job, new Path(args[1])); // sets output path
    job.waitForCompletion(true); // waits for the mapreduce job to be completed
  }
}
package it.unipi.hadoop;

import it.unipi.hadoop.PageRanking.NodeWritable;
import it.unipi.hadoop.PageRanking.PageRankMapper;
import it.unipi.hadoop.PageRanking.PageRankReducer;
import it.unipi.hadoop.Parse.ParseMapper;
import it.unipi.hadoop.Parse.ParseReducer;
import it.unipi.hadoop.Sort.DoubleComparator;
import it.unipi.hadoop.Sort.SortMapper;
import it.unipi.hadoop.Sort.SortReducer;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.TaskCounter;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class PageRank {

    private static long totalPages;
    private static final int N_REDUCERS = 3;

    public static void main(String[] args) throws Exception{

        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        if (otherArgs.length != 4) {
            System.err.println("Usage: PageRank <maxIteration> <damping factor> <input> <output>");
            System.exit(1);
        }

        // Get input args
        Integer maxIteration = Integer.parseInt(otherArgs[0]);
        Double alpha = Double.parseDouble(otherArgs[1]);
        String inputFile = otherArgs[2];
        String outputFile = otherArgs[3];
        System.out.println("args[0]: <maxIteration>=" + maxIteration);
        System.out.println("args[1]: <alpha>=" + alpha);
        System.out.println("args[2]: <input>=" + inputFile);
        System.out.println("args[3]: <output>=" + outputFile);

        String parseOutputPath = "parseOutput";

        //--------------------------- Parse Stage ---------------------------
        parseInput(inputFile, parseOutputPath);
        System.out.println("Parse stage completed.");

        //--------------------------- Rank Stages ---------------------------
        String path = "rankOutput";
        for(int i = 0; i<maxIteration; i++){
            pageRankCalculator((path + i), (path + (i+1)), alpha, i);
            System.out.println("Rank stage iteration " + i + " completed.");
        }

        //--------------------------- Sort Stage ----------------------------
        sort((path+maxIteration), outputFile);
        System.out.println("Sort stage completed.");

    }

    public static void parseInput(String input, String output) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "ParseInput");
        job.setJarByClass(PageRank.class);

        job.setMapperClass(ParseMapper.class);
        job.setReducerClass(ParseReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        if (!job.waitForCompletion(true)) throw new Exception("Exception: Job failed");

        // Get the total number of title page (we use this for N) getting the counter of input to the mapper.
        totalPages = job.getCounters().findCounter(TaskCounter.MAP_INPUT_RECORDS).getValue();

    }

    private static void pageRankCalculator(String input, String output, Double alpha, int iteration) throws Exception {
        Configuration conf = new Configuration();
        // Saving N and dampingFactor in the job configuration.
        conf.setLong("totalPages", totalPages);
        conf.setDouble("alpha", alpha);

        Job job = Job.getInstance(conf, "PageRank");
        job.setJarByClass(PageRank.class);

        job.setMapperClass(PageRankMapper.class);
        job.setReducerClass(PageRankReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NodeWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setNumReduceTasks(N_REDUCERS);

        if(iteration==0){
            FileInputFormat.addInputPath(job, new Path("parseOutput"+"/part-r-00000"));
            FileOutputFormat.setOutputPath(job, new Path(output));
        }else{
            FileInputFormat.setInputPaths(job,
                    new Path(input+"/part-r-00000"),
                    new Path(input+"/part-r-00001"),
                    new Path(input+"/part-r-00002"));
            FileOutputFormat.setOutputPath(job, new Path(output));
        }


        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        if (!job.waitForCompletion(true)) throw new Exception("Exception Job failed");
    }

    public static void sort(String input, String output) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "SortStage");
        job.setJarByClass(PageRank.class);

        job.setMapperClass(SortMapper.class);
        job.setReducerClass(SortReducer.class);

        job.setMapOutputKeyClass(DoubleWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(DoubleWritable.class);
        job.setOutputValueClass(Text.class);

        job.setSortComparatorClass(DoubleComparator.class);

        FileInputFormat.setInputPaths(job,
                new Path(input+"/part-r-00000"),
                new Path(input+"/part-r-00001"),
                new Path(input+"/part-r-00002"));
        FileOutputFormat.setOutputPath(job, new Path(output));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        if (!job.waitForCompletion(true)) throw new Exception("Exception: Job failed");
    }

}

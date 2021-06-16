package it.unipi.hadoop;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskCounter;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.w3c.dom.Node;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PageRank {

    private static long totalPages;


    public static void main(String[] args) throws Exception{

        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        if (otherArgs.length != 4) {
            System.err.println("Usage: PageRank <maxIteration> <input> <output>");
            System.exit(1);
        }

        Integer maxIteration = Integer.parseInt(otherArgs[0]);
        Double dampingFactor = Double.parseDouble(otherArgs[1]);
        String inputFile = otherArgs[2];
        String outputFile = otherArgs[3];
        System.out.println("args[0]: <maxIteration>=" + maxIteration);
        System.out.println("args[1]: <dampingFactor>=" + dampingFactor);
        System.out.println("args[2]: <input>=" + inputFile);
        System.out.println("args[3]: <output>=" + outputFile);


        String parseOutputPath = "src/main/resources/rankOutput0";
        parseInput(inputFile, parseOutputPath);
//         outputfile:   src/main/resources  +  "/part-r-00000"
        parseOutputPath += "/part-r-00000";
        outputFile += "/part-r-00000";
        String path = "src/main/resources/rankOutput";
        for(int i = 0; i<maxIteration; i++){
            pageRankCalculator((path + i + "/part-r-00000"), (path + (i+1)), dampingFactor);
        }

        sort((path+maxIteration+"/part-r-00000"), outputFile);

    }

    public static void parseInput(String input, String output) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "ParseInput");
        job.setJarByClass(PageRank.class);

        // set mapper/reducer
        job.setMapperClass(ParseMapper.class);
        job.setReducerClass(ParseReducer.class);

        // define mapper's output key-value
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // define reducer's output key-value
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // define I/O
        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        if (!job.waitForCompletion(true)) throw new Exception("Exception: Job failed");
        System.out.println("Fine Parse stage.");
        totalPages = job.getCounters().findCounter(TaskCounter.MAP_INPUT_RECORDS).getValue();
        System.out.println("global: " + totalPages);
    }

    private static void pageRankCalculator(String input, String output, Double dampingFactor) throws Exception {
        Configuration conf = new Configuration();
        conf.setLong("totalPages", totalPages);
        conf.setDouble("dampingFactor", dampingFactor);


        Job job = Job.getInstance(conf, "PageRank");
        job.setJarByClass(PageRank.class);

        // set mapper/reducer
        job.setMapperClass(PageRankMapper.class);
        job.setReducerClass(PageRankReducer.class);

        // define mapper's output key-value
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NodeWritable.class);

        // define reducer's output key-value
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // define I/O
        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        if (!job.waitForCompletion(true)) throw new Exception("Exception Job failed");
        System.out.println("Fine PageRank stage.");
    }

    public static void sort(String input, String output) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "SortStage");
        job.setJarByClass(PageRank.class);

        // set mapper/reducer
        job.setMapperClass(SortMapper.class);
        job.setReducerClass(SortReducer.class);

        // define mapper's output key-value
        job.setMapOutputKeyClass(DoubleWritable.class);
        job.setMapOutputValueClass(Text.class);

        // define reducer's output key-value
        job.setOutputKeyClass(DoubleWritable.class);
        job.setOutputValueClass(Text.class);

        job.setSortComparatorClass(DoubleComparator.class);

        // define I/O
        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        if (!job.waitForCompletion(true)) throw new Exception("Exception: Job failed");
        System.out.println("Fine sort stage.");
    }



}

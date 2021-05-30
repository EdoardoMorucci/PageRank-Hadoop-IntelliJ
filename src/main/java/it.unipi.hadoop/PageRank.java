package it.unipi.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
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


    public static void main(String[] args) throws Exception{

        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        if (otherArgs.length != 4) {
            System.err.println("Usage: PageRank <treshold> <maxIteration> <input> <output>");
            System.exit(1);
        }

        Double treshold = Double.parseDouble(otherArgs[0]);
        Integer maxIteration = Integer.parseInt(otherArgs[1]);
        String inputFile = otherArgs[2];
        String outputFile = otherArgs[3];
        System.out.println("args[0]: <treshold>=" + treshold);
        System.out.println("args[1]: <maxIteration>=" + maxIteration);
        System.out.println("args[2]: <input>=" + inputFile);
        System.out.println("args[3]: <output>=" + outputFile);

        pageRankCalculator(inputFile, outputFile);



//        Long GlobalNum = job.getCounters().findCounter(
//                TaskCounter.MAP_INPUT_RECORDS).getValue();
//        System.out.println("global " + GlobalNum);
    }

    private static void pageRankCalculator(String input, String output) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();

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

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}

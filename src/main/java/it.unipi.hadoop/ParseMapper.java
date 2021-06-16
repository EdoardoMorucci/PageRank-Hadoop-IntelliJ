package it.unipi.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParseMapper extends Mapper<LongWritable, Text, Text, Text> {
    private final Text reducerKey = new Text();
    private final Text reducerValue = new Text();
    Map<String, List<String>> combiner = new HashMap<String, List<String>>();

    @Override
    public void setup(Context context){
        System.out.println("Setup");
    }


    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String input = value.toString();
        // input:   <title>titlePage</title> ecc..  <text ...> [[outlink]] ecc.. [[outlink]] ... </text>
        String[] title = input.trim().split("</title>");
        title[0] = title[0].replaceAll("\t", "");
        title[0] = title[0].substring(7);
        title[0] = title[0].trim().split("\\|")[0];

        reducerKey.set(title[0]);
        String text = input.trim().split("<text")[1];
        String innerText = text.trim().split("</text>")[0];

        String[] substring = innerText.trim().split("\\[\\[");
        if(substring.length == 1){
            reducerValue.set("sinknode");
            context.write(reducerKey, reducerValue);
        }

        for(int j=1; j<substring.length; j++){
            String auxSubString = substring[j];
            // recive:    pagename]] other pagename]] ...
            String[] outLink = auxSubString.trim().split("\\]\\]");
            outLink[0] = outLink[0].trim().split("\\|")[0];
            reducerValue.set(outLink[0]);
            context.write(reducerKey, new Text(outLink[0]));
        }
    }
}

package it.unipi.hadoop.Sort;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class SortMapper extends Mapper<LongWritable, Text, DoubleWritable, Text> {

    private final DoubleWritable reducerKey = new DoubleWritable();
    private final Text reducerValue = new Text();


    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        // value: titlePage >> PR -> outlink1, outlink2
        String[] subString = value.toString().trim().split(">>");
        String pageRank = subString[1].trim().split("->")[0];

        reducerKey.set(Double.parseDouble(pageRank.trim()));
        reducerValue.set(subString[0].trim());
        context.write(reducerKey, reducerValue);
    }

}

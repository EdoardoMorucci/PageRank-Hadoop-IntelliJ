package it.unipi.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class ParseReducer extends Reducer<Text, Text, Text, Text> {

    private final Text outputValue = new Text();

    @Override
    public void setup(Context context){
        System.out.println("SetupRed");
    }

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        String output = ">> 0.0";
        for(Text str: values){
            String aux = str.toString();
            output += "-> " + aux;
        }

        outputValue.set(output);
        context.write(key, outputValue);
    }
}



package it.unipi.hadoop.Sort;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class SortReducer extends Reducer<DoubleWritable, Text, Text, Text> {

    private final Text outputValue = new Text();
    private final Text outputKey = new Text();

    public void reduce(DoubleWritable key, Iterable<Text> value, Context context) throws IOException, InterruptedException {
        // Key: pagerank
        // value: iterable of title page
        for(Text t: value){
            outputKey.set(t);
            outputValue.set(Double.toString(key.get()));
            context.write(outputKey, outputValue);
        }
    }


}

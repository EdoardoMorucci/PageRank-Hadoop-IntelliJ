package it.unipi.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class PageRankReducer extends Reducer<Text, NodeWritable, Text, Text> {

    private final Text outputValue = new Text();

    public void reduce(Text key, Iterable<NodeWritable> values, Context context) throws IOException, InterruptedException {
/*
            System.out.print(key.toString() + ": ");
            for(NodeWritable aux: values){
                System.out.print(aux.getPageRank() + " ");
                if(aux.getOutlinks() != null){
                    for(String str: aux.getOutlinks()){
                        System.out.print(str + " ");
                    }
                }
            }
            System.out.println("\n");
*/
        Double d = context.getConfiguration().getDouble("page.rank.N", 10);

        NodeWritable graphNode = null;
        Double sumPR = 0.0d;
        String out = "";
        for(NodeWritable aux: values){
            if(aux.getOutlinks() != null){
                // GraphStructure
                graphNode = aux;
            }else{
                // inlinks: link that point to this titlepage (key)
                sumPR += aux.getPageRank();
            }
        }

        if(sumPR == 0.0d){
            sumPR = graphNode.getPageRank();
        }
        out = ">> " + sumPR.toString();
        if(graphNode != null){

            for(String str: graphNode.getOutlinks()){
                out += "-> " + str;
            }
        }
        outputValue.set(out);
        context.write(key, outputValue);

    }

}

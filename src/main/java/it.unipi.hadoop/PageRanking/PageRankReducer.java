package it.unipi.hadoop.PageRanking;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class PageRankReducer extends Reducer<Text, NodeWritable, Text, Text> {

    private final Text outputValue = new Text();
    private Long totalPages;

    @Override
    public void setup(Context context){
        totalPages = context.getConfiguration().getLong("totalPages",0);
    }

    public void reduce(Text key, Iterable<NodeWritable> values, Context context) throws IOException, InterruptedException {
        // key: titlePage       iterable of NodeWritable:

        Double alpha = context.getConfiguration().getDouble("alpha",0);

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

        NodeWritable graphNode = null;
        Double sumPR = 0.0d;
        Double pageRank = 0.0d;
        String out = "";
        String graphStructure = "";

        for(NodeWritable aux: values){
            if(aux.getOutlinks() != null && aux.getOutlinks().size()>0){
                // I build the list of the outlinks for the GraphStructure
                for(String str: aux.getOutlinks()){
                    if(!str.equals("")){
                        graphStructure += "-> " + str;
                    }
                }
                //graphNode = aux;
            }else{
                // I sum all the other PR coming from the list except for the NodeWritable representing the graph structure
                // inlinks: link that point to this titlepage (key)
                sumPR += aux.getPageRank();
            }
        }

        pageRank = (alpha/totalPages)+((1-alpha)*sumPR);

        out = ">> " + pageRank.toString();
        out += graphStructure;

        // Node that isn't pointed from anyone
//        if(sumPR == 0.0d){
//            sumPR = graphNode.getPageRank();
//        }
        /*
        // Se ?? il graph structure
        if(graphNode != null){
            System.out.println(key);
            System.out.println(graphNode.getPageRank());
            System.out.println(graphNode.getOutlinks());

            for(String str: graphNode.getOutlinks()){
                out += "-> " + str;
            }
        }
        */

        outputValue.set(out);
        context.write(key, outputValue);

    }

}

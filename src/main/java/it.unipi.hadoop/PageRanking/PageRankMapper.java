package it.unipi.hadoop.PageRanking;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRankMapper extends Mapper<LongWritable, Text, Text, NodeWritable> {

    private final Text reducerKey = new Text();
    private  NodeWritable reducerValue = new NodeWritable();
    private Map<String, List<NodeWritable>> combiner;
    private long totalPages;

    @Override
    public void setup(Context context){
        combiner = new HashMap<String, List<NodeWritable>>();
        totalPages = context.getConfiguration().getLong("totalPages",0);
    }

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        // input:   titlePage   >> PR-> outlink1-> outlink2-> ...-> outlinkN
        String input = value.toString();
        // substring[0] = titlepage     substring[1] = PR-> outlink1-> outlink2-> ...-> outlinkN
        String[] subString = input.trim().split(">> ");
        String titlePage = subString[0].trim();

        // substring[1] = PR-> outlink1-> outlink2-> ...-> outlinkN
        // pageRankAndOutlinks[0]=pageRank     pageRankAndOutlinks[1]=outlink1     pageRankAndOutlinks[2]=outlink2
        String[] pageRankAndOutlinks = subString[1].trim().split("-> ");
        List<String> outlinks = new ArrayList<>();
        for (int i = 1; i < pageRankAndOutlinks.length; i++) {
            outlinks.add(pageRankAndOutlinks[i].trim());
        }

        // if this is a dangling node I can ignore it since its page rank will be calculated automatically. A dangling
        //node is a node without a page representation into the XML, I ignore it since it doesn't compose the graph
        if(outlinks.size()==0){
            return;
        }

        NodeWritable aux;
        if(Double.parseDouble(pageRankAndOutlinks[0])==0.0d){
            Double initialPageRank = (1/(double)totalPages);
            aux = new NodeWritable(initialPageRank, outlinks);
            pageRankAndOutlinks[0] = initialPageRank.toString();
        }else{
            aux = new NodeWritable(Double.parseDouble(pageRankAndOutlinks[0]), outlinks);
        }

        reducerKey.set(titlePage);
        reducerValue.set(aux);
        // Pass graph structure
        context.write(reducerKey, reducerValue);

        // if this is a sinkNode I only have to write it in the context in order to maintain the graph structure
        if(outlinks.get(0).trim().replaceAll("\\P{Print}","").equals("sinknode")){
            return;
        }

        // Add to combiner the list of outlinks for each title page
        Double pageRankFatherContribute = Double.parseDouble(pageRankAndOutlinks[0]) / (outlinks.size());

        for (String link : outlinks) {
            if (combiner.containsKey(link)) {
                aux = new NodeWritable(pageRankFatherContribute);
                combiner.get(link).add(aux);
            } else {
                List<NodeWritable> listNode = new ArrayList<>();
                aux = new NodeWritable(pageRankFatherContribute);
                listNode.add(aux);
                combiner.put(link, listNode);
            }
        }
    }

    //inMapper combiner to reduce the data to be transmitted
    @Override
    public void cleanup(Context context) throws IOException, InterruptedException {
        List<NodeWritable> aux;
        Double sumPR = 0.0d;
        for (String key : combiner.keySet()) {
            sumPR = 0.0d;
            aux = combiner.get(key);
            //if there is only an element into the list I can use that NodeWritable instance with the correct PR
            if(aux.size()==1){
                reducerValue.set(aux.get(0));
            }else{
                //in this case I have to sum the PR of all the instances
                for(NodeWritable node: aux){
                    sumPR += node.getPageRank();
                }
                reducerValue.set(new NodeWritable(sumPR));
            }
            reducerKey.set(key);
            context.write(reducerKey, reducerValue);
        }

        /*
        //code to debug the combiner
            List<NodeWritable> list = new ArrayList<>();
            File inputAdjacency = new File("src/main/resources/combiner.txt");
            FileWriter myWriter = null;
            try {
                myWriter = new FileWriter("src/main/resources/combiner.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String key : combiner.keySet()) {
                String str = key;
                str += ": ";
                list = combiner.get(key);
                for (NodeWritable nw : list) {
                    str += nw.toString();
                    str += " ";
                }

                try {
                    myWriter.write(str + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            myWriter.close();
        */
    }
}

package it.unipi.hadoop;

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
    private Map<String, List<NodeWritable>> combiner = new HashMap<String, List<NodeWritable>>();
    private long totalPages;

    @Override
    public void setup(Context context){
        totalPages = context.getConfiguration().getLong("totalPages",0);
        System.out.println("global num: " + totalPages);
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
//            System.out.print(pageRankAndOutlinks[i] + " ");
            outlinks.add(pageRankAndOutlinks[i].trim());
        }

        // Se dangling node lo ignoro tanto verrà tenuto di conto nel reducer
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


        // Va fatto se non abbiamo un sinknode, cioè se outlinks[0]!="sinknode"
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

    @Override
    public void cleanup(Context context) throws IOException, InterruptedException {
        List<NodeWritable> aux;
        Double sumPR = 0.0d;
        for (String key : combiner.keySet()) {
            aux = combiner.get(key);
            if(aux.size()==1){
                reducerValue.set(aux.get(0));
            }else{
                for(NodeWritable node: aux){
                    sumPR += node.getPageRank();
                }
                reducerValue.set(new NodeWritable(sumPR));
            }
            reducerKey.set(key);
            context.write(reducerKey, reducerValue);
        }

        /*
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

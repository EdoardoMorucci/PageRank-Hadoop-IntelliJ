package it.unipi.hadoop;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRankMapperr extends Mapper<LongWritable, Text, Text, NodeWritable> {

    private final Text reducerKey = new Text();
    private  NodeWritable reducerValue = new NodeWritable();
    Map<String, List<NodeWritable>> combiner = new HashMap<String, List<NodeWritable>>();

    @Override
    public void setup(Context context){
        double treshold = context.getConfiguration().getDouble("page.rank.treshold", 0);
        long globalNum = context.getConfiguration().getLong("GlobalNum",0);
        System.out.println("global num" + globalNum);
        System.out.println("treshold" + treshold);
    }

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        // input:   titlePage>> PR, outlink1, outlink2, ..., outlinkN
        String input = value.toString();
        // substring[0] = titlepage     substring[1] = PR, outlink1, outlink2, ..., outlinkN
        String[] subString = input.trim().split(">> ");
        String titlePage = subString[0];
        System.out.print(titlePage+ ":");
        // substring[1] = PR-> outlink1-> outlink2-> ...-> outlinkN
        // pageRankAndOutlinks[0]=pageRank     pageRankAndOutlinks[1]=outlink1     pageRankAndOutlinks[2]=outlink2
        String[] pageRankAndOutlinks = subString[1].trim().split("-> ");
        List<String> outlinks = new ArrayList<>();
        for (int i = 1; i < pageRankAndOutlinks.length; i++) {
            System.out.print(pageRankAndOutlinks[i] + " ");
            outlinks.add(pageRankAndOutlinks[i]);
        }
        System.out.println("");

        // Add to combiner titlePage and list of outlinks
        NodeWritable aux = new NodeWritable(Double.parseDouble(pageRankAndOutlinks[0]), outlinks);
        List<NodeWritable> list = new ArrayList<>();

            /*
            if(combiner.containsKey(titlePage)){
                combiner.get(titlePage).add(aux);
            }else{
                list.add(aux);
                combiner.put(titlePage, list);
            }
            */
        reducerKey.set(titlePage);
        reducerValue.set(aux);
        context.write(reducerKey, reducerValue);


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
            /*
            Stampa su file
            List<NodeWritable> list = new ArrayList<>();
            File inputAdjacency = new File("src/main/resources/output.txt");
            FileWriter myWriter = null;
            try {
                myWriter = new FileWriter("src/main/resources/output.txt");
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
                System.out.println(str);


                try {
                    myWriter.write(str + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            myWriter.close();

             */

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

    }
}

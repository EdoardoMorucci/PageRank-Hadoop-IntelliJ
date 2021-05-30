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

    public static class PageRankMapper extends Mapper<LongWritable, Text, Text, NodeWritable>{

        private final Text reducerKey = new Text();
        private  NodeWritable reducerValue = new NodeWritable();
        Map<String, List<NodeWritable>> combiner = new HashMap<String, List<NodeWritable>>();

        @Override
        public void setup(Context context){
            context.getConfiguration().setDouble("page.rank.N", 5);

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

    public static class PageRankReducer extends Reducer<Text, NodeWritable, Text, Text>{

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
            out = ">>> " + sumPR.toString();
            if(graphNode != null){

                for(String str: graphNode.getOutlinks()){
                    out += "-> " + str;
                }
            }
            outputValue.set(out);
            context.write(key, outputValue);

        }

    }


    public static void main(String[] args) throws Exception{

        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        if (otherArgs.length != 4) {
            System.err.println("Usage: PageRank <treshold> <maxIteration> <input> <output>");
            System.exit(1);
        }

        System.out.println("args[0]: <treshold>=" + otherArgs[0]);
        System.out.println("args[1]: <maxIteration>=" + otherArgs[1]);
        System.out.println("args[2]: <input>=" + otherArgs[2]);
        System.out.println("args[3]: <output>=" + otherArgs[3]);

//        pageRankCalculator();

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

        // set treshold and maxIteration
        double treshold = Double.parseDouble(otherArgs[0]);
        int maxIteration = Integer.parseInt(otherArgs[1]);
        job.getConfiguration().setDouble("page.rank.treshold", treshold);
        job.getConfiguration().setInt("page.rank.maxIteration", maxIteration);

      //  job.getConfiguration().getLong("GlobalNum", GlobalNum);


        // define I/O
        FileInputFormat.addInputPath(job, new Path(otherArgs[2]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[3]));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);

//        Long GlobalNum = job.getCounters().findCounter(
//                TaskCounter.MAP_INPUT_RECORDS).getValue();
//        System.out.println("global " + GlobalNum);
    }

    private void pageRankCalculator(){

    }

}

package it.unipi.hadoop.Parse;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseMapper extends Mapper<LongWritable, Text, Text, Text> {
    private final Text reducerKey = new Text();
    private final Text reducerValue = new Text();
    private static final Pattern titlePattern = Pattern.compile("<title>(.*)</title>");
    private static final Pattern textPattern = Pattern.compile("<text(.*?)</text>");
    private static final Pattern outLinkPattern = Pattern.compile("\\[\\[(.*?)\\]\\]");

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String input = value.toString();

        // input:   <title>titlePage</title> ecc..  <text ...> [[outlink]] ecc.. [[outlink]] ... </text>
        String titlePage = getTitlePage(input);
        reducerKey.set(titlePage);

        String innerText = getInnerText(input);

        List<String> outlinks = getOutlinks(innerText);

        if(outlinks.size()==0){

            reducerValue.set("sinknode");
            context.write(reducerKey, reducerValue);

        }else{

            for(String outlink: outlinks){
                reducerValue.set(outlink);
                context.write(reducerKey, new Text(outlink));
            }

        }

    }

    private String getTitlePage(String line){
        Matcher title = titlePattern.matcher(line);

        if(title.find()){
            return title.group(1).trim().split("\\|")[0];
        }else{
            return null;
        }
    }

    private String getInnerText(String line){
        Matcher text = textPattern.matcher(line);

        if(text.find()){
            return text.group(1);
        }else{
            return null;
        }
    }

    private List<String> getOutlinks(String line){
        Matcher outlinkMatch = outLinkPattern.matcher(line);
        List<String> outlinks = new ArrayList<>();

        while(outlinkMatch.find()){
            String aux = outlinkMatch.group(1);
            outlinks.add(aux.trim().split("\\|")[0]);
        }

        return outlinks;

    }

}

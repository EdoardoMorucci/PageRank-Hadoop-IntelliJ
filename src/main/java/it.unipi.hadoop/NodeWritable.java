package it.unipi.hadoop;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeWritable implements Writable{

    private Double pageRank;
    private List<String> outlinks = new ArrayList<>();

    public NodeWritable(){}

    public NodeWritable(Double pageRankFatherContr, List<String> list){
        pageRank = pageRankFatherContr;
        outlinks.addAll(list);
    }

    public NodeWritable(Double pageRank){
        this.pageRank = pageRank;
    }

    public double getPageRank() {
        return pageRank;
    }

    public List<String> getOutlinks() {
        List<String> listReturn = new ArrayList<>();
        listReturn = outlinks;
        return listReturn;
    }

    public void setOutlinks(List<String> outlinks) {
        this.outlinks = outlinks;
    }

    public void setPageRank(double pageRank) {
        this.pageRank = pageRank;
    }
    public void set(NodeWritable node){
        pageRank = node.pageRank;
        outlinks = node.outlinks;
    }

    public List<String> stampa(){

        return outlinks;
    }

    @Override
    public String toString(){
        String str = pageRank.toString();

        for(String aux: outlinks){
            str += " ";
            str += aux;
        }

        return str;
    }

    private String getStringFromOutlink(){
        String aux = "";
        if(outlinks.size() == 0){
            return "";
        }
        for(String str: outlinks){
            aux += "-> ";
            aux += str;
        }
        return aux;
    }

    private List<String> makeOutlinksFromString(String str){
        if(str.equals(""))
            return null;
        String[] aux = str.trim().split("-> ");
        List<String> listOutlinks = new ArrayList<>();
        listOutlinks.addAll(Arrays.asList(aux));
        return listOutlinks;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeDouble(this.pageRank);
//        out.writeChars(getStringFromOutlink());
        out.writeUTF(getStringFromOutlink());
//        out.writeBytes(getStringFromOutlink());
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.pageRank = in.readDouble();
//        this.outlinks = makeOutlinksFromString(in.readLine());
        this.outlinks = makeOutlinksFromString(in.readUTF());


    }
}

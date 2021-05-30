package it.unipi.hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Graph {

    /***
     * @param args
     * Trasform file wiki-micro.txt into adjacency list like: pageTitle: pageRank, outlink1, outlink2, ecc...
     * And save it on a file named inputAdjacency.txt
     */

    // SI POTREBBE ELIMINARE titles E totOutLinks

    public static void main(String args[]) {
        String fileName = "src/main/resources/wiki-micro.txt";
        // Contains all rows in the file
        List<String> list = new ArrayList<>();
        // Contains all titles of a row
        List<String> titles = new ArrayList<>();
        List<String> totOutLinks = new ArrayList<>();

        File inputAdjacency = new File("src/main/resources/inputAdjacency10.txt");
        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter("src/main/resources/inputAdjacency10.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {
            //br returns as stream and convert it into a List
            list = br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Double pageRank = 1.0/list.size();
        int counter = 0;
        for(String aux: list){
            String adjacencyList = "";
            String[] title = aux.trim().split("</title>");
            title[0] = title[0].replaceAll("\t", "");
            title[0] = title[0].substring(7);
            // Add title
            adjacencyList = title[0];
            // Add initial page rank
            adjacencyList = adjacencyList + ">> " + pageRank.toString();

            String[] substring = aux.trim().split("\\[\\[");

            for(int j=1; j<substring.length; j++){
                String auxSubString = substring[j];
                // recive: pagename]] other pagename]] ...
                String[] outLink = auxSubString.trim().split("\\]\\]");
                // Add outlink
                adjacencyList = adjacencyList + "-> " + outLink[0];
                totOutLinks.add(outLink[0]);
            }

            if(!titles.contains(title[0]))
                titles.add(title[0]);

            try {
                myWriter.write(adjacencyList + "\n");
                System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                System.out.println("An error occured in writing file.");
                e.printStackTrace();
            }

            if(counter == 10){
                break;
            }


            counter++;
        }

        try {
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
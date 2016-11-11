package edu.sharif.ce.gallivanter.core;

import edu.sharif.ce.gallivanter.datatypes.FileAndPositionHashMap;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mohammad on 11/11/16.
 */
public class RelevanceAsseser {
    public enum Function{
        MAP,R_PRECISION,F_MEASURE
    }
    private List<String> relevanceAssesmentFile=new ArrayList<>();
    private QueryManager queryManager;
    private double calculateFMesasure(Double precision,Double recall){
        return (2*precision*recall)/(precision+recall);
    }

    public RelevanceAsseser(QueryManager queryManager){
        try {
            relevanceAssesmentFile = new ArrayList(Files.readAllLines(Paths.get("resources/PersianPoemsData/RelevanceAssesment/RelevanceAssesment", new String[0]), Charset.forName("UTF8")));
            this.queryManager=queryManager;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public double calculateAll(boolean lnnLtn,Function function,Integer R){
        List<Double>forEachQuery=new ArrayList<>();
        for(String poem:relevanceAssesmentFile){
            if(poem.contains("persian_query"))
                forEachQuery.add(calculate(poem,lnnLtn,function,R));
        }
        return calculateAverage(forEachQuery);
    }
    public double calculate(String queryFile,boolean lnnLtn,Function function,Integer R){
        int poemIndex=relevanceAssesmentFile.indexOf(queryFile);
        List<String> goodPoemsList=new ArrayList<>();
        while(true){
            if(relevanceAssesmentFile.get(++poemIndex).trim().isEmpty())
                break;
            goodPoemsList.addAll(Arrays.asList(relevanceAssesmentFile.get(poemIndex).split("\\s")));
        }
        String query;
        try{
            int i=0;
            query=new ArrayList(Files.readAllLines(Paths.get("resources/PersianPoemsData/Queries/"+queryFile, new String[0]), Charset.forName("UTF8"))).get(i).toString();
            try{
                while(true){
                    String toAppend=new ArrayList(Files.readAllLines(Paths.get("resources/PersianPoemsData/Queries/"+queryFile, new String[0]), Charset.forName("UTF8"))).get(++i).toString();
                    query=query+" "+toAppend;
                }
            }catch (Exception ignored){}
        }catch (Exception e){
            throw new RuntimeException(e); //I don't have any more time!!!
        }
        FileAndPositionHashMap files=queryManager.fetch(query,lnnLtn);
        int i=0;
        int relevant=0;
        List<Double>precisionAtK=new ArrayList<>();
        for(String docID:files.keySet()){
            i++;
            docID=docID.substring(docID.lastIndexOf("/")+1);
            if(goodPoemsList.contains(docID))
                relevant++;
            if(function==Function.R_PRECISION&&i==R)
                return (double)relevant/i;
            precisionAtK.add((double)relevant/i);
        }
        if(function==Function.F_MEASURE)
            return calculateFMesasure((double)relevant/i,(double)relevant/goodPoemsList.size());
        return calculateAverage(precisionAtK);
    }
    private double calculateAverage(List <Double> list) {
        Double sum = 0.0;
        if(!list.isEmpty()) {
            for (Double mark : list) {
                sum += mark;
            }
            return sum.doubleValue() / list.size();
        }
        return sum;
    }

}

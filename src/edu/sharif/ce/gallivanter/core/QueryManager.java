package edu.sharif.ce.gallivanter.core;

import edu.sharif.ce.gallivanter.datatypes.DocScore;
import edu.sharif.ce.gallivanter.datatypes.FileAndPositionHashMap;
import edu.sharif.ce.gallivanter.datatypes.TermPosition;
import jhazm.Normalizer;
import jhazm.Stemmer;
import jhazm.tokenizer.WordTokenizer;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mohammad on 11/10/16.
 */
public class QueryManager {
    private static final double JACCARD_COEFFICIENT_SETTING=0.2;

    private final Normalizer normalizer=new Normalizer(); //Normalize each raw input line
    private final WordTokenizer tokenizer; //Tokenize each raw input line, after Normalizing
    private final Stemmer stemmer=new Stemmer(); //Stem each word after tokenization
    private IndexManager indexManager;
    public QueryManager(String queryDestinationPath,boolean watchPath){
        indexManager=new IndexManager(watchPath);
        indexManager.initIndex(queryDestinationPath);
        try {
            tokenizer = new WordTokenizer();
        }catch (IOException e){
            throw new RuntimeException("Resources not found at project root",e);
        }
    }
    private String correctQuery(String query){
        query=normalizer.run(query);
        List<String> tokenizedQuery=tokenizer.tokenize(query);
        String correctQuery="";
        for(String queryTerm:tokenizedQuery){
            queryTerm=stemmer.stem(query);
            if(!indexManager.fetch(queryTerm).isEmpty()||queryTerm.length()<3){ //Query for less than 3 chars is ignored being corrected. because I haven't indexed it...
                correctQuery+=queryTerm+" ";
                continue;
            }
            List<String>correctWordsByJaccard=new ArrayList<>();
            for(int i=0;i<queryTerm.length()-1;i++){
                String queryBiWord=queryTerm.substring(i,i+2);
                for(String word:indexManager.fetchByBigram(queryBiWord).keySet())
                    if(MathUtils.calculateJaccard(query,word)>JACCARD_COEFFICIENT_SETTING)
                        correctWordsByJaccard.add(word);
            }
            String minimumEditDistanceTerm=queryTerm;
            int editDistance=Integer.MAX_VALUE;
            for(String word:correctWordsByJaccard){
                if(MathUtils.minDistance(query,word)<editDistance){
                    editDistance=MathUtils.minDistance(query,word);
                    minimumEditDistanceTerm=word;
                }
            }
            if(!minimumEditDistanceTerm.equals(queryTerm))
                System.out.println("Word: \n"+queryTerm+"\nWas Replaced with: \n"+minimumEditDistanceTerm);
            correctQuery+=minimumEditDistanceTerm+" ";
        }
        return correctQuery.substring(0,correctQuery.length()-1);
    }
    public FileAndPositionHashMap fetch(String query,boolean lnnDocLtnQuery){
        FileAndPositionHashMap map=new FileAndPositionHashMap();
        query=correctQuery(query);
        query=normalizer.run(query);
        HashMap<String,Double> queryRanking=null;
        if(lnnDocLtnQuery)
            queryRanking=scoreQueryByLtn(query);
        else
            queryRanking=scoreQueryByLtc(query);
        for(String token:tokenizer.tokenize(query)){
            token=stemmer.stem(token);
            FileAndPositionHashMap retrieved=indexManager.fetch(token);
            for(String docId:retrieved.keySet()){
                if(!map.containsKey(docId))
                    map.put(docId,retrieved.get(docId));
                else
                    map.get(docId).addAll(retrieved.get(docId));
            }
        }
        List<DocScore>docRanking=new ArrayList<>();
        for(String docId:map.keySet()){
            HashMap<String,Double> docTermRanking=null;
            if(lnnDocLtnQuery)
                docTermRanking=scoreDocByLnn(docId);
            else
                docTermRanking=scoreDocByLnc(docId);
            Double docRank=0.0;
            for(String token:queryRanking.keySet())
                if(docTermRanking.containsKey(token))
                    docRank+=docTermRanking.get(token)*queryRanking.get(token);
            docRanking.add(new DocScore(docId,docRank));
        }
        Collections.sort(docRanking);
        FileAndPositionHashMap toReturn=new FileAndPositionHashMap();
        for(DocScore docScore:docRanking)
            toReturn.put(docScore.getDocID(),map.get(docScore.getDocID()));

        return toReturn;
    }

    private HashMap<String,Double> tf(String docID){
        HashMap<String,Double>toReturn=new HashMap<>();
        //TODO: BAD ORDER
        for(String singleTerm:indexManager.getIndex().keySet())
            if(indexManager.getIndex().get(singleTerm).get(docID)!=null)
                toReturn.put(singleTerm,MathUtils.calculateLogTfForDoc(indexManager.getIndex().get(singleTerm).get(docID).size()));
        return toReturn;
    }
    private HashMap<String,Double>scoreDocByLnn(String docID){
        return tf(docID);
    }
    private Double idf(String term){
        int num=indexManager.getSizeOfDocsForTerm(term);
        return MathUtils.calculateIdfForTerm(indexManager.getNumberOfDocs(),num);
    }

    private HashMap<String, Double> scoreDocByLnc(String doc){
        HashMap<String, Double> tf = tf(doc);
        HashMap<String, Double> weightMap = new HashMap<>();

        double normalizeValue = 0;
        for (String token : tf.keySet()) {
            double weight=(tf.get(token));
            weightMap.put(token, weight);
            normalizeValue += Math.sqrt(weight*weight);
        }
        for (String token : tf.keySet())
            weightMap.put(token, tf.get(token)/normalizeValue);

        return weightMap;
    }
    private HashMap<String, Double> scoreQueryByLtc(String query){
        HashMap<String,Integer>queryTermOccurence=new HashMap<>();
        for(String token:tokenizer.tokenize(query)){
            token=stemmer.stem(token);
            if(!queryTermOccurence.containsKey(token))
                queryTermOccurence.put(token,1);
            else
                queryTermOccurence.put(token,queryTermOccurence.get(token)+1);
        }
        HashMap<String,Double>tf=new HashMap<>();
        for(String token:queryTermOccurence.keySet())
            tf.put(token,MathUtils.calculateLogTfForDoc(queryTermOccurence.get(token)));

        HashMap<String, Double> weightMap = new HashMap<>();

        double normalizeValue = 0;
        for (String token : tf.keySet()) {
            double weight=(tf.get(token)*idf(token));
            weightMap.put(token, weight);
            normalizeValue += Math.sqrt(weight*weight);
        }
        for (String token : tf.keySet())
            weightMap.put(token, tf.get(token)/normalizeValue);

        return weightMap;
    }
    private HashMap<String, Double> scoreQueryByLtn(String query){
        HashMap<String,Integer>queryTermOccurence=new HashMap<>();
        for(String token:tokenizer.tokenize(query)){
            token=stemmer.stem(token);
            if(!queryTermOccurence.containsKey(token))
                queryTermOccurence.put(token,1);
            else
                queryTermOccurence.put(token,queryTermOccurence.get(token)+1);
        }
        HashMap<String,Double>tf=new HashMap<>();
        for(String token:queryTermOccurence.keySet())
            tf.put(token,MathUtils.calculateLogTfForDoc(queryTermOccurence.get(token)));

        HashMap<String, Double> weightMap = new HashMap<>();
        for (String token : tf.keySet()) {
            double weight=(tf.get(token)*idf(token));
            weightMap.put(token, weight);
        }

        return weightMap;
    }
}

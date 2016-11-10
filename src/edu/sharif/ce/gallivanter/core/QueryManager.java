package edu.sharif.ce.gallivanter.core;

import jhazm.Normalizer;
import jhazm.Stemmer;
import jhazm.tokenizer.WordTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    public String correctQuery(String query){
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
}

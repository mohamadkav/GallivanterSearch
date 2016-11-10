package edu.sharif.ce.gallivanter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sharif.ce.gallivanter.core.IndexManager;
import edu.sharif.ce.gallivanter.datatypes.FileAndPositionHashMap;
import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by mohammad on 10/21/16.
 */
public class Test {
    public static void main(String[] args) throws Exception{
        IndexManager indexManager=new IndexManager();
        indexManager.initIndex("/home/mohammad/IdeaProjects/MIR/resources/PersianPoemsData/testPoems");
  //      indexManager.writeIndexToFile("/home/mohammad/Desktop/shit2.index");
        HashMap<String,FileAndPositionHashMap> mylist=indexManager.fetchByBigram("خر");
        for (String s : mylist.keySet()) {
            System.out.println(s+":");
            System.out.println(mylist.get(s));
        }
    }
}

package edu.sharif.ce.gallivanter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sharif.ce.gallivanter.core.IndexManager;
import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

/**
 * Created by mohammad on 10/21/16.
 */
public class Test {
    public static void main(String[] args) throws Exception{
        IndexManager indexManager=new IndexManager();
        indexManager.initIndex("/Users/mohammad/IdeaProjects/MIR/resources/PersianPoemsData/Poems");
        indexManager.writeIndexToFile("/Users/mohammad/Desktop/shit2.index");
    }
}

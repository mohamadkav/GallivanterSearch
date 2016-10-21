package edu.sharif.ce.gallivanter;

import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

import java.io.File;
import java.util.Scanner;

/**
 * Created by mohammad on 10/21/16.
 */
public class Test {
    public static void main(String[] args) throws Exception{
        File f=new File("/home/mohammad/IdeaProjects/MIR/resources/PersianPoemsData/Poems");
        for (File file : f.listFiles()) {
            Scanner input=new Scanner(file);
            while(input.hasNext()){
                String oneline=input.nextLine();
                String[] raw=oneline.split("\\s");
                for(String one:raw)
                    if(one.length()<=1&&!one.equals("و")&&!one.equals("ز"))
                        System.out.println(oneline);
            }
        }
        WordTokenizer tokenizer=new WordTokenizer();
        System.out.println(tokenizer.tokenize("سلام. آیا، بهتر نیست؟ که لعنت بتو؟!"));
    }
}

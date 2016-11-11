package edu.sharif.ce.gallivanter;

import edu.sharif.ce.gallivanter.core.QueryManager;
import edu.sharif.ce.gallivanter.core.RelevanceAsseser;

import java.util.Scanner;

/**
 * Created by mohammad on 10/21/16.
 */
public class Main {
    public static void main(String[] args) {
        Scanner input=new Scanner(System.in);
        System.out.println("High There! Please enter your Poems folder: (default:\"/home/mohammad/IdeaProjects/MIR/resources/PersianPoemsData/Poems\")");
        String inputFolder=input.nextLine();
        if(inputFolder.trim().isEmpty())
            inputFolder="/home/mohammad/IdeaProjects/MIR/resources/PersianPoemsData/Poems";
        QueryManager queryManager=new QueryManager(inputFolder,true);
        RelevanceAsseser relevanceAsseser=new RelevanceAsseser(queryManager);
        while (true) {
            System.out.println("OK! seems to be ready... Enter your command: Save Index Manager to file, Load From file, Run Query, Assessment (s,l,q,a)");
            String command = input.nextLine().toLowerCase().trim();
            if (command.equals("s")) {
                System.out.println("Enter Path: ");
                queryManager.writeIndexToFile(input.nextLine());
                System.out.println("DONE!");
            }
            else if(command.equals("l")){
                System.out.println("Enter Path: ");
                if(queryManager.readIndexFromFile(input.nextLine()))
                    System.out.println("DONE!");
                else
                    System.out.println("ERROR :(");
            }
            else if(command.equals("q")){
                boolean lnnltn=false;
                System.out.println("aha! Please enter 1 lnn.ltn and 2 for lnc.ltc");
                command=input.nextLine();
                if(Integer.parseInt(command)==1)
                    lnnltn=true;
                System.out.println("ENTER QUERY....");
                command=input.nextLine();
                System.out.println(queryManager.fetch(command,lnnltn));
            }
            else if(command.equals("a")){
                boolean lnnltn=false;
                System.out.println("aha! Please enter 1 lnn.ltn and 2 for lnc.ltc");
                command=input.nextLine();
                if(Integer.parseInt(command)==1)
                    lnnltn=true;
                System.out.println("Enter Function: MAP/R_PRECISION/F_MEASURE (m,r,f)");
                command=input.nextLine();
                Integer R=null;
                RelevanceAsseser.Function function;
                if(command.equals("r")){
                    System.out.println("Enter R: ");
                    R=Integer.parseInt(input.nextLine());
                    function= RelevanceAsseser.Function.R_PRECISION;
                }
                else if(command.equals("m"))
                    function= RelevanceAsseser.Function.MAP;
                else if(command.equals("f"))
                    function= RelevanceAsseser.Function.F_MEASURE;
                else
                    throw new RuntimeException("Invalid Function");
                System.out.println("Enter Query: eg. 45.persian_query (all for ALL!)");
                command=input.nextLine().toLowerCase().trim();
                if(command.equals("all"))
                    System.out.println(relevanceAsseser.calculateAll(lnnltn,function,R));
                else
                    System.out.println(relevanceAsseser.calculate(command,lnnltn,function,R));
            }
        }
    }
}

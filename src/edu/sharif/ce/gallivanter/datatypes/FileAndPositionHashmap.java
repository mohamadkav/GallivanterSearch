package edu.sharif.ce.gallivanter.datatypes;

import java.util.HashMap;

/**
 * Created by mohammad on 10/27/16.
 */
public class FileAndPositionHashmap extends HashMap<String,PositionArrayList>{
    @Override
    public String toString() {
        StringBuilder stringBuilder=new StringBuilder("");
        for (String fileIdentifier:this.keySet()){
            stringBuilder.append(fileIdentifier+"  :: ");
            PositionArrayList positionArrayList=this.get(fileIdentifier);
            for(TermPosition termPosition:positionArrayList){
                stringBuilder.append(" | "+termPosition.getLine()+","+termPosition.getPosition());
            }
        }
        return stringBuilder.toString();
    }
}

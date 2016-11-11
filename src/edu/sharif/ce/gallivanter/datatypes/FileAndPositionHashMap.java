package edu.sharif.ce.gallivanter.datatypes;

import java.util.LinkedHashMap;

/**
 * Created by mohammad on 10/27/16.
 */
public class FileAndPositionHashMap extends LinkedHashMap<String,PositionArrayList> {
    @Override
    public String toString() {
        StringBuilder stringBuilder=new StringBuilder("");
        for (String fileIdentifier:this.keySet()){
            stringBuilder.append(fileIdentifier+"  :: ");
            PositionArrayList positionArrayList=this.get(fileIdentifier);
            for(TermPosition termPosition:positionArrayList){
                stringBuilder.append(" | "+termPosition.getLine()+","+termPosition.getPosition());
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}

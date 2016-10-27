package edu.sharif.ce.gallivanter.datatypes;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mohammad on 10/21/16.
 */
public class PositionArrayList extends ArrayList<TermPosition>{
    long version;
    public PositionArrayList(long version){
        this.version=version;
    }
    @Override
    public boolean add(TermPosition termPosition) {
        if(super.add(termPosition)){
            Collections.sort(this);
            return true;
        }
        return false;
    }
    public long getVersion(){
        return version;
    }
}

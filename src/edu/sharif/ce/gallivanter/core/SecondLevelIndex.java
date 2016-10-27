package edu.sharif.ce.gallivanter.core;

import java.util.HashMap;

/**
 * Created by mohammad on 10/27/16.
 */
public class SecondLevelIndex {
    private HashMap<String,Long> versionList=new HashMap<>();
    public Long getLatestVersionForFile(String fileIdentifier){
        if(versionList.containsKey(fileIdentifier))
            return versionList.get(fileIdentifier);
        return 1L;
    }
    public void notifyVersionUpdateForFile(String fileIdentifier){
        versionList.put(fileIdentifier,getLatestVersionForFile(fileIdentifier)+1);
    }
}

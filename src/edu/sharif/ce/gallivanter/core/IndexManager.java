package edu.sharif.ce.gallivanter.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sharif.ce.gallivanter.datatypes.FileAndPositionHashMap;
import edu.sharif.ce.gallivanter.datatypes.PositionArrayList;
import edu.sharif.ce.gallivanter.datatypes.TermPosition;
import jhazm.Normalizer;
import jhazm.Stemmer;
import jhazm.tokenizer.WordTokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by mohammad on 10/21/16.
 */
public class IndexManager {
    private final Normalizer normalizer=new Normalizer(); //Normalize each raw input line
    private final WordTokenizer tokenizer; //Tokenize each raw input line, after Normalizing
    private final Stemmer stemmer=new Stemmer(); //Stem each word after tokenization



    private HashMap<String ,FileAndPositionHashMap> index=new HashMap<>();
    private BigramIndex bigramIndex=new BigramIndex();
    private SecondLevelIndex secondLevelIndex=new SecondLevelIndex();
    private List<String> deletedFileList=new ArrayList<String>(){
        @Override
        public boolean add(String o) {
            N--;
            return super.add(o);
        }
    };
    private FileWatcher fileWatcher;
    private ArrayList stopWords;
    private boolean watchFolder;
    private Integer N=0;
    public IndexManager(boolean watchFolder){
        this.watchFolder=watchFolder;
        try {
            tokenizer = new WordTokenizer();
            stopWords=new ArrayList(Files.readAllLines(Paths.get("resources/PersianPoemsData/Stopwords/Stopwords", new String[0]), Charset.forName("UTF8")));
        }catch (IOException e){
            throw new RuntimeException("Resources not found at project root",e);
        }
    }
    public void initIndex(String directoryPath){
        File file=new File(directoryPath);
        if(!file.exists()||!file.isDirectory())
            throw new NullPointerException("Nope. not the right path");
        for(File f:file.listFiles()){
            addSingleFileToIndex(f,1,false);
        }
        System.out.println("Index init Finished Successfully!");
        if(watchFolder) {
            fileWatcher = new FileWatcher(directoryPath);
            fileWatcher.start();
        }
    }
    @Deprecated
    public HashMap<String ,FileAndPositionHashMap> getIndex(){
        return index;
    }
    public Integer getNumberOfDocs(){
        return N;
    }
    public Integer getSizeOfDocsForTerm(String term){
        try {
            return index.get(term).keySet().size();
        }catch (NullPointerException e){
            return 0;
        }
    }
    public FileAndPositionHashMap fetch(String token){
        FileAndPositionHashMap newlyFetched=index.get(token);
        FileAndPositionHashMap toReturn=new FileAndPositionHashMap();
        if(newlyFetched!=null)
            for (String fileIdentifier : newlyFetched.keySet()) {
                PositionArrayList positionArrayList = newlyFetched.get(fileIdentifier);
                if (!deletedFileList.contains(fileIdentifier) && secondLevelIndex.getLatestVersionForFile(fileIdentifier) == positionArrayList.getVersion())
                    toReturn.put(fileIdentifier, positionArrayList);
            }

        return toReturn;
    }

    public HashMap<String,FileAndPositionHashMap> fetchByBigram(String biword){
        List<String>wordsToGet=new ArrayList<>();
        HashMap<String,FileAndPositionHashMap>toReturn=new HashMap<>();
        if(!bigramIndex.containsKey(biword))
            return toReturn;
        for(String word:bigramIndex.get(biword)){
            if(!wordsToGet.contains(word))
                wordsToGet.add(word);
        }
        for(String word:wordsToGet){
            FileAndPositionHashMap fetched=fetch(word);
            if(fetched!=null&&!fetched.isEmpty())
                toReturn.put(word,fetched);
        }
        return toReturn;
    }

    public boolean writeIndexToFile(String path) {
        try{
            ObjectMapper objectMapper=new ObjectMapper();
            objectMapper.writeValue(new File(path),index);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public boolean isStopWord(String word){
        return stopWords.contains(word);
    }

    private void addSingleFileToIndex(File file, long version,boolean override){
        try {
            Scanner input = new Scanner(file);
            long currentLine=0;
            while (input.hasNext()){
                String rawInput=input.nextLine();
                currentLine++;
                String normalized=normalizer.run(rawInput);
                List<String> tokenizedInputs=tokenizer.tokenize(normalized);
                if(override)
                    for(String token:tokenizedInputs) {
                        token=stemmer.stem(token);
                        if(!stopWords.contains(token)&&token.length()>1)
                            if (index.get(token) != null)
                                if (index.get(token).get(file.getAbsolutePath()) != null)
                                    index.get(token).remove(file.getAbsolutePath());
                    }
                else
                    N++;
                for(int i=0;i<tokenizedInputs.size();i++){
                    String token=tokenizedInputs.get(i);
                    token=stemmer.stem(token);
                    if(!stopWords.contains(token)&&token.length()>1){
                        if(!index.containsKey(token)){
                            index.put(token,new FileAndPositionHashMap(){{put(file.getAbsolutePath(),new PositionArrayList(version));}});
                            addToBigram(token);
                            index.get(token).get(file.getAbsolutePath()).add(new TermPosition(currentLine,i));
                        }
                        else{
                            if(index.get(token).get(file.getAbsolutePath())!=null)
                                index.get(token).get(file.getAbsolutePath()).add(new TermPosition(currentLine,i));
                            else {
                                index.get(token).put(file.getAbsolutePath(), new PositionArrayList(version));
                                index.get(token).get(file.getAbsolutePath()).add(new TermPosition(currentLine,i));
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            System.out.println("Failed for file: "+file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    private void addToBigram(String token) {
        for(int i=0;i<token.length()-1;i++){
            String bi=token.substring(i,i+2);
            if(bi.length()!=2)
                throw new RuntimeException("Bigram Length is not equal to 2");
            if(!bigramIndex.containsKey(bi))
                bigramIndex.put(bi,new ArrayList<>());
            if(!bigramIndex.get(bi).contains(token))
                bigramIndex.get(bi).add(token);
        }
    }


    private class FileWatcher extends Thread implements Runnable{
        Path filePath;
        private FileWatcher(String pathString) {
            this.filePath=Paths.get(pathString);
            try {
                Boolean isFolder = (Boolean) Files.getAttribute(filePath,
                        "basic:isDirectory", NOFOLLOW_LINKS);
                if (!isFolder) {
                    throw new IllegalArgumentException("Path: " + filePath + " is not a folder");
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }

        @Override
        public void run() {
            System.out.println("Watching path: " + filePath);

            // We obtain the file system of the Path
            FileSystem fs =filePath.getFileSystem ();

            // We create the new WatchService using the new try() block
            try(WatchService service = fs.newWatchService()) {

                // We register the path to the service
                // We watch for creation events
                filePath.register(service, ENTRY_CREATE,ENTRY_DELETE,ENTRY_MODIFY);

                // Start the infinite polling loop
                WatchKey key;
                while(true) {
                    key = service.take();
                    // Dequeueing events
                    WatchEvent.Kind<?> kind;
                    for(WatchEvent<?> watchEvent : key.pollEvents()) {
                        // Get the type of the event
                        kind = watchEvent.kind();
                        if (OVERFLOW == kind) {
                            continue; //loop
                        } else if (ENTRY_CREATE == kind) {
                            // A new Path was created
                            Path dir=(Path)key.watchable();
                            Path newPath = dir.resolve(((WatchEvent<Path>) watchEvent).context());
                            if(isValidPath(newPath.toFile().getAbsolutePath()))
                                N++;
                            // Output
/*                            System.out.println("New path created: " + newPath);
                            File newFile=newPath.toFile();
                            if(newFile.isDirectory())
                                addFolderToModify(newFile);
                            else
                                addSingleFileToIndex(newFile,1,false);*/
                        }else if(ENTRY_DELETE==kind){
                            Path dir=(Path)key.watchable();
                            Path deletedPath = dir.resolve(((WatchEvent<Path>) watchEvent).context());
                            // Output
                            if(!isValidPath(deletedPath.toFile().getAbsolutePath()))
                                continue;
                            System.out.println("Path deleted: " + deletedPath);
                            File deletedFile=deletedPath.toFile();
                            if(deletedFile.isDirectory())
                                addFolderToDelete(deletedFile.listFiles());
                            else
                                deletedFileList.add(deletedFile.getAbsolutePath());
                        }else if(ENTRY_MODIFY==kind){
                            Path dir=(Path)key.watchable();
                            Path modifiedPath = dir.resolve(((WatchEvent<Path>) watchEvent).context());
                            if(!isValidPath(modifiedPath.toFile().getAbsolutePath()))
                                continue;
                            // Output
                            System.out.println("Path modified: " + modifiedPath);
                            File modifiedFile=modifiedPath.toFile();
                            addFolderToModify(modifiedFile);
                        }
                    }

                    if(!key.reset()) {
                        break; //loop
                    }
                }

            } catch(IOException ioe) {
                ioe.printStackTrace();
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        private void addFolderToDelete(File[] files) {
            for (File file : files) {
                if (file.isDirectory())
                    addFolderToDelete(file.listFiles());
                else
                    deletedFileList.add(file.getAbsolutePath());
            }
        }
        private void addFolderToModify(File file) {
            if (file.isDirectory()) {
                for (File subFiles : file.listFiles())
                    addFolderToModify(subFiles);
            }
            else {
                secondLevelIndex.notifyVersionUpdateForFile(file.getAbsolutePath());
                addSingleFileToIndex(file,secondLevelIndex.getLatestVersionForFile(file.getAbsolutePath()),true);
                if(deletedFileList.contains(file.getAbsolutePath()))
                    deletedFileList.remove(file.getAbsolutePath());
            }
        }
        private boolean isValidPath(String path){
            if(path.substring(path.lastIndexOf("/")+1).startsWith("."))
                return false;
            //TODO: Absolution only for these types of file
            if(!path.endsWith("poem"))
                return false;
            return true;
        }
    }
}

package edu.sharif.ce.gallivanter.core;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private Normalizer normalizer=new Normalizer(); //Normalize each raw input line
    private WordTokenizer tokenizer; //Tokenize each raw input line, after Normalizing
    private Stemmer stemmer=new Stemmer(); //Stem each word after tokenization
    private HashMap<String ,HashMap<String,PositionArrayList>> index=new HashMap<>();
    private List<String> deletedFileList;
    private List<String> modifiedFileList;
    private FileWatcher fileWatcher;
    private ArrayList stopWords;
    private Scanner input;
    public IndexManager(){
        try {
            tokenizer = new WordTokenizer();
            stopWords=new ArrayList(Files.readAllLines(Paths.get("resources/PersianPoemsData/Stopwords/Stopwords", new String[0]), Charset.forName("UTF8")));
        }catch (IOException e){
            throw new RuntimeException("Resources not found at project root",e);
        }
    }
    public boolean writeIndexToFile(String path){
        try{
            ObjectMapper objectMapper=new ObjectMapper();
            objectMapper.writeValue(new File(path),index);
            return true;
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public void initIndex(String directoryPath){
        File file=new File(directoryPath);
        if(!file.exists()||!file.isDirectory())
            throw new NullPointerException("Nope. not the right path");
        for(File f:file.listFiles()){
            addSingleFileToIndex(f);
        }
        System.out.println("Index init Finished Successfully!");
        fileWatcher=new FileWatcher(directoryPath);
        fileWatcher.start();
    }

    private void addSingleFileToIndex(File file){
        try {
            input = new Scanner(file);
            long currentLine=0;
            while (input.hasNext()){
                String rawInput=input.nextLine();
                currentLine++;
                String normalized=normalizer.run(rawInput);
                List<String> tokenizedInputs=tokenizer.tokenize(normalized);
                for(int i=0;i<tokenizedInputs.size();i++){
                    String token=tokenizedInputs.get(i);
                    token=stemmer.stem(token);
                    if(!stopWords.contains(token)&&token.length()>1){
                        if(!index.containsKey(token)){
                            index.put(token,new HashMap<String,PositionArrayList>(){{put(file.getAbsolutePath(),new PositionArrayList(1));}});
                            index.get(token).get(file.getAbsolutePath()).add(new TermPosition(currentLine,i));
                        }
                        else{
                            if(index.get(token).get(file.getAbsolutePath())!=null)
                                index.get(token).get(file.getAbsolutePath()).add(new TermPosition(currentLine,i));
                            else {
                                index.get(token).put(file.getAbsolutePath(), new PositionArrayList(1));
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
                            // Output
                            System.out.println("New path created: " + newPath);
                            File newFile=newPath.toFile();
                            if(newFile.isDirectory())
                               addFolderToIndex(newFile.listFiles());
                            else
                                addSingleFileToIndex(newFile);
                        }else if(ENTRY_DELETE==kind){
                            Path dir=(Path)key.watchable();
                            Path deletedPath = dir.resolve(((WatchEvent<Path>) watchEvent).context());
                            // Output
                            System.out.println("Path deleted: " + deletedPath);
                            File deletedFile=deletedPath.toFile();
                            if(deletedFile.isDirectory())
                                addFolderToDelete(deletedFile.listFiles());
                            else
                                deletedFileList.add(deletedFile.getAbsolutePath());
                        }else if(ENTRY_MODIFY==kind){
                            Path dir=(Path)key.watchable();
                            Path modifiedPath = dir.resolve(((WatchEvent<Path>) watchEvent).context());
                            // Output
                            System.out.println("Path modified: " + modifiedPath);
                            File modifiedFile=modifiedPath.toFile();
                            if(modifiedFile.isDirectory())
                                addFolderToModify(modifiedFile.listFiles());
                            else
                                modifiedFileList.add(modifiedFile.getAbsolutePath());
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
        private void addFolderToIndex(File[] files) {
            for (File file : files) {
                if (file.isDirectory())
                    addFolderToIndex(file.listFiles()); // Calls same method again.
                else
                    addSingleFileToIndex(file);

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
        private void addFolderToModify(File[] files) {
            for (File file : files) {
                if (file.isDirectory())
                    addFolderToModify(file.listFiles());
                else
                    modifiedFileList.add(file.getAbsolutePath());
            }
        }
    }
}

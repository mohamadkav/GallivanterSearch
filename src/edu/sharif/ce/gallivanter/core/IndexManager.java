package edu.sharif.ce.gallivanter.core;

import edu.sharif.ce.gallivanter.datatypes.PositionArrayList;
import jhazm.Normalizer;
import jhazm.Stemmer;
import jhazm.tokenizer.WordTokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by mohammad on 10/21/16.
 */
public class IndexManager {
    private Normalizer normalizer=new Normalizer(); //Normalize each raw input line
    private WordTokenizer tokenizer; //Tokenize each raw input line, after Normalizing
    private Stemmer stemmer=new Stemmer(); //Stem each word after tokenization
    private Map<String ,Map<String,PositionArrayList>> index;
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
    public void initIndex(String directoryPath){
        File file=new File(directoryPath);
        if(!file.exists()||!file.isDirectory())
            throw new NullPointerException("Nope. not the right path");
        for(File f:file.listFiles()){
            addSingleFileToIndex(f);
        }
        fileWatcher=new FileWatcher(directoryPath);
        fileWatcher.start();
    }
    private void addNewFileToIndex(File file){

    }

    private void addSingleFileToIndex(File file){
        try {
            input = new Scanner(file);
            while (input.hasNext()){
                String rawInput=input.nextLine();
                List<String> tokenizedInputs=tokenizer.tokenize(normalizer.run(rawInput));
                for(String token:tokenizedInputs){
                    token=stemmer.stem(token);
                    if(!stopWords.contains(token)){
                        //TODO: index!
                    }
                }
            }
        }catch (Exception e){
            System.out.println("Failed for file: "+file.getPath());
            e.printStackTrace();
        }
    }


    class FileWatcher extends Thread implements Runnable{
        Path filePath;
        public FileWatcher(String pathString) {
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
                    WatchEvent.Kind<?> kind = null;
                    for(WatchEvent<?> watchEvent : key.pollEvents()) {
                        // Get the type of the event
                        kind = watchEvent.kind();
                        if (OVERFLOW == kind) {
                            continue; //loop
                        } else if (ENTRY_CREATE == kind) {
                            // A new Path was created
                            Path newPath = ((WatchEvent<Path>) watchEvent).context();
                            // Output
                            System.out.println("New path created: " + newPath);
                            File newFile=newPath.toFile();
                            if(newFile.isDirectory())
                               addFolderToIndex(newFile.listFiles());
                            else
                                addNewFileToIndex(newFile);
                        }else if(ENTRY_DELETE==kind){
                            Path deletedPath = ((WatchEvent<Path>) watchEvent).context();
                            // Output
                            System.out.println("Path deleted: " + deletedPath);
                            File deletedFile=deletedPath.toFile();
                            if(deletedFile.isDirectory())
                                addFolderToDelete(deletedFile.listFiles());
                            else
                                deletedFileList.add(deletedFile.getPath());
                        }else if(ENTRY_MODIFY==kind){
                            Path modifiedPath = ((WatchEvent<Path>) watchEvent).context();
                            // Output
                            System.out.println("Path modified: " + modifiedPath);
                            File modifiedFile=modifiedPath.toFile();
                            if(modifiedFile.isDirectory())
                                addFolderToModify(modifiedFile.listFiles());
                            else
                                modifiedFileList.add(modifiedFile.getPath());
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
                    addNewFileToIndex(file);

            }
        }
        private void addFolderToDelete(File[] files) {
            for (File file : files) {
                if (file.isDirectory())
                    addFolderToDelete(file.listFiles());
                else
                    deletedFileList.add(file.getPath());
            }
        }
        private void addFolderToModify(File[] files) {
            for (File file : files) {
                if (file.isDirectory())
                    addFolderToModify(file.listFiles());
                else
                    modifiedFileList.add(file.getPath());
            }
        }
    }
}

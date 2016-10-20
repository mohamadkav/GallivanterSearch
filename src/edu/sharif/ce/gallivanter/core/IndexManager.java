package edu.sharif.ce.gallivanter.core;

import edu.sharif.ce.gallivanter.datatypes.PositionArrayList;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by mohammad on 10/21/16.
 */
public class IndexManager {
    private Map<String ,Map<String,PositionArrayList>> index;
    private List<String> deletedFileList;
    private List<String> modifiedFileList;
    private FileWatcher fileWatcher;
    public void initIndex(String directoryPath){
        //TODO: Index all/...
        fileWatcher=new FileWatcher(directoryPath);
        fileWatcher.start();
    }
    private void addNewFileToIndex(File file){

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

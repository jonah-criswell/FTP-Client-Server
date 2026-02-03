import java.io.*;
import java.net.*;

public String currentDirectory;

currentDirectory = System.getProperty("user.dir");

public void doLs() throws IOException {
    File currentFile = new File(currentDirectory);
    File[] fileList = currentFile.listFiles();

    StringBuilder serverResponse = new StringBuilder();
    if (fileList != null && fileList.length > 0) {
        for (File file : fileList) {
            serverResponse.append(file.getName()).append("\n");
        }
    } else {
        serverResponse.append("Directory is empty.\n");
    }

    //Write response to client with DataOutputStream
}

public void doCd(String requestedDir) throws IOException {
    File newDir; //Save path for processing

    //Handle movement to Parent Directory
    if (requestedDir.equals("..")) {
        File tempDir = new File(currentDirectory);
        File parentDir = tempDir.getParentFile();

        //If parent is null, we are at root
        if (parentDir == null) {
            //TODO WRITE ERROR MESSAGE TO CLIENT
            return;
        }
        //Wanted to check it before setting the real one
         newDir = parentDir;

    } else if (requestedDir.equals(".") { //Could be redundant
        //Return the same directory to user. No change server side.
        return;
    } else {
        newDir = new File(currentDirectory,requestedDir); //Change to requested directory
    }

    //Error Checking the new directory
    if (!newDir.exists() || !newDir.isDirectory()) {
        //TODO WRITE ERROR MESSAGE TO CLIENT, dir not found
        return;
    } else {
        //newDir = newDir.getCanonicalPath();
        currentDirectory = newDir.getCanonicalPath(); //Returns string, must put it here instead of the file object
        //TODO WRITE SUCCESS MESSAGE TO CLIENT
    }
}

public void doMkdir(String newDirName) throws IOException {
    File newDir = new File(currentDirectory, newDirName);

    if (newDir.exists()) {
        //Write error message to client, dir is already there
        return;
    }

    boolean mkdirbool = newDir.mkdir();
    if (mkdirbool) {
        //Write success message to client
    } else {
        //Write error message to client, mkdir failed
    }
}
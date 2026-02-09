import java.io.*;
import java.net.*;

public class myftpserver {

    private File cwd;
    private File localDir;

   public static void main(String[] args) {
      final int PORT = Integer.parseInt(args[0]);

      try (ServerSocket serverSocket = new ServerSocket(PORT)) {
         System.out.println("FTP Server is running on port " + PORT);

         while (true) {
            System.out.println("Active threads: " + Thread.activeCount());
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
            new Thread(() -> handleClient(clientSocket)).start();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true
            )
        ) {
            String command;
            while ((command = in.readLine()) != null) {
               if (command.equalsIgnoreCase("quit")) {
                    out.println("Goodbye");
                    break;
               }

                // For now, just echo
                out.println("Server received: " + command);
            }



        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }


    // get (double check)
    private void get(String[] parts) throws IOException {
        File src = new File(cwd, parts[1]).getCanonicalFile();
        if (!src.exists() || !src.isFile()) {
            System.out.println("File not found");
            return;
        }

        InputStream in = new BufferedInputStream(new FileInputStream(src));
        OutputStream out = socket.getOutputStream();
        
        byte[] buf = new byte[8129];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);

        }
        out.flush();
        in.close();
        
    }



// ls
public void doLs() throws IOException {
    

    File currentFile = new File(cwd);
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

// cd
public void doCd(String requestedDir) throws IOException {
    File newDir; //Save path for processing

    //Handle movement to Parent Directory
    if (requestedDir.equals("..")) {
        File tempDir = new File(cwd); //Get current directory as file object
        File parentDir = tempDir.getParentFile();

        //If parent is null, we are at root
        if (parentDir == null) {
            //TODO WRITE ERROR MESSAGE TO CLIENT
            return;
        }
        //Wanted to check it before setting the real one
         newDir = parentDir;

    } else if (requestedDir.equals(".")) { //Could be redundant
        //Return the same directory to user. No change server side.
        return;
    } else {
        newDir = new File(cwd,requestedDir); //Change to requested directory
    }

    //Error Checking the new directory
    if (!newDir.exists() || !newDir.isDirectory()) {
        //TODO WRITE ERROR MESSAGE TO CLIENT, dir not found
        return;
    } else {
        //newDir = newDir.getCanonicalPath();
        cwd = newDir.getCanonicalPath(); //Returns string, must put it here instead of the file object
        //TODO WRITE SUCCESS MESSAGE TO CLIENT
    }
}

// mkdir
public void doMkdir(String newDirName) throws IOException {
    File newDir = new File(cwd, newDirName);

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


    public void doDelete(String filename) throws IOException {
        File file = new File(cwd, filename).getCanonicalFile();

        if (!file.exists()) {
            System.out.println("File not found");
            return;
        } else {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("File deleted: " + file.getAbsolutePath());
            } else {
                System.out.println("Failed to delete file: " + file.getAbsolutePath());
            }
        }

    }


    private void put(String[] parts) throws IOException {
        File dst = new File(cwd, parts[1]).getCanonicalFile();
        if (dst.exists()) {
            System.out.println("File already exists");
            return;
        }

        InputStream in = socket.getInputStream();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(dst));

        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        out.flush();
        out.close();
        

    }

}


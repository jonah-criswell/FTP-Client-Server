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



   private void get(String[] parts) throws IOException {
    if (parts.length != 2) throw new IOException("Usage: get <filename>");

    String filename = parts[1];
    File src = new File(cwd, filename).getCanonicalFile();

    if (!src.exists() || !src.isFile()) {
      throw new IOException("File not found: " + src.getPath());
    }

    if (!localDir.exists()) {
      boolean ok = localDir.mkdirs();
      if (!ok) throw new IOException("Failed to create downloads directory: " + localDir.getPath());
    }

    File dst = new File(localDir, filename).getCanonicalFile();
    copyFileIO(src, dst);

    System.out.println("OK saved to: " + dst.getPath());
    }

    private void copyFileIO(File src, File dst) throws IOException {
    if (dst.exists() && !dst.delete()) {
    if (dst.exists() && !dst.delete()) {
      throw new IOException("Cannot overwrite: " + dst.getPath());
    }

    try (InputStream in = new BufferedInputStream(new FileInputStream(src));
         OutputStream out = new BufferedOutputStream(new FileOutputStream(dst))) {

      byte[] buf = new byte[8192];
      int n;
      while ((n = in.read(buf)) != -1) {
        out.write(buf, 0, n);
      }
      out.flush();
    }
  }
}

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

public void doCd(String requestedDir) throws IOException {
    File newDir; //Save path for processing

    //Handle movement to Parent Directory
    if (requestedDir.equals("..")) {
        File tempDir = new File(cwd);
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

}
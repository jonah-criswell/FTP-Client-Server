import java.io.*;
import java.net.*;

public class myftpserver {

   public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java myftpserver <port>");
            return;
        }

      final int PORT = Integer.parseInt(args[0]);

      try (ServerSocket serverSocket = new ServerSocket(PORT)) {
         System.out.println("FTP Server is running on port " + PORT);

         while (true) {
            System.out.println("Active threads: " + Thread.activeCount());
                
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                
                new Thread(new ClientHandler(clientSocket)).start();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private File cwd;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        // Initialize CWD where the server is running
        this.cwd = new File(System.getProperty("user.dir"));
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] localargs = inputLine.split(" "); //Allows us to parse commands with arguments
                String command = localargs[0].toLowerCase();

               if (command.equalsIgnoreCase("quit")) {
                    break;
               }

                //Available commands
                switch (command) {
                    case "ls":     doLs(out); break;
                    case "pwd":    doPwd(out); break;
                    case "cd":     doCd(localargs.length > 1 ? localargs[1] : "", out); break;
                    case "mkdir":  doMkdir(localargs.length > 1 ? localargs[1] : "", out); break;
                    case "delete": doDelete(localargs.length > 1 ? localargs[1] : "", out); break;
                    case "get":    get(localargs, out); break;
                    case "put":    put(localargs, in, out); break;
                    default:       out.println("Not a command: " + inputLine); break;
                }
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

    //LS appends filenames and prints them out
    
    private void doLs(PrintWriter out) throws IOException {
        File currentFile = new File(cwd.getAbsolutePath());
    File[] fileList = currentFile.listFiles();

    StringBuilder serverResponse = new StringBuilder();
    if (fileList != null && fileList.length > 0) {
        for (File file : fileList) {
                serverResponse.append(file.getName()).append("  ");
        }
            out.println(serverResponse.toString());
    } else {
            out.println("Directory is empty.");
    }
    }

    private void doPwd(PrintWriter out) throws IOException {
        out.println(cwd.getCanonicalPath());
    }

    private void doCd(String requestedDir, PrintWriter out) throws IOException {
        if (requestedDir.isEmpty()) {
            out.println("Usage: cd <dir>");
            return;
        }

        File newDir;
        if (requestedDir.equals("..")) {
            File parentDir = cwd.getParentFile();
            newDir = (parentDir != null) ? parentDir : cwd;
        } else if (requestedDir.equals(".")) {
        return;
    } else {
            newDir = new File(cwd, requestedDir);
        }
        //Changes current directory
        if (newDir.exists() && newDir.isDirectory()) {
            cwd = newDir.getCanonicalFile();
            out.println("Changed directory to " + cwd.getAbsolutePath());
    } else {
            out.println("Error: Directory not found");
    }
}

    private void doMkdir(String newDirName, PrintWriter out) throws IOException {
    File newDir = new File(cwd, newDirName);
    if (newDir.exists()) {
            out.println("Error: Directory already exists");
        } else if (newDir.mkdir()) {
            out.println("Directory created successfully");
    } else {
            out.println("Error: Failed to create directory");
    }
}

    private void doDelete(String filename, PrintWriter out) throws IOException {
        File file = new File(cwd, filename).getCanonicalFile();
        if (file.exists() && file.delete()) {
            out.println("File deleted: " + file.getAbsolutePath());
        } else {
            out.println("Error: File not found or delete failed");
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


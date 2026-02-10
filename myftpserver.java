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
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();           

           InputStream byteIn = new BufferedInputStream(in);
           OutputStream byteOut = new BufferedOutputStream(out);
        ) {
            String inputLine;

            while ((inputLine = readLine(byteIn)) != null) {
                String[] localargs = inputLine.split(" "); //Allows us to parse commands with arguments
                String command = localargs[0].toLowerCase();

                if (command.equalsIgnoreCase("quit")) {
                    break;
                }

                //Available commands
                switch (command) {
                    case "ls":     doLs(byteOut); break;
                    case "pwd":    doPwd(byteOut); break;
                    case "cd":     doCd(localargs.length > 1 ? localargs[1] : "", byteOut); break;
                    case "mkdir":  doMkdir(localargs.length > 1 ? localargs[1] : "", byteOut); break;
                    case "delete": doDelete(localargs.length > 1 ? localargs[1] : "", byteOut); break;
                    case "get":    get(localargs, byteOut); break;
                    case "put":    put(localargs, byteIn, byteOut); break;
                    default:       writeLine(byteOut, "Not a command: " + inputLine); break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }


    // get (double check)
    private void get(String[] parts, OutputStream byteOut) throws IOException {
        if (parts.length < 2) {
            writeLine(byteOut, "Error: Usage - get <filename>");
            return;
        }

        File src = new File(cwd, parts[1]).getCanonicalFile();

        if (!src.exists() || !src.isFile()) {
            writeLine(byteOut, "File not found");
            return;
        }
        
        long size = src.length();
        writeLine(byteOut, "OK " + size);

        try (InputStream fileIn = new BufferedInputStream(new FileInputStream(src))) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fileIn.read(buf)) != -1) {
                byteOut.write(buf, 0, n);   
            }
            byteOut.flush();
        }
        
    }

    //LS appends filenames and prints them out

    private void doLs(OutputStream out) throws IOException {
        File currentFile = new File(cwd.getAbsolutePath());
        File[] fileList = currentFile.listFiles();

        StringBuilder serverResponse = new StringBuilder();
        if (fileList != null && fileList.length > 0) {
            for (File file : fileList) {
                serverResponse.append(file.getName()).append("  ");
            }
            writeLine(out, serverResponse.toString());
        } else {
            writeLine(out, "Directory is empty.");
        }
    }

    private void doPwd(OutputStream out) throws IOException {
        writeLine(out, cwd.getCanonicalPath());
    }

    private void doCd(String requestedDir, OutputStream out) throws IOException {
        if (requestedDir.isEmpty()) {
            writeLine(out, "Usage: cd <dir>");
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
            writeLine(out, "Changed directory to " + cwd.getAbsolutePath());
        } else {
            writeLine(out, "Error: Directory not found");
        }
    }

    private void doMkdir(String newDirName, OutputStream out) throws IOException {
        File newDir = new File(cwd, newDirName);
        if (newDir.exists()) {
            writeLine(out, "Error: Directory already exists");
        } else if (newDir.mkdir()) {
            writeLine(out,  "Directory created successfully");
        } else {
            writeLine(out, "Error: Failed to create directory");
        }
    }

    private void doDelete(String filename, OutputStream out) throws IOException {
        File file = new File(cwd, filename).getCanonicalFile();
        if (file.exists() && file.delete()) {
            writeLine(out, "File deleted successfully");

        } else {
            writeLine(out, "Error: File not found or delete failed");
        }
    }

    private void put(String[] parts, InputStream byteIn, OutputStream byteOut) throws IOException {
        if (parts.length < 2) {
            writeLine(byteOut, "Error: Usage - put <filename>");
            return;
        }
        String filename = parts[1];
        File destination = new File(cwd, filename).getCanonicalFile();

        if (destination.exists()) {
            writeLine(byteOut, "File already exists");
            return;
        }

        String size = readLine(byteIn);
        long sizeFile;
        try {
            sizeFile = Long.parseLong(size.substring(5));
        } catch (NumberFormatException e) {
            writeLine(byteOut, "Invalid size format");
            return;
        }
        writeLine(byteOut, "OK");
        try (OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(destination))){
            byte[] buf = new byte[8192];
            long remaining = sizeFile;
            while (remaining > 0) {
                int n = byteIn.read(buf, 0, (int) Math.min(buf.length, remaining));
                if (n == -1) break; 
                fileOut.write(buf, 0, n);
                remaining -= n;
            }
            fileOut.flush();
        }
         writeLine(byteOut,  filename + "Saved successfully");
        
    }

    // decoding bytes for BufferedInputStream
    public String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break; 
            if (b != '\r') { 
                buffer.write(b);
            }
        }
        if (buffer.size() == 0 && b == -1) {
            return null; 
        }
        return buffer.toString("UTF-8");
    }

    // encoding lines for OutputStream
    private static void writeLine(OutputStream out, String line) throws IOException {
        out.write((line + "\n").getBytes("UTF-8"));
        out.flush();
    }

    
}



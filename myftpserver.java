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
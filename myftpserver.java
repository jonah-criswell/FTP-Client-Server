import java.io.*;
import java.net.*;

public class myftpserver {
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
}
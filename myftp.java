import java.io.*;
import java.net.*;
import java.util.Scanner;

public class myftp {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java myftp <host> <port>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (
            Socket socket = new Socket(host, port);
            BufferedInputStream byteIn = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream byteOut = new BufferedOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to server");
            System.out.print("myftp> ");

            while (scanner.hasNextLine()) {
                
                
                String command = scanner.nextLine();
                String[] parts = command.split(" ");
                String cmd = parts[0].toLowerCase();
                writeLine(byteOut, command);

                if (cmd.equalsIgnoreCase("quit")) {
                    break;
                }

                if (cmd.equals("put")) {
                     if (parts.length < 2) {
                        System.out.println("Usage: put <filename>");
                        continue;
                    }

                    File src = new File(parts[1]);
                    if (!src.exists() ) {
                        System.out.println("File not found does not exist " );
                        continue;
                    }

                    long size = src.length();
                    
                    writeLine(byteOut, "size " + size);

                    String response = readLine(byteIn);
                    if (!response.equals("OK")) {
                        System.out.println("Server error: " + response);
                        continue;
                    }

                    try (InputStream fileIn = new BufferedInputStream(new FileInputStream(src))) {
                        byte[] buf = new byte[8192];
                        int n;
                        while ((n = fileIn.read(buf)) != -1) {
                            byteOut.write(buf, 0, n);
                        }       
                        byteOut.flush();
                    }
                    String confirmation = readLine(byteIn);
                    System.out.println(confirmation);
                    System.out.print("myftp> ");
                    continue;
                } //put

                String response = readLine(byteIn);
                   switch (cmd) {

                        case "get": {                        
                        
                            if (!response.startsWith("OK ")) {
                                System.out.println("Server error: " + response); 
                                System.out.print("myftp> ");
                                break;
                            }

                            if (parts.length < 2) {
                            break;
                            }

                            String filename = parts[1];
                            long size = Long.parseLong(response.substring(3));

                            try (OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(filename))) {

                            byte[] buf = new byte[8192];
                            long fileSize = size;

                            while (fileSize > 0) {
                            
                            int toRead = (int) Math.min(buf.length, fileSize);
                            int n = byteIn.read(buf, 0, toRead);
                            if (n == -1) {
                                throw new EOFException("Server closed early during get");
                            }
                            fileOut.write(buf, 0, n);
                            fileSize -= n;
                                }
                                fileOut.flush();
                            }

                            System.out.println("Downloaded " + filename );    
                            System.out.print("myftp> ");
                            break;
                        }// get
                        
                    default:
                    System.out.println(response);
                    System.out.print("myftp> ");
                    break;
                    }
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // helper method to read line from BufferedInputStream
    private static String readLine(BufferedInputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
            if (b != '\r') buffer.write(b);
        }
        if (buffer.size() == 0 && b == -1) return null;
        return buffer.toString("UTF-8");
    }

    // helper method to write line to BufferedOutputStream
    private static void writeLine(BufferedOutputStream out, String line) throws IOException {
        out.write(line.getBytes("UTF-8"));
        out.write('\n');
        out.flush();
    }
}
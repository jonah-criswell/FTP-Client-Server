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
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to server");
            System.out.print("myftp> ");

            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                out.println(command);

                

                String response = in.readLine();
                System.out.println(response);
                if (command.equalsIgnoreCase("quit")) {
                    break;
                }
                System.out.print("myftp> ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
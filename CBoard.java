import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
 
public class CBoard {
    public static void main(String[] args) throws Exception {

        // System.out.println("Enter the IP address of a machine running the capitalize server:");
        String serverAddress = "localhost";//new Scanner(System.in).nextLine();
        Socket socket = new Socket(serverAddress, 4554);

        // Streams for conversing with server
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Consume and display welcome message from the server
        System.out.println(in.readLine());
        
        System.out.println(in.readLine());

        Scanner scanner = new Scanner(System.in);
        String readString = "";

        while (true) {

            //Get User Input
            System.out.println("\nEnter a string to send to the server (\"disconnect\" to quit):");
            String message = scanner.nextLine();
            //Check for request to end program.
            if (message.equals("DISCONNECT")) {
                break;
            }

            //Send users message to the server.
            out.println(message);

            //Print server response.
            try {
                readString = in.readLine();
                if (readString == null ){
                    break;
                }
            }catch(SocketException e ){
                System.out.println("Error99: Server has unexpectedly Disconnected. ");
                break;
            }
            while (true){
                System.out.println(readString);

                //Checks if buffer is empty before trying to read from it.
                if (in.ready()){
                    readString = in.readLine();
                } else {
                    break;
                }
            }
        }
        socket.close();
        scanner.close();
    }
}


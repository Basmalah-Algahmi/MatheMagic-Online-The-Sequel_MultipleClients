import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class ClientJPB1 {
    
    private static final int SERVER_PORT = 8765;

    public static void main(String[] args) {
        
        DataOutputStream toServer;   //send to server
        DataInputStream fromServer;  //get respond from the server
        Scanner input = new Scanner(System.in);
        
        String message, receivedMessage;
    
        System.out.print("You need to login in order to connect to the server by typing "
                 + "LOGIN follow by user name and password\n");
        
        
        try {
            Socket socket = 
                    new Socket("localhost", SERVER_PORT);
          
            //create input stream to receive data
            //from the server
            fromServer = 
                    new DataInputStream(socket.getInputStream());
            
            toServer =
                    new DataOutputStream(socket.getOutputStream());
            
            //keep sending command until the client enters quit or shutdown
            while(true)
            {
               System.out.print("Send command to server:\t");
               message = input.nextLine();
               toServer.writeUTF(message);
               //quit asking client to request, but if not quit, skip this if statment
               //and get the respond message from the server side
               if(message.equalsIgnoreCase("quit")) { 
                   break;
               }

               //received message:
               receivedMessage = fromServer.readUTF();
               System.out.println("Server says: " + receivedMessage);
               
               //if shurdown, break sending command loop
               if(message.equalsIgnoreCase("shutdown")) { 
                   break;
               }
            }            
        }
        
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch
        
    }//end main
}

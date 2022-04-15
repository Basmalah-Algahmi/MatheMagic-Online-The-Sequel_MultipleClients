
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;


public class MultiServerJPB1 {
    
    private static final int SERVER_PORT = 8765;
    // Vector to store active clients
    static Vector<ClientHandler> clients = new Vector<>();
    

    
    public static void main(String[] args) {
        
        //open file and read data to hashmap
       HashMap<String, String> loginsInfo = new HashMap<String, String>();
       String[] line;  //to store each client's login info  to array string
       
        try {
            File myObj = new File("logins.txt");
            Scanner myReader = new Scanner(myObj);  
            
            while (myReader.hasNextLine()) 
            {
              String data = myReader.nextLine();
              line = data.split(" ");
              loginsInfo.put(line[0],line[1]); //add to hashmap
            }
            myReader.close();
          } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }//end reading login data from a file to a hashmap
        
        //System.out.println(loginsInfo);   
        //createCommunicationLoop();
        createMultithreadCommunicationLoop(loginsInfo);
    }//end main
    
    public static void createMultithreadCommunicationLoop(HashMap<String, String> loginsInfo) {
        int clientNumber = 0;
        
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started on " + new Date() + ".");
            //listen for new connection request
            while(true) {
                Socket socket = serverSocket.accept();
                clientNumber++;  //increment client num
            
                //Find client's host name 
                //and IP address
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println("Connection from client " + 
                        clientNumber);
                System.out.println("\tHost name: " + 
                        inetAddress.getHostName());
                System.out.println("\tHost IP address: "+
                        inetAddress.getHostAddress());
                
                System.out.println("\tHost port address: "+
                        socket.getPort());
                
                //create and start new thread for the connection
                //is a class
                ClientHandler clientHandler=new ClientHandler("client" + clientNumber, socket, serverSocket,loginsInfo);
                Thread clientThread = new Thread(clientHandler);
                        
                // add this client to active clients list
                 clients.add(clientHandler);
 
                clientThread.start();  
            }//end while           
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        
    }//end createMultithreadCommunicationLoop
    
}
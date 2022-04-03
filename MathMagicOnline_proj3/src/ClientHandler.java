
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;



//Runnable class allows us to create a task
//to be run on a thread
public class ClientHandler implements Runnable {
    private Socket socket;  //connected socket
    private ServerSocket serverSocket;  //server's socket
    private int clientNumber;
    private HashMap<String, String> loginsInfo;
    
    //create an instance
    public ClientHandler(int clientNumber, Socket socket, ServerSocket serverSocket, HashMap<String, String> loginsInfo) {
        this.socket = socket;
        this.serverSocket = serverSocket;
        this.clientNumber = clientNumber;
        this.loginsInfo=loginsInfo;
    }//end ctor
    
    
    //run() method is required by all
    //Runnable implementers
    @Override
    public void run() {
        boolean authorizedUser=false;
        String[] clientCommand;
        String userName="", pwd;
        
        //run the thread in here
        try {
            DataInputStream inputFromClient =
                    new DataInputStream(socket.getInputStream());
            
            DataOutputStream outputToClient = 
                    new DataOutputStream(socket.getOutputStream());
            
            
            //continuously serve the client
            while(true) {                
               //login first
                String strReceived= inputFromClient.readUTF();
                clientCommand = strReceived.split(" ");//read client command to string array
                String command=clientCommand[0];

                if (!authorizedUser)
                {
                    if(command.equalsIgnoreCase("login"))
                    {
                        System.out.println("Sending login to client");
                        //only validate data if client sent the command as 
                        //login userName password
                        if (clientCommand.length==3)
                        {
                            userName=clientCommand[1];
                            pwd=clientCommand[2];

                            //check if username exist
                            if(loginsInfo.containsKey(userName))
                            {
                                //check if its is the right password for that usename
                                if(loginsInfo.get(userName).equals(pwd)){
                                    outputToClient.writeUTF("SUCCESS"); 
                                    authorizedUser=true;
                                }
                                else{
                                    outputToClient.writeUTF("FAILURE: Please provide correct username and password."
                                                              + " Try again."); 
                                }
                            }
                            else{
                                outputToClient.writeUTF("Your login info is incorrect"); 
                            }                       
                        }
                        //if length of array string is <3 or >3
                        else{
                            outputToClient.writeUTF("FAILURE: Please provide correct username and password."
                                                              + " Try again.");   
                        }
                    }//end validation client's login info
                    else{
                          outputToClient.writeUTF("You need to sign in first");  
                    }
                }//end login command 
                
                //handle solve command
                else{
                    if(command.equalsIgnoreCase("solve")) 
                    {
                        System.out.println("Sending solve to client");
                        //get solve command details
                        if(clientCommand.length>1)
                        {
                            String shape= clientCommand[1];
                            float radius, circumference,side1, side2,perimeter, area;

                            //create a solve file 
                            File solveFile = new File(userName+"_solutions.txt");

                            if(!solveFile.exists()){
                                    System.out.println("File created: " + solveFile.getName());
                                    solveFile.createNewFile();
                            }
                            PrintWriter write = new PrintWriter(new FileWriter(solveFile, true));

                            if(shape.equalsIgnoreCase("-c"))
                            {
                                if(clientCommand.length==3)
                                {
                                    radius= Float.parseFloat(clientCommand[2]);
                                    circumference= (float)(Math.PI*2*(radius));
                                    area= (float) (Math.PI * Math.pow(radius, 2));

                                    write.append(constructCircleMessage(radius,circumference,area));
                                    outputToClient.writeUTF(constructCircleMessage(radius,circumference,area));            
                                }
                                else
                                {
                                    write.append(constructSolveErrorMessage());
                                    outputToClient.writeUTF(constructSolveErrorMessage());
                                }  
                            }//end circle solve 
                            else if(shape.equalsIgnoreCase("-r"))
                            {
                                if(clientCommand.length==3)
                                {
                                    side1= Float.parseFloat(clientCommand[2]);
                                    perimeter= (float)(4 * side1);
                                    area= (float) (Math.pow(side1, 2));

                                    write.append(constructRecMessage(side1,side1,perimeter,area));
                                    outputToClient.writeUTF(constructRecMessage(side1,side1,perimeter,area));                          
                                }
                                else if(clientCommand.length==4)
                                {
                                    side1= Float.parseFloat(clientCommand[2]);
                                    side2= Float.parseFloat(clientCommand[3]);
                                    perimeter= (float)(2*(side1+side2));
                                    area= (float) (side1*side2);

                                   write.append(constructRecMessage(side1,side2,perimeter,area));
                                    outputToClient.writeUTF(constructRecMessage(side1,side2,perimeter,area));                            
                                }
                                else
                                {
                                    write.append(constructSolveErrorMessage());
                                    outputToClient.writeUTF(constructSolveErrorMessage());
                                }     
                            }//end rec solve
                            write.close();
                        }
                        
                        else{
                            outputToClient.writeUTF("Error, no shape found\n");
                        }   
                    }
                    //end solve command
                    
                    //handle list command
                    else if(command.equalsIgnoreCase("list")) 
                    {
                      System.out.println("Sending LIST to client");
                      //append all data to string array then send it to the client
                      String fileContent="";
                       
                       if(clientCommand.length==1)
                       {
                           File myObj = new File(userName+"_solutions.txt");
                           fileContent+= userName+"\n";
                                       
                           if (myObj.exists())
                           {
                                fileContent=readSolutionFile(myObj,fileContent);
                           } 
                            else{
                                    fileContent+= "No interactions yet\n";
                            }  
                           outputToClient.writeUTF(fileContent);
                                                  
                       }
                        else if(clientCommand.length==2)
                        {
                            if (clientCommand[1].equalsIgnoreCase("-all"))
                            {
                                if(userName.equals("root"))
                                {
                                    for (String userFile : loginsInfo.keySet())
                                    {
                                        //go over keys in hashmap,open file for each
                                        //get content of the file and append it to the string
                                        //finally, send the string to client side                               
                                        File myObj = new File(userFile+"_solutions.txt");                                 
                                        fileContent+= userFile+"\n";
                                        if (myObj.exists()){
                                            fileContent=readSolutionFile(myObj,fileContent);
                                        } 
                                        else{
                                            fileContent+= "No interactions yet\n";
                                        }   
                                }   
                                outputToClient.writeUTF(fileContent);                              
                                }
                                else{
                                    System.out.println("Error: you are not the root user\n");
                                    outputToClient.writeUTF("Error: you are not the root user\n");
                                }    
                            }
                            else {
                                System.out.println("Unknown command received: " 
                                    + strReceived);
                                outputToClient.writeUTF("300 invalid command");
                            }                         
                       }   
                       else {
                            outputToClient.writeUTF("300 invalid command");
                            }
                    }//end list command   
                    
                    /*Message command 
                     * Clients will be able to send each other messages through the server
                     * 
                     * */
                    else if(command.equalsIgnoreCase("message"))
                    {
                    	System.out.println("Test");
                    	outputToClient.writeUTF("Test"); 
                    	 if(loginsInfo.get(userName).equals("john"))
                    	 {
                    		 
                    		 System.out.println("Message from client\n" + "Sending to " + userName /* + user*/);
                    		 outputToClient.writeUTF("Message from client\n" + "Sending to " + userName /* + user*/);
                    		 break;
                    	 }
                    }
                
                //handle logout command
                //server still on, the only thing will happened when client logout is that
                // this client wont be authorized any more
                else if(command.equalsIgnoreCase("logout")) 
                {
                    System.out.println("logging out the client\n");
                    outputToClient.writeUTF("200 OK");
                    authorizedUser=false; 
                }//end logout command
                
                //handle shutdown command//both server and client will be Terminated
                else if(command.equalsIgnoreCase("SHUTDOWN"))
                {
                    System.out.println("\nShutting down server...");
                    outputToClient.writeUTF("\n200 OK...");
                    serverSocket.close();//close the server
                    socket.close();      //then close the client
                    break;  //get out of loop
                }//end shutdown command
                
                //handle all invalid command
                else {
                    System.out.println("Unknown command received: " 
                        + strReceived);
                    outputToClient.writeUTF("300 invalid command");
                    }
                } 
            }//end server while loop
            
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }//end try-catch
        
    }//end run
    
    
    
    
    public static String  constructCircleMessage(float radius, float circumference, float area) {
        return "radius "+radius+": Circle’s circumference is "+ String.format("%.2f", circumference)
                + " and area is "+ String.format("%.2f" ,area)+"\n";
                                
    }
    public static String  constructRecMessage(float side1, float side2,float perimeter, float area) {
        return "Side "+side1+" "+side2+": Rectangle’s perimeter is "+
                String.format("%.2f", perimeter)
                + " and area is "+ String.format("%.2f" ,area)+"\n";                        
    }
    
    public static String  constructSolveErrorMessage(){
        return "Error: No sides found/exceed accabtable number of sides\n";
    }
    
    public static String  readSolutionFile(File myObj, String fileContent) throws FileNotFoundException{
        Scanner myReader = new Scanner(myObj);
        System.out.println(myObj.exists());
        while (myReader.hasNextLine()) 
        {
             String data = myReader.nextLine();
             fileContent+=data+"\n";
        }
        myReader.close();
    
        return fileContent;                    
    }



    
    
}//end ClientHandler



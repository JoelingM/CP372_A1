/**
 * @author : Austin bursey
 * @date: 1/20/2019
 * @Last Updated: 1/20/2019
 * @Purpose: CP372 A1 , communicates with client.
 * @see A1_2019 CP367
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
public class SBoard{
    //Board Instance variables. Only one instance of Sboard is created at a time.
    //PRIVATE VARIABLES 
    private static SBoard single_instance = null; 
    private int portNum; 
    private ArrayList<Note> noteArr = new ArrayList<Note>();
    private ArrayList<Point> AllPins = new ArrayList<Point>(); 
    //PUBLIC VARIABLES
    public ArrayList<String> colors = new ArrayList<String>();
    public int width ; 
    public int height;    
    
    //CONSTRUCTORS
    private SBoard(final int portNum , final int width , final int height,final  ArrayList<String> colors){
        this.portNum = portNum; 
        this.width = width; 
        this.height = height; 
        int i = 0 ; 
        int n = colors.size();
        while(i < n ){
            this.colors.add(colors.get(i));
            i++;
        }
    }
    //FUNCTIONS 
    /**
     * @return Returns colors accepted on the board. 
     */
    private String getColors(){
        String colorsAvail = "";
        int n = colors.size(); 
        for(int i = 0 ; i <n; i ++  ){
            colorsAvail = colorsAvail.concat(colors.get(i));
            if (i != n -1 ){
                colorsAvail = colorsAvail.concat(" ");
            }
        }
        return colorsAvail; 
    }

    private synchronized ArrayList<Point> getPins(){
        ArrayList<Point> results = new ArrayList<Point>(); 
        for(Point pin : AllPins){
            results.add(new Point(pin.getX(), pin.getY()));
        }

        return results; 
    }

    private synchronized ArrayList<Note> get(String userInput){

        if (noteArr.isEmpty()){
            return null; 
        }

        ArrayList<Note> results = new ArrayList<Note>();
        

        int arg = 1; 
        boolean colorFound = false; 
        boolean refersFound = false; 
        boolean containsFound = false; 
        String color =""; 
        Point contain = new Point(-999, -999); 
        String refersTo=""; 
        if (userInput.contains("color")){
            arg ++; 
            colorFound = true; 
        }
        if (userInput.contains("refersTo")){
            arg++;
            refersFound = true; 
        }
        if (userInput.contains("contains")){
            arg++;
            containsFound = true; 
        }
        String[] data = userInput.split("color");

        String s1= "" ;
        for (String sub : data){
            s1 = s1.concat(sub);
        }
        String[] data1 = s1.split("contains");
        
        String s2 ="";
        for (String sub : data1){
            s2= s2.concat(sub);
        }
        String[] data2 = s2.split("refersTo");

        String s3 = "";
        for (String sub : data2){
            s3= s3.concat(sub);
        }
        String[] data3  = s3.split("=",arg);

        //preprocessing data done


        if (colorFound && containsFound && refersFound){// true true true 
            color = data3[1];
            String[] datac = data3[2].split(" ");
            contain = new Point(Integer.parseInt(datac[0]),Integer.parseInt(datac[1]) );
            refersTo = data3[3];
            //loop
        }else if (colorFound && containsFound){ // true true false// 
            color = data3[1]; 
            String[] datac = data3[2].split(" ");
            contain = new Point(Integer.parseInt(datac[0]),Integer.parseInt(datac[1]) );        

        }else if(colorFound && refersFound){ // true false true 
            color = data3[1]; 
            refersTo = data3[2];

        }else if(containsFound && refersFound){//false true true 
            String[] datac = data3[1].split(" ");
            contain = new Point(Integer.parseInt(datac[0]),Integer.parseInt(datac[1]) );
            refersTo = data3[2];            

        }else if (refersFound){//false false true 
            refersTo = data3[1];

        }else if (containsFound){//false true false 
            String[] datac = data3[1].split(" ");

            contain = new Point(Integer.parseInt(datac[0]),Integer.parseInt(datac[1]) );

        }else if(colorFound){
            color = data3[1]; 

        } 



        //searching 
        int i = 0; 
        int n = noteArr.size();
        while (i < n ){
            Note current = noteArr.get(i);
            boolean validNote = true ; 
            if (colorFound && !current.color.equals(color)){
                validNote = false  ; 
            }
            /*
            Boolean x1 = current.xBeg >contain.getX();
            System.out.println(x1);
            Boolean x2 = current.xEnd <contain.getX();
            System.out.println(x2);
            Boolean x3 = current.yBeg > contain.getY();
            System.out.println(x3);
            Boolean x4 = current.yEnd < contain.getY();
            System.out.println(x4);
            Boolean x5 =  !(current.xBeg > contain.getX() || current.xEnd < contain.getX() || current.yBeg > contain.getY() || current.yEnd < contain.getY());
            System.out.println(x5);     
            */       
            if (containsFound && (current.xBeg > contain.getX() || current.xEnd < contain.getX() || current.yBeg > contain.getY() || current.yEnd < contain.getY())){
                validNote = false; 
            }
            if (refersFound && !current.content.contains(refersTo)){
                validNote = false; 
            }
            if (validNote){
                results.add(new Note(current.xBeg , current.yBeg , current.xEnd - current.xBeg , current.yEnd - current.yBeg, current.color , current.content ));
            }

            i ++;

        }
        return results;
    }
    /**
     * Pins all the notes that have intersects with PIN coordinates
     * @author Austin Bursey 
     * @param x coordinate for pinning 
     * @param y coordinate for pinning 
     */
    private synchronized int pin(String userInput){
        String[] data = userInput.split(",");
        Point newPin = new  Point(Integer.parseInt(data[0]),Integer.parseInt(data[1]));
        int amtPinned = 0; 
        if (AllPins.indexOf(newPin) == -1 ){

            AllPins.add(newPin);
        }
        int x = newPin.getX();
        int y = newPin.getY();
        int i = 0; 
        int n = noteArr.size();
        while (i < n && noteArr.get(i).xBeg <=  x){

            Note current = noteArr.get(i); 
            if (current.xEnd >= x && current.yEnd >= y  && current.yBeg <= y ){

                ArrayList<Point> pinArr = current.pinned; 
                if (current.pinned == null ){
                    current.pinned = new ArrayList<Point>();
                    current.pinned.add(newPin);
                    amtPinned ++;
                }else if (pinArr.indexOf(newPin) == -1 ){
                    current.pinned.add(newPin);
                    amtPinned ++;
                }
            }
            i ++;
        }
        
        return amtPinned; 
    }
    /**
     * Unpins all the notes that have intersects with UNPIN coordinates
     * @author Austin Bursey 
     * @param x coordinate for pinning 
     * @param y coordinate for pinning 
     */
    private synchronized  int  unpin(String userInput ){
        String[] data = userInput.split(",");
        Point targetPin = new Point(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
        int unPinned = 0;
        if (AllPins.indexOf(targetPin) != -1){
            AllPins.remove(targetPin);

            int x = targetPin.getX();
            int y = targetPin.getY();
            int i = 0; 
            int n = noteArr.size();
            //int removedNum = 0; deprecated 
            while (i < n && noteArr.get(i).xBeg <= x  ){
                Note current = noteArr.get(i); 
                if (current.xEnd >= x && current.yEnd >= y && current.yBeg <= y ){
                    ArrayList<Point> pinArr = current.pinned; 
                    if (pinArr.indexOf(targetPin)!= -1 ){
                        current.pinned.remove(targetPin);
                        unPinned++;
                        //removedNum ++; deprecated
                    }               
                }
                i ++;
            }
        }else {
            unPinned = -1; 
        }
        return unPinned;
    }
    /**
     *@author Austin Bursey 
     */
    private synchronized int clear(){
        int n = 0; 
        int i = noteArr.size() -1; 
        int amtCleared = 0;
        if (i != -1 ){         
            while (i >= n ){
                if (noteArr.get(i).pinned == null  ||   noteArr.get(i).pinned.isEmpty()){
                    noteArr.remove(i);
                    amtCleared ++; 
                }
                i --;
            }
        }else{
            amtCleared = -1;
        } 
        return amtCleared;
    }
    /**
     * @author Austin Bursey 
     * @param post the <POST> String Given by the client 
     * 
     * @return
     */
    private synchronized String post(final String post){
        String[] data = post.split("\\s", 6); 
        String receipt = "ERROR\n================\nYour Post was Unsuccessful";
        Note newNote = new Note(Integer.parseInt(data[0]), Integer.parseInt(data[1]),
        Integer.parseInt(data[2]), Integer.parseInt(data[3]), data[4] , data[5]); 
        int i = 0; 
        int n = noteArr.size();

        //compareTo != -1 when xBeg < newNote.xBeg
        while (i < n && noteArr.get(i).compareTo(newNote)!= -1){
            i++;
        }

        if (i < n ){
            noteArr.add(i , newNote); 
            receipt = String.format("NOTE SUCCESSFULLY ADDED \n===============\nX= %d, Y= %d \nWidth= %s Height= %s\nColor= %s\nContent= %s\n",
            newNote.xBeg, newNote.yBeg,data[2] ,data[3],newNote.color, newNote.content);

        }else if (noteArr.add(newNote)) {
            receipt = String.format("NOTE SUCCESSFULLY ADDED \n===============\nX= %d \nY= %d \nWidth= %s\nHeight= %s\nColor= %s\nContent= %s\n",
             newNote.xBeg, newNote.yBeg,data[2] ,data[3],newNote.color, newNote.content);
        }
        return receipt; 
    }
    //INNER CLASSES 
    /**
     * @Params: xBeg = bottom left x coordinate of Note , yBeg bottom left  y coordinate of Note 
     *  @author : Austin Bursey 
     * 
     */
    private  class Note implements Comparable<Note> {
        // Note variables 
        private int xBeg ; 
        private int yBeg ; 
        private int yEnd; 
        private int xEnd; 
        private String color; 
        private String content; 
        private ArrayList<Point> pinned;  

        private Note(int xBeg , int yBeg ,int  width ,int  height , String color, String content ){
            //initializing variables
            this.xBeg = xBeg;
            this.yBeg = yBeg; 
            this.xEnd = xBeg + width; 
            this.yEnd = yBeg + height; 
            this.color = color; 
            this.content = content; 

        }
        @Override
        public int compareTo(final Note rs) {
            int order =0; 
            if (this.xBeg < rs.xBeg){
                order = 1; 
            }else if (this.xBeg > rs.xBeg){
                order = -1; 
            }

            return order; 
        }

    }

    private class   Point{
        private int x; 
        private int y; 
        private Point(int x , int y ){
            this.x = x; 
            this.y = y; 
        }
        public int getX(){
            return this.x;
        }
        public int getY(){
            return this.y; 
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 3;
            result = prime * result + this.x;
            result = prime * result + this.y;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj){
                return true;
            }else if (obj == null){
                return false;
            }else if (getClass() != obj.getClass()){
                return false;
            }
            Point other = (Point) obj;
            if (x != other.x || y != other.y ){
                return false;
            }
            return true;
        } 
    }
    /**
     * @param : socket = the socket server is currently using. board = the board the server is currently using.
     * Purpose : A private thread that handles the connection between Client and Server 
     * Clients can POST,  GET, PIN , UNPIN , CLEAR , DISCONNECT
     * Clients must use the disconnect command to disconnect 
     * @author: Austin Bursey
     */
    private static class Connector extends Thread{
        //socket variables
        private Socket socket;
        private SBoard board; 
        private int clientNum;
        public Connector (Socket socket,  SBoard board, int clientNum){
            this.socket = socket;
            this.board = board;
            this.clientNum = clientNum; 
        }

        public void run(){

            try{
                //initialize ways to talk to the client. 
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter clientOut = new PrintWriter(socket.getOutputStream(),true);
                //creating string of available colors 
                String colorsAvail = board.getColors();

                //Begin interaction with client 

                clientOut.printf("Acceptable Note colors are | %s\n",colorsAvail); 
                clientOut.printf("Board Dimensions : Width = %d , Height = %d\n", board.width , board.height);
                
                String input =  clientIn.readLine();
                while (true){
                    if (input == null || input.equals("DISCONNECT")){
                        break;
                    }
                    //System.out.println(input);
                    String[] userInput= input.split(" ", 2); 

                    if (userInput[0].equals("GET") && userInput.length == 1){
                        
                        ArrayList<Note>  result = board.get(" ");       
                             
                        if (result == null  ){
                            
                            clientOut.println("Error01");
                        }else if (result.size() > 0 ){ // checks to make sure theres results to print otherwise print message letting user know there search came up empty 
                            int i = 1; 
                            for (Note current : result){
                                clientOut.printf("======NOTE : %d======\nX = %d , Y= %d\n", i, current.xBeg , current.yBeg); 
                                
                                clientOut.printf("Width = %d , Height = %d\n" , current.xEnd - current.xBeg, current.yEnd -current.yBeg);
                                clientOut.printf("Color = %s\n", current.color);
                                clientOut.printf("Content = %s\n", current.content);  
                                i ++;
                            }
                        }
                        else {
                            clientOut.println("No Notes Match your criteria.");
                        }

                    }else if (userInput[0].equals("GET") && !userInput[1].equals("PINS")){

                        ArrayList<Note> result = board.get(userInput[1]);

                        if (result == null  ){
                            clientOut.println("Error01");
                        }  
                        else if (result.size() > 0 ){ // checks to make sure theres results to print otherwise print message letting user know there search came up empty 
                            int i = 1; 
                            for (Note current : result){
                                clientOut.printf("======NOTE : %d======\nX = %d , Y= %d\n", i, current.xBeg , current.yBeg); 
                                
                                clientOut.printf("Width = %d , Height = %d\n" , current.xEnd - current.xBeg, current.yEnd -current.yBeg);
                                clientOut.printf("Color = %s\n", current.color);
                                clientOut.printf("Content = %s\n", current.content);  
                                i ++;
                            }
                        }
                        else {
                            clientOut.println("No Notes Match your criteria.");
                        }                        
                           
                    }else if (userInput[0].equals("POST")){
                        String receipt = board.post(userInput[1]); 
                        clientOut.println(receipt); 
                    }else if(userInput[0].equals("PIN")){
                        int amtPinned = board.pin(userInput[1]);
                        clientOut.printf("%d Sticky Notes pinned.\n", amtPinned);
                    }else if(userInput[0].equals("UNPIN")){
                        int unPinned = board.unpin(userInput[1]);
                        if (unPinned == -1 ){
                            clientOut.printf("Error60\n");
                        }else {
                            clientOut.printf("%d Sticky Notes un-pinned.\n", unPinned);
                        }
                    }else if(userInput[0].equals("CLEAR")){
                        int amtCleared = board.clear();
                        if (amtCleared != -1 ){
                            clientOut.printf("%d Sticky Notes cleared.\n", amtCleared);
                        }else{
                            clientOut.printf("Trying to clear on Empty Board\n");
                        }
                        

                    }else if(userInput[0].equals("DISCONNECT")){
                        break; 
                    }else if(userInput[0].equals("GET") && userInput[1].equals("PINS")){
                        ArrayList<Point> results = board.getPins();
                        if (results.size() > 0 ){ // checks to make sure theres results to print otherwise print message letting user know there search came up empty 
                            int i = 1; 
                            for (Point current : results){
                                clientOut.printf("======Pin: %d======\nX = %d , Y= %d\n", i++, current.getX() , current.getY());  
                            }
                        }
                        else {
                            clientOut.println("No Pins are currently on the board.\n");
                        }

                    }else {
                        clientOut.println("Invalid Input.\n");
                    }
                    input = clientIn.readLine(); 
                }
               
            }catch(IOException e ){
                System.out.println("Error handling client# " + clientNum + ": " + e);
            }finally{
                try{
                    socket.close();
                }catch(IOException e ){
                    System.out.println("Error closing socket"); 
                }
            }
        };

    }

    public static void main (String args[]) throws Exception{
        System.out.println("The Board is currently running.");
        ArrayList<String> colors = new ArrayList<String>(); 
        int n = args.length;
        int clientNum= 0; 
        int i = 3; //i = 3 since args[0] = portNum  , args[1] = width , args[2] = height
        while (i < n ){
            colors.add(args[i].toUpperCase());
            i ++; 
        }
        
        SBoard board = new SBoard(Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]), colors );
        ServerSocket listener = new ServerSocket( board.portNum);//creates serversocket at portNum 
        try {
            
            while (true) {
                new Connector(listener.accept(), board, ++clientNum).start();
            }
        } finally {
            listener.close();
        }    
    }
}
package Tweets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.*;  
import java.net.*;
import java.sql.Date;

public class TweetGen {
	private static final int SLEEP_TIME = 1000;       //Sleep time between tweets in ms

    public static void main(String[] args) throws IOException, InterruptedException {
    	ServerSocket ss=new ServerSocket(6666);  
    	Socket s=ss.accept(); //establishes connection
    	
    	DataOutputStream out=new DataOutputStream(s.getOutputStream());  
    	  
    	
        String csvFile = "src/Tweets.txt";	//"/home/ubuntu/Tweets.txt";   
        String line = "";
        String cvsSplitBy = "\t";
        int counter=0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {
            	if(counter>0)
            		out.writeUTF(line);
                counter++;
                 Thread.sleep(SLEEP_TIME);
                out.flush();
                    
            }
            s.close();
            ss.close();

        } catch (IOException e) {
        	if (e.getMessage().equals("Connection reset by peer: socket write error")) {
        		System.out.println("Lost connection to tweet server.");
        	} else {
            e.printStackTrace();
        	}
        }
    }
}
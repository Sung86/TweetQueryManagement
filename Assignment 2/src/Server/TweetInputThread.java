/*
 * Jagmeet Grewal, Matthew Vince, Josh Oxford and Sung Phang
 * 444648, 034097, 484088, 481605
 * TweetInputThread 
 * Desc: Thread that handles the continuous stream of tweets
 */


package Server;
import java.util.LinkedList;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.IOException;
import Worker.*;

public class TweetInputThread  extends Thread {

	private static final String IP = "127.0.0.1";        //The IP of the tweet gen server.
	private static final int PORT = 6666;                //The port number of the tweet gen server
	private static final int MAX_INBOX_SIZE = 3000;      //The maximum size of the tweets that can be stored.  

	private static LinkedList<Tweet> inbox = new LinkedList<Tweet>();
	private static Object lockLink = new Object(); //

	private Socket socket;
	private DataInputStream input;


	/**
	 * TweetInputThread()
	 */
	public TweetInputThread(LinkedList<Tweet> i, Object l){
		inbox = i;
		lockLink = l;
	}

	//Connects to the Tweet Generator
	private void setup() throws IOException{
		System.out.println("Attempting to connect to Tweeet Gen Server: " + IP + ": " + PORT + "...");
		socket = new Socket(IP, PORT);
		System.out.println("You are connected to Tweet Gen Server: " + IP + ": " + PORT);

		input = new DataInputStream(socket.getInputStream());
	}


	/*
	 * Listens for string coming from TweetGen to read tweet
	 */
	private void listen() throws IOException{
		String incoming = "";
		Tweet tweet;

		while(true){
			incoming = input.readUTF();

			synchronized(lockLink) {
				//If inbox isnt full, add tweet to tweet inbox 
				if (inbox.size()==MAX_INBOX_SIZE) {
				} else {

					tweet = createTweet(incoming);
					inbox.add(tweet);
					System.out.println("Tweet: "+tweet.getTweet_id() +" "+ tweet.getAirline() +" "+ tweet.getName() +" "+ tweet.getTweet_created());   
				}	
				lockLink.notify();
			}	

			//Wait 1/2 second
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Makes a tweet object from stream
	 * Input: Tweet stream from generator
	 * Output: Tweet Object
	 */
	private Tweet createTweet(String incoming){
		Tweet tweet;

		String[] array = incoming.split("\t");
		tweet = new Tweet(array[0], array[5], array[7], array[10], array[12], array[1], array[9]);

		return tweet;
	}


	public void run(){
		try {
			setup();
			listen();
		}
		catch (IOException e) {
			e.printStackTrace();
		};	
	}
}

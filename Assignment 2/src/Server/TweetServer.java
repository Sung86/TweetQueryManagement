/*
 * Jagmeet Grewal, Matthew Vince, Josh Oxford and Sung Phang
 * 444648, 034097, 484088, 481605
 * TweetServer
 * Desc: Main Tweet Server
 */

package Server;

import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;


import Worker.*;


public class TweetServer {

	private static final boolean DEBUG = false;
	private static final int PORT = 7000; 													 //This is the port the server will use.
	private static final String CLIENT_ID = "CLIENT";										 //Used to identify client programs
	private static final String WORKER_ID = "WORKER";										 //Used to identify worker programs
	private static final int NUM_OF_WORKERS = 6;               								 //The number of workers that will be running.

	private static LinkedList<Tweet> inbox = new LinkedList<Tweet>();                        //The list of tweets from the tweet gen.
	private static LinkedList<Job> query = new LinkedList<Job>();                            //The list of queries from the client
	private static Object queryLock = new Object();                                          //The lock to querylist
	private static Object lockLink = new Object();										     //This will lock the linklist

	static Hashtable<Integer, Job> container = new Hashtable<Integer, Job>();                //The hash table that holds the completed jobs.
	static Object lockHash = new Object();                                                   //This will lock the hashtable when using.
	static WorkerDetails[] workers = new WorkerDetails[NUM_OF_WORKERS];                      //Array to hold the workerdeatils of workers
	static int workersConnected = 0;                                                         //The number of workers connected
	static Object workersConnectedLock = new Object();                                       //Allows the workersConnected to be locked
	static LinkedList<Job> responses = new LinkedList<Job>();                                //The linklist that will hold parts of a muilti worker job
	static Object responsesLock = new Object();                                              //Locks the response list
	static Hashtable<String, String> users = new Hashtable<String, String>();                //The hashtable of users
	private static Object jobIdLock = new Object();                                          //The lock for jobID


	private static DataInputStream input;                                                    //The stream that will be receiving the data

	/**
	 * The main method for the server program.
	 * @param args
	 */
	public static void main(String[] args) {
		ServerSocket serverSocket; 		                                    //This is the socket that will be listening for the client.
		Socket newSocket;                                                	//This is the socket that will be streaming the time back to the client.
		TweetServerClientThread tweetServerClientThread;                    //This is the thread that will handle to client communication.
		TweetInputThread tweetInputThread;
		ServerWorkerListener serverWorkerListener;                          //This will handle communication to the worker programs                
		TweetWorkerHandlerThread tweetWorkerHandlerThread;                  //This will start the logic of the worker handler.
		AggregateResults aggregateResultsThread;
		String incoming;                                                    //The String that will hold the connecting programs ID                                           //How many workers are connected
		workers[0] = null;

		try {

			serverSocket = new ServerSocket(PORT);
			System.out.println("Tweet Server is ready...");

			//Start thread to get Tweets from Gen
			tweetInputThread = new TweetInputThread(inbox, lockLink);
			tweetInputThread.start();

			//Start Worker Handler to start worker and send jobs
			tweetWorkerHandlerThread = new TweetWorkerHandlerThread(inbox, lockLink);
			tweetWorkerHandlerThread.start();

			//Start Thread that aggregate results
			aggregateResultsThread = new AggregateResults();
			aggregateResultsThread.start();

			while(true) {

				//Incoming Connection
				newSocket = serverSocket.accept(); 

				input = new DataInputStream(newSocket.getInputStream());

				incoming = input.readUTF();

				//Starts the client or worker communicating thread as required.
				if (incoming.equals(CLIENT_ID)) {			
					tweetServerClientThread = new TweetServerClientThread(newSocket, query, queryLock,input, container, jobIdLock); // newJob, newJobLock);				
					tweetServerClientThread.start();			
				} else if ((incoming.equals(WORKER_ID)) && (workersConnected < NUM_OF_WORKERS)) {		
					workers[workersConnected] = new WorkerDetails(workersConnected, newSocket);
					workers[workersConnected].setWorkerId(workersConnected);
					workers[workersConnected].setInputStream(new ObjectInputStream(newSocket.getInputStream()));
					workers[workersConnected].setOutputStream(new ObjectOutputStream(newSocket.getOutputStream()));
					serverWorkerListener = new ServerWorkerListener(workers[workersConnected]);
					System.out.println("Worker connected: ID " + workersConnected);
					synchronized(workersConnectedLock) {
						workersConnected++;
						workersConnectedLock.notify();
					}
					serverWorkerListener.start();

				} else {
					input.close();
					newSocket.close();
				}


			}			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}


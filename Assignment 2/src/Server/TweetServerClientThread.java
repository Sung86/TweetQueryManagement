/*
 * Jagmeet Grewal, Matthew Vince, Josh Oxford and Sung Phang
 * 444648, 034097, 484088, 481605
 * TweetServerClientThread 
 * Desc: Thread that handles Server Client communication
 */


package Server;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import Worker.*;

public class TweetServerClientThread extends Thread {

	private static final boolean DEBUG = false;          //If true shows the debug messages

	private Socket clientSocket;				   		//The socket that the client is connected to
	private DataInputStream input;   			  		//The stream that will be receiving data
	private DataOutputStream output;   			    	//The stream that will sending the time of the server
	private String request;                             //The request from the client
	private static int jobId = 0;                       //The id of the job
	private static Object jobIdLock;                    //The lock for jobId





	//The constructor for the new thread
	public TweetServerClientThread(Socket s, LinkedList<Job> i, Object l, DataInputStream is, Hashtable<Integer, Job> c, Object jil) { 	
		clientSocket = s;
		input = is;
		jobIdLock = jil;
	}

	/**
	 * send()
	 * Sends a string in the format of UTF to the client program.
	 * @param message
	 */
	private void send(String message) {
		try {
			output.writeUTF(message);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * closeConnection()
	 * Closes the connection to the client program.
	 */
	private void closeConnection() {
		System.out.println("Terminating connection to client: " + clientSocket);
		try {
			input.close();
			output.close();
			clientSocket.close();
			System.out.println("Connection to client " + clientSocket + " closed.");
		} catch (IOException e) {
			System.out.println("Failed to close client " + clientSocket + " connection.");
			e.printStackTrace();
		}		
	}

	/**
	 * Sends back the job results of a job based on the passcode.
	 * @param s
	 */
	private void getResult(String s) {
		Job job = new Job(-1, request, false);                        //The job that will be used to store the looked up job.
		//The passcode that will identify the clients job.
		String result = "";
		if (!(s.equals("backBtn") || s.equals("cancel") || s.equals("result"))) {
			int jobId = Integer.parseInt(s); 
			if (DEBUG) t("in getResult");

			//Checks if job is finished, if so send result or send Query still in process
			synchronized(TweetServer.lockHash) {
				if (DEBUG) t("in locklink lock: " + jobId);
				if (TweetServer.container.containsKey(jobId)) {
					if (DEBUG) t("Before getting job: ");
					job = TweetServer.container.get(jobId);
					if (DEBUG) t("Got job from hash table");
					TweetServer.lockHash.notify();
					result = job.getResult() + "";
					if (DEBUG) t("----------->"+result);
				} else {
					result = "Query is still in process";					
				}
			}

			send(result);
		}
	}

	/**
	 * genPassword()
	 * This generates a password from the users name.
	 * @param name : The name that the user gives as a user name.
	 * @return long : this will be the password for the user.
	 */
	private long genPassword(String name) {
		long pass = 0;
		int time = (int) (new Date().getTime()/1000);
		char[] code = name.toCharArray();

		for (int i=0; i<code.length; i++) {
			pass = pass + code[i];
		}

		pass = (pass * time) % 10000000;
		if (pass < 0) {
			pass = pass * -1;
		}
		return pass;
	}

	/**
	 * register()
	 * This checks to see if the user name is already used and if not sends a password.
	 * @param username : This is the username the user want to sign up with.
	 */
	private boolean register(String username) {
		Long password;

		if (!(TweetServer.users.containsKey(username))) {	

			password = genPassword(username);
			send("valid");
			send(password+"");
			TweetServer.users.put(username, password+"");
			return true;
		} else {
			send("invalid");
			return true;
		}
	}

	/**
	 * login()
	 * This will check to see if the username and password match.
	 * @param userDetails : This is what the user entered into the client.
	 */
	private boolean login(String userDetails) {
		String[] array;
		array = userDetails.split(",", 2);
		if(DEBUG) t("in login check");
		if ((TweetServer.users.containsKey(array[0])) && (array[1].equals(TweetServer.users.get(array[0])))) {						
			send("valid");
			return false;
		} else {
			send("invalid");
			return true;
		}

	}

	/**
	 * t()
	 * This is used for debugging by printing out data, shorthand for System.out.println()
	 * t is short for test
	 * @param o : The object you want to print to console
	 */
	private void t(Object o) {
		System.out.println(o+"");
	}

	/**
	 * startSession()
	 * This starts the session for the client by either creating a account or logging in
	 * @param input : This is the data stream that is listening to the client.
	 * @throws Exception : This is for the reading in of data from the client.
	 */
	private String startSession() throws Exception {
		String username = "";
		String[] array;
		boolean running = true;

		while (running) {
			username = input.readUTF();
			array = username.split(":", 2);
			if(DEBUG) t("" +  username);
			switch (array[0]) {
				case "register": 
					running = register(array[1]);
					break;
				case "login":   
					running = login(array[1]);
					break;
			}
		}
		return username;
	}

	/**
	 * jobId()
	 * This make a job id for each new job.
	 * @return int : the job id for the new job.
	 */
	private int jobId() {
		int job;

		synchronized(jobIdLock) {
			jobId = jobId + 1;
			job = jobId;
			jobIdLock.notify();
		}
		if(DEBUG) t("JobId: " + jobId + ", job: " + job);
		return job;
	}

	/**
	 * tweetSubOption()
	 * This gets the user query option and creates a job for the user.
	 * @param username : the users name
	 * @throws Exception
	 */
	private void tweetSubOption(String username) throws Exception {
		String option;
		String query;
		String array[];
		Job job;
		boolean important;
		int jobId;

		if (DEBUG) t("in the tweetSubOption");

		//Gets a new Job ID
		jobId = jobId();
		send(jobId+"");
		option = input.readUTF();


		if (!(option.equals("backBtn"))) {
			query = input.readUTF();
			if (!(query.equals("backBtn"))) {
				array = query.split(",",2);

				//Create a new job
				if(DEBUG) t(option + " "+ query);

				if (array[1].equals("true")) {
					important = true;
				} else {
					important = false;
				}

				job = new Job(jobId, array[0], important);	
				job.setUser(username);
				if(DEBUG) t("Just Before Switch");
				switch (option) {
					case "option1": 
						job.setType(Job.QueryType.valueOf("SEARCHBYID"));
						break;
					case "option2":   
						job.setType(Job.QueryType.valueOf("NUMTWEETSBYWORD"));
						break;
					case "option3":   
						job.setType(Job.QueryType.valueOf("NUMTWEETSBYAIRLINE"));
						break;
					case "option4":   
						job.setType(Job.QueryType.valueOf("FREQCHARBYID"));
						break;
				}
				
				//Add the job to the job list
				if (DEBUG) t("Adding to Job List");
				synchronized(TweetWorkerHandlerThread.newJobLock) {
					TweetWorkerHandlerThread.newJob.add(job);
					TweetWorkerHandlerThread.newJobLock.notify();
				}
				if(DEBUG) t("JOB ADDED: " + job.toString());
				System.out.println("Job Submitted");
			}
		}
	}

	private void result() throws Exception {
		String id;
		boolean back = false;

		//Get the result from a job and send to client
		while (!back) {
			id = input.readUTF();
			if ((id.equals("backBtn"))) {
				back = true;
			} else {
				if (DEBUG) t("in result method: " + id);
				getResult(id);		
			}
		}
	}

	private void cancel() throws Exception  {
		String id;
		Job job; 

		//Get the Job ID
		id = input.readUTF();
		if (DEBUG) t("in cancel method: " + id);
		if (!(id.equals("backBtn"))) {
			//Create a Cancel job
			job = new Job(Integer.parseInt(id), "cancel", true);
			job.setType(Job.QueryType.valueOf("NUMTWEETSBYWORD"));

			//Add the Delete Job to the Queue
			synchronized(TweetWorkerHandlerThread.newJobLock) {
				TweetWorkerHandlerThread.newJob.add(job);
				TweetWorkerHandlerThread.newJobLock.notify();
			}
			//If job was already complete, change the result to be deleted
			synchronized(TweetServer.lockHash) {
				if (TweetServer.container.containsKey(Integer.parseInt(id))) {
					TweetServer.container.get(Integer.parseInt(id)).setResult("Query has been deleted");					
				}
			}
			send("Deleted query id: " + id);
		}	
	}

	/**
	 * mainOption()
	 * This selects the users main option to make a query or check results from another query.
	 * @throws Exception
	 */
	private void mainOption(String username) throws Exception {
		String incoming;

		incoming = input.readUTF();

		if (DEBUG) t("in main option: " + incoming);
		switch (incoming) {
			case "tweet": 
				tweetSubOption(username);
				break;
			case "result":
				result();
				break;
			case "cancel": 
				cancel();
				break;				
		}
	}

	/**
	 * The execution code for the server client program.
	 * ---------------------------------------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------This needs to be updated to communicate with the client.------------------------------------------------
	 * ---------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	public void run() {
		boolean runningg = true;             //The condition to keep the program running.
		String incoming;           			//The string that will be modified by the incoming data
		String outgoing;           			//The string that will be modified to contain the server time
		String username;

		try {			
			//Creating streams
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());

			System.out.println("Client Connected: " + clientSocket);

			username = startSession();

			while (runningg) {
				mainOption(username);
				if (DEBUG) t("Running: " + runningg);
			}
			if (DEBUG) t("Not Running: " + runningg);
		}catch (Exception e){
			System.out.println("Lost connection to client: " + clientSocket.getInetAddress());
		} 
		closeConnection();
	}
}
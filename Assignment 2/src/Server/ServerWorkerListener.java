/*
 * Jagmeet Grewal, Matthew Vince, Josh Oxford and Sung Phang
 * 444648, 034097, 484088, 481605
 * ServerWorkerListener 
 * Desc: Thread that listens for results and information coming from a worker
 */

package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Date;

import Worker.Job;
import Worker.Tweet;
import Worker.WorkerInputObject;

public class ServerWorkerListener extends Thread {
	private static final boolean DEBUG = false;                  //if true will show debug messages
	private WorkerDetails worker; 								 //The worker that the server is listening to.                                            //This will lock the hash table when using.
	
	/**
	 * Constructor
	 * @param i
	 */
	public ServerWorkerListener(WorkerDetails worker) {
		//Initialise the worker
		this.worker = worker;
	}

	/**
	 * setupWorker()
	 * This creates the input and output stream for the worker
	 * @param worker
	 */
	private void setupWorker(int i) {
		ObjectInputStream input;           //Object input stream
		ObjectOutputStream output;         //Object output stream

		try {
			//Create the streams
			input = new ObjectInputStream(worker.getSocket().getInputStream());
			output = new ObjectOutputStream(worker.getSocket().getOutputStream());	
			
			//Setting the streams
			worker.setInputStream(input); 
			worker.setOutputStream(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		TweetServer.workers[worker.getWorkerId()] = worker;	
	}

	/**
	 * addToHash()
	 * Adds a completed job to the hash table.
	 * @param job
	 */
	private void addToHash(Job job) {
		int jobId = job.getJobId();
		if(DEBUG) System.out.println("Going into sync for listener");
		synchronized(TweetServer.lockHash) {	
			if(DEBUG) System.out.println("adding job to hash");
			TweetServer.container.put(jobId, job);
			if(DEBUG) System.out.println("added job to hash");
			TweetServer.lockHash.notify();
		}
		if(DEBUG) System.out.println("Added job to Hash all done");
	}

	/**
	 * calculate()
	 * calculates the cost of a job.
	 * @param job
	 */
	private void calculateCost(Job job) {
		Long totalTime;
		String ans;
		long cost;

		totalTime = job.getEndTime() - job.getStartTime();      //time taken in ms
		if(DEBUG) System.out.println(job.getUser());
		cost = totalTime;
		String userName = job.getUser().split(":", 2)[1].split(",", 2)[0].toLowerCase();
		if(DEBUG) System.out.println(userName);

		//Calculate any extra costs
		if ((job.getType() == Job.QueryType.SEARCHBYID) || (job.getType() == Job.QueryType.FREQCHARBYID)) {
			if (job.getImportant()) {
				cost = cost * 10;
			} 
			if (userName.equals("saurabh")) {
				cost = cost * 1000000; //special cost for this special person
			}		
			ans = (String)(job.getResult()+" ");
			job.setResult(ans + "   Cost: $" + cost); 
		} else {
			if (job.getImportant()) {
				cost = cost * 10;
			} 
			if (userName.equals("saurabh")) {
				cost = cost * 1000000; //special cost for this special person
			}	
			//Calcuate the final cost
			cost = cost * TweetServer.workersConnected;
			job.setCost(cost);
		}
	}

	/**
	 * addToResponses()
	 * adds a part of a completed job to the responses linkedlist.
	 * @param job
	 */
	private void addToResponses(Job job) {

		synchronized(TweetServer.responsesLock) {	
			if(DEBUG) System.out.println("adding job to list");
			TweetServer.responses.add(job);
			if(DEBUG) System.out.println("added job to list");
			TweetServer.responsesLock.notify();
		}
	}


	/**
	 * The run method for the ServerWorkerListener thread
	 */
	public void run() {
		WorkerDetails worker = this.worker;
		Job incoming;                                     //The object that the thread is listening for.
		
		//Loop that constantly listens for input from the worker.
		while(true) {
			try {

				//Receiving the incoming object
				if (DEBUG) System.out.println("Listener is wating for a job from: " +  worker.getWorkerId());
				incoming = (Job)worker.getInputStream().readObject();
				if (DEBUG) System.out.println("Got a new result from worker");
				Job job = incoming;


				//Checking to see what object was received >>>>>may be needed to check status of server if not remove.
				if ((job.getType() == Job.QueryType.SEARCHBYID) || (job.getType() == Job.QueryType.FREQCHARBYID)) {      //always true

					//Add the result to jobs completed Hash
					calculateCost(job);
					addToHash(job);				
					if (DEBUG) System.out.println("Result added to hash: " + (job.getResult()).toString());

				}  else if (job.getType() == Job.QueryType.HEALTH){
					//Print the Health of the Worker
					System.out.println("Worker id : " + worker.getWorkerId() + " \nHas " + TweetServer.workers[worker.getWorkerId()].getCapacity() + " tweets stored.\n" + job.getResult());

				} else {
					//Store the job to partial result as this job was a job that was sent to all workers
						
					calculateCost(job);
					if(DEBUG) System.out.println("Result added to partial result linkedlist: "+ (job.getResult()).toString());
					addToResponses(job);
				}
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Worker id: " + worker.getWorkerId() + " has lost connection.");
				TweetServer.workers[worker.getWorkerId()] = null;
				break;
			}
		}		
	}	
}

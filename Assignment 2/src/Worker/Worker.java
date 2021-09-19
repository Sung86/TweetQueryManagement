package Worker;

import java.net.*;
import java.util.LinkedList;



import java.io.*;



public class Worker {
	/*
	 *ll accept the request for execution of queries from the data server
	 *It will execute the query and send back the results with time taken for execution of the query. 
	 *The worker will inform the master node about the status of the query request.
	 *If request is completed, the query result  will be sent to the data server */
	private static final int PORT = 7000; 
	private static final String HOST = 	"localhost";	//"144.6.231.172";
	private static final String WORKER_ID = "WORKER";
	private static final boolean DEBUG = false; 

	public static void main(String[] arg) {
		//Connect to Server	
		LinkedList<Tweet> tweets = new LinkedList<Tweet>();
		LinkedList<Job> jobs = new LinkedList<Job>(); 
		LinkedList<Job> jobsCompleted = new LinkedList<Job>();
		Object jobLock = new Object();
		Object completedLock = new Object();
		Object tweetLock = new Object();
		Object outputLock = new Object();
		
		try {
			Socket s1 = new Socket(HOST, PORT);
			
			//Connect to server and server that your a worker
			DataOutputStream dos = new DataOutputStream(s1.getOutputStream());
			dos.writeUTF(WORKER_ID);
			dos.flush();
			ObjectOutputStream workerObjectOutputStream = new ObjectOutputStream(s1.getOutputStream());
			if (DEBUG) System.out.println("Worker is being made: " + s1);
			
			//Make Worker Thread
			WorkerThread worker = new WorkerThread(jobs, jobLock, tweets, tweetLock,  workerObjectOutputStream, outputLock, jobsCompleted, completedLock);
			worker.start();
			//Process work given by server
			
			ObjectInputStream inputStream = new ObjectInputStream(s1.getInputStream());
			while(true) {
				//Wait for job
				if (DEBUG) System.out.println("Waiting for a new job");
				WorkerInputObject input = (WorkerInputObject) inputStream.readObject();
				switch(input.type) {
					case JOB:
						//If its a job, add job to the JOb list
						Job job = (Job) input.object;
						job.setStatus(Job.StatusEnum.NOTSTARTED);
						if (DEBUG) System.out.println("Adding Job to Worker: "+ job.getJobId());
						if(job.getImportant()) {
							synchronized (jobLock) {
								if (DEBUG) System.out.println("Adding job to worker important list.");
								jobs.addFirst(job);	
								if (DEBUG) System.out.println("Added job to worker important list.");
								jobLock.notifyAll();
							}
						} else {
							synchronized (jobLock) {
								if (DEBUG) System.out.println("Adding job to worker list.");
								jobs.addLast(job);
								if (DEBUG) System.out.println("Added job to worker list.");
								jobLock.notifyAll();
							}
						}
						break;
					case TWEET:
						//Add tweet to tweet list or update tweet list if tweet alreadt exists
						synchronized (tweetLock) {
							Boolean found = false;
							Tweet t = (Tweet) input.object;
							for(int i = 0; i < tweets.size();i++) {
								if(tweets.get(i).getTweet_created().equals(t.getTweet_id())) {
									tweets.set(i, t);
									found = true;
								}
							}
							
							//If tweet doesnt already exist, add tweet
							if(!found) {
								tweets.add(t);
							}
							tweetLock.notifyAll();
							if (DEBUG) System.out.println("from worker: " + ((Tweet)input.object).toString());
						}
						break;
					case DISCARD:
						//Discard job if it hasnt been processed yet
						synchronized (jobLock) {
							if (DEBUG) System.out.println("Removing Job");
							int size = jobs.size();
							int id = ((Job) input.object).getJobId();
							for (int i = 0; i < size; i++) {
								if(jobs.get(i).getJobId() == id) {
									if (DEBUG) System.out.println("Removing Job: " + jobs.get(i).getJobId());
									jobs.remove(i);
									break;
								}
							}
							jobLock.notifyAll();
						}
						break;
					case STATUS:
						StatusThread thread = new StatusThread((Job)input.object, jobs, jobLock, workerObjectOutputStream, outputLock, jobsCompleted, completedLock);
						thread.start();
						break;
					case HEALTH:
						//Get the health of a worker and send it to the server
						String data;
					    data = CPUMemInfo.getData();
					    Job jobby = new Job(-1,"Health",false); 
					    jobby.setResult(data);
					    jobby.setType(Job.QueryType.HEALTH);
					    try {
							synchronized (outputLock) {
								workerObjectOutputStream.writeObject(jobby);
								workerObjectOutputStream.flush();
								if (DEBUG) System.out.println("worker has sent Health job back:");
								outputLock.notify();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
						
			}
		} catch (Exception e) {
			System.out.println("Error Occurred in Worker: "+ e.getMessage());
		}
		
		
	}
}

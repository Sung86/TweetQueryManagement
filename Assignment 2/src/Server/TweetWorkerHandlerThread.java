/*
 * Jagmeet Grewal, Matthew Vince, Josh Oxford and Sung Phang
 * 444648, 034097, 484088, 481605
 * TweetWorkerHandlerThread
 * Desc: Makes new workers, send queries and tweets to workers and checks the health of workers
 */


package Server;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import Worker.Job;
import Worker.Tweet;
import Worker.Worker;
import Worker.WorkerInputObject;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.Server.Status;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.image.Image;
import org.openstack4j.openstack.OSFactory;

public class TweetWorkerHandlerThread extends Thread {
	private static final boolean CHEAT = true;
	private static final boolean DEBUG = false;                                                 //If set to true will show debugging messages.
	private final String URL = "https://keystone.rc.nectar.org.au:5000/v3/";                   //The url of the instance to start
	private final String USERNAME[] = { "mpvince@utas.edu.au", 
										"joxford@utas.edu.au", 
										"joxford@utas.edu.au",
										"jsgrewal@utas.edu.au", 
										"jsgrewal@utas.edu.au",
										"asphang@utas.edu.au",                                 //The username for the nectar account
										"asphang@utas.edu.au"};
	private final String PASSWORD[] = { "YmRiYmM0OGE2MDMyM2Yz", 
										"YWI1YzhkZmEyYzBmZjA5", 
										"YWI1YzhkZmEyYzBmZjA5",
										"MDQ2NDM0NzhkNGQxMTIw",
										"MDQ2NDM0NzhkNGQxMTIw", 
										"ZWY5MmRiZTY4NzVhN2Mz",
									 	"ZWY5MmRiZTY4NzVhN2Mz"};                                                 //The password for the nectar account??
	private final String IDENTIFIER[] = {   "09e5537decbb4ecab28d61492e91ea49", 
											"56bfacb67cbf4579a28c8c023be9a9d8",
											"56bfacb67cbf4579a28c8c023be9a9d8" ,
											"2e742f85b431410fad0fd1cc80066cc4",
											"2e742f85b431410fad0fd1cc80066cc4", 
											"b7fc6adf00b446168311afabfcc852bc",
											"b7fc6adf00b446168311afabfcc852bc"};                                               //The identifier of the nectar project
	private final String NAME[] = {"worker 1", "worker 2", "worker 3", 
									"worker 4", "worker 5", "worker 6"};                        //currently not sure
	private final String FLAVOR = "406352b0-2413-4ea6-b219-1a4218fd7d3b";                      //The floavor if of the VM t3.xsmall
	private final String IMAGE_ID = "ded5fa88-1417-4ffb-93ae-f1c0d984ea07";                    //The image ID of the VM
	private final String INSTANCE_NAME = "Ubuntu 2";
	private final String KEY_PAIR = "KIT318";
	private final int CAPACITY = 20;                                                         //The max number of tweets per worker
	private final int TWEET_LIMIT = 1; 															//The capacity a worker gets to before starting a new worker.
	private static LinkedList<Tweet> inbox;// = new LinkedList<Tweet>();                       //The linked list that holds the tweets to send to the workers
	private static Object lockLink;								                                //The lock for the tweet linked list
	static WorkerDetails[] workers;                                                            //Array of workers that are running.
	static Server[] servers = new Server[6];
	private OSClientV3 os = null;                                                              //Something to do with staring ubuntu... I forgot what



	private static WorkerInputObject wrapper;                                                   //The wrapper for the tweets/job data to sent to the workers
	static LinkedList<Job> newJob = new LinkedList<Job>();
	static Object newJobLock = new Object();



	public TweetWorkerHandlerThread(LinkedList<Tweet> t,Object l){
		inbox = t;
		lockLink = l;
	}

	/**
	 * StartNewWorker()
	 * This is start up a new worker when one is required.
	 * @param id : This is the identifier of the user so it will use the correct key pair. 
	 */
	private Server startNewWorker(int id){

		String script=Base64.getEncoder().encodeToString(("#!/bin/bash \ncd /home/ubuntu/KIT318-Tweets/'Assignment 2'/src \n java Worker/Worker").getBytes()); 

		ServerCreate server = Builders.server()
				.name(INSTANCE_NAME)  //Instance name
				.flavor(FLAVOR) // flavour id
				.image(IMAGE_ID) // image id
				.keypairName(KEY_PAIR)// key pair name
				.userData(script)
				.build(); //build the VM with above configuration
		return os.compute().servers().boot(server);
	}

	/**
	 * checkWorkerCapacity()
	 * This will check to see how full a worker is on tweets stored by asking the worker.
	 * @param wd : This is the worker that will be checked to see if it nearing max capacity.
	 * @param id : This is the worker id
	 * @return boolean : Will return true if a new worker VM needs to be started. 
	 */
	private boolean checkWorkerCapacity(WorkerDetails wd, int id){
		if (TweetServer.workers[id] != null){
			if (wd.getCapacity() == TWEET_LIMIT) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * checkWorkerStatus()
	 * This will check the status of the worker, running, cpu load, storage, requests being served.
	 * @param workId : This is the Id of the worker VM that is to checked.
	 */
	private void checkWorkerStatus(int workerId){

		if (TweetServer.workers[workerId] != null){
			WorkerInputObject healthCheck = new WorkerInputObject(WorkerInputObject.InputType.HEALTH , null);
			sendToWorker(TweetServer.workers[workerId], healthCheck);
		}

	}

	/**
	 * sendToWorker()
	 * This will send a object to the worker programs
	 * @param worker : This is the details of the worker that the object will be sent to
	 * @param o :  This it the object that will be sent to the worker.
	 */
	private void sendToWorker(WorkerDetails worker, Object o){
		try {
			worker.getOutputStream().writeObject(o);
		} catch (IOException e) {
			System.out.println("Worker id: " + worker.getWorkerId() + " has lost connection.");
			TweetServer.workers[worker.getWorkerId()] = null;
		}	
	}

	/**
	 * openStackSetup()
	 * This will set up the credentials for the VM that is to be started.
	 * @param user : This is who's nectar account that the VM will be started on.
	 * @param worker : This is the number of the VM to identify it.
	 */
	private void openStackSetup(int worker){

		os = (OSClientV3) OSFactory.builderV3() 
				.endpoint(URL) 
				.credentials(USERNAME[worker], PASSWORD[worker],Identifier.byName("Default"))
				.scopeToProject(Identifier.byId(IDENTIFIER[worker]))
				.authenticate(); 

	}

	/**
	 * startup()
	 * This will start up a instance of a worker for either server or local for testing.
	 */
	private void startNewInstance(int id) {
		//start up a instance stuff
		//    	openStackSetup(id);
		//    	Server serverid = startNewWorker(id);
		//    	servers[id] = serverid;
		if (DEBUG) System.out.println("Ubuntu should be starting");

		//testing on local machine 
		if (CHEAT) {
			if (id < 6) {
				if (DEBUG) System.out.println("Running in cheat mode: " + id);
				if (TweetServer.workersConnected < 6) {
					Thread workerThread = new Thread() {
						public void run() {
							String[] args = new String[1];
							args[0] = "";
							Worker.main(args);
						}
					};
					workerThread.start();
					if (DEBUG) System.out.println("new worker instacen started in cheat mode");
				}
			}
		}
	}
	/**
	 * objectSend()
	 * Sends an WorkerInputObject to a worker by worker id.
	 * @param id
	 * @param o
	 */
	private void objectSend(int id, WorkerInputObject o) {
		try {
			TweetServer.workers[id].getOutputStream().writeObject(o);
			TweetServer.workers[id].getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}   	
	}

	/**
	 * querySend()
	 * Sends a query to the worker if there is any queries and to the correct worker(s).
	 * @param id
	 */
	private void querySend(int id) {
		Job job;
		int temp; //---------temp value need to set syn right

		synchronized(newJobLock) {
			temp = newJob.size();
			newJobLock.notify();
		}

		if (id > 5) {
			id = 5;
		}

		if (temp > 0) {	
			synchronized(newJobLock) {
				job = newJob.removeFirst();
				newJobLock.notify();
				if (DEBUG) System.out.println("sent query to worker");
			}

			if (!(job.getQuery().equals("cancel"))) {
				//wrapping up the object to send in object stream
				wrapper = new WorkerInputObject(WorkerInputObject.InputType.JOB, job);
			} else {
				wrapper = new WorkerInputObject(WorkerInputObject.InputType.DISCARD, job);
			}
			//If find tweet by id || most freq char in a tweet
			if ((job.getType() == Job.QueryType.SEARCHBYID) || (job.getType() == Job.QueryType.FREQCHARBYID)) {
				//If find tweet by id || most freq char in a tweet send to work with tweetId range
				Long tweetId = Long.parseLong(job.getQuery());
				boolean found = false;
				for ( int x = 0; x <= id; x++) {
					if (TweetServer.workers[x] != null && TweetServer.workers[x].getStartId() != null) {
						if ((TweetServer.workers[x].getStartId() <= tweetId) && (TweetServer.workers[x].getEndId() >= tweetId)) {
							sendToWorker(TweetServer.workers[x],wrapper);
							if (DEBUG) System.out.println("SENT QUERY TO WORKER");
							found = true;
						} 
						
					}				
				}
				if (!found) {
					job.setResult("Tweet id could not be found");
					synchronized(TweetServer.lockHash) {	
						if(DEBUG) System.out.println("adding job to hash");
						TweetServer.container.put(job.getJobId(), job);
						if(DEBUG)System.out.println("added job to hash");
						TweetServer.lockHash.notify();
					}
				}
			} else {
				//send to all workers connected
				for(int x = 0; x <= id; x++) {
					if (TweetServer.workers[x] != null) {
						if(DEBUG) System.out.println("Sending Work to worker: "+ TweetServer.workers[x].getWorkerId());
						sendToWorker(TweetServer.workers[x], wrapper);
					}
				}

			}					
		}

	}    
	/**
	 * tweetSend()
	 * Sends up to 10 tweets if there are any.
	 * @param id
	 */
	private void tweetSend(int id) {
		Tweet tweet;
		if (TweetServer.workers[id] != null) {
			if (inbox.size() > 0) {
				synchronized(lockLink) {
					tweet = inbox.removeFirst();
					lockLink.notify();
					if (DEBUG) System.out.println("from inbox: " + tweet.toString());
				}
				updateWorker(id, tweet);
				wrapper = new WorkerInputObject(WorkerInputObject.InputType.TWEET, tweet);
				objectSend(id,wrapper);
				if (DEBUG) System.out.println("sent tweet to worker id: " + id);
				
			}
		}
	}


	//Update the index of the worker
	private void updateWorker(int id, Tweet tweet) {
		if (TweetServer.workers[id].getCapacity() == 0) {
			TweetServer.workers[id].setStartId(Long.parseLong(tweet.getTweet_id()));
			TweetServer.workers[id].setEndId(Long.parseLong(tweet.getTweet_id()));
			TweetServer.workers[id].setCapacity(TweetServer.workers[id].getCapacity() +1); 
		} else {
			TweetServer.workers[id].setEndId(Long.parseLong(tweet.getTweet_id()));
			TweetServer.workers[id].setCapacity(TweetServer.workers[id].getCapacity() +1); 
		}
	}

	/**
	 * firstWorkerCheck()
	 * Checks to see if the first worker instance has started yet.
	 */
	private void firstWorkerCheck() {
		boolean workerStarted = false;
		if (DEBUG) System.out.println("start of checking if worker is started");
		while (!workerStarted) {

			if (TweetServer.workers[0] == null) {
				try {
					Thread.sleep(1000);
					if (DEBUG) System.out.println("waiting for worker to start");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}		
			} else {
				workerStarted = true;
				if (DEBUG) System.out.println("worker started");
				//maybe check to see if server is alive here before moving on
			}
		}
	}

	/**
	 * run()
	 * The run method for the thread.
	 */
	public void run(){
		int wid = 0; 
		int cycle = 0;

		//Initialises the first worker instance.
		startNewInstance(wid);

		//check to see if first worker has started every second.
		firstWorkerCheck();

		//while loop for the server operation to send to workers
		while(true) {
			boolean running = true;
			querySend(wid);
			cycle++;
			//start loop to send objects to a worker by id
			//Only go in if all workers havent reached capacity yet
			while ((wid < 6) && running && (wid >= 0)) {
				cycle++;
				if (DEBUG) System.out.println("start of main worker handler loop cycle wid : " + wid  +" cycle: " + cycle);
				//send requests to worker if there are any.

				querySend(wid);
				//send tweets to the worker if there are any  		
				tweetSend(wid);

				//check status of workers every 30 sec
				if ((cycle % 30) == 0) {
					for (int i = wid; i >= 0; i--) {
						checkWorkerStatus(i);
					}
				}


				//starts new VM is TWEET_LIMIT is reached
				if ((wid < 6) && checkWorkerCapacity(TweetServer.workers[wid],wid) && (wid >= 0)) {
					startNewInstance(wid + 1);

				}

				//Exit while loop if at capacity
				if ( (TweetServer.workers[wid] != null) && (TweetServer.workers[wid].getCapacity() >= CAPACITY)) {
					running = false;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			//Check Worker Health
			if ((cycle % 30) == 0) {
				for (int i = wid; i >= 0; i--) {
					if(i <= 5) {
						checkWorkerStatus(i);
					}
				}
			}
			
			//Increment to the next worker id
			if (wid < 6) {
				wid++;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

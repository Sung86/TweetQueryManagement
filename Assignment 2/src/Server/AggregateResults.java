/*
 * Jagmeet Grewal, Matthew Vince, Josh Oxford and Sung Phang
 * 444648, 034097, 484088, 481605
 * Aggregate Results
 * Desc: Thread that checks for the results from multiple workers for the same job id to aggregate the results
 */

package Server;

import Worker.Job;

public class AggregateResults extends Thread{

	private static final boolean DEBUG = false;
	/**
	 * addToHash()
	 * Adds a completed job to the hash table so server can send it to the client
	 * @param job
	 */
	private void addToHash(Job job) {
		int jobId = job.getJobId();
		if(DEBUG) System.out.println("going into sync for listener");
		synchronized(TweetServer.lockHash) {	
			if(DEBUG) System.out.println("adding job to hash");
			TweetServer.container.put(jobId, job);
			if(DEBUG) System.out.println("added job to hash");
			TweetServer.lockHash.notify();
		}
		if(DEBUG) System.out.println("added job to hash all done");
	}


	public void run() {

		//Local Variables
		Job job;
		int jobId;
		int arrayPosition;
		int resultTotal;
		long costTotal;


		while (true) {
			//check through list for X workers of jobId
			int parts = 0;
			int size;
			synchronized(TweetServer.responsesLock) {
				//Check for any partial results
				if ( TweetServer.responses.size() > 0) {

					//Grab the first partial result
					job = TweetServer.responses.pop();

					//Get the number of workers connected
					int x = TweetServer.workersConnected;
					if (x>6) x = 6;

					//Grab the number of workers that are still connected
					for (int i = 0; i < x; i++) { 
						if (TweetServer.workers[i] != null && TweetServer.workers[i].getStartId() != null) {
							parts = parts +1;
						}
					}
					if(DEBUG) System.out.println("Parts is: " + parts);
					Job[] array = new Job[parts];
					array[0] = job;
					arrayPosition = 1;
					resultTotal = 0;
					costTotal = 0;

					jobId = job.getJobId();

					size = TweetServer.responses.size();

					//Look for the rest of the partial results matching the jobid
					if(DEBUG) System.out.println("size of the list: " +TweetServer.responses.size() );
					for (int i = 0; i < size; i++) {
						if(DEBUG) System.out.println(" search JobId: " + jobId );
						if (TweetServer.responses.get(i).getJobId() == jobId) {
							if(DEBUG) System.out.println("array pos: " + arrayPosition + "list spot " + i );
							array[arrayPosition] = TweetServer.responses.get(i);
							arrayPosition++;
						}
					}

					//If the array isnt full, that means that all results havent arrived yet
					if (array[parts-1] != null) {

						//if array is full calculate total cost and final result and add to hash
						for (int i=0; i < parts; i++) {							
							job = array[i];
							TweetServer.responses.remove(array[i]);
							resultTotal = resultTotal + Integer.parseInt((String)job.getResult());
							costTotal = costTotal + job.getCost();
						}

						job.setResult(resultTotal + " Cost is: " + costTotal);
						job.setCost(costTotal);
						addToHash(job);

					} else {
						//If all the responses are not there, add the inital results and add it back to the list 
						TweetServer.responses.addLast(array[0]);
					}

				}
				TweetServer.responsesLock.notify();
			}

			//Sleep for 6 seconds to before checking if more responses have arrived
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

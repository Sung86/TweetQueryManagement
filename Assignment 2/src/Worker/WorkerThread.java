package Worker;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.LinkedList;



public class WorkerThread extends Thread{
	private static final boolean DEBUG = false;						//Boolean, which is used to determine is debugging messages are printed
    private LinkedList<Job> jobs = new LinkedList<Job>();     		//List of jobs
    private LinkedList<Tweet> tweets = new LinkedList<Tweet>();		//List of Tweets
    private LinkedList<Job> jobsCompleted = new LinkedList<Job>();	//List of Jobs that are Completed
	private ObjectOutputStream serverOutputStream;					//OutputStream to Server
    
    //Locks
  private Object jobLock = new Object();							
	private Object tweetLock = new Object();
	private Object outputLock = new Object();
	private Object completedLock = new Object();
	
	public WorkerThread(LinkedList<Job> jobs, Object jobLock, LinkedList<Tweet> tweets, Object tweetLock, ObjectOutputStream out, Object outputLock, LinkedList<Job> jobsCompleted, Object completedLock) {
		this.jobs = jobs;
		this.jobLock = jobLock;
		this.serverOutputStream = out;
		this.tweets = tweets;
		this.tweetLock = tweetLock;
		this.outputLock = outputLock;
		this.jobsCompleted = jobsCompleted;
		this.completedLock = completedLock;
	}
	
	
	public void run() {
		while(true) {
			Job job = null;
			
			//Wait for a job
			synchronized (jobLock) {
				job = jobs.poll();
				if (DEBUG) System.out.println("checking worker job list");
				while(job == null) {
					try {
						if (DEBUG) System.out.println("Job list is null in worker");
						jobLock.wait();
					} catch (Exception e) {
						e.printStackTrace();
					}
					job = jobs.poll();
				}
				jobLock.notifyAll();
			}
			if (DEBUG) System.out.println("Worker is about to start processing Job: " + job.getJobId());
			job.setStatus(Job.StatusEnum.PROCESSING);
			job.setStartTime(System.nanoTime()/1000);
			
			//Figure out how to process job
			switch(job.getType()) {
				case SEARCHBYID:
					job = searchById(job);
					break;
				case NUMTWEETSBYWORD:
					job = numTweetsByWord(job);
					break;
				case NUMTWEETSBYAIRLINE:
					job = numTweetsByAirline(job);
					break;
				case FREQCHARBYID:
					job = FreqCharById(job);
					break;
				default:
					break;
			}
			
			synchronized (completedLock) {
				//Set the End time of Job and add it to jobCompleted list
				job.setEndTime(System.nanoTime()/1000);
				jobsCompleted.add(job);
				if (DEBUG) System.out.println("Job Completed: " + job.toString());
				completedLock.notify();
			}
			//Return Job to server
			returnJob(job);
		}
	}
	
	//Return the Job back to the server
	private void returnJob(Job job) {
		try {
			synchronized (outputLock) {
				serverOutputStream.writeObject(job);
				serverOutputStream.flush();
				if (DEBUG) System.out.println("worker has sent completed job back: ");
				outputLock.notify();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in output stream");
		}
	}

	/*
	 * Get the number of tweets that include a word
	 */
	private Job numTweetsByWord(Job job) {
		int count = 0;
		//Go through tweets and check how many tweet contain the inputted word
		String word = job.getQuery();
		synchronized (tweetLock) {
			int size = tweets.size();
			for(int i = 0; i<size;i++) {
				if(tweets.get(i).getText().contains(word)) {
					count++;
				}
			}
			tweetLock.notify();
		}
		job.setResult(count+"");
		job.setCompleted(true);
		job.setStatus(Job.StatusEnum.COMPLETED);
		return job;
	}


	/*
	 * Get the number of Tweets that mention an airline
	 */
	private Job numTweetsByAirline(Job job) {
		int count = 0;
		
		//Go through tweets and count the number of tweets about airline
		String airline = job.getQuery();
		synchronized (tweetLock) {
			int size = tweets.size();
			for(int i = 0; i<size;i++) {
				if(tweets.get(i).getAirline().equals(airline)) {
					count++;
				}
			}
			tweetLock.notify();
		}
		job.setResult(count+"");
		job.setCompleted(true);
		job.setStatus(Job.StatusEnum.COMPLETED);
		return job;
	}

	
	/*
	 * Get the Text of a tweet and return to server
	 */
	private Job searchById(Job job) {
		//Get Tweet
		Tweet tweet = findTweet(job);
		
		//If tweet was found, get Text
		if (tweet == null){
			job.setResult("Sorry no tweet found.");
		} else {
			job.setResult(tweet.getText());
		}
		//Return Tweet
		job.setCompleted(true);
		job.setStatus(Job.StatusEnum.COMPLETED);
		return job;
	}

	
	/*
	 * Go through Tweet List and find tweet by id
	 * Input Job
	 * Output Tweet
	 */
	private Tweet findTweet(Job job) {
		Tweet tweet = null;
		synchronized (tweetLock) {
			int size = tweets.size();
			int i = 0;
			Boolean found = false;
			while(!found && i < size) {
				if(tweets.get(i).getTweet_id().equals(job.getQuery())) {
					tweet = tweets.get(i);
					found = true;
				}
				i++;
			}
		}
		
		return tweet;
	}
	
	private Job FreqCharById(Job job) {
		
		Tweet tweet = findTweet(job);
		if (tweet == null){
			job.setResult("Sorry no tweet found.");
		} else {
			String s = tweet.getText().replaceAll("[^a-zA-Z]", "");	//For string t, remove all characters that arent letters	
			int l; 		//Length of t
			int[] alphabet = new int[26];

			s = s.toLowerCase();
			l = s.length();
			for (int i = 0; i < 26; i++) {
				alphabet[i] = 0;
			}
			//Calculate the sum
			for (int i = 0; i < l; i++) {
				alphabet[Pos(s.charAt(i))-1]++;
			}
		
			int index = 0;
			for (int i = 0; i < 26; i++) {
				if(alphabet[i]>=alphabet[index]) {
				index = i;
				}
			}
			//Return the average character occurring 
			job.setResult((char) Alpha(index+1));
			}
		job.setCompleted(true);
		job.setStatus(Job.StatusEnum.COMPLETED);
		return job;
		
	}
	
	public char Alpha(int num) {
		return (char) (num + 96);
	}

	public int Pos(char a) {
		return (int) a - 96;
	}

	//Set up serverOutputStream
	public void setServerOutputStream(ObjectOutputStream serverOutputStream) {
		this.serverOutputStream = serverOutputStream;
	}
}

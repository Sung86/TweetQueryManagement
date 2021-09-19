package Worker;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

public class StatusThread extends Thread {
	private static final boolean DEBUG = false;
	private Job job;
    private LinkedList<Job> jobs = new LinkedList<Job>();     
    private LinkedList<Job> jobsCompleted = new LinkedList<Job>();
	private Object jobLock = new Object();
	private ObjectOutputStream serverOutputStream;
	private Object outputLock = new Object();
	private Object completedLock = new Object();

	public StatusThread(Job job,LinkedList<Job> jobs, Object jobLock, ObjectOutputStream out, Object outputLock, LinkedList<Job> jobsCompleted, Object completedLock) {
		this.job = job;
		this.jobs = jobs;
		this.jobLock = jobLock;
		this.serverOutputStream = out;
		this.outputLock = outputLock;
		this.jobsCompleted = jobsCompleted;
		this.completedLock = completedLock;
	}
	
	//Return the job back to server
	private void returnJob(Job job) {
		// TODO Auto-generated method stub
		try {
			synchronized (outputLock) {
				serverOutputStream.writeObject(job);
				serverOutputStream.flush();
				if (DEBUG) System.out.println("worker has sent completed job back: ");
				outputLock.notify();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Job Status(Job job) {
		String status = "";

		int id = job.getJobId();
		
		//Check job list and see if job has started or not
		synchronized (jobLock) {
			int size = jobs.size();
			for (int i = 0; i < size; i++) {
				if(jobs.get(i).getJobId() == id) {
					switch(jobs.get(i).getStatus()) {
						case NOTSTARTED:
							status = "not started";
							break;
						case PROCESSING:
							status = "processing";
						default:
							break;
					}
					break;
				}
			}
			jobLock.notifyAll();
		}	
		
		//Check if the job has been completed if the job wasnt found  in the job list
		if(status.equals("")) {
			synchronized(completedLock) {
				int size = jobsCompleted.size();
				for(int i = 0; i < size; i++) {
					if(jobsCompleted.get(i).getJobId() == id) {
						if(jobsCompleted.get(i).getStatus() == Job.StatusEnum.COMPLETED) {
							status = "completed";
						}
					}
				}
				jobsCompleted.notifyAll();
			}
		}
		
		//If job still isnt found, then its being processed
		if(status.equals("")) {
			status = "processing";
		}
		job.setResult(status);
		job.setCompleted(true);
		return job;
	}
	
	public void run() {
		this.job = Status(this.job);
		returnJob(this.job);
	}
}

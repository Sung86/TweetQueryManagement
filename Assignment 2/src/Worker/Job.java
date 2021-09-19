package Worker;

import java.io.Serializable;


public class Job implements Serializable{

	public enum QueryType {SEARCHBYID, NUMTWEETSBYWORD, NUMTWEETSBYAIRLINE, FREQCHARBYID, HEALTH} 
	public enum StatusEnum {NOTSTARTED, PROCESSING, COMPLETED};
	private static final long serialVersionUID = 1L;
	private int jobId;
	private String query;
	private StatusEnum status;
	private Boolean important;
	private Boolean completed;
	private Object result;
	private String user;
	private Tweet tweet;
	private QueryType type;
	private Long startTime;
	private Long endTime;
	private long cost;
	private String health; 
	
		
	public Job (int id, String q, boolean i) {
		jobId = id;
		query = q;
		important = i;
	}

	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public Boolean getImportant() {
		return important;
	}
	public void setImportant(Boolean important) {
		this.important = important;
	}
	public Boolean getCompleted() {
		return completed;
	}
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

	public Tweet getTweet() {
		return tweet;
	}

	public void setTweet(Tweet tweet) {
		this.tweet = tweet;
	}
	public QueryType getType() {
		return type;
	}
	public void setType(QueryType type) {
		this.type = type;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}
	
	public String toString() {
		return "Job: " + jobId + " " + query + " " + important + " " + user + " " + result + " " + tweet + " " + type + " " + startTime + " " + endTime;
				 
	}

	public Long getCost() {
		return cost;
	}

	public void setCost(Long cost) {
		this.cost = cost;
	}

	public String getHealth() {
		return health;
	}

	public void setHealth(String health) {
		this.health = health;
	}
}

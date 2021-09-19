package Worker;
import java.io.Serializable;

public class Tweet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String tweet_id;
	private String airline;
	private String name;
	private String text;
	private String tweet_created;
	private String airline_sentiment;
	private String retweet;

	
	public Tweet (String array, String a, String n, String t, String array2, String array3, String array4) {
		tweet_id = array;
		airline = a;
		name = n;
		text = t;
		tweet_created = array2;
		airline_sentiment = array3;
		retweet = array4;

	}
	
	public void updateTweet(String id, String a, String n, String t, String d, String as, String r) {
		tweet_id = id;
		airline = a;
		name = n;
		text = t;
		tweet_created = d;
		airline_sentiment = as;
		retweet = r;
	}

	public String getTweet_id() {
		return tweet_id;
	}

	public void setTweet_id(String tweet_id) {
		this.tweet_id = tweet_id;
	}

	public String getAirline() {
		return airline;
	}

	public void setAirline(String airline) {
		this.airline = airline;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTweet_created() {
		return tweet_created;
	}

	public void setTweet_created(String tweet_created) {
		this.tweet_created = tweet_created;
	}

	public String getAirline_sentiment() {
		return airline_sentiment;
	}

	public void setAirline_sentiment(String airline_sentiment) {
		this.airline_sentiment = airline_sentiment;
	}

	public String getRetweet() {
		return retweet;
	}

	public void setRetweet(String retweet) {
		this.retweet = retweet;
	}
	
	public String toString() {
		String tweet = "";
		tweet = tweet_id + " " + airline + " " + name + " " + text + " " + tweet_created + " " + airline_sentiment + " " + retweet;

		return tweet;
	}
}
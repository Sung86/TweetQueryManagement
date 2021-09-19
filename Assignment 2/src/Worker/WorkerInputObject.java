package Worker;

import java.io.Serializable;

public class WorkerInputObject implements Serializable { //Object that is sent to worker 
	private static final long serialVersionUID = 1L;
	public enum InputType {JOB, TWEET, STATUS, DISCARD, HEALTH}
	public InputType type;
	public Object object;
	
	public WorkerInputObject(InputType type, Object object) {
		this.type = type;
		this.object = object;
	}
}

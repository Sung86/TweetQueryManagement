package Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.openstack4j.model.compute.Server;

/**
 * WorkerDetials
 * This object holds the information of the worker VM. Part of the Server program for Assignment 2.
 * @author 
 * @version 1.1
 *
 */

public class WorkerDetails {

	private int workerId;                                 //The ID of the worker.
	private Server server;                                //The server VM data
	private int capacity;                                 //The number of tweets in the worker set to 0 when created.
	private Socket socket;                                //The socket of the worker.
	private ObjectInputStream inputStream;                //The incoming object data stream. 
	private ObjectOutputStream outputStream;              //The outgoing object data stream.
	private Long startId;                                 //The starting ID of the tweet range
	private Long EndId;                                   //The ending ID of the tweet range.
	private int cpuLoad;                                  //The last check of cpu load.
	private String status;                                //The last check of server status.

	public WorkerDetails(int i, Socket s) {
		workerId = i;
		socket = s;
		capacity = 0;
	}

	public int getWorkerId() {
		return workerId;
	}

	public void setWorkerId(int workerId) {
		this.workerId = workerId;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public ObjectInputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(ObjectInputStream input) {
		this.inputStream = input;
	}

	public ObjectOutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(ObjectOutputStream output) {
		this.outputStream = output;
	}

	public int getCpuLoad() {
		return cpuLoad;
	}

	public void setCpuLoad(int cpuLoad) {
		this.cpuLoad = cpuLoad;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setStartId(Long startId) {
		this.startId = startId;
	}

	public void setEndId(Long endId) {
		EndId = endId;
	}

	public Long getStartId() {
		return startId;
	}

	public Long getEndId() {
		return EndId;
	}
	
}

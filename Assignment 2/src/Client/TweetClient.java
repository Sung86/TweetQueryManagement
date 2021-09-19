/*
 * Jagmeet Grewal, Matthew Vince, Josh Oxford and Sung Phang
 * 444648, 034097, 484088, 481605
 * TweetClient
 * Desc: Client program that connects to the server to send queries, get results, cancel queries 
 */

package Client;

import java.io.*;
import java.util.*;
import java.net.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.collections.*; 


public class TweetClient extends Application  {
	private ArrayList<Integer> queryId = new ArrayList<Integer>();	//Holds the ids of the jobs that client sends
	private Socket socket;											//Socket for connecting to server
	private DataOutputStream dos;									//OutputStream to server
	private DataInputStream dis;									//InputStream to server
	private boolean goToLogin = false;

	public static void main(String[] args){
		//launches the application
		launch(args);      
	}

	/**
	 * Display the registerView
	 * @param s the Stage object
	 * @return GridPane of register view
	 */ 
	private GridPane registerView(Stage s) {
		Label errorMessage = new Label();
		Label nameLabel = new Label("Enter name: ");
		TextField nameTextField = new TextField();
		Button submitBtn = new Button("Submit");
		Button loginBtn = new Button("Go to Login");
		submitBtn.setOnAction(e->
		{
			if(!nameTextField.getText().isEmpty()){
				try{

					//let server know that client is about to register
					dos.writeUTF("register:"+nameTextField.getText()); 

					//server response
					String serverReply = dis.readUTF();

					//server response tells user that if register name they tried is alreay taken or not
					if( serverReply.equals("valid")){
						goToLogin = false;
						loginView(s);
					}
					else 
						errorMessage.setText("Name has been taken, please enter a unique name");

				}
				catch(Exception error){
					error.printStackTrace();
				}
			}
			else
				errorMessage.setText("Please fill in your name");
		});

		//Client already has login, so they want to login instead of register
		loginBtn.setOnAction(e->
		{
			goToLogin = true;
			loginView(s);

		});

		GridPane r = new GridPane();
		r.addRow(1, nameLabel, nameTextField);
		r.addRow(2, errorMessage);
		r.addRow(3, loginBtn, submitBtn);
		return r;
	}

	/**
	 * Display the login view
	 * @param s the Stage object
	 */
	private void loginView(Stage s) {

		Label errorMessage = new Label();
		Label usernameLabel = new Label("Enter your name: ");
		Label idLabel = new Label("Enter your unique identifier: ");
		Label generatedId = new Label();
		//If the user is logining in just after registration, the server prints the unique id for that login
		if (!goToLogin) {
			try {
				generatedId.setText("your unique identifier is : " + dis.readUTF());
			} catch (Exception e) {
				//TODO: handle exception
			}
		}
		Label tab= new Label("\t");
		TextField usernameTextField = new TextField();
		TextField idTextField = new TextField();

		Button submitBtn = new Button("Submit");
		submitBtn.setOnAction(e->
		{

			//Check if login is valid, and login is it is valid
			if(!usernameTextField.getText().isEmpty() && !idTextField.getText().isEmpty()){
				try{
					dos.writeUTF("login:"+usernameTextField.getText()+","+ idTextField.getText()); 
					String serverReply = dis.readUTF();
					if(serverReply.equals("valid")){
						queryForm(s);
					}
					else 
						errorMessage.setText("Invalid inputs");
				}
				catch(Exception error){
					error.printStackTrace();
				}

			}
			else 
				errorMessage.setText("Please fill in all the fields");

		});
		GridPane r = new GridPane();
		r.addRow(1, usernameLabel, usernameTextField);
		r.addRow(2, idLabel, idTextField);
		r.addRow(3, generatedId);
		// r.addRow(2, passwordLabel, passwordTextField, tab, generatedPassowrd);
		r.addRow(4, submitBtn, errorMessage);
		Scene sc=new Scene(r, 600,300);
		s.setScene(sc);
		s.setTitle("Login");
	}

	/**
	 * Display the query section view
	 * @param s the Stage object
	 */
	public void queryForm(Stage s){

		Button queryTweetBtn = new Button("Query Tweet Database");
		Button queryResultBtn = new Button("Query Result/Status");
		Button queryCancelBtn = new Button("Cancel Query");

		//Tells server your about to query about tweet
		queryTweetBtn.setOnAction(e->
		{
			try {
				dos.writeUTF("tweet");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			queryTweet(s);
		});

		//Tell user that client is about to get a result from server
		queryResultBtn.setOnAction(e->
		{
			try {
				dos.writeUTF("result");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			queryResult(s);
		});

		//Tells user that the client is about to cancel a query
		queryCancelBtn.setOnAction(e->
		{
			try {
				dos.writeUTF("cancel");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			cancelQuery(s);
		});

		GridPane r = new GridPane();
		r.addRow(1, queryTweetBtn);
		r.addRow(2, queryResultBtn);
		r.addRow(3, queryCancelBtn);
		Scene sc=new Scene(r, 600,300);
		s.setScene(sc);
		s.setTitle("Query");
	}

	/**
	 * Display the view for cancel query
	 * @param s the Stage object
	 */
	private void cancelQuery(Stage s){
		Label inputLabel=new Label("Enter query id: ");
		Label errorMessage=new Label();
		TextField inputField = new TextField();
		Button submitBtn = new Button("Submit");
		Button backBtn = new Button("Back");
		Label ans =new Label();

		submitBtn.setOnAction(e->
		{
			//Tell the server which query to delete
			if(!(inputField.getText().isEmpty())){
				try {
					dos.writeUTF(inputField.getText());
					submitBtn.setDisable(true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				//The response from the server and delete the query id from the client's local list of queries 
				try {
					ans.setText(dis.readUTF());
					queryId.remove(Integer.valueOf(inputField.getText()));

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				errorMessage.setText("Please give a query id");
			}
		});

		backBtn.setOnAction(e-> 
		{
			//Back to main page
			try {
				dos.writeUTF("backBtn");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			queryForm(s);
		}


				);

		int i = 0;
		GridPane r = new GridPane();
		r.addRow(++i, inputLabel, inputField);
		r.addRow(++i, submitBtn, errorMessage);
		r.addRow(++i, backBtn);
		r.addRow(++i, ans);
		//Display the different query ids the client has
		if(!queryId.isEmpty()){
			r.addRow(++i,new Label("Query Id"));
			for (int id : queryId) {
				Label displayId =new Label(""+ id);
				r.addRow(++i, displayId);
			}
		}
		Scene sc=new Scene(r, 600,300);
		s.setScene(sc);
		s.setTitle("Cancel Query by id");
	}

	/**
	 * Display the view of query result
	 * @param s the Stage object
	 */
	private void queryResult(Stage s){
		Label inputLabel=new Label("Enter query id: ");
		Label errorMessage=new Label();
		TextField inputField = new TextField();
		Button submitBtn = new Button("Submit");
		Button backBtn = new Button("Back");
		Label ans =new Label();

		submitBtn.setOnAction(e->
		{
			//Send the server which query the client wants to result for
			if(!(inputField.getText().isEmpty())){
				try {
					dos.writeUTF(inputField.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				//Print the result from server
				try {
					ans.setText("answer: "+dis.readUTF());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				errorMessage.setText("Please give a id");
			}

		});

		//go Back to the main page
		backBtn.setOnAction(e-> 
		{
			try {
				dos.writeUTF("backBtn");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			queryForm(s);
		});

		int i = 0;
		GridPane r = new GridPane();
		r.addRow(++i, inputLabel, inputField);
		r.addRow(++i, submitBtn, errorMessage);
		r.addRow(++i, backBtn);
		r.addRow(++i, ans);
		if(!queryId.isEmpty()){
			r.addRow(++i,new Label("Query Id"));
			for (int id : queryId) {
				Label displayId =new Label(""+ id);
				r.addRow(++i, displayId);
			}
		}
		Scene sc=new Scene(r, 600,300);
		s.setScene(sc);
		s.setTitle("Get result by query id");
	}

	/**
	 * Display the view of query tweet options
	 * @param s the Stage object
	 */
	private void queryTweet(Stage s){
		Button submitBtn= new Button("Submit");
		String options[] = {"Selection one option",
				"Search text of a tweet given its id", 
				"Search number of tweets containing a specific words", 
				"Search number of tweets from a specific airline", 
		"Find the most frequent character used in a tweet given its id" }; 


		ComboBox menu =  new ComboBox(FXCollections.observableArrayList(options));
		menu.getSelectionModel().selectFirst();

		//Submit a query type
		submitBtn.setOnAction(e->{
			int selectedIndex = menu.getSelectionModel().getSelectedIndex();
			if(selectedIndex!= 0){ 
				String option = "option" + selectedIndex;

				//Send query to worker and get the query id from server
				try {
					dos.writeUTF(option);
					queryId.add(Integer.parseInt(dis.readUTF()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				//Take the client to the correct query type form
				switch (selectedIndex) {
				case 1:  
					searchTweetById(s);
					break;
				case 2:
					searchNumTweetByWords(s);
					break;
				case 3:
					searchNumTweetByAirline(s);
					break;
				case 4:
					mostFreqCharById(s);
					break;
				}
			}
		});
		GridPane r = new GridPane();
		r.addRow(1, menu);
		r.addRow(2, submitBtn);
		Scene sc=new Scene(r, 600,300);
		s.setScene(sc);
		s.setTitle("Query Tweet Database");
	}
	/**
	 * Display the view for Search Tweet by Tweet id
	 * @param s the Stage object
	 */
	private void searchTweetById(Stage s){
		Label askImportantness=new Label("Is this query important?:");
		Label inputLabel=new Label("Enter Tweet Id: ");
		TextField inputField = new TextField();
		Button submitBtn = new Button("Submit");
		CheckBox importantnessCheckBox = new CheckBox();

		//Send the tweet id to server
		submitBtn.setOnAction(e->
		{
			try {
				dos.writeUTF(inputField.getText()+","+importantnessCheckBox.isSelected());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			queryForm(s);
		});
		//create a gridpane
		GridPane r = new GridPane();
		r.addRow(1, inputLabel, inputField);
		r.addRow(2, askImportantness, importantnessCheckBox);
		r.addRow(3, submitBtn);
		Scene sc=new Scene(r, 600,300);
		s.setScene(sc);
		s.setTitle("Search text of a tweet by id");
	}

	/**
	 * Display the view of Number of Tweets based on word query
	 * @param s the Stage object
	 */
	private void searchNumTweetByWords(Stage s){
		Label askImportantness=new Label("Is this query important?:");
		Label inputLabel=new Label("Enter word(s): ");
		TextField inputField = new TextField();
		Button submitBtn = new Button("Submit");
		CheckBox importantnessCheckBox = new CheckBox();

		//Send word to be searched to the server
		submitBtn.setOnAction(e->
		{
			try {
				dos.writeUTF(inputField.getText()+","+importantnessCheckBox.isSelected());
			} catch (IOException e1) {	
				e1.printStackTrace();
			}
			queryForm(s);
		});

		//create a gridpane
		GridPane r = new GridPane();
		r.addRow(1, inputLabel, inputField);
		r.addRow(2, askImportantness, importantnessCheckBox);
		r.addRow(3, submitBtn);
		Scene sc=new Scene(r, 600,300);
		s.setScene(sc);
		s.setTitle("Searching number of tweets containing a specific  words");
	}

	/**
	 * Display the view for Number of tweets with Airline option
	 * @param s the Stage object
	 */
	private void searchNumTweetByAirline(Stage s){
		Label askImportantness=new Label("Is this query important?:");
		Label inputLabel=new Label("Enter a specific airline: ");
		TextField inputField = new TextField();
		Button submitBtn = new Button("Submit");
		CheckBox importantnessCheckBox = new CheckBox();

		//Send the airline to the server to search for
		submitBtn.setOnAction(e->
		{
			try {
				dos.writeUTF(inputField.getText()+","+importantnessCheckBox.isSelected());
			} catch (IOException e1) {	
				e1.printStackTrace();
			}
			queryForm(s);
		});

		//create a gridpane
		GridPane r = new GridPane();
		r.addRow(1, inputLabel, inputField);
		r.addRow(2, askImportantness, importantnessCheckBox);
		r.addRow(3, submitBtn);
		Scene sc=new Scene(r, 600,300);
		s.setScene(sc);
		s.setTitle("Searching number of tweets containing a specific airline");
	}

	/**
	 * Display the view of Frequent Char by in Tweet option
	 * @param s the Stage object
	 */
	private void mostFreqCharById(Stage s) {
		Label askImportantness=new Label("Is this query important?:");
		Label inputLabel=new Label("Enter a tweet id: ");
		TextField inputField = new TextField();
		Button submitBtn = new Button("Submit");
		CheckBox importantnessCheckBox = new CheckBox();
		//action to be performed
		submitBtn.setOnAction(e->
		{
			try {
				dos.writeUTF(inputField.getText()+","+importantnessCheckBox.isSelected());
			} catch (IOException e1) {	
				e1.printStackTrace();
			}
			queryForm(s);
		});
		GridPane r = new GridPane();
		r.addRow(1, inputLabel, inputField);
		r.addRow(2, askImportantness, importantnessCheckBox);
		r.addRow(3, submitBtn);
		Scene sc=new Scene(r, 600,300);
		s.setScene(sc);
		s.setTitle("Find the most frequent character used in a tweet by id");
	}

	//application starts here
	@Override
	public void start(Stage s) throws Exception {
		try {
			//Connect to server and let it know this is a client connection
			socket =  new Socket("localhost", 7000);	//("144.6.231.172, 7000"); This is what the Server ip will be 
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			dos.writeUTF("CLIENT");
		} catch (Exception e) {
			e.printStackTrace();
		}
		GridPane view = registerView(s);
		Scene sc=new Scene(view, 600,300);
		s.setScene(sc);
		s.setTitle("Register"); //initial title of the scene
		s.show();
	}
}
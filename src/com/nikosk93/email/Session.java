package com.nikosk93.email;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import com.nikosk93.email.user.Account;
import com.nikosk93.email.user.Email;
import com.nikosk93.email.utils.BufferedString;

public class Session extends Thread {
	public static final String EXIT_FLAG = "Exit_Application_Flag";
	private static final int ID_COL_LENGTH = 10;
	private static final int FROM_COL_LENGTH = 22;
	private static final int SUBJECT_COL_LENGTH = 40;
	
	public static MailServer server;
	
	private Socket client;
	private ObjectInputStream clientInput;
	private ObjectOutputStream clientOutput;
	private BufferedString output;
	private Account user;
	
	public Session(Socket client) {
		this.client = client;
		output = new BufferedString();
		
		try {
			clientOutput = new ObjectOutputStream(client.getOutputStream());
			clientInput = new ObjectInputStream(client.getInputStream());
			
			start();
		} catch (IOException e) {
			exit();
		}
	}
	
	/*
	 * Εμφανίζει το menu και επιστρέφει την είσοδο του χρήστη
	 */
	private String getInputFromOptions(ArrayList<String> options) {
		String input;
		
		while(true) {
			output.addPatternLine('=', 12);
			for (String option: options)
				output.addLine("> " + option);
			output.addPatternLine('=', 12);
			
			input = getInput();
			
			if (options.contains(input) || input == null)
				return input;
			
			output.addLine("Wrong input! Please try again");
		}		
	}
	
	/*
	 * Διαχειρίζεται την είσοδο του χρήστη
	 */
	public void run() {
		String input;
		boolean prevStatus = true;
		
		ArrayList<String> guestOptions = new ArrayList<String>();
		guestOptions.add("Login");
		guestOptions.add("Register");
		guestOptions.add("Exit");
		
		ArrayList<String> loggedOptions = new ArrayList<String>();
		loggedOptions.add("NewEmail");
		loggedOptions.add("ShowEmails");
		loggedOptions.add("ReadEmail");
		loggedOptions.add("DeleteEmail");
		loggedOptions.add("Logout");
		loggedOptions.add("Exit");
		
		while(true) {
			if (user == null) {
				prevStatus = welcomeMessage("Welcome guest, pick one option", prevStatus);
				input = getInputFromOptions(guestOptions);
			} else {
				prevStatus = welcomeMessage("Welcome back " + user.getUsername(), prevStatus);				
				input = getInputFromOptions(loggedOptions);
			}
			
			if (input == null || input.equals("Exit")){
				exit();
				break;
			} else if (input.equals("Login")) {
				login();
			} else if (input.equals("Register")) {
				register();
			} else if (input.equals("NewEmail")) {
				newEmail();
			} else if (input.equals("ShowEmails")) {
				showEmails();
			} else if (input.equals("ReadEmail")) {
				readEmail();
			} else if (input.equals("DeleteEmail")) {
				deleteEmail();
			} else if (input.equals("Logout")) {
				logout();
			}
		}
	}
	
	/*
	 * Καλωσορίζει τον χρήστη
	 */
	private boolean welcomeMessage(String message, boolean prev) {
		if ( (user == null && prev == true) || (user != null && prev == false)) {
			output.addPatternLine('=', 12);
			output.addLine(message);
			
			return !prev;
		}
		
		return prev;
	}
	
	/*
	 * Στέλνει το output στον χρήστη και επιστρέφει την είσοδο του
	 */
	private String getInput() {
		try {
			clientOutput.writeObject(output.flush());
			return (String)clientInput.readObject();
		} catch (Exception e) {
			return null;
		}
	}
	
	/*
	 * Συνδέεται σε έναν υπάρχων Account
	 */
	private void login() {
		Account user;
		String username;
		String password;
		
		output.addLine("Username:");
		username = getInput();
		
		output.addLine("Password:");
		password = getInput();
		
		user = server.getAccountByUsername(username);
		
		if (user == null || !user.passwordEquals(password)) {
			output.addLine("[ERROR] Failed to login. Try a different username and password combination");
		} else {
			this.user = user;
		}
	}
	
	/*
	 * Δημιουργεί ένα καινούργιο Account
	 */
	private void register() {
		Account user;
		String username;
		String password;
		
		output.addLine("Username:");
		username = getInput();
		
		output.addLine("Password:");
		password = getInput();
		
		user = new Account(username, password);
		
		if (server.addAccount(user)) {
			this.user = user;
		} else {
			output.addLine("[ERROR] User already exists");
		}
	}
	
	/*
	 * Δημιουργεί και στέλνει ένα Email σε κάποιο υπάρχων Account
	 */
	private void newEmail() {
		Email email;
		Account to;
		String receiver;
		String subject;
		String mainbody;
		
		output.addLine("Receiver:");
		receiver = getInput();
		
		output.addLine("Subject:");
		subject = getInput();
		
		output.addLine("Mainbody:");
		mainbody = getInput();
		
		to = server.getAccountByUsername(receiver);
		email = new Email(user.getUsername(), receiver, subject, mainbody);
		
		if (to != null && to.addEmail(email)) {
			output.addLine("Email sent successfully");
		} else {
			output.addLine("[ERROR] Failed to send email. Receiver doesn't exists");
		}
	}
	
	/*
	 * Εμφανίζει το mailbox του user
	 */
	private boolean showEmails() {
		if (user.getMailbox().size() < 1) {
			output.addLine("Your mailbox is empty");
			return false;
		}
		
		output.addFixedLengthString("Id", ID_COL_LENGTH);
		output.addFixedLengthString("From", FROM_COL_LENGTH);
		output.addFixedLengthString("Subject", SUBJECT_COL_LENGTH);
		output.addSeparator();
		
		for (Email email : user.getMailbox()) {
			String id = String.format("%d. %s", email.getId(), email.isUnread() ? "[New]" : "");
			
			output.addFixedLengthString(id, ID_COL_LENGTH);
			output.addFixedLengthString(email.getSender(), FROM_COL_LENGTH);
			output.addFixedLengthString(email.getSubject(), SUBJECT_COL_LENGTH);
			output.addSeparator();
		}
		
		return true;
	}
	
	/*
	 * Εμφανίζει ένα Εmail με βάση το id του
	 */
	private void readEmail() {
		Email email;
		int id;
		
		output.addLine("Email id:");
		try {
			id = Integer.parseInt(getInput());
		} catch (Exception e) {
			id = -1;
		}
		
		email = user.getEmail(id);
		
		if (email != null) {
			output.addFixedLengthString("From: ", 9);
			output.add(email.getSender());
			output.addSeparator();
			
			output.addFixedLengthString("Subject: ", 9);
			output.add(email.getSubject());
			output.addSeparator();
			
			output.addPatternLine('*', 12);
			
			output.addLine(email.getMainbody());
			
			email.setAsRead();
		} else {
			output.addLine("[ERROR] Can't show email. Email with id #" + id + " doesn't exists");
		}
	}
	
	/*
	 * Διαγράφει ένα Email αν υπάρχει
	 */
	private void deleteEmail() {
		int id;
		
		output.addLine("Email id:");
		try {
			id = Integer.parseInt(getInput());
		} catch (Exception e) {
			id = -1;
		}
		
		if (user.deleteEmail(id)) {
			output.addLine("Email with id #" + id + " deleted successfully");
		} else {
			output.addLine("[ERROR] Can't delete email. Email with id #" + id + " doesn't exists");
		}
	}
	
	/*
	 * Αποσυνδέει τον user
	 */
	private void logout() {
		user = null;
	}
	
	/*
	 * Έξοδος
	 */
	private void exit() {
		try {
			clientOutput.writeObject(EXIT_FLAG);
		} catch (IOException e) {}
		
		closeStreams();
		server.removeSession(this);
	}
	
	/*
	 * Κλείνει τα ανοιχτά streams/sockets
	 */
	private void closeStreams() {
		try {
			client.close();
			clientInput.close();
			clientOutput.close();
		} catch (IOException e) {}
	}
}

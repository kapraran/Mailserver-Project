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
	public static MailServer server;
	
	protected Socket client;
	protected ObjectInputStream clientInput;
	protected ObjectOutputStream clientOutput;
	protected BufferedString output;
	protected Account user;
	
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
			output.addPatternLine('=', 10);
			for (String option: options)
				output.addLine("> " + option);
			output.addPatternLine('=', 10);
			
			input = getInput();
			
			if (options.contains(input))
				return input;
			
			output.addLine("Wrong input! Please try again");
		}
	}
	
	/*
	 * Διαχειρίζεται την είσοδο του χρήστη
	 */
	public void run() {
		String input;
		
		ArrayList<String> guestOptions = new ArrayList<>();
		guestOptions.add("Login");
		guestOptions.add("Register");
		guestOptions.add("Exit");
		
		ArrayList<String> loggedOptions = new ArrayList<>();
		loggedOptions.add("NewEmail");
		loggedOptions.add("ShowEmails");
		loggedOptions.add("ReadEmail");
		loggedOptions.add("DeleteEmail");
		loggedOptions.add("Logout");
		loggedOptions.add("Exit");
		
		while(true) {
			if (user == null) {
				output.addLine("Welcome guest, pick one option");
				input = getInputFromOptions(guestOptions);
			} else {
				output.addLine("Welcome back " + user.getUsername());
				input = getInputFromOptions(loggedOptions);
			}
			
			if (input.equals("Login")) {
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
			} else if (input.equals("Exit")) {
				exit();
				break;
			}
		}
	}
	
	/*
	 * Στέλνει το output στον χρήστη και επιστρέφει την είσοδο του
	 */
	private String getInput() {
		try {
			clientOutput.writeObject(output.flush());
			return (String)clientInput.readObject();
		} catch (ClassNotFoundException | IOException e) {
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
		
		if (to != null) {
			to.addEmail(email);
			output.addLine("Email sent successfully");
		} else {
			output.addLine("[ERROR] Failed to send email. Receiver does't exists");
		}
	}
	
	/*
	 * Εμφανίζει το mailbox του user
	 */
	private void showEmails() {
		output.addFixedLengthString("Id", 12);
		output.addFixedLengthString("From", 30);
		output.addFixedLengthString("Subject", 50);
		output.addSeparator();
		
		for (Email email : user.getMailbox()) {
			String id = email.getId() + ". ";
			
			if (email.isUnread())
				id += "[New]";
			
			output.addFixedLengthString(id, 12);
			
			output.addFixedLengthString(email.getSender(), 30);
			output.addFixedLengthString(email.getSubject(), 50);
			output.addSeparator();
		}
	}
	
	/*
	 * Εμφανίζει ένα Εmail με βάση το id του
	 */
	private void readEmail() {
		Email email;
		int id;
		
		output.addLine("Email id:");
		id = Integer.parseInt(getInput());
		
		email = user.getEmail(id);
		
		if (email != null) {
			output.add("From: ");
			output.add(email.getSender());
			output.addSeparator();
			
			output.add("Subject: ");
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
		id = Integer.parseInt(getInput());
		
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

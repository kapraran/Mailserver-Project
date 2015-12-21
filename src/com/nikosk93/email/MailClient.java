package com.nikosk93.email;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.nikosk93.email.utils.BufferedString;

public class MailClient {
	public static final String EXIT_FLAG = "Exit_Application_Flag";
	public static final int DEFAULT_PORT = 1993;
	
	private InetAddress ip;
	private int port;
	private Scanner userInput;
	private Socket server;
	private ObjectInputStream serverInput;
	private ObjectOutputStream serverOutput;
	
	public MailClient() throws UnknownHostException {
		this(InetAddress.getLocalHost(), DEFAULT_PORT);
	}
	
	public MailClient(InetAddress ip, int port) {
		this.ip = ip;
		this.port = port;
		this.userInput = new Scanner(System.in);
		
		if (connect()) {
			start();
		} else {
			exit("[ERROR] Can't connect to server");
		}
	}
	
	/*
	 * Συνδέεται με τον server
	 */
	private boolean connect() {
		try {
			server = new Socket(ip, port);
			serverInput = new ObjectInputStream(server.getInputStream());
			serverOutput = new ObjectOutputStream(server.getOutputStream());
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	/*
	 * Ξεκινά την ανταλλαγή δεδομένων
	 */
	private void start() {
		String input;
		
		while (true) {
			if (!displayOutput())
				break;
				
			input = userInput.nextLine();
			sendInput(input);
		}
		
		exit();
	}
	
	/*
	 * Εμφανίζει τα δεδομένα που στέλνει ο server. Σε περίπτωση που ο server επιστρέψει EXIT_FLAG 
	 * τότε επιστρέφει false
	 */
	private boolean displayOutput() {
		String response;
		BufferedString buffer = new BufferedString();
		
		try {
			response = (String)serverInput.readObject();
			
			if (response.equals(EXIT_FLAG))
				return false;

			buffer.addPatternLine('-', 12);
			buffer.addLine("MailServer:");
			buffer.addPatternLine('-', 12);
			buffer.add(response);
			
			System.out.print(buffer.flush());
		} catch (ClassNotFoundException | IOException e) {
			exit("[ERROR] Lost connection to server");
		}
		
		return true;
	}
	
	/*
	 * Στέλνει την είσοδο του χρήστη στον server
	 */
	private void sendInput(String input) {
		try {
			serverOutput.writeObject(input);
		} catch (IOException e) {
			exit("[ERROR] Lost connection to server");
		}
	}
	
	/*
	 * Κλείνει τα ανοιχτά streams/sockets
	 */
	private void closeStreams() {
		try {
			userInput.close();
			serverInput.close();
			serverOutput.close();
			server.close();
		} catch (Exception e) {}
	}
	
	/*
	 * Έξοδος χωρίς error
	 */
	private void exit() {
		exit(null);
	}
	
	/*
	 * Έξοδος
	 */
	private void exit(String error) {
		int status = 0;
		
		if (error != null) {
			status = 1;
			System.out.println(error);
		}
		
		closeStreams();
		System.exit(status);
	}

	public static void main(String[] args) {
		try {
			if (args.length == 2) {
				InetAddress ip = InetAddress.getByName(args[0]);
				int port = Integer.parseInt(args[1]);
				
				new MailClient(ip, port);
			} else {
				new MailClient();
			}
		} catch (Exception e) {
			System.out.println("[ERROR] Please use a valid <ip> <port> combination");
		}
	}
}

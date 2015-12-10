package com.nikosk93.email;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import com.nikosk93.email.user.Account;
import com.nikosk93.email.user.Email;

public class MailServer {
	public static final int DEFAULT_PORT = 1993;
	
	private int port;
	private ServerSocket server;
	private List<Session> sessions;
	private List<Account> accounts;
	
	public MailServer() {
		this(DEFAULT_PORT);
	}
	
	public MailServer(int port) {
		this.port = port;
		sessions = new ArrayList<>();
		accounts = new ArrayList<>();
		
		Session.server = this;
		
		seedAccounts();
		start();
	}
	
	/*
	 * Προσθέτει δεδομένα στο accounts
	 */
	private void seedAccounts() {
		Account a1 = new Account("nick@csd.gr", "111");
		Account a2 = new Account("ria@csd.gr", "222");
		Account a3 = new Account("you@csd.gr", "333");
		
		a1.addEmail(new Email("ria@csd.gr", "nick@csd.gr", "Assignment #1", "Integer ullamcorper lobortis pretium. Donec dignissim eu velit quis interdum."));
		a1.addEmail(new Email("ria@csd.gr", "nick@csd.gr", "Register for class", "Mauris venenatis ac magna ac egestas. Cras egestas nulla tincidunt velit lacinia, eu malesuada urna sodales."));
		a1.addEmail(new Email("you@csd.gr", "nick@csd.gr", "Hello!", "Welcome to csd. none"));
		
		a2.addEmail(new Email("you@csd.gr", "ria@csd.gr", "SPAM SPAM", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc arcu libero, aliquam a aliquam eget, pulvinar ut neque."));
		a2.addEmail(new Email("nick@csd.gr", "ria@csd.gr", "Antispam movement", "Maecenas a augue ut justo tincidunt volutpat. Phasellus eu lectus a nisi accumsan consequat."));
		a2.addEmail(new Email("nick@csd.gr", "ria@csd.gr", "Learn Java", "Cras in risus quis sapien fermentum sagittis. Aliquam pretium nec tortor non egestas. Etiam facilisis nibh vitae libero finibus, quis imperdiet diam rutrum."));
		
		a3.addEmail(new Email("ria@csd.gr", "you@csd.gr", "Today's class canceled", "Integer ullamcorper lobortis pretium. Donec dignissim eu velit quis interdum. "));
		a3.addEmail(new Email("nick@csd.gr", "you@csd.gr", "Hi, Do you have a minute to talk about C++?", "Donec dignissim eu velit quis interdum. Cras egestas nulla tincidunt velit lacinia, eu malesuada urna sodales."));
		a3.addEmail(new Email("ria@csd.gr", "you@csd.gr", "Hello!", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc arcu libero, aliquam a aliquam eget."));
		
		addAccount(a1);
		addAccount(a2);
		addAccount(a3);
	}
	
	/*
	 * Εκκίνει τον server. Ακούει στην port και στέλνει κάθε σύνδεση σε ένα νέο session
	 */
	private void start() {
		try {
			server = new ServerSocket(port);
			System.out.println("Waiting for connections on port " + port);
		} catch (IOException e) {
			System.out.println("[ERROR] " + e.getMessage());
			System.exit(1);
		}
		
		while(true) {
			Socket client = null;
			
			try {
				client = server.accept();
			} catch (IOException e) {
				continue;
			}
			
			System.out.println("Connected with " + client.getInetAddress() + ":" + client.getPort());
			
			Session session = new Session(client);
			sessions.add(session);
		}
	}
	
	/*
	 * Προσθέτει ένα νέο Account σε περίπτωση που το username δεν υπάρχει ήδη
	 */
	public synchronized boolean addAccount(Account account) {
		if (getAccountByUsername(account.getUsername()) != null)
			return false;
		
		if (!Account.isValidUsername(account.getUsername()))
			return false;
		
		accounts.add(account);
		return true;
	}
	
	/*
	 * Επιστρέφει το Account με βάση το username. Αν δεν υπάρχει επιστρέφει null
	 */
	public synchronized Account getAccountByUsername(String username) {
		for (Account account: accounts) {
			if (username.equals(account.getUsername()))
				return account;
		}
		
		return null;
	}
	
	/*
	 * Αφαιρεί το τερματισμένο Session απο την λίστα με τα ενεργά sessions
	 */
	public synchronized void removeSession(Session session) {
		System.out.println("Session disconnected ");
		sessions.remove(session);
	}

	public static void main(String[] args) {
		if (args.length == 1)
			new MailServer(Integer.parseInt(args[0]));
		else
			new MailServer();
	}

}

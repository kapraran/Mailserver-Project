package com.nikosk93.email.user;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Account {
	private static int idCounter = 1;

	private int id;
	private String username;
	private String password;
	private List<Email> mailbox;
	
	public Account(String username, String password) {
		assingId();
		
		this.username = username;
		this.password = password;
		mailbox = new ArrayList<Email>();
	}
	
	/*
	 * Θέτει ένα μοναδικό id στο Account
	 */
	private void assingId() {
		id = idCounter++;
	}
	
	/*
	 * Ελέγχει αν το password είναι ίδιο με το password του Account
	 */
	public boolean passwordEquals(String password) {
		return this.password.equals(password);
	}
	
	/*
	 * Επιστρέφει το id
	 */
	public int getId() {
		return id;
	}

	/*
	 * Επιστρέφει το username
	 */
	public String getUsername() {
		return username;
	}

	/*
	 * Επιστρέφει το mailbox
	 */
	public List<Email> getMailbox() {
		return mailbox;
	}
	
	/*
	 * Επιστρέφει το email με βάση το id του. Αν δεν υπάρχει επιστρέφει null
	 */
	public Email getEmail(int id) {
		for (Email email : mailbox) {
			if (email.getId() == id)
				return email;
		}
		
		return null;
	}
	
	/*
	 * Προσθέτει το email στο mailbox
	 */
	public boolean addEmail(Email email) {
		if (email.getReceiver().equals(email.getSender()))
			return false;
		
		if (!email.getReceiver().equals(username))
			return false;
		
		mailbox.add(0, email);
		return true;
	}
	
	/*
	 * Διαγράφει το email από το mailbox αν υπάρχει
	 */
	public boolean deleteEmail(int id) {
		Email email = getEmail(id);
		
		if (email == null)
			return false;
		
		return mailbox.remove(email);
	}
	
	/*
	 * Ελέγχει αν το username είναι της μορφής ενός email
	 */
	public static boolean isValidUsername(String username) {
		Pattern pattern = Pattern.compile(".+@.+\\..+");
		Matcher matcher = pattern.matcher(username);
		
		return matcher.matches();
	}
}

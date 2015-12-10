package com.nikosk93.email.user;

public class Email {
	private static int idCounter = 1;

	private int id;
	private boolean isNew = true;
	private String sender;
	private String receiver;
	private String subject;
	private String mainbody;
	
	public Email(String sender, String receiver, String subject, String mainbody) {
		assingId();
		
		this.sender = sender;
		this.receiver = receiver;
		this.subject = subject;
		this.mainbody = mainbody;
	}
	
	/*
	 * Θέτει ένα μοναδικό id στο Email
	 */
	private void assingId() {
		id = idCounter++;
	}
	
	/*
	 * Ελέγχει να έχει διαβαστεί
	 */
	public boolean isUnread() {
		return isNew;
	}
	
	/*
	 * Θέτει το email ως διαβασμένο
	 */
	public void setAsRead() {
		isNew = false;
	}
	
	/*
	 * Επιστρέφει το id
	 */
	public int getId() {
		return id;
	}

	/*
	 * Επιστρέφει τον sender
	 */
	public String getSender() {
		return sender;
	}
	
	/*
	 * Επιστρέφει τον receiver
	 */
	public String getReceiver() {
		return receiver;
	}
	
	/*
	 * Επιστρέφει το subject
	 */
	public String getSubject() {
		return subject;
	}
	
	/*
	 * Επιστρέφει το mainbody
	 */
	public String getMainbody() {
		return mainbody;
	}
}

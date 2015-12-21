package com.nikosk93.email.utils;

public class BufferedString {
	protected String buffer;
	
	public BufferedString() {
		this("");
	}
	
	public BufferedString(String str) {
		set(str);
	}
	
	/*
	 * Θέτει το περιεχόμενο του buffer
	 */
	public void set(String str) {
		buffer = str;
	}
	
	/*
	 * Καθαρίζει το buffer
	 */
	public void clear() {
		buffer = "";
	}
	
	/*
	 * Προσθέτει περιεχόμενο στο buffer
	 */
	public void add(String str) {
		buffer += str;
	}
	
	/*
	 * Προσθέτει περιεχόμενο στο buffer και αλλάζει σειρά
	 */
	public void addLine(String str) {
		add(str + System.lineSeparator());
	}
	
	/*
	 * Αλλάζει σειρά
	 */
	public void addSeparator() {
		add(System.lineSeparator());
	}
	
	/*
	 * Προσθέτει περιεχόμενο στο buffer συγκεκριμένου μήκους
	 */
	public void addFixedLengthString(String str, int length) {
		int diff = length - str.length();
		String fixedStr = str;
		
		if (diff <= 0) {
			fixedStr = fixedStr.substring(0, length);
		} else {
			String pad = "";
			for (int i=0; i < diff; i++) pad += " ";
			
			fixedStr += pad;
		}
		
		add(fixedStr);
	}
	
	/*
	 * Προσθέτει ένα pattern στο buffer
	 */
	public void addPatternLine(char symbol, int repeat) {
		String pattern = "";
		for (int i=0; i < repeat; i++) pattern += symbol;
		
		addLine(pattern);
	}
	
	/*
	 * Ελέγχει αν ο buffer είναι άδειος
	 */
	public boolean isEmpty() {
		return buffer.length() < 1;
	}
	
	/*
	 * Επιστρέφει το περιεχόμενο του buffer και τον καθαρίζει
	 */
	public String flush() {
		String content = buffer;
		clear();
		
		return content;
	}
}

package com.arcao.geocaching4locus.util;

public class Account {
	private final static byte[] KEY = "@3$gT9^#i0-Wqa{'](".getBytes();
	
	private final String userName;
	private final String password;
	private String session = null;

	public Account(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}
	
	public Account(String userName, String password, String session) {
		this(userName, password);
		
		if (session!= null && session.length() > 0)
			this.session = session;
	}

	public String getUserName() {
		return userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getSession() {
		return session;
	}
	
	public void setSession(String session) {
		this.session = session;
	}
	
	public static Account decrypt(String crypted) {
		try {
			byte[] data = Base64.decode(crypted, Base64.URL_SAFE);
			
			byte previous = 0;
			for (int i = 0; i < data.length; i++) {
				byte tmp = data[i];
				data[i] ^= (KEY[i % KEY.length] ^ previous);
				previous = tmp;
			}
			
			String[] parts = new String(data, "UTF-8").split("\n");
			
			
			return new Account(parts[0], parts[1], parts[2]);
		} catch(Exception e) {
			return null;
		}
	}
	
	public static String encrypt(String userName, String password, String session) {
		try {
			if (session == null)
				session = "";
			
			byte[] data = new StringBuilder(userName)
								.append("\n")
								.append(password)
								.append("\n")
								.append(session)
								.toString()
								.getBytes("UTF-8");
			
			byte previous = 0;
			for (int i = 0; i < data.length; i++) {
				data[i] ^= (KEY[i % KEY.length] ^ previous);
				previous = data[i];
			}

			return Base64.encodeToString(data, Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String encrypt(Account account) {
		return encrypt(account.getUserName(), account.getPassword(), account.getSession());
	}
	
	public String encrypt() {
		return encrypt(this);
	}
	
	@Override
	public String toString() {
		return "{UserName: " + userName + "; Password: " + password + "; Session: " + ((session == null) ? "" : session) + "}";
	}
}

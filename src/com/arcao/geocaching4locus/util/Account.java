package com.arcao.geocaching4locus.util;

public class Account {
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
	
	public boolean isValid() {
		return userName != null && userName.length() > 0 && password != null && password.length() > 0;
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
	
	@Override
	public String toString() {
		return "{UserName: " + userName + "; Password: " + password + "; Session: " + ((session == null) ? "" : session) + "}";
	}
}

package geocaching.api.data;

import geocaching.api.data.type.MemberType;

public class User {
	protected String avatarUrl;
	protected int findCount;
	protected int hideCount;
	protected float[] homeCoordinates;
	protected long id;
	protected boolean admin;
	protected MemberType memberType;
	protected String publicGuid;
	protected String userName;
	
	public User(String avatarUrl, int findCount, int hideCount, float[] homeCoordinates, long id, boolean admin, MemberType memberType, String publicGuid,
			String userName) {
		this.avatarUrl = avatarUrl;
		this.findCount = findCount;
		this.hideCount = hideCount;
		this.homeCoordinates = homeCoordinates;
		this.id = id;
		this.admin = admin;
		this.memberType = memberType;
		this.publicGuid = publicGuid;
		this.userName = userName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public int getFindCount() {
		return findCount;
	}

	public int getHideCount() {
		return hideCount;
	}

	public float[] getHomeCoordinates() {
		return homeCoordinates;
	}

	public long getId() {
		return id;
	}

	public boolean isAdmin() {
		return admin;
	}

	public MemberType getMemberType() {
		return memberType;
	}

	public String getPublicGuid() {
		return publicGuid;
	}

	public String getUserName() {
		return userName;
	}
}

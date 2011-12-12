package geocaching.api.data;


public class UserProfile {
	//protected List<FavoritePoint> favoritePoints;
	//protected GeocacheFindStats findStats;
	//protected PublicProfile publicProfile;
	//protected List<Souvenir> souvenirs;
	//protected GlobalStats globalStats;
	//protected TrackableStats trackableStats;
	protected User user;
	
	public UserProfile(/*List<FavoritePoint> favoritePoints, GeocacheFindStats findStats, PublicProfile publicProfile, List<Souvenir> souvenirs,
			GlobalStats globalStats, TrackableStats trackableStats,*/ User user) {
/*		this.favoritePoints = favoritePoints;
		this.findStats = findStats;
		this.publicProfile = publicProfile;
		this.souvenirs = souvenirs;
		this.globalStats = globalStats;
		this.trackableStats = trackableStats;*/
		this.user = user;
	}
	
	
}

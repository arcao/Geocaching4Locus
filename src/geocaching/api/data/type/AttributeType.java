package geocaching.api.data.type;

public enum AttributeType {
	DogsYes(1, "Dogs", true, "http://www.geocaching.com/images/attributes/dogs-yes.gif"),
	DogsNo(1, "Dogs", false, "http://www.geocaching.com/images/attributes/dogs-no.gif"),
	FeeYes(2, "Access or parking fee", true, "http://www.geocaching.com/images/attributes/fee-yes.gif"),
	FeeNo(2, "Access or parking fee", false, "http://www.geocaching.com/images/attributes/fee-no.gif"),
	RappellingYes(3, "Climbing gear", true, "http://www.geocaching.com/images/attributes/rappelling-yes.gif"),
	RappellingNo(3, "Climbing gear", false, "http://www.geocaching.com/images/attributes/rappelling-no.gif"),
	BoatYes(4, "Boat", true, "http://www.geocaching.com/images/attributes/boat-yes.gif"),
	BoatNo(4, "Boat", false, "http://www.geocaching.com/images/attributes/boat-no.gif"),
	ScubaYes(5, "Scuba gear", true, "http://www.geocaching.com/images/attributes/scuba-yes.gif"),
	ScubaNo(5, "Scuba gear", false, "http://www.geocaching.com/images/attributes/scuba-no.gif"),
	KidsYes(6, "Recommended for kids", true, "http://www.geocaching.com/images/attributes/kids-yes.gif"),
	KidsNo(6, "Recommended for kids", false, "http://www.geocaching.com/images/attributes/kids-no.gif"),
	OnehourYes(7, "Takes less than an hour", true, "http://www.geocaching.com/images/attributes/onehour-yes.gif"),
	OnehourNo(7, "Takes less than an hour", false, "http://www.geocaching.com/images/attributes/onehour-no.gif"),
	ScenicYes(8, "Scenic view", true, "http://www.geocaching.com/images/attributes/scenic-yes.gif"),
	ScenicNo(8, "Scenic view", false, "http://www.geocaching.com/images/attributes/scenic-no.gif"),
	HikingYes(9, "Significant Hike", true, "http://www.geocaching.com/images/attributes/hiking-yes.gif"),
	HikingNo(9, "Significant Hike", false, "http://www.geocaching.com/images/attributes/hiking-no.gif"),
	ClimbingYes(10, "Difficult climbing", true, "http://www.geocaching.com/images/attributes/climbing-yes.gif"),
	ClimbingNo(10, "Difficult climbing", false, "http://www.geocaching.com/images/attributes/climbing-no.gif"),
	WadingYes(11, "May require wading", true, "http://www.geocaching.com/images/attributes/wading-yes.gif"),
	WadingNo(11, "May require wading", false, "http://www.geocaching.com/images/attributes/wading-no.gif"),
	SwimmingYes(12, "May require swimming", true, "http://www.geocaching.com/images/attributes/swimming-yes.gif"),
	SwimmingNo(12, "May require swimming", false, "http://www.geocaching.com/images/attributes/swimming-no.gif"),
	AvailableYes(13, "Available at all times", true, "http://www.geocaching.com/images/attributes/available-yes.gif"),
	AvailableNo(13, "Available at all times", false, "http://www.geocaching.com/images/attributes/available-no.gif"),
	NightYes(14, "Recommended at night", true, "http://www.geocaching.com/images/attributes/night-yes.gif"),
	NightNo(14, "Recommended at night", false, "http://www.geocaching.com/images/attributes/night-no.gif"),
	WinterYes(15, "Available during winter", true, "http://www.geocaching.com/images/attributes/winter-yes.gif"),
	WinterNo(15, "Available during winter", false, "http://www.geocaching.com/images/attributes/winter-no.gif"),
	CactusYes(16, "Cactus", true, "http://www.geocaching.com/images/attributes/cactus-yes.gif"),
	CactusNo(16, "Cactus", false, "http://www.geocaching.com/images/attributes/cactus-no.gif"),
	PoisonoakYes(17, "Poison plants", true, "http://www.geocaching.com/images/attributes/poisonoak-yes.gif"),
	PoisonoakNo(17, "Poison plants", false, "http://www.geocaching.com/images/attributes/poisonoak-no.gif"),
	DangerousAnimalsYes(18, "Dangerous Animals", true, "http://www.geocaching.com/images/attributes/dangerousanimals-yes.gif"),
	DangerousAnimalsNo(18, "Dangerous Animals", false, "http://www.geocaching.com/images/attributes/dangerousanimals-no.gif"),
	TicksYes(19, "Ticks", true, "http://www.geocaching.com/images/attributes/ticks-yes.gif"),
	TicksNo(19, "Ticks", false, "http://www.geocaching.com/images/attributes/ticks-no.gif"),
	MineYes(20, "Abandoned mines", true, "http://www.geocaching.com/images/attributes/mine-yes.gif"),
	MineNo(20, "Abandoned mines", false, "http://www.geocaching.com/images/attributes/mine-no.gif"),
	CliffYes(21, "Cliff / falling rocks", true, "http://www.geocaching.com/images/attributes/cliff-yes.gif"),
	CliffNo(21, "Cliff / falling rocks", false, "http://www.geocaching.com/images/attributes/cliff-no.gif"),
	HuntingYes(22, "Hunting", true, "http://www.geocaching.com/images/attributes/hunting-yes.gif"),
	HuntingNo(22, "Hunting", false, "http://www.geocaching.com/images/attributes/hunting-no.gif"),
	DangerYes(23, "Dangerous area", true, "http://www.geocaching.com/images/attributes/danger-yes.gif"),
	DangerNo(23, "Dangerous area", false, "http://www.geocaching.com/images/attributes/danger-no.gif"),
	WheelchairYes(24, "Wheelchair accessible", true, "http://www.geocaching.com/images/attributes/wheelchair-yes.gif"),
	WheelchairNo(24, "Wheelchair accessible", false, "http://www.geocaching.com/images/attributes/wheelchair-no.gif"),
	ParkingYes(25, "Parking available", true, "http://www.geocaching.com/images/attributes/parking-yes.gif"),
	ParkingNo(25, "Parking available", false, "http://www.geocaching.com/images/attributes/parking-no.gif"),
	PublicYes(26, "Public transportation", true, "http://www.geocaching.com/images/attributes/public-yes.gif"),
	PublicNo(26, "Public transportation", false, "http://www.geocaching.com/images/attributes/public-no.gif"),
	WaterYes(27, "Drinking water nearby", true, "http://www.geocaching.com/images/attributes/water-yes.gif"),
	WaterNo(27, "Drinking water nearby", false, "http://www.geocaching.com/images/attributes/water-no.gif"),
	RestroomsYes(28, "Public restrooms nearby", true, "http://www.geocaching.com/images/attributes/restrooms-yes.gif"),
	RestroomsNo(28, "Public restrooms nearby", false, "http://www.geocaching.com/images/attributes/restrooms-no.gif"),
	PhoneYes(29, "Telephone nearby", true, "http://www.geocaching.com/images/attributes/phone-yes.gif"),
	PhoneNo(29, "Telephone nearby", false, "http://www.geocaching.com/images/attributes/phone-no.gif"),
	PicnicYes(30, "Picnic tables nearby", true, "http://www.geocaching.com/images/attributes/picnic-yes.gif"),
	PicnicNo(30, "Picnic tables nearby", false, "http://www.geocaching.com/images/attributes/picnic-no.gif"),
	CampingYes(31, "Camping available", true, "http://www.geocaching.com/images/attributes/camping-yes.gif"),
	CampingNo(31, "Camping available", false, "http://www.geocaching.com/images/attributes/camping-no.gif"),
	BicyclesYes(32, "Bicycles", true, "http://www.geocaching.com/images/attributes/bicycles-yes.gif"),
	BicyclesNo(32, "Bicycles", false, "http://www.geocaching.com/images/attributes/bicycles-no.gif"),
	MotorcyclesYes(33, "Motorcycles", true, "http://www.geocaching.com/images/attributes/motorcycles-yes.gif"),
	MotorcyclesNo(33, "Motorcycles", false, "http://www.geocaching.com/images/attributes/motorcycles-no.gif"),
	QuadsYes(34, "Quads", true, "http://www.geocaching.com/images/attributes/quads-yes.gif"),
	QuadsNo(34, "Quads", false, "http://www.geocaching.com/images/attributes/quads-no.gif"),
	JeepsYes(35, "Off-road vehicles", true, "http://www.geocaching.com/images/attributes/jeeps-yes.gif"),
	JeepsNo(35, "Off-road vehicles", false, "http://www.geocaching.com/images/attributes/jeeps-no.gif"),
	SnowmobilesYes(36, "Snowmobiles", true, "http://www.geocaching.com/images/attributes/snowmobiles-yes.gif"),
	SnowmobilesNo(36, "Snowmobiles", false, "http://www.geocaching.com/images/attributes/snowmobiles-no.gif"),
	HorsesYes(37, "Horses", true, "http://www.geocaching.com/images/attributes/horses-yes.gif"),
	HorsesNo(37, "Horses", false, "http://www.geocaching.com/images/attributes/horses-no.gif"),
	CampfiresYes(38, "Campfires", true, "http://www.geocaching.com/images/attributes/campfires-yes.gif"),
	CampfiresNo(38, "Campfires", false, "http://www.geocaching.com/images/attributes/campfires-no.gif"),
	ThornYes(39, "Thorns", true, "http://www.geocaching.com/images/attributes/thorn-yes.gif"),
	ThornNo(39, "Thorns", false, "http://www.geocaching.com/images/attributes/thorn-no.gif"),
	StealthYes(40, "Stealth required", true, "http://www.geocaching.com/images/attributes/stealth-yes.gif"),
	StealthNo(40, "Stealth required", false, "http://www.geocaching.com/images/attributes/stealth-no.gif"),
	StrollerYes(41, "Stroller accessible", true, "http://www.geocaching.com/images/attributes/stroller-yes.gif"),
	StrollerNo(41, "Stroller accessible", false, "http://www.geocaching.com/images/attributes/stroller-no.gif"),
	FirstaidYes(42, "Needs maintenance", true, "http://www.geocaching.com/images/attributes/firstaid-yes.gif"),
	FirstaidNo(42, "Needs maintenance", false, "http://www.geocaching.com/images/attributes/firstaid-no.gif"),
	CowYes(43, "Watch for livestock", true, "http://www.geocaching.com/images/attributes/cow-yes.gif"),
	CowNo(43, "Watch for livestock", false, "http://www.geocaching.com/images/attributes/cow-no.gif"),
	FlashlightYes(44, "Flashlight required", true, "http://www.geocaching.com/images/attributes/flashlight-yes.gif"),
	FlashlightNo(44, "Flashlight required", false, "http://www.geocaching.com/images/attributes/flashlight-no.gif"),
	LandfYes(45, "Lost And Found Tour", true, "http://www.geocaching.com/images/attributes/landf-yes.gif"),
	LandfNo(45, "Lost And Found Tour", false, "http://www.geocaching.com/images/attributes/landf-no.gif"),
	RvYes(46, "Truck Driver/RV", true, "http://www.geocaching.com/images/attributes/rv-yes.gif"),
	RvNo(46, "Truck Driver/RV", false, "http://www.geocaching.com/images/attributes/rv-no.gif"),
	FieldPuzzleYes(47, "Field Puzzle", true, "http://www.geocaching.com/images/attributes/field_puzzle-yes.gif"),
	FieldPuzzleNo(47, "Field Puzzle", false, "http://www.geocaching.com/images/attributes/field_puzzle-no.gif"),
	UVYes(48, "UV Light Required", true, "http://www.geocaching.com/images/attributes/UV-yes.gif"),
	UVNo(48, "UV Light Required", false, "http://www.geocaching.com/images/attributes/UV-no.gif"),
	SnowshoesYes(49, "Snowshoes", true, "http://www.geocaching.com/images/attributes/snowshoes-yes.gif"),
	SnowshoesNo(49, "Snowshoes", false, "http://www.geocaching.com/images/attributes/snowshoes-no.gif"),
	SkiisYes(50, "Cross Country Skis", true, "http://www.geocaching.com/images/attributes/skiis-yes.gif"),
	SkiisNo(50, "Cross Country Skis", false, "http://www.geocaching.com/images/attributes/skiis-no.gif"),
	SToolYes(51, "Special Tool Required", true, "http://www.geocaching.com/images/attributes/s-tool-yes.gif"),
	SToolNo(51, "Special Tool Required", false, "http://www.geocaching.com/images/attributes/s-tool-no.gif"),
	NightcacheYes(52, "Night Cache", true, "http://www.geocaching.com/images/attributes/nightcache-yes.gif"),
	NightcacheNo(52, "Night Cache", false, "http://www.geocaching.com/images/attributes/nightcache-no.gif"),
	ParkNGrabYes(53, "Park and Grab", true, "http://www.geocaching.com/images/attributes/parkngrab-yes.gif"),
	ParkNGrabNo(53, "Park and Grab", false, "http://www.geocaching.com/images/attributes/parkngrab-no.gif"),
	AbandonedBuildingYes(54, "Abandoned Structure", true, "http://www.geocaching.com/images/attributes/AbandonedBuilding-yes.gif"),
	AbandonedBuildingNo(54, "Abandoned Structure", false, "http://www.geocaching.com/images/attributes/AbandonedBuilding-no.gif"),
	HikeShortYes(55, "Short hike (less than 1km)", true, "http://www.geocaching.com/images/attributes/hike_short-yes.gif"),
	HikeShortNo(55, "Short hike (less than 1km)", false, "http://www.geocaching.com/images/attributes/hike_short-no.gif"),
	HikeMedYes(56, "Medium hike (1km-10km)", true, "http://www.geocaching.com/images/attributes/hike_med-yes.gif"),
	HikeMedNo(56, "Medium hike (1km-10km)", false, "http://www.geocaching.com/images/attributes/hike_med-no.gif"),
	HikeLongYes(57, "Long Hike (+10km)", true, "http://www.geocaching.com/images/attributes/hike_long-yes.gif"),
	HikeLongNo(57, "Long Hike (+10km)", false, "http://www.geocaching.com/images/attributes/hike_long-no.gif"),
	FuelYes(58, "Fuel Nearby", true, "http://www.geocaching.com/images/attributes/fuel-yes.gif"),
	FuelNo(58, "Fuel Nearby", false, "http://www.geocaching.com/images/attributes/fuel-no.gif"),
	FoodYes(59, "Food Nearby", true, "http://www.geocaching.com/images/attributes/food-yes.gif"),
	FoodNo(59, "Food Nearby", false, "http://www.geocaching.com/images/attributes/food-no.gif"),
	WirelessBeaconYes(60, "Wireless Beacon", true, "http://www.geocaching.com/images/attributes/wirelessbeacon-yes.gif"),
	WirelessBeaconNo(60, "Wireless Beacon", false, "http://www.geocaching.com/images/attributes/wirelessbeacon-no.gif"),
	PartnershipYes(61, "Partnership Cache", true, "http://www.geocaching.com/images/attributes/partnership-yes.gif"),
	PartnershipNo(61, "Partnership Cache", false, "http://www.geocaching.com/images/attributes/partnership-no.gif"),
	SeasonalYes(62, "Seasonal Access", true, "http://www.geocaching.com/images/attributes/seasonal-yes.gif"),
	SeasonalNo(62, "Seasonal Access", false, "http://www.geocaching.com/images/attributes/seasonal-no.gif"),
	TouristOkYes(63, "Tourist Friendly", true, "http://www.geocaching.com/images/attributes/touristOK-yes.gif"),
	TouristOkNo(63, "Tourist Friendly", false, "http://www.geocaching.com/images/attributes/touristOK-no.gif"),
	TreeClimbingYes(64, "Tree Climbing", true, "http://www.geocaching.com/images/attributes/treeclimbing-yes.gif"),
	TreeClimbingNo(64, "Tree Climbing", false, "http://www.geocaching.com/images/attributes/treeclimbing-no.gif"),
	FrontYardYes(65, "Front Yard (Private Residence)", true, "http://www.geocaching.com/images/attributes/frontyard-yes.gif"),
	FrontYardNo(65, "Front Yard (Private Residence)", false, "http://www.geocaching.com/images/attributes/frontyard-no.gif"),
	TeamworkYes(66, "Teamwork Required", true, "http://www.geocaching.com/images/attributes/teamwork-yes.gif"),
	TeamworkNo(66, "Teamwork Required", false, "http://www.geocaching.com/images/attributes/teamwork-no.gif");
	
	private int id;
	private String name;
	private boolean on;
	private String imageUrl;
	
	private AttributeType(int id, String name, boolean on, String imageUrl) {
		this.id = id;
		this.name = name;
		this.on = on;
		this.imageUrl = imageUrl;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isOn() {
		return on;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	
	public static AttributeType parseAttributeTypeByGroundSpeakId(int groundSpeakId, boolean on) {
		for (AttributeType type : values()) {
			if (type.getId() == groundSpeakId && type.isOn() == on)
				return type;
		}
		
		return (on) ? DogsYes : DogsNo;
	}
}

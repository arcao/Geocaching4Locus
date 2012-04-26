package com.arcao.geocaching4locus.constants;

import android.net.Uri;

public interface AppConstants {
	static final String CONSUMER_KEY = "90C7F340-7998-477D-B4D3-AC48A9A0F560";
	static final String LICENCE_KEY = "40940392-0C8E-487B-BC40-EA250D6D9AE0";

	static final int CACHES_PER_REQUEST = 10;

	static final String BUGSENSE_URI = "http://www.bugsense.com/api/acra?api_key=9c8b7588";
	static final String ERROR_FORM_KEY = "dFJfSDQzTlI2ZzhxaFJndm1MYjhkWHc6MQ";

	static final Uri MANUAL_URI = Uri.parse("http://geocaching4locus.eu/manual/");
	static final Uri WEBSITE_URI = Uri.parse("http://g4l.arcao.com");
	static final String DONATE_PAYPAL_URI = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=arcao%%40arcao%%2ecom&lc=CZ&item_name=Geocaching4Locus&item_number=g4l&currency_code=%s&bn=PP%%2dDonationsBF%%3abtn_donateCC_LG%%2egif%%3aNonHosted";
}

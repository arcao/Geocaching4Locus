package com.arcao.geocaching.api.oauth.services;

import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.TokenExtractorImpl;
import org.scribe.model.Token;
import org.scribe.utils.OAuthEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomTokenExtractorImpl extends TokenExtractorImpl {
	private static final Pattern ERROR_MESSAGE_REGEX = Pattern.compile("oauth_error_message=([^&]*)");

	@Override
	public Token extract(String response) {
		if (response != null) {
			checkError(response);
		}

		return super.extract(response);
	}

	private static void checkError(CharSequence response) {
		Matcher matcher = ERROR_MESSAGE_REGEX.matcher(response);
		if (matcher.find() && matcher.groupCount() >= 1)
		{
			throw new OAuthException(OAuthEncoder.decode(matcher.group(1)));
		}
	}
}

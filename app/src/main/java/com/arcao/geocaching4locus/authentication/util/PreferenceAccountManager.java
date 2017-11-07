package com.arcao.geocaching4locus.authentication.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arcao.geocaching.api.data.User;
import com.arcao.geocaching.api.data.coordinates.Coordinates;
import com.arcao.geocaching.api.data.type.MemberType;
import com.arcao.geocaching4locus.authentication.LoginActivity;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.github.scribejava.core.model.OAuth1RequestToken;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PreferenceAccountManager implements AccountManager {
    private final SharedPreferences preferences;
    private final Context context;
    private final AccountRestrictions restrictions;
    private Account account;
    private long lastAccountUpdateTime;

    public PreferenceAccountManager(Context context) {
        // Do not store username, password and hash in default shared preferences
        // PreferencesBackupAgent backup default shared preferences to Google Backup Service
        this.context = context.getApplicationContext();

        preferences = this.context.getSharedPreferences(PrefConstants.ACCOUNT_STORAGE_NAME, Context.MODE_PRIVATE);
        restrictions = new AccountRestrictions(this.context);

        load();
    }

    private void load() {
        upgradeStorage();

        lastAccountUpdateTime = preferences.getLong(PrefConstants.ACCOUNT_LAST_ACCOUNT_UPDATE_TIME, 0);
        String userName = preferences.getString(PrefConstants.ACCOUNT_USERNAME, null);

        if (userName == null)
            return;

        account = Account.builder()
                .name(userName)
                .premium(preferences.getBoolean(PrefConstants.ACCOUNT_PREMIUM, false))
                .avatarUrl(preferences.getString(PrefConstants.ACCOUNT_AVATAR_URL, null))
                .homeCoordinates(Coordinates.builder()
                        .latitude(preferences.getFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LAT, Float.NaN))
                        .longitude(preferences.getFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LON, Float.NaN))
                        .build())
                .build();
    }

    private void store() {
        if (account != null) {
            preferences.edit()
                    .putString(PrefConstants.ACCOUNT_USERNAME, account.name())
                    .putBoolean(PrefConstants.ACCOUNT_PREMIUM, account.premium())
                    .putString(PrefConstants.ACCOUNT_AVATAR_URL, account.avatarUrl())
                    .putFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LAT, Float.NaN)
                    .putFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LON, Float.NaN)
                    .apply();

            Coordinates homeCoordinates = account.homeCoordinates();
            if (homeCoordinates != null) {
                preferences.edit()
                        .putFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LAT, (float) homeCoordinates.latitude())
                        .putFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LON, (float) homeCoordinates.longitude())
                        .apply();
            }
        } else {
            removeAccount();
        }
    }

    @Override
    @NonNull
    public AccountRestrictions getRestrictions() {
        return restrictions;
    }

    @Override
    @Nullable
    public Account getAccount() {
        return account;
    }

    @NonNull
    @Override
    public Account createAccount(@NonNull User user) {
        return Account.builder()
                .name(user.userName())
                .premium(user.memberType() == MemberType.Premium || user.memberType() == MemberType.Charter)
                .avatarUrl(user.avatarUrl())
                .homeCoordinates(user.homeCoordinates())
                .build();
    }

    @Override
    public void addAccount(@NonNull Account account) {
        if (this.account != null)
            removeAccount();

        this.account = account;
        lastAccountUpdateTime = new Date().getTime();
        store();

        restrictions.applyRestrictions(account.premium());
    }

    @Override
    public void removeAccount() {
        account = null;

        preferences.edit()
                .remove(PrefConstants.ACCOUNT_USERNAME)
                .remove(PrefConstants.ACCOUNT_SESSION)
                .remove(PrefConstants.ACCOUNT_PREMIUM)
                .remove(PrefConstants.ACCOUNT_AVATAR_URL)
                .remove(PrefConstants.ACCOUNT_HOME_COORDINATES_LAT)
                .remove(PrefConstants.ACCOUNT_HOME_COORDINATES_LON)
                .apply();

        restrictions.remove();
    }

    @Override
    public boolean isPremium() {
        return account != null && account.premium();
    }

    public boolean isAccountUpdateRequired() {
        return TimeUnit.MILLISECONDS.toDays(new Date().getTime() - lastAccountUpdateTime) > 0;
    }

    public void updateAccountNextTime() {
        lastAccountUpdateTime = 0;
        store();
    }

    public void updateAccount(@NonNull Account account) {
        this.account = account;
        lastAccountUpdateTime = new Date().getTime();
        store();
    }

    @Override
    @Nullable
    public String getOAuthToken() {
        if (account == null)
            return null;

        return preferences.getString(PrefConstants.ACCOUNT_SESSION, null);
    }

    @Override
    public void setOAuthToken(@Nullable String authToken) {
        if (account == null)
            return;

        Editor editor = preferences.edit();
        if (authToken != null) {
            editor.putString(PrefConstants.ACCOUNT_SESSION, authToken);
        } else {
            editor.remove(PrefConstants.ACCOUNT_SESSION);
        }
        editor.apply();
    }

    @Override
    public void invalidateOAuthToken() {
        preferences.edit().remove(PrefConstants.ACCOUNT_SESSION).apply();
    }

    private void upgradeStorage() {
        SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(context);

        int prefVersion = preferences.getInt(PrefConstants.PREF_VERSION, 0);
        if (prefVersion < 1) {
            // remove user name, password and session from old storage
            defaultPref.edit()
                    .remove(PrefConstants.ACCOUNT_USERNAME)
                    .remove(PrefConstants.ACCOUNT_PASSWORD)
                    .remove(PrefConstants.ACCOUNT_SESSION)
                    .apply();
        }
        if (prefVersion < 3) {
            // remove old account with unset home coordinates and avatar
            removeAccount();
        }

        // update pref_version to latest one
        if (prefVersion != PrefConstants.CURRENT_PREF_VERSION)
            preferences.edit().putInt(PrefConstants.PREF_VERSION, PrefConstants.CURRENT_PREF_VERSION).apply();
    }

    @Override
    public boolean requestSignOn(@NonNull Activity activity, int requestCode) {
        if (account != null)
            return false;

        activity.startActivityForResult(LoginActivity.createIntent(activity), requestCode);
        return true;
    }

    @Override
    public void setOAuthRequestToken(@Nullable OAuth1RequestToken token) {
        if (token == null || token.isEmpty())
            return;

        preferences.edit()
                .putString(PrefConstants.OAUTH_TOKEN, token.getToken())
                .putString(PrefConstants.OAUTH_TOKEN_SECRET, token.getTokenSecret())
                .putBoolean(PrefConstants.OAUTH_CALLBACK_CONFIRMED, token.isOauthCallbackConfirmed())
                .apply();
    }

    @Override
    @NonNull
    public OAuth1RequestToken getOAuthRequestToken() {
        return new OAuth1RequestToken(
                preferences.getString(PrefConstants.OAUTH_TOKEN, ""),
                preferences.getString(PrefConstants.OAUTH_TOKEN_SECRET, ""),
                preferences.getBoolean(PrefConstants.OAUTH_CALLBACK_CONFIRMED, false),
                null
        );
    }

    @Override
    public void deleteOAuthRequestToken() {
        preferences.edit()
                .remove(PrefConstants.OAUTH_TOKEN)
                .remove(PrefConstants.OAUTH_TOKEN_SECRET)
                .remove(PrefConstants.OAUTH_CALLBACK_CONFIRMED)
                .apply();
    }
}

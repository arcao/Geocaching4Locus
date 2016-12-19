package com.arcao.geocaching4locus.authentication.task;

import android.content.Context;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.UserProfile;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidResponseException;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.util.Account;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.authentication.util.DeviceInfoFactory;

public class GeocachingApiLoginTask {
    private final Context mContext;
    private final GeocachingApi mApi;

    private GeocachingApiLoginTask(Context context, GeocachingApi api) {
        mContext = context.getApplicationContext();
        mApi = api;
    }

    public static GeocachingApiLoginTask create(Context context, GeocachingApi api) {
        return new GeocachingApiLoginTask(context, api);
    }

    public void perform() throws GeocachingApiException {
        AccountManager manager = App.get(mContext).getAccountManager();

        Account account = manager.getAccount();
        if (account == null)
            throw new InvalidCredentialsException("Account not found.");

        String token = manager.getOAuthToken();
        if (token == null) {
            manager.removeAccount();
            throw new InvalidCredentialsException("Account not found.");
        }

        mApi.openSession(token);

        if (manager.isAccountUpdateRequired()) {
            UserProfile userProfile = mApi.getYourUserProfile(false, false, false, false, false, false, DeviceInfoFactory.create(mContext));

            if (userProfile == null)
                throw new InvalidResponseException("User profile is null");

            account = manager.createAccount(userProfile.user());
            manager.updateAccount(account);
        }
    }
}

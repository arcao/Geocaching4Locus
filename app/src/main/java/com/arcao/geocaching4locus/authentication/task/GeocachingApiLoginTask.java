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
    private final Context context;
    private final GeocachingApi api;

    private GeocachingApiLoginTask(Context context, GeocachingApi api) {
        this.context = context.getApplicationContext();
        this.api = api;
    }

    public static GeocachingApiLoginTask create(Context context, GeocachingApi api) {
        return new GeocachingApiLoginTask(context, api);
    }

    public void perform() throws GeocachingApiException {
        AccountManager manager = App.get(context).getAccountManager();

        Account account = manager.getAccount();
        if (account == null)
            throw new InvalidCredentialsException("Account not found.");

        String token = manager.getOAuthToken();
        if (token == null) {
            manager.removeAccount();
            throw new InvalidCredentialsException("Account not found.");
        }

        api.openSession(token);

        if (manager.isAccountUpdateRequired()) {
            UserProfile userProfile = api.getYourUserProfile(false, false, false, false, false, false, DeviceInfoFactory.create(context));

            if (userProfile == null)
                throw new InvalidResponseException("User profile is null");

            account = manager.createAccount(userProfile.user());
            manager.updateAccount(account);
        }
    }
}

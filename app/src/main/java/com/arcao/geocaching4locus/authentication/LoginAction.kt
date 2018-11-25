package com.arcao.geocaching4locus.authentication

import android.content.Intent

sealed class LoginAction {
    class LoginUrlAvailable(val url : String) : LoginAction()
    class Finish(val showBasicMembershipWarning : Boolean) : LoginAction()
    class Error(val intent : Intent) : LoginAction()
    object Cancel : LoginAction()
}

package com.arcao.geocaching4locus.authentication

import android.content.Intent
import com.arcao.geocaching4locus.data.api.model.enums.MembershipType

sealed class LoginAction {
    class LoginUrlAvailable(val url: String) : LoginAction()
    class Finish(val membership: MembershipType) : LoginAction()
    class Error(val intent: Intent) : LoginAction()
    object Cancel : LoginAction()
}

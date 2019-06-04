package locus.api.android

import android.content.Context
import android.content.Intent
import locus.api.android.utils.exceptions.RequiredVersionMissingException

object ActionDisplayInternal {
    @Throws(RequiredVersionMissingException::class)
    internal fun sendData(
        action: String,
        context: Context,
        intent: Intent,
        callImport: Boolean,
        center: Boolean
    ): Boolean = ActionDisplay.sendData(action, context, intent, callImport, center)
}
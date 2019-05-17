package com.arcao.geocaching4locus.data.api.model

class GeocacheLogType(
    id: Int,
    name: String,
    imageUrl: String
) : Type(id, name, imageUrl) {
    companion object {
        /** found the geocache */
        const val FOUND_IT = 2
        /** Did Not Find (DNF) the geocache */
        const val DNF_IT = 3
        /** Adding a comment to the geocache */
        const val WRITE_NOTE = 4
        /** changing the status of the geocache to archived */
        const val ARCHIVE = 5
        /** flagging the geocache as needing to be archived */
        const val NEEDS_ARCHIVING = 7
        /** RSVPing for an event */
        const val WILL_ATTEND = 9
        /** Attended an event (counts as a find) */
        const val ATTENDED = 10
        /** Successfully captured a webcam geocache (counts as a find) */
        const val WEBCAM_PHOTO_TAKEN = 11
        /** changing the status of the geocache from archived to active */
        const val UNARCHIVE = 12
        /** changing the status of the geocache to disabled */
        const val TEMPORARILY_DISABLE_LISTING = 22
        /** changing the status of the geocache from disabled to active */
        const val ENABLE_LISTING = 23
        /** changing the status of the geocache from unpublished to active */
        const val PUBLISH_LISTING = 24
        /** flagging a geocache owner that the geocache needs some attention */
        const val NEEDS_MAINTENANCE = 45
        /** announcing that owner maintenance was done */
        const val OWNER_MAINTENANCE = 46
        /** updating the coordinates of the geocache */
        const val UPDATE_COORDINATES = 47
        /** a note left by the reviewer */
        const val POST_REVIEWER_NOTE = 68
        /** event host announcement to attendees */
        const val EVENT_ANNOUNCEMENT = 74
    }
}
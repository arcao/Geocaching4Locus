package com.arcao.geocaching4locus.data.api.model.enum

enum class GeocacheLogType(override val id: Int, override val value: String) : IdValueType {
    /** found the geocache */
    FOUND_IT(2, "Found It"),
    /** Did Not Find (DNF) the geocache */
    DNF_IT(3, "DNF it"),
    /** Adding a comment to the geocache */
    WRITE_NOTE(4, "Write note"),
    /** changing the status of the geocache to archived */
    ARCHIVE(5, "Archive"),
    /** flagging the geocache as needing to be archived */
    NEEDS_ARCHIVING(7, "Needs archiving"),
    /** RSVPing for an event */
    WILL_ATTEND(9, "Will attend"),
    /** Attended an event (counts as a find) */
    ATTENDED(10, "Attended"),
    /** Successfully captured a webcam geocache (counts as a find) */
    WEBCAM_PHOTO_TAKEN(11, "Webcam photo taken"),
    /** changing the status of the geocache from archived to active */
    UNARCHIVE(12, "Unarchive"),
    /** changing the status of the geocache to disabled */
    TEMPORARILY_DISABLE_LISTING(22, "Temporarily Disable Listing"),
    /** changing the status of the geocache from disabled to active */
    ENABLE_LISTING(23, "Enable Listing"),
    /** changing the status of the geocache from unpublished to active */
    PUBLISH_LISTING(24, "Publish Listing"),
    /** flagging a geocache owner that the geocache needs some attention */
    NEEDS_MAINTENANCE(45, "Needs Maintenance"),
    /** announcing that owner maintenance was done */
    OWNER_MAINTENANCE(46, "Owner Maintenance"),
    /** updating the coordinates of the geocache */
    UPDATE_COORDINATES(47, "Update Coordinates"),
    /** a note left by the reviewer */
    POST_REVIEWER_NOTE(68, "Post Reviewer Note"),
    /** event host announcement to attendees */
    EVENT_ANNOUNCEMENT(74, "Event Announcement");

    companion object {
        fun from(value: String?) = values().find { it.value == value } ?: WRITE_NOTE
    }
}
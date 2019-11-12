package com.arcao.geocaching4locus.data.api.model.enums

enum class StatusCode(override val id: Int) : IdType {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    UNPROCESSABLE_ENTITY(422),
    TOO_MANY_REQUESTS(429),
    INTERNAL_SERVER_ERROR(500),
    SERVICE_UNAVAILABLE(503);

    companion object {
        fun from(id: Int?) = values().find { it.id == id }
    }
}

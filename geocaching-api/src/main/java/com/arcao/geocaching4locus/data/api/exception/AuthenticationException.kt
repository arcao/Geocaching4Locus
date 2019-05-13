package com.arcao.geocaching4locus.data.api.exception

class AuthenticationException(val code : String, message : String?) : Exception(message)

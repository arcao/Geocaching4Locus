/*
  Some parts of this file contains work from c:geo licensed under
  Apache License 2.0.
 */
package com.arcao.geocaching4locus.data.api.util

import com.arcao.geocaching4locus.data.api.model.Coordinates

import java.text.ParseException
import java.util.regex.Pattern

object CoordinatesParser {
    private const val MINUTES_PER_DEGREE = 60.0
    private const val SECONDS_PER_DEGREE = 3600.0

    private val LATITUDE_PATTERN = Pattern.compile(
        //        ( 1 )     ( 2 )          ( 3 )        ( 4 )        ( 5 )
        "\\b([NS])\\s*(\\d+)°?(?:\\s*(\\d+)(?:[.,](\\d+)|'?\\s*(\\d+(?:[.,]\\d+)?)(?:''|\")?)?)?",
        Pattern.CASE_INSENSITIVE
    )
    private val LONGITUDE_PATTERN = Pattern.compile(
        "\\b([WE])\\s*(\\d+)°?(?:\\s*(\\d+)(?:[.,](\\d+)|'?\\s*(\\d+(?:[.,]\\d+)?)(?:''|\")?)?)?",
        Pattern.CASE_INSENSITIVE
    )

    private val LATITUDE_PATTERN_UNSAFE = Pattern.compile(
        //                                                 ( 1 )      ( 2 )            ( 3 )        ( 4 )        ( 5 )
        "(?:(?=[\\-\\w])(?<![\\-\\w])|(?<![^\\-\\w]))([NS]|)\\s*(-?\\d+)°?(?:\\s*(\\d+)(?:[.,](\\d+)|'?\\s*(\\d+(?:[.,]\\d+)?)(?:''|\")?)?)?",
        Pattern.CASE_INSENSITIVE
    )
    private val LONGITUDE_PATTERN_UNSAFE = Pattern.compile(
        "(?:(?=[\\-\\w])(?<![\\-\\w])|(?<![^\\-\\w]))([WE]|)\\s*(-?\\d+)°?(?:\\s*(\\d+)(?:[.,](\\d+)|'?\\s*(\\d+(?:[.,]\\d+)?)(?:''|\")?)?)?",
        Pattern.CASE_INSENSITIVE
    )

    private enum class CoordinateType {
        LAT,
        LON,
        LAT_UNSAFE,
        LON_UNSAFE
    }

    @Throws(ParseException::class)
    fun parse(latitude: String, longitude: String, safe: Boolean = true): Coordinates {
        return Coordinates(
            parseLatitude(latitude, safe),
            parseLongitude(longitude, safe)
        )
    }

    @Throws(ParseException::class)
    fun parseLatitude(latitude: String, safe: Boolean = true): Double {
        return parse(latitude, if (safe) CoordinateType.LAT else CoordinateType.LAT_UNSAFE).result
    }

    @Throws(ParseException::class)
    fun parseLongitude(longitude: String, safe: Boolean = true): Double {
        return parse(longitude, if (safe) CoordinateType.LON else CoordinateType.LON_UNSAFE).result
    }

    @Throws(ParseException::class)
    fun parse(coordinates: String): Coordinates {
        val latitudeWrapper = parse(coordinates, CoordinateType.LAT)
        val lat = latitudeWrapper.result
        // cut away the latitude part when parsing the longitude
        val longitudeWrapper =
            parse(coordinates.substring(latitudeWrapper.matcherPos + latitudeWrapper.matcherLen), CoordinateType.LON)

        if (longitudeWrapper.matcherPos - (latitudeWrapper.matcherPos + latitudeWrapper.matcherLen) >= 10) {
            throw ParseException(
                "Distance between latitude and longitude text is to large.",
                latitudeWrapper.matcherPos + latitudeWrapper.matcherLen + longitudeWrapper.matcherPos
            )
        }

        val lon = longitudeWrapper.result
        return Coordinates(lat, lon)
    }

    @Throws(ParseException::class)
    private fun parse(coordinate: String, coordinateType: CoordinateType): ParseResult {
        val pattern = when (coordinateType) {
            CoordinatesParser.CoordinateType.LAT_UNSAFE -> LATITUDE_PATTERN_UNSAFE
            CoordinatesParser.CoordinateType.LON_UNSAFE -> LONGITUDE_PATTERN_UNSAFE
            CoordinatesParser.CoordinateType.LON -> LONGITUDE_PATTERN
            CoordinatesParser.CoordinateType.LAT -> LATITUDE_PATTERN
        }

        val matcher = pattern.matcher(coordinate)

        if (matcher.find()) {
            var sign = if ("S".equals(matcher.group(1), ignoreCase = true) || "W".equals(
                    matcher.group(1),
                    ignoreCase = true
                )
            ) -1.0 else 1.0
            var degree = matcher.group(2).toDouble()

            if (degree < 0) {
                sign = -1.0
                degree = Math.abs(degree)
            }

            var minutes = 0.0
            var seconds = 0.0

            if (matcher.group(3) != null) {
                minutes = matcher.group(3).toDouble()

                if (matcher.group(4) != null) {
                    seconds = ("0." + matcher.group(4)).toDouble() * MINUTES_PER_DEGREE
                } else if (matcher.group(5) != null) {
                    seconds = matcher.group(5).replace(",", ".").toDouble()
                }
            }

            var result = sign * (degree + minutes / MINUTES_PER_DEGREE + seconds / SECONDS_PER_DEGREE)

            // normalize result
            result = when (coordinateType) {
                CoordinatesParser.CoordinateType.LON_UNSAFE,
                CoordinatesParser.CoordinateType.LON -> normalize(result, -180.0, 180.0)
                CoordinatesParser.CoordinateType.LAT_UNSAFE,
                CoordinatesParser.CoordinateType.LAT -> normalize(result, -90.0, 90.0)
            }

            return ParseResult(result, matcher.start(), matcher.group().length)
        } else {

            // Nothing found with "N 52...", try to match string as decimaldegree
            try {
                val items = coordinate.trim { it <= ' ' }.split(Regex("\\s+"))
                if (items.size > 1) {
                    val index = if (coordinateType == CoordinateType.LON) {
                        items.size - 1
                    } else {
                        0
                    }
                    val pos = if (coordinateType == CoordinateType.LON) {
                        coordinate.lastIndexOf(items[index])
                    } else {
                        coordinate.indexOf(items[index])
                    }

                    return ParseResult(items[index].toDouble(), pos, items[index].length)
                }
            } catch (e: NumberFormatException) {
                // do nothing
            }
        }
        throw ParseException("Could not parse coordinate: \"$coordinate\"", 0)
    }

    /**
     * Normalizes any number to an arbitrary range by assuming the range wraps around when going below min or above max.
     *
     * @param value input
     * @param start range start
     * @param end   range end
     * @return normalized number
     */
    private fun normalize(value: Double, start: Double, end: Double): Double {
        val width = end - start
        val offsetValue = value - start // value relative to 0

        return offsetValue - Math.floor(offsetValue / width) * width + start // + start to reset back to start of original range
    }

    private class ParseResult(
        internal val result: Double,
        internal val matcherPos: Int,
        internal val matcherLen: Int
    )
}

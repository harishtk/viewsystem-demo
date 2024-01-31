package com.example.viewsystem.core.util.time

/*
 * Copyright (c) 2016, marlonlom
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import timber.log.Timber
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * The Class **TimeAgo**. Performs date time parsing into a text with 'time ago' syntax.
 * <br></br>
 * <br></br>
 * Usage:
 * <br></br>
 * <br></br>
 * *(1) Default:*
 * <pre>
 * TimeAgo.using(new java.util.Date().getTime());
 * </pre>
 * <br></br>
 * *(2) With Specific Locale (by language tag):*
 * <br></br>
 * <pre>
 * Locale LocaleByLanguageTag = Locale.forLanguageTag("es");
 * TimeAgo.using(new java.util.Date().getTime(), new TimeAgoMessages.Builder().withLocale(LocaleByLanguageTag).build());
 * </pre>
 * <br></br>
 *
 * @author marlonlom
 * @version 4.1.0
 * @see TimeAgoMessages
 *
 * @since 1.0.0
 */
object TimeAgo {

    /**
     * Returns the 'time ago' formatted text using date time.
     *
     * @param time      the date time for parsing
     * @param resources the resources for localizing messages
     * @return the 'time ago' formatted text using date time
     * @see TimeAgoMessages
     */
    @JvmStatic
    fun using(time: Long): String {
        val dim = getTimeDistanceInMinutesInternal(time)
        val timeAgo = buildTimeagoText(dim)
        return timeAgo.toString()
    }

    fun getTimeDistanceInMinutes(time: Long): Long {
        return getTimeDistanceInMinutesInternal(time)
    }

    /**
     * Build timeago text string builder.
     *
     * @param resources the resources
     * @param dim       the distance in minutes from now
     * @return the string builder
     */
    private fun buildTimeagoText(dim: Long): StringBuilder {
        val timeAgo = StringBuilder()

        val foundTimePeriod = Periods.findByDistanceMinutes(dim)
        if (foundTimePeriod != null) {
            Timber.d("TimeAgo: $dim $foundTimePeriod")
            val periodKey = foundTimePeriod.propertyKey
            when (foundTimePeriod) {
                Periods.ONE_MINUTE_PAST -> {
                    timeAgo.append("a minute ago")
                }
                Periods.ONE_MINUTE_FUTURE -> {
                    timeAgo.append("in a minute")
                }
                Periods.X_MINUTES_PAST -> {
                    timeAgo.append("$dim minutes ago")
                }
                Periods.X_MINUTES_FUTURE -> {
                    timeAgo.append("in $dim minute")
                }
                Periods.ABOUT_AN_HOUR_PAST -> {
                    timeAgo.append("an hour ago")
                }
                Periods.ABOUT_AN_HOUR_FUTURE -> {
                    timeAgo.append("in an hour")
                }
                Periods.X_HOURS_PAST -> {
                    val hours = (dim / 60f).roundToLong()
                    timeAgo.append("$hours hours ago")
                }
                Periods.X_HOURS_FUTURE -> {
                    val hours = (dim / 60f).roundToLong()
                    timeAgo.append("in $hours hours")
                }
                Periods.ONE_DAY_PAST -> {
                    timeAgo.append("a day ago")
                }
                Periods.ONE_DAY_FUTURE -> {
                    timeAgo.append("in a day")
                }
                Periods.X_DAYS_PAST -> {
                    val days = (dim / 1440f).roundToLong()
                    timeAgo.append("$days days ago")
                }
                Periods.X_DAYS_FUTURE -> {
                    val days = abs(dim / 1440f).roundToLong()
                    timeAgo.append("in $days days")
                }
                Periods.ONE_WEEK_PAST -> {
                    timeAgo.append("a week ago")
                }
                Periods.ONE_WEEK_FUTURE -> {
                    timeAgo.append("in a week")
                }
                Periods.X_WEEKS_PAST -> {
                    val weeks = abs(dim / 10080f).roundToLong()
                    timeAgo.append("$weeks weeks ago")
                }
                Periods.X_WEEKS_FUTURE -> {
                    val weeks = abs(dim / 10080f).roundToLong()
                    timeAgo.append("in $weeks weeks")
                }
                Periods.ABOUT_A_MONTH_PAST -> {
                    timeAgo.append("a month ago")
                }
                Periods.ABOUT_A_MONTH_FUTURE -> {
                    timeAgo.append("next month")
                }
                Periods.X_MONTHS_PAST -> {
                    val months = abs(dim / 43200f).roundToLong()
                    timeAgo.append("$months months ago")
                }
                Periods.X_MONTHS_FUTURE -> {
                    val months = abs(dim / 43200f).roundToLong()
                    timeAgo.append("in $months months")
                }
                Periods.ABOUT_A_YEAR_PAST,
                Periods.OVER_A_YEAR_PAST
                -> {
                    timeAgo.append("a year ago")
                }
                Periods.ABOUT_A_YEAR_FUTURE,
                Periods.OVER_A_YEAR_FUTURE
                -> {
                    timeAgo.append("next year")
                }
                Periods.ALMOST_TWO_YEARS_PAST,
                Periods.X_YEARS_PAST
                -> {
                    val years = abs(dim / 525600f).roundToLong()
                    timeAgo.append("$years years ago")
                }
                Periods.ALMOST_TWO_YEARS_FUTURE,
                Periods.X_YEARS_FUTURE
                -> {
                    val years = abs(dim / 525600f).roundToLong()
                    timeAgo.append("in $years years")
                }
                else -> timeAgo.append("a moment ago")
            }
        }
        return timeAgo
    }

    /**
     * Handle period key as plural string.
     *
     * @param resources the resources
     * @param periodKey the period key
     * @param value     the value
     * @return the string
     */
    /*private fun handlePeriodKeyAsPlural(resources: TimeAgoMessages,
                                        periodKey: String,
                                        pluralKey: String, value: Int): String =
        if (value == 1) resources.getPropertyValue(periodKey) else resources.getPropertyValue(pluralKey, value)*/

    /**
     * Returns the time distance in minutes.
     *
     * @param time the date time
     * @return the time distance in minutes
     */
    private fun getTimeDistanceInMinutesInternal(time: Long): Long {
        val timeDistance = System.currentTimeMillis() - time
        return (timeDistance / 1000 / 60).toFloat().roundToLong()
    }
}

/**
 * The enum Periods.
 *
 * @author marlonlom
 * @version 4.1.0
 * @since 2.0.0
 */
enum class Periods(
    /**
     * The property key.
     */
    val propertyKey: String,
    /**
     * The predicate.
     */
    private val distancePredicate: (distance: Long) -> Boolean
) {

    NOW("ml.timeago.now", { distance -> distance in 0L..(0.99).toLong() }),
    ONE_MINUTE_PAST("ml.timeago.oneminute.past", { distance -> distance == 1L }),
    X_MINUTES_PAST("ml.timeago.xminutes.past", { distance -> distance in 2..44 }),
    ABOUT_AN_HOUR_PAST("ml.timeago.aboutanhour.past", { distance -> distance in 45..89 }),
    X_HOURS_PAST("ml.timeago.xhours.past", { distance -> distance in 90..1439 }),
    ONE_DAY_PAST("ml.timeago.oneday.past", { distance -> distance in 1440..2519 }),
    X_DAYS_PAST("ml.timeago.xdays.past", { distance -> distance in 2520..10079 }),
    ONE_WEEK_PAST("ml.timeago.oneweek.past", { distance -> distance in 10080..20159 }),
    X_WEEKS_PAST("ml.timeago.xweeks.past", { distance -> distance in 20160..43199 }),
    ABOUT_A_MONTH_PAST("ml.timeago.aboutamonth.past", { distance -> distance in 43200..86399 }),
    X_MONTHS_PAST("ml.timeago.xmonths.past", { distance -> distance in 86400..525599 }),
    ABOUT_A_YEAR_PAST("ml.timeago.aboutayear.past", { distance -> distance in 525600..655199 }),
    OVER_A_YEAR_PAST("ml.timeago.overayear.past", { distance -> distance in 655200..914399 }),
    ALMOST_TWO_YEARS_PAST("ml.timeago.almosttwoyears.past", { distance -> distance in 914400..1051199 }),
    X_YEARS_PAST("ml.timeago.xyears.past", { distance -> (distance / 525600).toFloat().roundToLong() > 1 }),
    ONE_MINUTE_FUTURE("ml.timeago.oneminute.future", { distance -> distance.toInt() == -1 }),
    X_MINUTES_FUTURE("ml.timeago.xminutes.future", { distance -> distance <= -2 && distance >= -44 }),
    ABOUT_AN_HOUR_FUTURE("ml.timeago.aboutanhour.future", { distance -> distance <= -45 && distance >= -89 }),
    X_HOURS_FUTURE("ml.timeago.xhours.future", { distance -> distance <= -90 && distance >= -1439 }),
    ONE_DAY_FUTURE("ml.timeago.oneday.future", { distance -> distance <= -1440 && distance >= -2519 }),
    X_DAYS_FUTURE("ml.timeago.xdays.future", { distance -> distance <= -2520 && distance >= -10079 }),
    ONE_WEEK_FUTURE("ml.timeago.oneweek.future", { distance -> distance <= -10080 && distance >= -20159 }),
    X_WEEKS_FUTURE("ml.timeago.xweeks.future", { distance -> distance <= -20160 && distance >= -43199 }),
    ABOUT_A_MONTH_FUTURE("ml.timeago.aboutamonth.future", { distance -> distance <= -43200 && distance >= -86399 }),
    X_MONTHS_FUTURE("ml.timeago.xmonths.future", { distance -> distance <= -86400 && distance >= -525599 }),
    ABOUT_A_YEAR_FUTURE("ml.timeago.aboutayear.future", { distance -> distance <= -525600 && distance >= -655199 }),
    OVER_A_YEAR_FUTURE("ml.timeago.overayear.future", { distance -> distance <= -655200 && distance >= -914399 }),
    ALMOST_TWO_YEARS_FUTURE("ml.timeago.almosttwoyears.future", { distance -> distance <= -914400 && distance >= -1051199 }),
    X_YEARS_FUTURE("ml.timeago.xyears.future", { distance -> (distance / 525600).toFloat().roundToLong() < -1 });

    companion object {

        /**
         * Find by distance minutes periods.
         *
         * @param distanceMinutes the distance minutes
         * @return the periods
         */
        fun findByDistanceMinutes(distanceMinutes: Long): Periods? {
            val values = values()
            for (item in values) {
                val successful = item.distancePredicate.invoke(distanceMinutes)
                if (successful) {
                    return item
                }
            }
            return null
        }
    }
}
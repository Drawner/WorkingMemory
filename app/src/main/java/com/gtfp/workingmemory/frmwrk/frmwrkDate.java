package com.gtfp.workingmemory.frmwrk;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


/**
 * Copyright (C) 2015  Greg T. F. Perry
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
class frmwrkDate
{
    private static final int[] DATE_DIGITS = { 0, 1, 2, 3, 5, 6, 8, 9 };
    private static final int[] DATETIME_DIGITS = { 0, 1, 2, 3, 5, 6, 8, 9, 11, 12, 14, 15, 17, 18 };
    private static final int[] MAX_DAY = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    private final Map<String, long[]> m_cache = new HashMap<String, long[]>( 20 );
    private final TimeZone m_timeZone;

    public frmwrkDate(TimeZone m_timeZone) {

        this.m_timeZone = m_timeZone;
    }

    private static class DateParts
    {
        public final int year;
        public final int month;
        public final int day;

        public DateParts(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }

    private static boolean allDigits( final String s, final int[] positions )
    {
        for ( final int pos : positions )
        {
            final char c = s.charAt( pos );
            if ( c > '9' || c < '0' )
                return false;
        }
        return true;
    }

    private static final int TWO_SUBTRACT = '0' * 11;

    private static int two( final String s, final int from )
    {
        return s.charAt( from ) * 10 + s.charAt( from + 1 ) - TWO_SUBTRACT;
    }

    private static int four( final String s )
    {
        return 100 * two( s, 0 ) + two( s, 2 );
    }

    private static DateParts parseDate( final String s )
    {
        return new DateParts( four( s ), two( s, 5 ) - 1, two( s, 8 ) );
    }

    public void fastDate( final String date )
    {
        checkDate(date);
        final DateParts dp = parseDate( date );
        checkDateParts( dp.year, dp.month, dp.day );
        m_cache.put( date, prepareDate(dp) );
    }

    private static void checkDateParts( final int year, final int month, final int day )
    {
        if ( year < 1800 || year > 2200 )
            throw new IllegalArgumentException( "Year must be between 1800 and 2200!" );
        if ( month < 0 || month > 11 )
            throw new IllegalArgumentException( "Month must be between 1 and 12!" );
        if ( month != 1 )
        {
            if ( day < 1 || day > MAX_DAY[ month ] )
                throw new IllegalArgumentException( "Day must be between 1 and " + MAX_DAY[ month ] + '!' );
        }
        else {
            int maxFeb = 28;
            if ( ( year & 3 ) == 0 )
                maxFeb = (year % 100 == 0) && (year % 400 != 0) ? 28 : 29;
            if ( day < 1 || day > maxFeb )
                throw new IllegalArgumentException( "Day must be between 1 and " + maxFeb + '!' );
        }
    }

    private static void checkTimeParts( final int hour, final int min, final int sec )
    {
        if ( hour < 0 || hour > 23 )
            throw new IllegalArgumentException( "Hours must be between 0 and 23!" );
        if ( min < 0 || min > 59 )
            throw new IllegalArgumentException( "Minutes must be between 0 and 59!" );
        if ( sec < 0 || sec > 59 )
            throw new IllegalArgumentException( "Seconds must be between 0 and 59!" );
    }

    private void checkDate( final String date )
    {
        if ( date == null || date.length() != 10 || !allDigits( date, DATE_DIGITS ) ||
                date.charAt( 4 ) != '.' || date.charAt( 7 ) != '.' )
            throw new IllegalArgumentException( "Date should be in yyyy.MM.dd format!" );
    }

    private void checkDateTime( final String dateTime )
    {
        if ( dateTime == null || dateTime.length() != 19 || !allDigits( dateTime, DATETIME_DIGITS ) ||
                dateTime.charAt( 4 ) != '.' || dateTime.charAt( 7 ) != '.' || dateTime.charAt( 10 ) != ' ' ||
                dateTime.charAt( 13 ) != ':' || dateTime.charAt( 16 ) != ':' )
            throw new IllegalArgumentException( "DateTime should be in yyyy.MM.dd HH:mm:ss format!" );
    }

    private long[] prepareDate( final DateParts dp )
    {
        final long[] res = new long[ 24 ];
        for ( int i = 0; i < 23; ++i )
        {
            final Calendar cl = new GregorianCalendar( dp.year, dp.month, dp.day, i, 0, 0 );
            cl.setTimeZone( m_timeZone );
            res[ i ] = cl.getTimeInMillis();
        }
        return res;
    }

    public long parse( final String dateTime )
    {
        checkDateTime( dateTime );
        final long[] cache = m_cache.get( dateTime.substring( 0, 10 ) );
        if ( cache != null )
        {
            final int hour = two( dateTime, 11 );
            final int min = two( dateTime, 14 );
            final int sec = two( dateTime, 17 );
            checkTimeParts( hour, min, sec );
            final long hourTime = cache[ hour ];
            return hourTime + min * 60000 + sec * 1000;
        }
        else {
            final int year = four( dateTime );
            final int month = two( dateTime, 5 ) - 1;
            final int day = two( dateTime, 8 );
            final int hour = two( dateTime, 11 );
            final int min = two( dateTime, 14 );
            final int sec = two( dateTime, 17 );
            checkDateParts( year, month, day );
            checkTimeParts( hour, min, sec );
            final Calendar cl = new GregorianCalendar( year, month, day, hour, min, sec );
            cl.setTimeZone( m_timeZone );
            return cl.getTimeInMillis();
        }
    }
}

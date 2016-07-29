/*
 * Copyright 2016 EuregJUG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.euregjug.site.support.thymeleaf.expressions;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.FormatStyle;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2015-01-09
 */
public class TemporalsTest {

    @Test
    public void formatDateShouldWork() {
        final Temporals temporals = new Temporals(Locale.US);
        Assert.assertEquals("6/30/09", temporals.formatDate(ZonedDateTime.now().withYear(2009).withMonth(6).withDayOfMonth(30), FormatStyle.SHORT));
        Assert.assertEquals("6/30/09 7:03:47 AM", temporals.formatDateTime(
                ZonedDateTime.now()
                .withYear(2009).withMonth(6).withDayOfMonth(30)
                .withHour(7).withMinute(3).withSecond(47),
                FormatStyle.SHORT,
                FormatStyle.MEDIUM
        ));
        Assert.assertEquals("10/26/14 1:30:00 AM", temporals.formatDateTime(
                ZonedDateTime.of(2014, 10, 26, 1, 30, 0, 0, ZoneId.of("UTC")),
                FormatStyle.SHORT,
                FormatStyle.MEDIUM
        ));
        Assert.assertEquals("March 1979", temporals.format(YearMonth.of(1979, 3), "MMMM yyyy"));
        Assert.assertEquals("2014.10.26 01:30", temporals.format(ZonedDateTime.of(2014, 10, 26, 1, 30, 0, 0, ZoneId.of("UTC")), "yyyy.MM.dd HH:mm"));
    }
}

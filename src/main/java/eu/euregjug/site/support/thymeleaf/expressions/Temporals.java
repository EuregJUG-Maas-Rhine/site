/*
 * Copyright 2015 michael-simons.eu.
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

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

/**
 * Expression for formatting {@code java.time} objects.
 *
 * @author Michael J. Simons, 2015-01-04
 */
public final class Temporals {

    /**
     * Locale of this instance
     */
    private final Locale locale;

    /**
     * Creates a new instance of a {@code Temporals} object bound to the given
     * {@link #locale}.
     *
     * @param locale The locale of this instance
     */
    public Temporals(Locale locale) {
        this.locale = locale;
    }

    /**
     * Formats both date and time of the given {@code temporal} with one of the
     * predefined {@link FormatStyle}s.
     *
     * @param temporal The temporal object to be formatted
     * @param dateStyle The chosen date style
     * @param timeStyle The chosen time style
     * @return Formatted object
     */
    public String formatDateTime(TemporalAccessor temporal, FormatStyle dateStyle, FormatStyle timeStyle) {
        return DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle).withLocale(this.locale).format(temporal);
    }

    /**
     * Formats the date part of the given {@code temporal} with one of the
     * predefined {@link FormatStyle}s.
     *
     * @param temporal The temporal object to be formatted
     * @param dateStyle The chosen date style
     * @return Formatted object
     */
    public String formatDate(TemporalAccessor temporal, FormatStyle dateStyle) {
        return DateTimeFormatter.ofLocalizedDate(dateStyle).withLocale(this.locale).format(temporal);
    }

    /**
     * Formats a temporal accessor according to the given {@code pattern}.
     *
     * @param temporal An arbitrary temporal thing
     * @param pattern The pattern to format the year-mont
     * @return Formatted object
     */
    public String format(TemporalAccessor temporal, String pattern) {
        return DateTimeFormatter.ofPattern(pattern, locale).format(temporal);
    }
}

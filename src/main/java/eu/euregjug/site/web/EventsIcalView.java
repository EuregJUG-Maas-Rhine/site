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
package eu.euregjug.site.web;

import eu.euregjug.site.events.EventEntity;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Super simple (if not primitive) iCal / ics View for EuregJUG events.
 *
 * @author Michael J. Simons, 2016-01-04
 */
@Component("events.ics")
final class EventsIcalView extends AbstractView {

    private final DateTimeFormatter tstampFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH).withZone(ZoneId.of("UTC"));
    private final ZoneId zoneId = ZoneId.systemDefault();
    private final String br = "\r\n";

    EventsIcalView() {
        super.setContentType("text/calendar");
    }

    @Override
    protected void renderMergedOutputModel(final Map<String, Object> model, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final List<EventEntity> events = (List<EventEntity>) model.get("events");
        super.setResponseContentType(request, response);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            w.write("BEGIN:VCALENDAR" + br);
            w.write("VERSION:2.0" + br);
            w.write("PRODID:http://www.euregjug.eu/events" + br);
            for (EventEntity event : events) {
                final StringBuilder summaryBuilder = new StringBuilder(event.getName());
                if (event.getSpeaker() != null) {
                    summaryBuilder.append(" (").append(event.getSpeaker()).append(")");
                }

                final ZonedDateTime heldOn = event.getHeldOn().toInstant().atZone(zoneId);
                w.write("BEGIN:VEVENT" + br);
                w.write(String.format("UID:%d@euregjug.eu%s", event.getId(), br));
                w.write("ORGANIZER:EuregJUG" + br);
                w.write("DTSTAMP:" + tstampFormat.format(event.getCreatedAt().toInstant().atZone(zoneId)) + br);
                w.write("DTSTART:" + tstampFormat.format(heldOn) + br);
                w.write("DTEND:" + tstampFormat.format(heldOn.plusMinutes(Optional.ofNullable(event.getDuration()).orElse(120))) + br);
                w.write("SUMMARY:" + summaryBuilder.toString() + br);
                w.write("DESCRIPTION:" + event.getDescription() + br);
                w.write("URL:" + UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).replacePath("/register/{eventId}").buildAndExpand(event.getId()) + br);
                w.write("END:VEVENT" + br);
            }
            w.write("END:VCALENDAR" + br);
        }
        response.flushBuffer();
    }
}

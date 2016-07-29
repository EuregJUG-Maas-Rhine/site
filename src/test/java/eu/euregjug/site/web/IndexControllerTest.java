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

import com.github.mkopylec.recaptcha.validation.RecaptchaValidator;
import eu.euregjug.site.config.MailChimpConfig;
import eu.euregjug.site.events.EventEntity;
import eu.euregjug.site.events.EventRepository;
import eu.euregjug.site.events.RegistrationService;
import eu.euregjug.site.links.LinkRepository;
import eu.euregjug.site.posts.PostRenderingService;
import eu.euregjug.site.posts.PostRepository;
import static eu.euregjug.site.web.EventsIcalView.ICS_LINEBREAK;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import org.joor.Reflect;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 *
 * @author Michael J. Simons, 2016-07-17
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@WebMvcTest(
        controllers = IndexController.class,
        secure = false,
        // Need the custom view component and config as well...
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {EventsIcalView.class, MailChimpConfig.class}
        )
)
@EnableSpringDataWebSupport // Needed to enable resolving of Pageable and other parameters
@MockBean(classes = {
    RegistrationService.class,
    LinkRepository.class,
    PostRepository.class,
    PostRenderingService.class,
    RecaptchaValidator.class
})
public class IndexControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventRepository eventRepository;

    private final List<EventEntity> events;

    public IndexControllerTest() {
        final ZonedDateTime eventDate = ZonedDateTime.of(2016, 7, 7, 19, 0, 0, 0, ZoneId.of("Europe/Berlin"));
        final EventEntity event1 = Reflect.on(
                new EventEntity(GregorianCalendar.from(eventDate), "name-1", "desc-1")
        ).set("id", 23).set("createdAt", GregorianCalendar.from(eventDate)).get();
        event1.setDuration(60);
        event1.setSpeaker("Farin Urlaub");
        event1.setLocation("Am Strand\n4223 Schlaraffenland\n\nirgendwo");

        final ZonedDateTime eventDate2 = ZonedDateTime.of(2016, 11, 22, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"));
        final EventEntity event2 = Reflect.on(
                new EventEntity(GregorianCalendar.from(eventDate2), "name-2", "desc-2")
        ).set("id", 42).set("createdAt", GregorianCalendar.from(eventDate2)).get();
        this.events = Arrays.asList(event1, event2);
    }

    @Test
    public void eventsShouldWork() throws Exception {
        when(this.eventRepository.findUpcomingEvents()).thenReturn(events);
        this.mvc
                .perform(get("http://euregjug.eu/events.ics").accept("text/calendar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/calendar"))
                .andExpect(content().string(""
                        + "BEGIN:VCALENDAR" + ICS_LINEBREAK
                        + "VERSION:2.0" + ICS_LINEBREAK
                        + "PRODID:http://www.euregjug.eu/events" + ICS_LINEBREAK
                        + "BEGIN:VEVENT" + ICS_LINEBREAK
                        + "UID:23@euregjug.eu" + ICS_LINEBREAK
                        + "ORGANIZER:EuregJUG" + ICS_LINEBREAK
                        + "DTSTAMP:20160707T170000Z" + ICS_LINEBREAK
                        + "DTSTART:20160707T170000Z" + ICS_LINEBREAK
                        + "DTEND:20160707T180000Z" + ICS_LINEBREAK
                        + "SUMMARY:name-1 (Farin Urlaub)" + ICS_LINEBREAK
                        + "DESCRIPTION:desc-1" + ICS_LINEBREAK
                        + "URL:http://euregjug.eu/register/23" + ICS_LINEBREAK
                        + "LOCATION: Am Strand, 4223 Schlaraffenland, irgendwo" + ICS_LINEBREAK
                        + "END:VEVENT" + ICS_LINEBREAK
                        + "BEGIN:VEVENT" + ICS_LINEBREAK
                        + "UID:42@euregjug.eu" + ICS_LINEBREAK
                        + "ORGANIZER:EuregJUG" + ICS_LINEBREAK
                        + "DTSTAMP:20161122T170000Z" + ICS_LINEBREAK
                        + "DTSTART:20161122T170000Z" + ICS_LINEBREAK
                        + "DTEND:20161122T190000Z" + ICS_LINEBREAK
                        + "SUMMARY:name-2" + ICS_LINEBREAK
                        + "DESCRIPTION:desc-2" + ICS_LINEBREAK
                        + "URL:http://euregjug.eu/register/42" + ICS_LINEBREAK
                        + "END:VEVENT" + ICS_LINEBREAK
                        + "END:VCALENDAR" + ICS_LINEBREAK));

        verify(this.eventRepository).findUpcomingEvents();
        verifyNoMoreInteractions(this.eventRepository);
    }

    @Test
    public void registerFormShouldWork() throws Exception {
        when(this.eventRepository.findOne(23)).thenReturn(Optional.of(this.events.get(0)));

        this.mvc
                .perform(get("http://euregjug.eu/register/{eventId}", 23))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("register"))
                .andExpect(MockMvcResultMatchers.model().attribute("registered", false))
                .andExpect(MockMvcResultMatchers.model().attribute("event", this.events.get(0)))
                .andExpect(MockMvcResultMatchers.model().attributeExists("registration"));

        verify(this.eventRepository).findOne(23);
        verifyNoMoreInteractions(this.eventRepository);
    }
}

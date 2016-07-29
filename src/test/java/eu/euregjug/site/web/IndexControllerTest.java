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
import eu.euregjug.site.events.EventEntity;
import eu.euregjug.site.events.EventRepository;
import eu.euregjug.site.events.RegistrationService;
import eu.euregjug.site.links.LinkRepository;
import eu.euregjug.site.posts.PostRenderingService;
import eu.euregjug.site.posts.PostRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import org.joor.Reflect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

/**
 *
 * @author Michael J. Simons, 2016-07-17
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@WebMvcTest(
        controllers = IndexController.class,
        secure = false,        
        // Need the custom view component as well...
        includeFilters = @ComponentScan.Filter(classes = EventsIcalView.class, type = FilterType.ASSIGNABLE_TYPE)
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

        final ZonedDateTime eventDate2 = ZonedDateTime.of(2016, 11, 22, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"));
        final EventEntity event2 = Reflect.on(
                new EventEntity(GregorianCalendar.from(eventDate2), "name-2", "desc-2")
        ).set("id", 42).set("createdAt", GregorianCalendar.from(eventDate2)).get();
        this.events = Arrays.asList(event1, event2);
    }

    @Test
    public void eventsShouldWork() throws Exception {
        when(this.eventRepository.findUpcomingEvents()).thenReturn(events); 
        final String br = "\r\n";
        this.mvc
                .perform(get("http://euregjug.eu/events.ics").accept("text/calendar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/calendar"))
                .andExpect(content().string(""
                        + "BEGIN:VCALENDAR" + br
                        + "VERSION:2.0" + br
                        + "PRODID:http://www.euregjug.eu/events" + br
                        + "BEGIN:VEVENT" + br
                        + "UID:23@euregjug.eu" + br
                        + "ORGANIZER:EuregJUG" + br
                        + "DTSTAMP:20160707T170000Z" + br
                        + "DTSTART:20160707T170000Z" + br
                        + "DTEND:20160707T180000Z" + br
                        + "SUMMARY:name-1" + br
                        + "DESCRIPTION:desc-1" + br
                        + "URL:http://euregjug.eu/register/23" + br
                        + "END:VEVENT" + br
                        + "BEGIN:VEVENT" + br
                        + "UID:42@euregjug.eu" + br
                        + "ORGANIZER:EuregJUG" + br
                        + "DTSTAMP:20161122T170000Z" + br
                        + "DTSTART:20161122T170000Z" + br
                        + "DTEND:20161122T190000Z" + br
                        + "SUMMARY:name-2" + br
                        + "DESCRIPTION:desc-2" + br
                        + "URL:http://euregjug.eu/register/42" + br
                        + "END:VEVENT" + br
                        + "END:VCALENDAR" + br));

        verify(this.eventRepository).findUpcomingEvents();
        verifyNoMoreInteractions(this.eventRepository);
    }

}

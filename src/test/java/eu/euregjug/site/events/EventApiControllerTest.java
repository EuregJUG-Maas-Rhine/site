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
package eu.euregjug.site.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.euregjug.site.config.SecurityTestConfig;
import eu.euregjug.site.events.EventEntity.Type;
import eu.euregjug.site.posts.PostEntity;
import eu.euregjug.site.posts.PostRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import org.joor.Reflect;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author Michael J. Simons, 2016-07-14
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@WebMvcTest(EventApiController.class)
@EnableSpringDataWebSupport // Needed to enable resolving of Pageable and other parameters
@Import(SecurityTestConfig.class) // Needed to get rid of default CSRF protection
@AutoConfigureRestDocs(
        outputDir = "target/generated-snippets",
        uriHost = "euregjug.eu",
        uriPort = 80
)
public class EventApiControllerTest {
    
    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private EventRepository eventRepository;
    
    @MockBean
    private PostRepository postRepository;
    
    @MockBean
    private RegistrationRepository registrationRepository;
    
    private JacksonTester<EventEntity> json;
    
    private final List<EventEntity> events;
    
    public EventApiControllerTest() {
        ZonedDateTime eventDate;
        eventDate = ZonedDateTime.of(2016, 9, 14, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"));
        final EventEntity event1 = Reflect.on(
                new EventEntity(GregorianCalendar.from(eventDate), "name-1", "description-1")
        ).set("id", 42).get();
        event1.setDuration(60);
        
        eventDate = ZonedDateTime.of(2016, 11, 22, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"));
        final EventEntity event2 = Reflect.on(
                new EventEntity(GregorianCalendar.from(eventDate), "name-2", "description-2")
        ).set("id", 23).get();
        event2.setDuration(90);
        this.events = Arrays.asList(event1, event2);
    }
    
    @Before
    public void setup() {
        JacksonTester.initFields(this, objectMapper);
    }
    
    @Test
    public void createShouldWork() throws Exception {
        final EventEntity eventEntity = new EventEntity(Calendar.getInstance(), "Mark Paluch - Hallo, ich bin Redis", "Mark spricht in diesem Vortrag über den Open Source NoSQL Data Store Redis. Der Vortrag ist eine Einführung in Redis und veranschaulicht mit Hilfe von Code-Beispielen, wie Redis mit Spring Data, Hibernate OGM und plain Java verwendet werden kann. Der Vortrag findet bei Thinking Networks in Aachen statt.");
        eventEntity.setNeedsRegistration(true);
        eventEntity.setType(Type.talk);        
        eventEntity.setPost(new PostEntity(new Date(), "foo", "foo", "foo"));
        Reflect.on(eventEntity).set("id", 30).get();
        
        when(this.eventRepository.save(any(EventEntity.class))).then(invocation -> {
            // Do the stuff JPA would do for us...
            final EventEntity rv = invocation.getArgumentAt(0, EventEntity.class);            
            Assert.assertThat(rv.getId(), nullValue());
            return Reflect.on(rv).call("prePersistAndUpdate").set("id", 4712).get();
        });
        
        this.mvc
                .perform(
                        post("/api/events")
                        .content(this.json.write(eventEntity).getJson())
                        .contentType(APPLICATION_JSON)
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(4712)))
                .andExpect(jsonPath("$.name", equalTo(eventEntity.getName())))
                .andExpect(jsonPath("$.post").doesNotExist())
                .andDo(document("api/events/create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
        
        verify(this.eventRepository).save(any(EventEntity.class));
        verifyNoMoreInteractions(this.eventRepository, this.postRepository, this.registrationRepository);
    }
    
    @Test
    public void addPostShouldWork() throws Exception {
        final ZonedDateTime eventDate = ZonedDateTime.of(2016, 9, 14, 18, 0, 0, 0, ZoneId.of("Europe/Berlin"));
        final EventEntity event = Reflect.on(
                new EventEntity(GregorianCalendar.from(eventDate), "name-1", "description-1")
        ).set("id", 42).get();
        final PostEntity post = Reflect.on(
                new PostEntity(new Date(), "ruckblick-zum-aim42-vortrag-mit-gernot-starke", "Rückblick zum aim42 Vortrag mit Gernot Starke", "Am 7. April lud die Euregio JUG zusammen mit der http://www.inside-online.de/de/[inside Unternehmensgruppe] zu einem Vortrag von Gernot Starke zum Thema _aim42 - software architecture improvement_ ein.")
        ).call("updateUpdatedAt").set("id", 23).get();
        
        when(this.eventRepository.findOne(1)).thenReturn(Optional.empty());
        when(this.eventRepository.findOne(42)).thenReturn(Optional.of(event));
        when(this.postRepository.findOne(2)).thenReturn(Optional.empty());
        when(this.postRepository.findOne(23)).thenReturn(Optional.of(post));
        
        this.mvc.perform(
                put("/api/events/{id}/post/{postId}", 1, 2)
                .principal(() -> "euregjug")
        ).andExpect(status().isNotFound());
        
        this.mvc.perform(
                put("/api/events/{id}/post/{postId}", 42, 2)
                .principal(() -> "euregjug")
        ).andExpect(status().isNotFound());
        
        this.mvc.perform(
                put("/api/events/{id}/post/{postId}", 1, 23)
                .principal(() -> "euregjug")
        ).andExpect(status().isNotFound());
        
        this.mvc.perform(
                put("/api/events/{id}/post/{postId}", 42, 23)
                .principal(() -> "euregjug")
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("name-1")))
                .andExpect(jsonPath("$.description", equalTo("description-1")))
                .andExpect(jsonPath("$.post.slug", equalTo(post.getSlug())))
                .andExpect(jsonPath("$.post.title", equalTo(post.getTitle())))
                .andExpect(jsonPath("$.post.content", equalTo(post.getContent())))
                .andDo(document("api/events/-id-/post/-postId-",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
        
        verify(this.eventRepository, times(2)).findOne(1);
        verify(this.eventRepository, times(2)).findOne(42);
        verify(this.postRepository, times(2)).findOne(2);
        verify(this.postRepository, times(2)).findOne(23);
        verifyNoMoreInteractions(this.eventRepository, this.postRepository, this.registrationRepository);
    }
    
    @Test
    public void getShouldWork() throws Exception {
        final PageImpl page = new PageImpl(this.events);
        
        when(this.eventRepository.findAll(any(Pageable.class))).thenReturn(page);
        
        this.mvc
                .perform(
                        get("/api/events")
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", equalTo(2)))
                .andExpect(jsonPath("$.totalElements", equalTo(2)))
                .andExpect(jsonPath("$.totalPages", equalTo(1)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", equalTo("name-1")))
                .andExpect(jsonPath("$.content[0].description", equalTo("description-1")))
                .andExpect(jsonPath("$.content[0].duration", equalTo(60)))
                .andExpect(jsonPath("$.content[1].name", equalTo("name-2")))
                .andExpect(jsonPath("$.content[1].description", equalTo("description-2")))
                .andExpect(jsonPath("$.content[1].duration", equalTo(90)))
                .andDo(document("api/events/get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
        
        verify(this.eventRepository).findAll(any(Pageable.class));
        verifyNoMoreInteractions(this.eventRepository, this.postRepository, this.registrationRepository);
    }
    
    @Test
    public void getRegistrationsShouldWork() throws Exception {
        final List<RegistrationEntity> registrations = this.events.stream()
                .filter(event -> event.getId() == 42)
                .map(event -> new RegistrationEntity(event, "mail" + event.getId() + "@euregjug.eu", "name " + event.getId(), "vorname " + event.getId(), event.getId() == 42))
                .collect(toList());
        
        when(this.registrationRepository.findAllByEventId(42)).thenReturn(registrations);
        
        this.mvc
                .perform(
                        get("/api/events/{id}/registrations", 42)
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].email", equalTo("mail42@euregjug.eu")))
                .andExpect(jsonPath("$.[0].name", equalTo("name 42")))
                .andExpect(jsonPath("$.[0].firstName", equalTo("vorname 42")))
                .andExpect(jsonPath("$.[0].subscribeToNewsletter", equalTo(true)))
                .andDo(document("api/events/-id-/registrations",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
        
        verify(this.registrationRepository).findAllByEventId(42);
        verifyNoMoreInteractions(this.eventRepository, this.postRepository, this.registrationRepository);
    }
    
    @Test
    public void updateShouldShouldWork() throws Exception {
        final EventEntity updateEntity = new EventEntity(Calendar.getInstance(), "NewName", "NewDescription");
        updateEntity.setDuration(90);
        updateEntity.setNeedsRegistration(true);
        updateEntity.setType(Type.talk);
        updateEntity.setSpeaker("Mark Paluch");
        updateEntity.setLocation("Thinking Networks AG\n"
                + "Markt 45-47\n"
                + "D-52062 Aachen");
        final EventEntity oldEntity = Reflect.on(
                new EventEntity(Calendar.getInstance(), "Mark Paluch - Hallo, ich bin Redis", "Mark spricht in diesem Vortrag über den Open Source NoSQL Data Store Redis. Der Vortrag ist eine Einführung in Redis und veranschaulicht mit Hilfe von Code-Beispielen, wie Redis mit Spring Data, Hibernate OGM und plain Java verwendet werden kann. Der Vortrag findet bei Thinking Networks in Aachen statt.")
        ).set("id", 42).get();
        oldEntity.setDuration(60);
        oldEntity.setNeedsRegistration(false);
        oldEntity.setType(Type.meetup);
        
        when(this.eventRepository.findOne(23)).thenReturn(Optional.empty());
        when(this.eventRepository.findOne(42)).thenReturn(Optional.of(oldEntity));
        
        this.mvc
                .perform(
                        put("/api/events/{id}", 23)
                        .content(this.json.write(updateEntity).getJson())
                        .contentType(APPLICATION_JSON)
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isNotFound())
                .andDo(document("api/events/update/notfound",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
        
        this.mvc
                .perform(
                        put("/api/events/{id}", 42)
                        .content(this.json.write(updateEntity).getJson())
                        .contentType(APPLICATION_JSON)
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(42)))
                .andExpect(jsonPath("$.name", equalTo(oldEntity.getName())))
                .andExpect(jsonPath("$.description", equalTo(updateEntity.getDescription())))
                .andExpect(jsonPath("$.duration", equalTo(updateEntity.getDuration())))
                .andExpect(jsonPath("$.needsRegistration", equalTo(true)))
                .andExpect(jsonPath("$.speaker", equalTo(updateEntity.getSpeaker())))
                .andExpect(jsonPath("$.location", equalTo(updateEntity.getLocation())))
                .andExpect(jsonPath("$.type", equalTo("talk")))
                .andDo(document("api/events/update/updated",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
        
        verify(this.eventRepository).findOne(23);
        verify(this.eventRepository).findOne(42);
        verifyNoMoreInteractions(this.eventRepository, this.postRepository, this.registrationRepository);
    }
}

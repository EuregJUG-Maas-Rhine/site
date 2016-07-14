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
import java.util.Calendar;
import static org.hamcrest.Matchers.equalTo;
import org.joor.Reflect;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Before
    public void setup() {
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    public void createShouldWork() throws Exception {
        final EventEntity eventEntity = new EventEntity(Calendar.getInstance(), "Mark Paluch - Hallo, ich bin Redis", "Mark spricht in diesem Vortrag über den Open Source NoSQL Data Store Redis. Der Vortrag ist eine Einführung in Redis und veranschaulicht mit Hilfe von Code-Beispielen, wie Redis mit Spring Data, Hibernate OGM und plain Java verwendet werden kann. Der Vortrag findet bei Thinking Networks in Aachen statt.");
        eventEntity.setNeedsRegistration(true);
        eventEntity.setType(Type.talk);

        when(this.eventRepository.save(any(EventEntity.class))).then(invocation -> {
            // Do the stuff JPA would do for us...
            final EventEntity rv = invocation.getArgumentAt(0, EventEntity.class);
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
                .andDo(document("api/events/create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        verify(this.eventRepository).save(any(EventEntity.class));
        verifyNoMoreInteractions(this.eventRepository, this.postRepository, this.registrationRepository);
    }
}

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
package eu.euregjug.site.posts;

import eu.euregjug.site.config.SecurityTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.euregjug.site.posts.PostEntity.Format;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import org.joor.Reflect;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * @author Michael J. Simons, 2016-07-13
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@WebMvcTest(PostApiController.class)
@EnableSpringDataWebSupport // Needed to enable resolving of Pageable and other parameters
@Import(SecurityTestConfig.class) // Needed to get rid of default CSRF protection
@AutoConfigureRestDocs(
        outputDir = "target/generated-snippets",
        uriHost = "euregjug.eu",
        uriPort = 80
)
public class PostApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private JacksonTester<PostEntity> json;

    @Before
    public void setup() {
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    public void createShouldWork() throws Exception {
        final PostEntity postEntity = new PostEntity(new Date(), "new-site-is-live", "New site is live", "Welcome to the new EuregJUG website. We have switched off the old static pages and replaced it with a little application based on Hibernate, Spring Data JPA, Spring Boot and Thymeleaf.");

        when(this.postRepository.save(postEntity)).then(invocation -> {
            // Do the stuff JPA would do for us...
            final PostEntity rv = invocation.getArgumentAt(0, PostEntity.class);
            return Reflect.on(rv).call("updateUpdatedAt").set("id", 4711).get();
        });

        this.mvc
                .perform(
                        post("/api/posts")
                        .content(this.json.write(postEntity).getJson())
                        .contentType(APPLICATION_JSON)
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(4711)))
                .andExpect(jsonPath("$.slug", equalTo("new-site-is-live")))
                .andDo(document("api/posts/create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        verify(this.postRepository).save(postEntity);
        verifyNoMoreInteractions(this.postRepository);
    }

    @Test
    public void getShouldWork() throws Exception {
        final PostEntity p1 = Reflect.on(
                new PostEntity(new Date(), "new-site-is-live", "New site is live", "Welcome to the new EuregJUG website. We have switched off the old static pages and replaced it with a little application based on Hibernate, Spring Data JPA, Spring Boot and Thymeleaf.")
        ).call("updateUpdatedAt").set("id", 23).get();
        final PostEntity p2 = Reflect.on(
                new PostEntity(new Date(), "ruckblick-zum-aim42-vortrag-mit-gernot-starke", "Rückblick zum aim42 Vortrag mit Gernot Starke", "Am 7. April lud die Euregio JUG zusammen mit der http://www.inside-online.de/de/[inside Unternehmensgruppe] zu einem Vortrag von Gernot Starke zum Thema _aim42 - software architecture improvement_ ein.")
        ).call("updateUpdatedAt").set("id", 42).get();

        final PageImpl page = new PageImpl(Arrays.asList(p1, p2));

        when(this.postRepository.findAll(any(Pageable.class))).thenReturn(page);

        this.mvc
                .perform(
                        get("/api/posts")
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", equalTo(2)))
                .andExpect(jsonPath("$.totalElements", equalTo(2)))
                .andExpect(jsonPath("$.totalPages", equalTo(1)))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].slug", equalTo("new-site-is-live")))
                .andExpect(jsonPath("$.content[1].slug", equalTo("ruckblick-zum-aim42-vortrag-mit-gernot-starke")))
                .andDo(document("api/posts/get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        verify(this.postRepository).findAll(any(Pageable.class));
        verifyNoMoreInteractions(this.postRepository);
    }

    @Test
    public void updateShouldShouldWork() throws Exception {
        final PostEntity updateEntity = new PostEntity(new Date(), "newslug", "newtitle", "newcontent");
        updateEntity.setFormat(Format.markdown);        
        final PostEntity oldEntity = new PostEntity(new Date(), "oldslug", "oldtitle", "oldcontent");
        oldEntity.setLocale(new Locale("de", "DE"));
        oldEntity.setFormat(Format.asciidoc);
        when(this.postRepository.findOne(4711)).thenReturn(Optional.empty());
        when(this.postRepository.findOne(4712)).thenReturn(Optional.of(oldEntity));

        this.mvc
                .perform(
                        put("/api/posts/{id}", 4711)
                        .content(this.json.write(updateEntity).getJson())
                        .contentType(APPLICATION_JSON)
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isNotFound())
                .andDo(document("api/posts/update/notfound",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        this.mvc
                .perform(
                        put("/api/posts/{id}", 4712)
                        .content(this.json.write(updateEntity).getJson())
                        .contentType(APPLICATION_JSON)
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", equalTo("newcontent")))
                .andExpect(jsonPath("$.format", equalTo("markdown")))
                .andExpect(jsonPath("$.title", equalTo("newtitle")))
                .andExpect(jsonPath("$.slug", equalTo("oldslug")))
                .andExpect(jsonPath("$.locale", equalTo("de_DE")));
        
        updateEntity.setLocale(new Locale("en", "US"));
        this.mvc
                .perform(
                        put("/api/posts/{id}", 4712)
                        .content(this.json.write(updateEntity).getJson())
                        .contentType(APPLICATION_JSON)
                        .principal(() -> "euregjug")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale", equalTo("en_US")))
                .andDo(document("api/posts/update/updated",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

        verify(this.postRepository).findOne(4711);
        verify(this.postRepository, times(2)).findOne(4712);
        verifyNoMoreInteractions(this.postRepository);
    }
}

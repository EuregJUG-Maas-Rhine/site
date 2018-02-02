/*
 * Copyright 2015-2018 EuregJUG.
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
import eu.euregjug.site.events.Registration;
import eu.euregjug.site.events.RegistrationEntity;
import eu.euregjug.site.events.RegistrationService;
import eu.euregjug.site.events.RegistrationService.InvalidRegistrationException;
import eu.euregjug.site.links.LinkEntity;
import eu.euregjug.site.links.LinkRepository;
import eu.euregjug.site.posts.Post;
import eu.euregjug.site.posts.PostEntity;
import eu.euregjug.site.posts.PostEntity.Status;
import eu.euregjug.site.posts.PostRenderingService;
import eu.euregjug.site.posts.PostRepository;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static java.util.stream.Collectors.groupingBy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Michael J. Simons, 2015-12-27
 */
@Controller
@RequiredArgsConstructor
@Slf4j
class IndexController {

    private static final String ATTRIBUTE_ALERTS = "alerts";

    private static final String ATTRIBUTE_REGISTERED = "registered";

    private static final String ATTRIBUTE_POSTS = "posts";

    private static final String ATTRIBUTE_POST = "post";

    private static final String VIEW_POST = ATTRIBUTE_POST;

    private static final String ATTRIBUTE_EVENT = "event";

    private final EventRepository eventRepository;

    private final RegistrationService registrationService;

    private final LinkRepository linkRepository;

    private final PostRepository postRepository;

    private final PostRenderingService postRenderingService;

    private final RecaptchaValidator recaptchaValidator;

    @RequestMapping({"", "/", "/feed"})
    public String index(
            @RequestParam(required = false, defaultValue = "0") final Integer page,
            final Model model
    ) {
        model
                .addAttribute("upcomingEvents", this.eventRepository.findUpcomingEvents())
                .addAttribute("links", this.linkRepository.findAllByOrderByTypeAscSortColAscTitleAsc().stream().collect(groupingBy(LinkEntity::getType)))
                .addAttribute(ATTRIBUTE_POSTS, this.postRepository.findAllByStatus(Status.published, new PageRequest(page, 5, Direction.DESC, "publishedOn", "createdAt")).map(postRenderingService::render));
        return "index";
    }

    @RequestMapping({
        "/{year:\\d+}/{month:\\d+}/{day:\\d+}/{slug}",
        "/posts/{year:\\d+}-{month:\\d+}-{day:\\d+}-{slug}"
    })
    public String post(
            @PathVariable final Integer year,
            @PathVariable final Integer month,
            @PathVariable final Integer day,
            @PathVariable final String slug,
            final Model model
    ) {

        String rv = "redirect:/";
        try {
            final Date publishedOn = Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());
            final Optional<PostEntity> post = this.postRepository
                    .findByPublishedOnAndSlug(publishedOn, slug)
                    .filter(PostEntity::isPublished);
            model
                    .addAttribute("previousPost", post.flatMap(this.postRepository::getPrevious))
                    .addAttribute(ATTRIBUTE_POST, post.map(postRenderingService::render).get())
                    .addAttribute("nextPost", post.flatMap(this.postRepository::getNext));
            rv = VIEW_POST;

        } catch (DateTimeException | NoSuchElementException e) {
            log.debug("Invalid request for post", e);
        }
        return rv;
    }

    @RequestMapping({"/archive", "/archives"})
    public String archive(final Model model) {
        model.addAttribute(ATTRIBUTE_POSTS,
                this.postRepository
                .findAll(new Sort(Direction.DESC, "publishedOn")).stream()
                .filter(PostEntity::isPublished)
                .map(Post::new)
                .collect(groupingBy(
                        post -> post.getPublishedOn().withDayOfMonth(1),
                        () -> new TreeMap<LocalDate, List<Post>>(reverseOrder()),
                        toList()
                )
                )
        );
        return "archive";
    }

    @RequestMapping("/search")
    public String search(@RequestParam final String q, final Model model) {
        final TreeMap<LocalDate, List<Post>> posts = this.postRepository
                .searchByKeyword(q).stream()
                .filter(PostEntity::isPublished)
                .map(Post::new)
                .collect(groupingBy(
                        post -> post.getPublishedOn().withDayOfMonth(1),
                        () -> new TreeMap<LocalDate, List<Post>>(reverseOrder()),
                        toList()
                ));
        if (posts.isEmpty()) {
            model.addAttribute(ATTRIBUTE_ALERTS, Arrays.asList("search.noResults"));
        }
        model
                .addAttribute(ATTRIBUTE_POSTS, posts)
                .addAttribute("q", q);
        return "archive";
    }

    @RequestMapping(value = "/events", produces = "text/calendar")
    public String events(final Model model) {
        model.addAttribute("events", this.eventRepository.findUpcomingEvents());
        return "events";
    }

    @RequestMapping(value = "/register/{eventId}", method = GET)
    public String register(
            @PathVariable final Integer eventId,
            final Model model,
            final RedirectAttributes redirectAttributes
    ) {
        final EventEntity event = this.eventRepository.findById(eventId).orElse(null);
        String rv;
        if (event == null) {
            redirectAttributes.addFlashAttribute(ATTRIBUTE_ALERTS, Arrays.asList("invalidEvent"));
            rv = "redirect:/";
        } else {
            model.addAttribute(ATTRIBUTE_EVENT, event);
            if (!model.containsAttribute("registration")) {
                model.addAttribute("registration", new Registration());
            }
            if (!model.containsAttribute(ATTRIBUTE_REGISTERED)) {
                model.addAttribute(ATTRIBUTE_REGISTERED, false);
            }
            rv = "register";
        }
        return rv;
    }

    @RequestMapping(value = "/register/{eventId}", method = POST)
    public String register(
            @PathVariable final Integer eventId,
            @Valid final Registration registration,
            final BindingResult registrationBindingResult,
            final Locale locale,
            final HttpServletRequest request,
            final Model model,
            final RedirectAttributes redirectAttributes
    ) {
        String rv;
        if (registrationBindingResult.hasErrors() || recaptchaValidator.validate(request).isFailure()) {
            model.addAttribute(ATTRIBUTE_ALERTS, Arrays.asList("invalidRegistration"));
            rv = register(eventId, model, redirectAttributes);
        } else {
            try {
                final RegistrationEntity registrationEntity = this.registrationService.register(eventId, registration);
                this.registrationService.sendConfirmationMail(registrationEntity, locale);
                redirectAttributes
                        .addFlashAttribute(ATTRIBUTE_EVENT, registrationEntity.getEvent())
                        .addFlashAttribute(ATTRIBUTE_REGISTERED, true)
                        .addFlashAttribute(ATTRIBUTE_ALERTS, Arrays.asList(ATTRIBUTE_REGISTERED));
                rv = "redirect:/register/" + eventId;
            } catch (InvalidRegistrationException e) {
                log.debug("Invalid registration request", e);
                model.addAttribute(ATTRIBUTE_ALERTS, Arrays.asList(e.getLocalizedMessage()));
                rv = register(eventId, model, redirectAttributes);
            }
        }
        return rv;
    }
}

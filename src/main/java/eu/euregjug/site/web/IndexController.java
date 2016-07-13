/*
 * Copyright 2015-2016 EuregJUG.
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

/**
 * @author Michael J. Simons, 2015-12-27
 */
@Controller
class IndexController {
    private final EventRepository eventRepository;
    
    private final RegistrationService registrationService;
    
    private final LinkRepository linkRepository;

    private final PostRepository postRepository;
    
    private final PostRenderingService postRenderingService;
          
    private final RecaptchaValidator recaptchaValidator;
    
    public IndexController(
	    EventRepository eventRepository, 
	    RegistrationService registrationService, 
	    LinkRepository linkRepository, 
	    PostRepository postRepository, 
	    PostRenderingService postRenderingService,
	    RecaptchaValidator recaptchaValidator
    ) {
	this.eventRepository = eventRepository;
	this.registrationService = registrationService;
	this.linkRepository = linkRepository;	
	this.postRepository = postRepository;
	this.postRenderingService = postRenderingService;	
	this.recaptchaValidator = recaptchaValidator;
    }
    
    @RequestMapping({"", "/", "/feed"})
    public String index(
	    final @RequestParam(required = false, defaultValue = "0") Integer page,
	    final Model model
    ) {
	model
		.addAttribute("upcomingEvents", this.eventRepository.findUpcomingEvents())
		.addAttribute("links", this.linkRepository.findAllByOrderByTypeAscSortColAscTitleAsc().stream().collect(groupingBy(LinkEntity::getType)))
		.addAttribute("posts", this.postRepository.findAll(new PageRequest(page, 5, Direction.DESC, "publishedOn", "createdAt")).map(postRenderingService::render))	
		;
	return "index";
    }   
    
    @RequestMapping({
	"/{year:\\d+}/{month:\\d+}/{day:\\d+}/{slug}",
	"/posts/{year:\\d+}-{month:\\d+}-{day:\\d+}-{slug}"
    })
    public String post(
	    final @PathVariable Integer year,
	    final @PathVariable Integer month,
	    final @PathVariable Integer day,
	    final @PathVariable String slug,
	    final Model model
    ) {

	String rv = "redirect:/";
	try {
	    final Date publishedOn = Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());	    
	    final Optional<PostEntity> post = this.postRepository.findByPublishedOnAndSlug(publishedOn, slug);
	    model
		    .addAttribute("previousPost", post.flatMap(this.postRepository::getPrevious))
		    .addAttribute("post", post.map(postRenderingService::render).get())
		    .addAttribute("nextPost", post.flatMap(this.postRepository::getNext))
		    ;
	    rv = "post";

	} catch (DateTimeException | NoSuchElementException e) {
	}
	return rv;
    }
    
    @RequestMapping({"/archive", "/archives"})
    public String archive(final Model model) {	
	model.addAttribute("posts", 
		this.postRepository
			.findAll(new Sort(Direction.DESC, "publishedOn")).stream()
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
    
    @RequestMapping(value = "/events", produces = "text/calendar")
    public String events(final Model model) {
	model.addAttribute("events", this.eventRepository.findUpcomingEvents());
	return "events";
    }
    
    @RequestMapping(value = "/register/{eventId}", method = GET)
    public String register(
	    final @PathVariable Integer eventId,
	    final Model model,
	    final RedirectAttributes redirectAttributes
    ) {
	final EventEntity event = this.eventRepository.findOne(eventId);
	String rv;
	if (event == null) {
	    redirectAttributes.addFlashAttribute("alerts", Arrays.asList("invalidEvent"));
	    rv = "redirect:/";
	} else {
	    model.addAttribute("event", event);
	    if(!model.containsAttribute("registration")) {
		model.addAttribute("registration", new Registration());
	    }
	    rv = "register";
	}
	return rv;
    }
    
    @RequestMapping(value = "/register/{eventId}", method = POST)
    public String register(
	    final @PathVariable Integer eventId,
	    final @Valid Registration registration,
	    final BindingResult registrationBindingResult,
	    final Locale locale,
	    final HttpServletRequest request,
	    final Model model,
	    final RedirectAttributes redirectAttributes
    ) {
	String rv;
	if (registrationBindingResult.hasErrors() || recaptchaValidator.validate(request).isFailure()) {
	    model.addAttribute("alerts", Arrays.asList("invalidRegistration"));
	    rv = register(eventId, model, redirectAttributes);
	} else {
	    try {
		final RegistrationEntity registrationEntity = this.registrationService.register(eventId, registration);
                this.registrationService.sendConfirmationMail(registrationEntity, locale);
		redirectAttributes
			.addFlashAttribute("event", registrationEntity.getEvent())
			.addFlashAttribute("registered", true)
			.addFlashAttribute("alerts", Arrays.asList("registered"));                
		rv = "redirect:/register/" + eventId;
	    } catch (InvalidRegistrationException e) {
		model.addAttribute("alerts", Arrays.asList(e.getLocalizedMessage()));
		rv = register(eventId, model, redirectAttributes);
	    }	    
	}
	return rv;
    }
}
/*
 * Copyright 2015 EuregJUG.
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

import eu.euregjug.site.events.EventRepository;
import eu.euregjug.site.links.LinkEntity;
import eu.euregjug.site.links.LinkRepository;
import eu.euregjug.site.posts.PostEntity;
import eu.euregjug.site.posts.PostRenderingService;
import eu.euregjug.site.posts.PostRepository;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author Michael J. Simons, 2015-12-27
 */
@Controller
public class IndexController {
    private final EventRepository eventRepository;
    
    private final LinkRepository linkRepository;

    private final PostRepository postRepository;
    
    private final PostRenderingService postRenderingService;
    
    @Autowired
    public IndexController(EventRepository eventRepository, LinkRepository linkRepository, PostRepository postRepository, PostRenderingService postRenderingService) {
	this.eventRepository = eventRepository;
	this.linkRepository = linkRepository;	
	this.postRepository = postRepository;
	this.postRenderingService = postRenderingService;	
    }
    
    @RequestMapping({"", "/"})
    public String index(
	    final @RequestParam(required = false, defaultValue = "0") Integer page,
	    final Model model
    ) {
	model
		.addAttribute("upcomingEvents", this.eventRepository.findUpcomingEvents())
		.addAttribute("links", this.linkRepository.findAllByOrderByTypeAscSortColAscTitleAsc().stream().collect(groupingBy(LinkEntity::getType)))
		.addAttribute("posts", this.postRepository.findAll(new PageRequest(page, 5, Direction.DESC, "publishedOn")).getContent().stream().map(this::createRenderedPosts).collect(toList()))	
		;
	return "index";
    }
    
    Post createRenderedPosts(final PostEntity postEntity) {
	final String renderedContent = this.postRenderingService.render(postEntity);	
	return new Post(postEntity.getPublishedOn(), postEntity.getTitle(), renderedContent);
    }
}

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
package eu.euregjug.site.events;

import eu.euregjug.site.posts.PostEntity;
import eu.euregjug.site.posts.PostRepository;
import eu.euregjug.site.support.ResourceNotFoundException;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import static org.springframework.http.HttpStatus.CREATED;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Michael J. Simons, 2015-12-27
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
class EventApiController {

    private final EventRepository eventRepository;

    private final PostRepository postRepository;

    private final RegistrationRepository registrationRepository;

    @RequestMapping(method = POST)
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(CREATED)
    public EventEntity create(@Valid @RequestBody final EventEntity newEvent) {
        return this.eventRepository.save(newEvent);
    }

    /**
     * Selects the event with the id {@code id}, the post with the id
     * {@code postId} and links them together. If the event is linked to another
     * post, this link is dropped. Returns a 404 if either event or post is not
     * found.
     *
     * @param id The id of the event to link a post to
     * @param postId The id of the post to link to the event
     * @return The event with the linked post
     */
    @RequestMapping(value = "/{id:\\d+}/post/{postId:\\d+}", method = PUT)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public EventEntity addPost(@PathVariable final Integer id, @PathVariable final Integer postId) {
        final EventEntity eventEntity = this.eventRepository.findOne(id).orElse(null);
        final PostEntity postEntity = this.postRepository.findOne(postId).orElse(null);
        if (eventEntity == null || postEntity == null) {
            throw new ResourceNotFoundException();
        }
        eventEntity.setPost(postEntity);
        return eventEntity;
    }

    @RequestMapping(method = GET)
    public Page<EventEntity> get(final Pageable pageable) {
        return this.eventRepository.findAll(pageable);
    }

    @RequestMapping(value = "/{eventId}/registrations", method = GET)
    @PreAuthorize("isAuthenticated()")
    public List<RegistrationEntity> getRegistrations(@PathVariable final Integer eventId) {
        return this.registrationRepository.findAllByEventId(eventId);
    }

    @RequestMapping(value = "/{id:\\d+}", method = PUT)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public EventEntity update(@PathVariable final Integer id, @Valid @RequestBody final EventEntity updatedEvent) {
        final EventEntity eventEntity = this.eventRepository.findOne(id).orElseThrow(ResourceNotFoundException::new);
        eventEntity.setDescription(updatedEvent.getDescription());
        eventEntity.setDuration(updatedEvent.getDuration());
        eventEntity.setNeedsRegistration(updatedEvent.isNeedsRegistration());
        eventEntity.setType(updatedEvent.getType());
        eventEntity.setSpeaker(updatedEvent.getSpeaker());
        eventEntity.setLocation(updatedEvent.getLocation());
        return eventEntity;
    }
}

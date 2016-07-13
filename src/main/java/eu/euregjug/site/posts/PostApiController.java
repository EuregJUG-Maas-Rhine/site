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
package eu.euregjug.site.posts;

import eu.euregjug.site.support.ResourceNotFoundException;
import javax.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * @author Michael J. Simons, 2015-12-28
 */
@RestController
@RequestMapping("/api/posts")
class PostApiController {

    private final PostRepository postRepository;

    public PostApiController(PostRepository postRepository) {
	this.postRepository = postRepository;
    }

    @RequestMapping(method = POST)
    @PreAuthorize("isAuthenticated()")
    public PostEntity create(final @Valid @RequestBody PostEntity newPost) {
	return this.postRepository.save(newPost);
    }

    @RequestMapping(method = GET)
    public Page<PostEntity> get(final Pageable pageable) {
	return this.postRepository.findAll(pageable);
    }
    
    @RequestMapping(value = "/{id:\\d+}", method = PUT)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @CacheEvict(cacheNames = "renderedPosts", key = "#id")
    public PostEntity update(final @PathVariable Integer id, final @Valid @RequestBody PostEntity updatedPost) {
	final PostEntity postEntity =  this.postRepository.findOne(id);
	if(postEntity == null) {
	    throw new ResourceNotFoundException();
	}
	postEntity.setContent(updatedPost.getContent());
	postEntity.setFormat(updatedPost.getFormat());
	postEntity.setTitle(updatedPost.getTitle());
	return postEntity;	
    }
}

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
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Michael J. Simons, 2015-12-28
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
class PostApiController {

    private final PostRepository postRepository;

    private final PostIndexService postIndexService;

    @RequestMapping(method = POST)
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(CREATED)
    public PostEntity create(@Valid @RequestBody final PostEntity newPost) {
        return this.postRepository.save(newPost);
    }

    @RequestMapping(method = GET)
    public Page<PostEntity> get(final Pageable pageable) {
        return this.postRepository.findAll(pageable);
    }

    @RequestMapping(path = "/search", method = GET)
    public List<PostEntity> get(@RequestParam final String q) {
        return this.postRepository.searchByKeyword(q);
    }

    @RequestMapping(path = "/{id:\\d+}", method = PUT)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    @CacheEvict(cacheNames = "renderedPosts", key = "#id")
    public PostEntity update(@PathVariable final Integer id, @Valid @RequestBody final PostEntity updatedPost) {
        final PostEntity postEntity =  this.postRepository.findOne(id).orElseThrow(ResourceNotFoundException::new);
        postEntity.setContent(updatedPost.getContent());
        postEntity.setFormat(updatedPost.getFormat());
        postEntity.setTitle(updatedPost.getTitle());
        if (updatedPost.getLocale() != null) {
            postEntity.setLocale(updatedPost.getLocale());
        }
        return postEntity;
    }

    @RequestMapping(path = "/rebuildIndex", method = POST)
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public void rebuildIndex() throws InterruptedException {
        this.postIndexService.rebuildIndex();
    }
}

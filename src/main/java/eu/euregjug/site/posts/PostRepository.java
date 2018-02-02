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
package eu.euregjug.site.posts;

import eu.euregjug.site.posts.PostEntity.Status;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons, 2015-12-28
 */
public interface PostRepository extends Repository<PostEntity, Integer>, PostRepositoryExt {

    /**
     * Saves the given post.
     *
     * @param entity
     * @return Persisted post
     */
    PostEntity save(PostEntity entity);

    /**
     * @param id
     * @return Post with the given Id or an empty optional
     */
    @Transactional(readOnly = true)
    Optional<PostEntity> findById(Integer id);

    /**
     * Selects a post by date and slug.
     *
     * @param publishedOn
     * @param slug
     * @return
     */
    @Transactional(readOnly = true)
    Optional<PostEntity> findByPublishedOnAndSlug(Date publishedOn, String slug);

    /**
     * Selects a "page" of posts with a given status.
     *
     * @param status status as selection criteria
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    Page<PostEntity> findAllByStatus(Status status, Pageable pageable);

    /**
     * Selects a "page" of posts.
     *
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    Page<PostEntity> findAll(Pageable pageable);

    /**
     * Selects all posts sorted by the specified sort.
     *
     * @param sort
     * @return
     */
    @Transactional(readOnly = true)
    List<PostEntity> findAll(Sort sort);
}

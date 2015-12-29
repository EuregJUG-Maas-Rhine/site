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
package eu.euregjug.site.posts;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Michael J. Simons, 2015-12-29
 */
public class PostRepositoryImpl implements PostRepositoryExt {

    private final EntityManager entityManager;

    @Autowired
    public PostRepositoryImpl(EntityManager entityManager) {
	this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostEntity> getPrevious(PostEntity post) {
	return getRelatedPost("PostEntity.getPrevious", post);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PostEntity> getNext(PostEntity post) {
	return getRelatedPost("PostEntity.getNext", post);
    }
    
    Optional<PostEntity> getRelatedPost(final String query, PostEntity post) {
	Optional<PostEntity> rv;
	try {
	    rv = Optional.of(
		    entityManager.createNamedQuery(query, PostEntity.class)
		    .setParameter("id", post.getId())
		    .setMaxResults(1)
		    .getSingleResult()
	    );
	} catch (NoResultException e) {
	    rv = Optional.empty();
	}
	return rv;
    }
}

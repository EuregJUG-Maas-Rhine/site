/*
 * Copyright 2015-2017 EuregJUG.
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

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Michael J. Simons, 2015-12-29
 */
public class PostRepositoryImpl implements PostRepositoryExt {

    private final EntityManager entityManager;

    public PostRepositoryImpl(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostEntity> getPrevious(final PostEntity post) {
        return getRelatedPost("PostEntity.getPrevious", post);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PostEntity> getNext(final PostEntity post) {
        return getRelatedPost("PostEntity.getNext", post);
    }

    Optional<PostEntity> getRelatedPost(final String query, final PostEntity post) {
        Optional<PostEntity> rv;
        try {
            rv = Optional.of(
                    entityManager.createNamedQuery(query, PostEntity.class)
                    .setParameter("id", post.getId())
                    .setMaxResults(1)
                    .getSingleResult()
            );
        } catch (@SuppressWarnings({"squid:S1166"}) NoResultException e) {
            // The only reason the query call is implemented manually
            // is the fact the I cannot use @Query and limiting
            // the result set in a database agnostic way
            // (for example through the method name (findXXX) or an annotation)
            rv = Optional.empty();
        }
        return rv;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostEntity> searchByKeyword(final String keyword) {
        final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        final QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(PostEntity.class).get();
        return fullTextEntityManager
                .createFullTextQuery(queryBuilder.simpleQueryString()
                .onFields("content")
                .withAndAsDefaultOperator()
                .matching(keyword)
                .createQuery(), PostEntity.class).getResultList();
    }
}

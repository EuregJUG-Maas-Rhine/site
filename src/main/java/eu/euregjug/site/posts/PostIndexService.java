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

import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons, 2016-09-19
 */
@Service
@RequiredArgsConstructor
public class PostIndexService {

    private final EntityManager entityManager;

    /**
     * Rebuilds the search index.
     * @throws java.lang.InterruptedException When index rebuild gets interupted
     */
    @Transactional
    public void rebuildIndex() throws InterruptedException {
        final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(this.entityManager);
        fullTextEntityManager.createIndexer(PostEntity.class).optimizeOnFinish(true).startAndWait();
    }
}

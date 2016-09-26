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

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons, 2015-12-26
 */
public interface EventRepository extends Repository<EventEntity, Integer> {

    /**
     * Saves the given event.
     *
     * @param entity
     * @return Persisted event
     */
    EventEntity save(EventEntity entity);

    /**
     * @return All upcoming events
     */
    @Query(value
            = " Select e"
            + "   from EventEntity e"
            + "  where e.heldOn > current_date()"
            + "  order by e.heldOn asc "
    )
    @Transactional(readOnly = true)
    List<EventEntity> findUpcomingEvents();

    /**
     * @param id
     * @return Event with the given Id or an empty optional
     */
    @Transactional(readOnly = true)
    Optional<EventEntity> findOne(Integer id);

    /**
     * Selects a "page" of events.
     *
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    Page<EventEntity> findAll(Pageable pageable);
}

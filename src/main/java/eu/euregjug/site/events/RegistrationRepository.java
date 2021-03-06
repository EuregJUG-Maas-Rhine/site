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
package eu.euregjug.site.events;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons, 2015-12-26
 */
public interface RegistrationRepository extends Repository<RegistrationEntity, Integer> {

    /**
     * Saves the given registration.
     *
     * @param entity
     * @return Persisted registration
     */
    RegistrationEntity save(RegistrationEntity entity);

    /**
     * @param event
     * @param email
     * @return A registration for a given event by a user.
     */
    Optional<RegistrationEntity> findByEventAndEmail(EventEntity event, String email);

    @Transactional(readOnly = true)
    List<RegistrationEntity> findAllByEventId(Integer eventId);

    /**
     * Deletes all registrations for a given event.
     *
     * @param event The event whose registrations should be deleted
     */
    void deleteByEvent(EventEntity event);

    /**
     * Counts the registrations for a given event.
     *
     * @param event The event whose registrations should be counted
     */
    int countByEvent(EventEntity event);

    /**
     * Deletes all registrations for events in the past.
     */
    @Modifying
    @Query("Delete from RegistrationEntity r where r.event in (select e from EventEntity e where e.heldOn < current_timestamp())")
    void deleteAllFromExpiredEvents();
}

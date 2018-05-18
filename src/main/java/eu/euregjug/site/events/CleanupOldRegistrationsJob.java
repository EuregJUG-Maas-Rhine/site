/*
 * Copyright 2018 EuregJUG.
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Used for cleaning up old registrations.
 *
 * @author Michael J. Simons, 2018-05-17
 */
@Component
@Slf4j
@RequiredArgsConstructor
final class CleanupOldRegistrationsJob {

    private final RegistrationService registrationService;

    @Scheduled(cron = "0 0 8 * * ?", zone = "Europe/Berlin")
    public void run() {
        log.info("Cleaning up old registrations");
        registrationService.cleanupOldRegistrations();
    }
}

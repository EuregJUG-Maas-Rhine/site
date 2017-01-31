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
package eu.euregjug.site.events;

import static org.hamcrest.Matchers.is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Michael J. Simons, 2016-08-25
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("it")
public class RegistrationRepositoryIT {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Test
    public void idGeneratorsShouldWorkWithPostgreSQLAsExpected() {
        // data-id.sql creates on registration with id "1"
        final EventEntity event = this.eventRepository.findOne(1).get();
        final RegistrationEntity savedRegistration = this.registrationRepository.save(new RegistrationEntity(event, "foo@bar.baz", "idGeneratorsShouldWorkWithPostgreSQLAsExpected", null, true));
        Assert.assertThat(savedRegistration.getId(), is(2));
    }
}

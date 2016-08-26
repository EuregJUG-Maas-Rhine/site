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
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author Michael J. Simons, 2016-08-26
 */
@RunWith(MockitoJUnitRunner.class)
public class RegistrationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MessageSource messageSource;

    @Test
    public void htmlTextToPlainTextShouldWork() {
        final RegistrationService service = new RegistrationService(eventRepository, registrationRepository, mailSender, null, messageSource, "info@euregjug.eu");
        final String plainText = service.htmlTextToPlainText("<strong>Hallo<br>das ist ein<br />test.<br/>Auf wiedersehen.");
        assertThat(plainText, is("Hallo\r\ndas ist ein\r\ntest.\r\nAuf wiedersehen."));
    }

}

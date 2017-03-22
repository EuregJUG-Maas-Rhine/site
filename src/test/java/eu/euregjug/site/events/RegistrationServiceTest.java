/*
 * Copyright 2016-2017 EuregJUG.
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

import java.util.Optional;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.rules.ExpectedException.none;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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

    @Rule
    public final ExpectedException expectedException = none();

    @Test
    public void htmlTextToPlainTextShouldWork() {
        final RegistrationService service = new RegistrationService(eventRepository, registrationRepository, mailSender, null, messageSource, "info@euregjug.eu");
        final String plainText = service.htmlTextToPlainText("<strong>Hallo<br>das ist ein<br />test.<br/>Auf wiedersehen.");
        assertThat(plainText, is("Hallo\r\ndas ist ein\r\ntest.\r\nAuf wiedersehen."));
    }

    @Test
    public void shouldHandleInvalidEvent() {
        final RegistrationService service = new RegistrationService(eventRepository, registrationRepository, mailSender, null, messageSource, "info@euregjug.eu");
        when(this.eventRepository.findOne(23)).thenReturn(Optional.empty());

        expectedException.expect(RegistrationService.InvalidRegistrationException.class);
        expectedException.expectMessage("No event with the id 23");

        service.register(23, new Registration());
    }

    @Test
    public void shouldHandleEventWithoutRegistration() {
        final RegistrationService service = new RegistrationService(eventRepository, registrationRepository, mailSender, null, messageSource, "info@euregjug.eu");
        final EventEntity event = mock(EventEntity.class);
        when(event.isNeedsRegistration()).thenReturn(false);
        when(this.eventRepository.findOne(23)).thenReturn(Optional.of(event));

        expectedException.expect(RegistrationService.InvalidRegistrationException.class);
        expectedException.expectMessage("Event 23 doesn't need a registration");
        final Registration registration = new Registration();
        registration.setEmail("michael@euregjug.eu");
        service.register(23, registration);
    }

    @Test
    public void shouldHandleClosedEvents() {
        final RegistrationService service = new RegistrationService(eventRepository, registrationRepository, mailSender, null, messageSource, "info@euregjug.eu");
        final EventEntity event = mock(EventEntity.class);
        when(event.isNeedsRegistration()).thenReturn(true);
        when(event.isOpenForRegistration()).thenReturn(false);
        when(this.eventRepository.findOne(23)).thenReturn(Optional.of(event));

        expectedException.expect(RegistrationService.InvalidRegistrationException.class);
        expectedException.expectMessage("Event 23 doesn't isn't open");
        final Registration registration = new Registration();
        registration.setEmail("michael@euregjug.eu");
        service.register(23, registration);
    }

    @Test
    public void shouldHandleAlreadyRegisteredGuest() {
        final RegistrationService service = new RegistrationService(eventRepository, registrationRepository, mailSender, null, messageSource, "info@euregjug.eu");
        final EventEntity event = mock(EventEntity.class);
        when(event.isNeedsRegistration()).thenReturn(true);
        when(event.isOpenForRegistration()).thenReturn(true);
        when(this.eventRepository.findOne(23)).thenReturn(Optional.of(event));
        when(this.registrationRepository.findByEventAndEmail(event, "michael@euregjug.eu")).thenReturn(Optional.of(mock(RegistrationEntity.class)));
        expectedException.expect(RegistrationService.InvalidRegistrationException.class);
        expectedException.expectMessage("Guest 'michael@euregjug.eu' already registered for event 23");

        final Registration registration = new Registration();
        registration.setEmail("michael@euregjug.eu");
        service.register(23, registration);
    }
    
    @Test
    public void registrationShouldWork() {
        final RegistrationService service = new RegistrationService(eventRepository, registrationRepository, mailSender, null, messageSource, "info@euregjug.eu");
        final EventEntity event = mock(EventEntity.class);
        when(event.isNeedsRegistration()).thenReturn(true);
        when(event.isOpenForRegistration()).thenReturn(true);
        when(this.eventRepository.findOne(23)).thenReturn(Optional.of(event));
        when(this.registrationRepository.findByEventAndEmail(event, "michael@euregjug.eu")).thenReturn(Optional.empty());
        when(this.registrationRepository.save(any(RegistrationEntity.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
        
        final Registration registration = new Registration();
        registration.setFirstName("Michael");
        registration.setName("Simons");
        registration.setEmail("michael@euregjug.eu");
        
        final RegistrationEntity registrationEntity = service.register(23, registration);
        assertThat(registrationEntity.getEvent(), is(event));
        assertThat(registrationEntity.getFirstName(), is(registration.getFirstName()));
        assertThat(registrationEntity.getName(), is(registration.getName()));
        assertThat(registrationEntity.getEmail(), is(registration.getEmail()));
        
        verify(this.eventRepository).findOne(23);
        verify(this.registrationRepository).findByEventAndEmail(event, "michael@euregjug.eu");
        verify(this.registrationRepository).save(any(RegistrationEntity.class));
        verifyNoMoreInteractions(this.eventRepository, this.registrationRepository);
    }
}

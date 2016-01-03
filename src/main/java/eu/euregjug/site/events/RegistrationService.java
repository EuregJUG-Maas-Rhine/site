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
package eu.euregjug.site.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons, 2015-12-29
 */
@Service
public class RegistrationService {
    public static class InvalidRegistrationException extends RuntimeException {	

	private static final long serialVersionUID = -2624768968401255945L;

	private final String localizedMessage;
	
	public InvalidRegistrationException(String message, final String localizedMessage) {	   
	    super(message);
	    this.localizedMessage = localizedMessage;
	}

	@Override
	public String getLocalizedMessage() {
	    return localizedMessage;
	}		
    }
    
    private final EventRepository eventRepository;
    
    private final RegistrationRepository registrationRepository;

    @Autowired
    public RegistrationService(EventRepository eventRepository, RegistrationRepository registrationRepository) {
	this.eventRepository = eventRepository;
	this.registrationRepository = registrationRepository;
    }
    
    @Transactional
    public RegistrationEntity register(final Integer eventId, final Registration newRegistration) {
	final EventEntity event = this.eventRepository.findOne(eventId);
	final String email = newRegistration.getEmail().toLowerCase();
	if(event == null) {
	    throw new InvalidRegistrationException(String.format("No event with the id %d", eventId), "invalidEvent");
	} else if(!event.isNeedsRegistration()) {
	    throw new InvalidRegistrationException(String.format("Event %d doesn't need a registration", eventId), "eventNeedNoRegistration");
	} else if(!event.isOpen()) {
	    throw new InvalidRegistrationException(String.format("Event %d doesn't isn't open", eventId), "eventNotOpen");
	} else if(this.registrationRepository.findByEventAndEmail(event, email).isPresent())  {
	    throw new InvalidRegistrationException(String.format("Guest '%s' already registered for event %d", email, eventId), "alreadyRegistered");
	} else {
	    return this.registrationRepository.save(new RegistrationEntity(event, email, newRegistration.getName(), newRegistration.getFirstName()));
	}	
    }
}

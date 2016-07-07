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

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;


/**
 * @author Michael J. Simons, 2015-12-29
 */
@Service
public class RegistrationService {
    
    public static final Logger LOGGER = LoggerFactory.getLogger(RegistrationService.class.getPackage().getName());
    
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
    
    /**
     * Needed to render the HTML template.
     */
    private final SpringTemplateEngine templateEngine;

    /**
     * Sender address for confirmation emails, taken from <code>spring.mail.properties.mail.smtp.from</code>
     */
    private final String mailFrom;
    
    /**
     * Spring Abstraction over JavaMail.
     */
    private final JavaMailSender mailSender;
    
    /**
     * Needed for translating some email properties.
     */
    private final MessageSource messageSource;

    @Autowired
    public RegistrationService(
	    final EventRepository eventRepository, 
	    final RegistrationRepository registrationRepository, 
	    final JavaMailSender mailSender,
	    final SpringTemplateEngine templateEngine,
	    final MessageSource messageSource,
	    final @Value("${spring.mail.properties.mail.smtp.from}") String mailFrom
    ) {
	this.eventRepository = eventRepository;
	this.registrationRepository = registrationRepository;
	this.mailFrom = mailFrom;
	this.mailSender = mailSender;
	this.templateEngine = templateEngine;
	this.messageSource = messageSource;		
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
	    return this.registrationRepository.save(
		    new RegistrationEntity(event, email, newRegistration.getName(), newRegistration.getFirstName(), newRegistration.isSubscribeToNewsletter())
	    );
	}	
    }
    
    @Async
    public void sendConfirmationMail(final RegistrationEntity registrationEntity, final Locale locale) {
        try {
            final Context context = new Context();
            context.setLocale(locale);
            context.setVariable("registration", registrationEntity);
            final Writer w = new StringWriter();
            templateEngine.process("registered", context, w);
            final String htmlText = w.toString();            
            mailSender.send(mimeMessage -> {
                final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
                message.setFrom(mailFrom);
                message.setTo(registrationEntity.getEmail());
                message.setSubject(messageSource.getMessage("registrationConfirmationSubject", new Object[]{registrationEntity.getEvent().getName()}, locale));
                message.setText(htmlTextToPlainText(htmlText), htmlText);

            });
            LOGGER.info("Sent confirmation email for '{}' to '{}'.", registrationEntity.getEvent().getName(), registrationEntity.getEmail());
        } catch (MailException e) {
            LOGGER.warn("Could not send an email to {} for event '{}': {}", registrationEntity.getEmail(), registrationEntity.getEvent().getName(), e.getMessage());
        }
    }
    
    /**
     * Cleans some html text by stripping all tags but <code>br</code> and then
     * unescapes named entitiesl like '&quote';. brs will be replaced by
     * newlines.
     *
     * @param htmlText
     * @return
     */
    String htmlTextToPlainText(final String htmlText) {
	final Whitelist whitelist = Whitelist.none();
	whitelist.addTags("br");
	final Cleaner cleaner = new Cleaner(whitelist);
	final Document cleanedDocument = cleaner.clean(Jsoup.parse(htmlText));
	cleanedDocument
		.outputSettings()
		.prettyPrint(false)
		.escapeMode(EscapeMode.xhtml)
		.charset(StandardCharsets.UTF_8);
	return Parser.unescapeEntities(cleanedDocument.body().html().trim(), true).replaceAll(Pattern.quote("<br />"), "\n");
    }
}

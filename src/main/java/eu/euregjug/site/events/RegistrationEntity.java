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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * A registration by a person identified by his email for an
 * {@link EventEntity event}.
 *
 * @author Michael J. Simons, 2015-12-26
 */
@Entity
@Table(
	name = "registrations",
	uniqueConstraints = {
	    @UniqueConstraint(name = "registrations_uk", columnNames = {"event_id", "email"})
	}
)
public class RegistrationEntity implements Serializable {

    private static final long serialVersionUID = 6473617754778481078L;

    /**
     * Primary key of this registration.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "registrations_id_seq_generator")
    @SequenceGenerator(name = "registrations_id_seq_generator", sequenceName = "registrations_id_seq")
    @JsonIgnore
    private Integer id;

    /**
     * The event the person registered for.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", referencedColumnName = "id")
    @JsonIgnore
    private EventEntity event;

    /**
     * Name of the person registered for the event.
     */
    @Column(name = "email", length = 1024, nullable = false)
    private String email;

    /**
     * Name of the person registered for the event.
     */
    @Column(name = "name", length = 512, nullable = false)
    private String name;

    /**
     * First name of the person registered for the event.
     */
    @Column(name = "first_name", length = 512)
    private String firstName;

    /**
     * A flag if the registered person also wants to subscribe to our newsletter. Defaults to  
     * {@code false}.
     */
    @Column(name = "subscribe_to_newsletter", nullable = false)
    private boolean subscribeToNewsletter = false;
    
    /**
     * Needed for Hibernate, not to be called by application code.
     */
    @SuppressWarnings({"squid:S2637"})
    protected RegistrationEntity() {
    }
        
    public RegistrationEntity(EventEntity event, String email, String name, String firstName, boolean subscribeToNewsletter) {
	this.event = event;
	this.email = email;
	this.name = name;
	this.firstName = firstName;
	this.subscribeToNewsletter = subscribeToNewsletter;
    }

    @JsonProperty
    public Integer getId() {
	return id;
    }

    public EventEntity getEvent() {
	return event;
    }

    public String getEmail() {
	return email;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getFirstName() {
	return firstName;
    }

    public void setFirstName(String firstName) {
	this.firstName = firstName;
    }

    public boolean isSubscribeToNewsletter() {
	return subscribeToNewsletter;
    }

    public void setSubscribeToNewsletter(boolean subscribeToNewsletter) {
	this.subscribeToNewsletter = subscribeToNewsletter;
    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 29 * hash + Objects.hashCode(this.event);
	hash = 29 * hash + Objects.hashCode(this.email);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final RegistrationEntity other = (RegistrationEntity) obj;
	if (!Objects.equals(this.email, other.email)) {
	    return false;
	}
	return Objects.equals(this.event, other.event);
    }
}

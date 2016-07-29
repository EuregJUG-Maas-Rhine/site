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

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Commandobject for new registrations. Needs getters and setters and all to
 * work inside thymeleaf forms.
 *
 * @author Michael J. Simons, 2016-01-03
 */
public final class Registration implements Serializable {

    private static final long serialVersionUID = 770264014400800147L;

    @Size(max = 512)
    private String firstName;

    @NotBlank
    @Size(max = 512)
    private String name;

    @NotBlank
    @Email
    @Size(max = 1024)
    private String email;

    @NotNull
    private boolean subscribeToNewsletter = false;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public boolean isSubscribeToNewsletter() {
        return subscribeToNewsletter;
    }

    public void setSubscribeToNewsletter(final boolean subscribeToNewsletter) {
        this.subscribeToNewsletter = subscribeToNewsletter;
    }
}

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

import eu.euregjug.site.events.EventEntity.Status;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2016-08-16
 */
public class EventEntityTest {

    @Test
    public void getDisplayNameShouldWork() {
        final EventEntity event = new EventEntity(Calendar.getInstance(), "test", "test");
        assertThat(event.getDisplayName(), is("test"));
        event.setSpeaker(" \t ");
        assertThat(event.getDisplayName(), is("test"));
        event.setSpeaker("test");
        assertThat(event.getDisplayName(), is("test - test"));
    }
    
    @Test
    public void isOpenForRegistrationShouldWork() {
        EventEntity event;
        event = new EventEntity(GregorianCalendar.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault())), "test", "test");        
        assertThat(event.isOpenForRegistration(), is(true));
        event.setStatus(Status.closed);
        assertThat(event.isOpenForRegistration(), is(false));
        event = new EventEntity(GregorianCalendar.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault())), "test", "test");
        assertThat(event.isOpenForRegistration(), is(false));
    }
}

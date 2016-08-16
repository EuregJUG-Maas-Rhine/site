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

import java.util.Calendar;
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
}

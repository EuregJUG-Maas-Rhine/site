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
package eu.euregjug.site.web;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Michael J. Simons, 2016-07-29
 */
public class EventsIcalViewTest {
    
    public EventsIcalViewTest() {
    }
    
    @Test
    public void formatLineShouldWork() {
        System.out.println("DESCRIPTION:1234567890123456789012345678901234567890123456789012345678901".length());
        final EventsIcalView view = new EventsIcalView();
        String s = ""
                + "DESCRIPTION:123456789012345678901234567890123456789012345678901234567890123456789012345"
                + "123456789012345678901234567890123456789012345678901234567890123456789012345"
                + "123456789012345678901234567890123456789012345678901234567890123456789012345";
        String e = ""
                + "DESCRIPTION:123456789012345678901234567890123456789012345678901234567890123" + EventsIcalView.ICS_LINEBREAK
                + " 45678901234512345678901234567890123456789012345678901234567890123456789012" + EventsIcalView.ICS_LINEBREAK
                + " 34567890123451234567890123456789012345678901234567890123456789012345678901" + EventsIcalView.ICS_LINEBREAK
                + " 23456789012345";        
        Assert.assertEquals(e, view.formatLine(new StringBuilder(s)));        
    }    
}

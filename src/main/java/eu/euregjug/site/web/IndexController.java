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
package eu.euregjug.site.web;

import eu.euregjug.site.events.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Michael J. Simons, 2015-12-27
 */
@Controller
public class IndexController {
    private final EventRepository eventRepository;

    @Autowired
    public IndexController(EventRepository eventRepository) {
	this.eventRepository = eventRepository;
    }
    
    @RequestMapping({"", "/"})
    public String index(final Model model) {
	model.addAttribute("upcomingEvents", this.eventRepository.findUpcomingEvents());
	return "index";
    }
}

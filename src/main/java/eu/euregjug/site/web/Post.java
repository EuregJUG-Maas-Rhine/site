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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Michael J. Simons, 2015-12-28
 */
public class Post {
    private final LocalDate publishedOn;
    
    private final String title;
    
    private final String content;

    public Post(final Date publishedOn, String title, String content) {
	this.publishedOn = publishedOn instanceof java.sql.Date? ((java.sql.Date)publishedOn).toLocalDate() : publishedOn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	this.title = title;
	this.content = content;
    }

    public LocalDate getPublishedOn() {
	return publishedOn;
    }
    
    public String getTitle() {
	return title;
    }

    public String getContent() {
	return content;
    }
}
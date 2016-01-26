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

import ac.simons.syndication.modules.atom.AtomContent;
import ac.simons.syndication.modules.atom.AtomModuleImpl;
import ac.simons.syndication.utils.SyndicationGuid;
import ac.simons.syndication.utils.SyndicationLink;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Content;
import com.rometools.rome.feed.rss.Item;
import eu.euregjug.site.posts.Post;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

/**
 * @author Michael J. Simons, 2015-12-30
 */
@Component("index.rss")
class IndexRssView extends AbstractRssFeedView {

    private final MessageSource messageSource;

    private final DateTimeFormatter permalinkDateFormatter;

    @Autowired
    public IndexRssView(MessageSource messageSource) {
	this.messageSource = messageSource;
	this.permalinkDateFormatter = DateTimeFormatter.ofPattern("/y/M/d", Locale.ENGLISH);
    }

    String getAbsoluteUrl(final HttpServletRequest request, final String relativeUrl) {
	final int port = request.getServerPort();
	final String hostWithPort = String.format("%s://%s%s",
		(request.isSecure() ? "https" : "http"),
		request.getServerName(),
		(Arrays.asList(80, 443).contains(port) ? "" : String.format(":%d", port))
	);
	return String.format("%s%s%s", hostWithPort, request.getContextPath(), relativeUrl);
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Channel feed, HttpServletRequest request) {
	final Page<Post> posts = (Page<Post>) model.get("posts");

	final Locale locale = request.getLocale();
	feed.setEncoding("UTF-8");
	feed.setTitle(String.format("%s - %s", messageSource.getMessage("siteTitle", null, locale), messageSource.getMessage("siteSubTitle", null, locale)));
	feed.setDescription(messageSource.getMessage("feedDescription", null, locale));
	feed.setLink(getAbsoluteUrl(request, ""));
	if (posts.hasContent()) {
	    final Date pubDate = Date.from(posts.getContent().get(0).getPublishedOn().atStartOfDay(ZoneId.systemDefault()).toInstant());
	    feed.setLastBuildDate(pubDate);
	    feed.setPubDate(pubDate);
	}
	feed.setGenerator("https://github.com/EuregJUG-Maas-Rhine/site");

	final String self = getAbsoluteUrl(request, "/feed.rss");
	final AtomContent atomContent = new AtomContent();
	atomContent.addLink(new SyndicationLink().withRel("previous").withType(super.getContentType()).withHref(self).getLink());
	if (posts.hasPrevious()) {
	    atomContent.addLink(new SyndicationLink()
		    .withRel("previous")
		    .withType(super.getContentType())
		    .withHref(String.format("%s?page=%d", self, posts.previousPageable().getPageNumber()))
		    .getLink()
	    );
	}
	if (posts.hasNext()) {
	    atomContent.addLink(new SyndicationLink()
		    .withRel("next")
		    .withType(super.getContentType())
		    .withHref(String.format("%s?page=%d", self, posts.nextPageable().getPageNumber()))
		    .getLink()
	    );
	}
	feed.getModules().add(new AtomModuleImpl(atomContent));
    }

    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
	final Page<Post> posts = (Page<Post>) model.get("posts");

	return posts.map(post -> {
	    final Item rv = new Item();
	    final Content content = new Content();
	    content.setType(Content.HTML);
	    content.setValue(post.getContent());
	    final String link = getAbsoluteUrl(request, String.format("%s/%s", permalinkDateFormatter.format(post.getPublishedOn()), post.getSlug()));

	    rv.setAuthor("euregjug.eu");
	    rv.setContent(content);
	    rv.setGuid(new SyndicationGuid().withPermaLink(true).withValue(link).getGuid());
	    rv.setLink(link);
	    rv.setPubDate(Date.from(post.getPublishedOn().atStartOfDay(ZoneId.systemDefault()).toInstant()));
	    rv.setTitle(post.getTitle());	    
	    return rv;
	}).getContent();
    }
}

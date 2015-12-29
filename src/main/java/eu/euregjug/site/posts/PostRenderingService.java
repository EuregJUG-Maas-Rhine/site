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
package eu.euregjug.site.posts;

import eu.euregjug.site.posts.PostEntity.Format;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A post rendering service that supports only AsciiDoc at the moment.
 * 
 * @author Michael J. Simons, 2015-12-28
 */
@Service
public class PostRenderingService {
    
    public static final Logger logger = LoggerFactory.getLogger(PostRenderingService.class.getPackage().getName());
	
    @FunctionalInterface
    interface Renderer {
	public String render(final String content);
    }
    
    static class AsciiDocRenderer implements Renderer {
	private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
	private final Options options = OptionsBuilder.options().inPlace(false).get();
		
	@Override
	public String render(String content) {
	    String rv = null;
	    try {		
		rv = asciidoctor.render(content, options);
	    } catch(Exception e) {
		logger.error("Could not render AsciiDoc content!", e);
		rv = "<strong>Could not render content.</strong>";
	    }	    
	    return rv;
	}
    }
    
    private final Renderer renderer = new AsciiDocRenderer();
	        
    public Post render(final PostEntity post) {
	String renderedContent = null;
	if(post.getFormat() != Format.asciidoc) {
	    renderedContent = "<strong>Could not render content.</strong>";
	} else {
	    renderedContent = renderer.render(post.getContent());
	}
	
	return new Post(post.getPublishedOn(), post.getSlug(), post.getTitle(), renderedContent);
    }
}

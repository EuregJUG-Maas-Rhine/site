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
package eu.euregjug.site.posts;

import java.util.Date;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2016-07-15
 */
public class PostRenderingServiceTest {

    @Test
    public void renderShouldWork() {
        final PostEntity entity1 = new PostEntity(new Date(), "a-title", "A title", "some *ASCIIdoc* content");
        entity1.setFormat(PostEntity.Format.markdown);
        final PostEntity entity2 = new PostEntity(new Date(), "a-title", "A title", "some *ASCIIdoc* content");
        entity2.setFormat(PostEntity.Format.asciidoc);
        final PostRenderingService postRenderingService = new PostRenderingService();

        Post post;
        post = postRenderingService.render(entity1);
        assertThat(post.getContent(), is("<strong>Could not render content.</strong>"));

        post = postRenderingService.render(entity2);
        assertThat(post.getContent(), is("<div class=\"paragraph\">\n<p>some <strong>ASCIIdoc</strong> content</p>\n</div>"));
    }
}

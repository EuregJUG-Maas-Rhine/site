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

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michael J. Simons, 2016-07-13
 */
public class PostEntityTest {

    @Test
    public void generateSlugShouldWork() {
        PostEntity postEntity = new PostEntity();
        String slug;
        slug = postEntity.generateSlug(null, "Rückblick zum aim42 Vortrag mit Gernot Starke");
        assertThat(slug, is("ruckblick-zum-aim42-vortrag-mit-gernot-starke"));
        slug = postEntity.generateSlug(" ", "Rückblick zum aim42 Vortrag mit Gernot Starke");
        assertThat(slug, is("ruckblick-zum-aim42-vortrag-mit-gernot-starke"));
        slug = postEntity.generateSlug("something-else", "Foo bar blah");
        assertThat(slug, is("something-else"));
    }
}

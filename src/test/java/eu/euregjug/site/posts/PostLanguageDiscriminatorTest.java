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
import java.util.Locale;
import static org.hamcrest.Matchers.is;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael J. Simons, 2016-09-19
 */
public class PostLanguageDiscriminatorTest {

    @Test
    public void testSomeMethod() {
        final PostEntity oldEntity = new PostEntity(new Date(), "oldslug", "oldtitle", "oldcontent");
        oldEntity.setLocale(new Locale("de", "DE"));
        Assert.assertThat(new PostLanguageDiscriminator().getAnalyzerDefinitionName("oldcontent", oldEntity, "content"), is("german"));
    }

}

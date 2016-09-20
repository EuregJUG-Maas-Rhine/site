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
package eu.euregjug.site.config;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.de.GermanStemFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.cfg.SearchMapping;

/**
 * This configuration can be used through
 * <pre>
 * spring.jpa.properties.hibernate.search.model_mapping = eu.euregjug.site.config.DefaultSearchMapping
 * </pre>
 *
 * This configuration is usefull if you want to run Hibernate Search against a
 * default directory based index which doesn't offer preconfigured analyzers like
 * lucene does.
 *
 * @author Michael J. Simons, 2016-09-20
 */
public final class DefaultSearchMapping {

    @Factory
    public SearchMapping getSearchMapping() {
        final SearchMapping mapping = new SearchMapping();
        mapping
                .analyzerDef("english", StandardTokenizerFactory.class)
                    .filter(LowerCaseFilterFactory.class)
                    .filter(SnowballPorterFilterFactory.class)
                .analyzerDef("german", StandardTokenizerFactory.class)
                    .filter(LowerCaseFilterFactory.class)
                    .filter(GermanStemFilterFactory.class);
        return mapping;
    }
}

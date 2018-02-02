/*
 * Copyright 2015-2018 EuregJUG.
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
package eu.euregjug.site.support.thymeleaf;

import eu.euregjug.site.support.thymeleaf.expressions.Temporals;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

/**
 * This is a custom dialect for the thymeleaf templates. It contains among
 * others serveral methods for formatting modern {@code java.time} instances.
 *
 * @author Michael J. Simons, 2015-01-04
 */
public final class EuregJUGDialect implements IExpressionObjectDialect {

    @Override
    public String getName() {
        return "eur";
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return new EuregJUGDialectExpressionObjectFactory();
    }

    static class EuregJUGDialectExpressionObjectFactory implements IExpressionObjectFactory {

        private final Set<String> allExpressionObjectNames = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("extemporals")));

        @Override
        public Set<String> getAllExpressionObjectNames() {
            return allExpressionObjectNames;
        }

        @Override
        public Object buildObject(final IExpressionContext context, final String expressionObjectName) {
            return new Temporals(context.getLocale());
        }

        @Override
        public boolean isCacheable(final String expressionObjectName) {
            return true;
        }

    }
}

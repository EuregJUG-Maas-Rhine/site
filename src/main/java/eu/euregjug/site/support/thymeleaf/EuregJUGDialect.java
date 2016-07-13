/*
 * Copyright 2015-2016 EuregJUG.
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
import java.util.HashMap;
import java.util.Map;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;

/**
 * This is a custom dialect for the thymeleaf templates. It contains among
 * others serveral methods for formatting modern {@code java.time} instances.
 *
 * @author Michael J. Simons, 2015-01-04
 */
public final class EuregJUGDialect extends AbstractDialect implements IExpressionEnhancingDialect {

    @Override
    public String getPrefix() {
        return "eur";
    }

    @Override
    public Map<String, Object> getAdditionalExpressionObjects(final IProcessingContext processingContext) {
        final Map<String, Object> expressionObjects = new HashMap<>();
        expressionObjects.put("temporals", new Temporals(processingContext.getContext().getLocale()));
        return expressionObjects;
    }
}

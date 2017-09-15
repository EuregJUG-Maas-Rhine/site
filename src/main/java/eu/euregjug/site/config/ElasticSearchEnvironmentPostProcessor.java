/*
 * Copyright 2017 EuregJUG.
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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Takes care of Pivotal CFs user:password@host format, that doesn't work with
 * Hibernates Elastic Search integration.
 *
 * @author Michael J. Simons, 2017-09-15
 */
public final class ElasticSearchEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_BASE = "spring.jpa.properties.hibernate.search.default.elasticsearch";
    private static final String PROPERTY_HOST = PROPERTY_BASE + ".host";
    private static final String PROPERTY_USERNAME = PROPERTY_BASE + ".username";
    private static final String PROPERTY_PASSWORD = PROPERTY_BASE + ".password";
    private static final String PROPERTY_INDEXMANAGER = "spring.jpa.properties.hibernate.search.default.indexmanager";

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application) {
        if (!"elasticsearch".equalsIgnoreCase(environment.getProperty(PROPERTY_INDEXMANAGER, String.class, "n/a"))) {
            return;
        }

        try {
            final URL url = new URL(environment.getRequiredProperty(PROPERTY_HOST));
            final String[] userInfo = url.getUserInfo().split(":");
            final Map<String, Object> properties = new HashMap<>();
            properties.put(PROPERTY_HOST, url.getProtocol() + "://" + url.getHost());
            properties.put(PROPERTY_USERNAME, userInfo[0]);
            properties.put(PROPERTY_PASSWORD, userInfo[1]);
            environment.getPropertySources().addFirst(new MapPropertySource(this.getClass().getName(), properties));
        } catch (Exception ex) {
            // I just assume there is a foo:bar@baz.com URL, if not, that's probably ok
        }
    }

    @Override
    public int getOrder() {
        return ConfigFileApplicationListener.DEFAULT_ORDER + 1;
    }
}

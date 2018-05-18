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
package eu.euregjug.site;

import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Michael J. Simons, 2015-12-26
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@PropertySource("classpath:build.properties")
@SuppressWarnings({"checkstyle:designforextension"})
public class Application {

    @Bean
    public Executor taskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @SuppressWarnings({"squid:S2095"}) // Ignore 'Close this "ConfigurableApplicationContext".'
    public static void main(final String... args) {
        SpringApplication.run(Application.class, args);
    }
}

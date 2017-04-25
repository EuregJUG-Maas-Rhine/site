/*
 * Copyright 2015-2017 EuregJUG.
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

import eu.euregjug.site.support.thymeleaf.EuregJUGDialect;
import java.time.Duration;
import java.util.Locale;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * @author Michael J. Simons, 2015-12-27
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/about").setViewName("about");
    }

    @Bean
    public EuregJUGDialect enSupplyDialect() {
        return new EuregJUGDialect();
    }

    @Bean
    public LocaleResolver localeResolver() {
        final CookieLocaleResolver rv = new CookieLocaleResolver();
        rv.setDefaultLocale(Locale.ENGLISH);
        rv.setDefaultTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        rv.setLanguageTagCompliant(true);
        rv.setCookieMaxAge((int) Duration.ofDays(365).getSeconds());
        return rv;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        final LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("*");
    }
}

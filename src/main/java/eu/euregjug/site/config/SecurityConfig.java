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
package eu.euregjug.site.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;


/**
 * @author Michael J. Simons, 2015-12-27
 */
@Configuration
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SuppressWarnings({"squid:S1118"}) // This is not a utility class. It cannot have a private constructor.
public class SecurityConfig {

    @Configuration
    @EnableAuthorizationServer
    @ConditionalOnBean(SecurityConfig.class)
    static class AuthorizationServerConfig {
    }

    @Configuration
    @Profile("cloud")
    @ConditionalOnBean(SecurityConfig.class)
    static class TokenStoreConfig {

        @Bean
        public TokenStore tokenStore(final RedisConnectionFactory redisConnectionFactory) {
            log.debug("Enabling RedisTokenStore");
            return new RedisTokenStore(redisConnectionFactory);
        }
    }

    @Configuration
    @EnableResourceServer
    @ConditionalOnBean(SecurityConfig.class)
    static class ResourceServerConfig extends ResourceServerConfigurerAdapter {

        /**
         * The part of the api that needs protection is secured by method
         * level security. So all here the http security is matched on the /api/**
         * endpoints but permits all access by default. Secured resources are
         * then blacklisted on method level.
         * <br>
         * We must also take care of the actuator endpoints. An alternative
         * would be excluding them by using
         * <pre>http.regexMatcher("/api/(?!.*(system)).*")</pre> as first matcher
         * but that would mean having to separate auth mechanism in place.
         * <br>
         * I prefer manually enabling the non sensitive ones.
         *
         * @param http
         * @throws Exception
         */
        @Override
        public void configure(final HttpSecurity http) throws Exception {
            http
                .antMatcher("/api/**")
                    .authorizeRequests()
                        .regexMatchers("/api/system/(?:info|health|metrics)").permitAll()
                        .antMatchers("/api/system/**").authenticated()
                        .antMatchers("/api/**").permitAll()
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .enableSessionUrlRewriting(false)
                .and()
                    .csrf()
                    .disable();
        }
    }

    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    @ConditionalOnBean(SecurityConfig.class)
    protected static class ApplicationWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http
                .httpBasic()
                    .and()
                .authorizeRequests()
                    .antMatchers("/oauth/**").authenticated()
                    .antMatchers("/**").permitAll()
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .enableSessionUrlRewriting(false);
        }
    }
}

    package com.expo.security.config;

    import com.expo.login.service.UserService;
    import com.expo.security.model.AuthEntryPointJwt;
    import com.expo.security.model.AuthTokenFilter;
    import com.expo.security.model.JwtUtils;
    import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

    @Configuration
    @EnableWebSecurity
    public class WebSecurityConfig {

        private final JwtUtils jwtUtils;
        private final UserService userDetailsService;
        public WebSecurityConfig(JwtUtils jwtUtils, UserService userDetailsService) {
            this.jwtUtils = jwtUtils;
            this.userDetailsService = userDetailsService;
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthEntryPointJwt unauthorizedHandler() {
            return new AuthEntryPointJwt();
        }

        @Bean
        public AuthTokenFilter jwtTokenFilter() {
            return new AuthTokenFilter(jwtUtils, userDetailsService);
        }

        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable()
                    .exceptionHandling().authenticationEntryPoint(unauthorizedHandler())
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .authorizeRequests(authorizeRequests -> {
                        try {
                            configureAuthorization(authorizeRequests.and());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        }

        private void configureAuthorization(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .requestMatchers(String.valueOf(PathRequest.toStaticResources().atCommonLocations())).permitAll() // Allow access to static resources
                    .requestMatchers(new AntPathRequestMatcher("/api/auth/users/login")).permitAll() // Allow access to login endpoint
                    .requestMatchers(new AntPathRequestMatcher("/api/auth/users/")).permitAll() // Allow access to login endpoint
                    .anyRequest().authenticated() // Require authentication for other requests
                    .and()
                    .formLogin().disable(); // Disable the default form login
        }





    }

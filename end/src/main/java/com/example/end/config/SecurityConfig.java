package com.example.end.config;

import com.example.end.domain.model.User;
import com.example.end.exception.ApiException;
import com.example.end.repository.UserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static java.lang.String.format;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

  @Value("${jwt.public.key}")
  private RSAPublicKey rsaPublicKey;

  @Value("${jwt.private.key}")
  private RSAPrivateKey rsaPrivateKey;

  @Value("${springdoc.api-docs.path}")
  private String restApiDocPath;

  @Value("${springdoc.swagger-ui.path}")
  private String swaggerPath;

  @Bean
  public AuthenticationManager authenticationManager(
      HttpSecurity http,
      UserDetailsService service,
      BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {

    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .userDetailsService(service)
        .passwordEncoder(bCryptPasswordEncoder)
        .and()
        .build();
  }

  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
    return identifier -> {
      User user;

      if(userRepository.existsByUsername(identifier)) {
        user = userRepository.findByUsername(identifier).get();
      } else if(userRepository.existsByEmail(identifier)) {
        user = userRepository.findByEmail(identifier).get();
      } else {
        throw new ApiException(HttpStatus.UNAUTHORIZED, "bad credentials");
      }

      if(!user.isEnabled()) {
        throw new ApiException(
                HttpStatus.FORBIDDEN,
                "Confirm your account on email to get access to your profile");
      }

      return user;
    };
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable();

    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    http.exceptionHandling(
        (exceptions) ->
            exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));

    http.authorizeHttpRequests()
            .requestMatchers(
                    "/",
                    format("%s/**", restApiDocPath),
                    format("%s/**", swaggerPath))
            .permitAll()
            .anyRequest().permitAll()
            .and()
            .httpBasic(Customizer.withDefaults())
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

    return http.build();
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    var jwk = new RSAKey.Builder(this.rsaPublicKey).privateKey(this.rsaPrivateKey).build();
    var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwks);
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(this.rsaPublicKey).build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsFilter corsFilter() {
    var source = new UrlBasedCorsConfigurationSource();
    var config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}

package com.rbc.ResourceServer.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import com.rbc.ResourceServer.exception.InvalidTokenException;
import com.rbc.ResourceServer.token.AccessToken;
import com.rbc.ResourceServer.token.JwtTokenValidator;

public class AccessTokenFilter extends AbstractAuthenticationProcessingFilter {
	
	private JwtTokenValidator tokenVerifier;
	Logger log = LogManager.getLogger(AccessTokenFilter.class);
	
	public AccessTokenFilter(
            JwtTokenValidator jwtTokenValidator,
            AuthenticationManager authenticationManager, AuthenticationFailureHandler authenticationFailureHandler) {

        super(AnyRequestMatcher.INSTANCE);
        setAuthenticationManager(authenticationManager);
        this.tokenVerifier = jwtTokenValidator;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) {

        log.info("Attempting to authenticate for a request {}", request.getRequestURI());

        String authorizationHeader = extractAuthorizationHeaderAsString(request);
        AccessToken accessToken = tokenVerifier.validateAuthorizationHeader(authorizationHeader);
        return this.getAuthenticationManager()
                            .authenticate(new JwtAuthentication(accessToken));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        log.info("Successfully authentication for the request {}", request.getRequestURI());

        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

    private String extractAuthorizationHeaderAsString(HttpServletRequest request) {
        try {
            return request.getHeader("Authorization");
        } catch (Exception ex){
            throw new InvalidTokenException("There is no Authorization header in a request", ex);
        }
    }
}

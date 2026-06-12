package org.civlabs.civlabsschedulebackend.config;

import org.civlabs.civlabsschedulebackend.interceptor.JwtCookieHandshakeInterceptor;
import org.civlabs.civlabsschedulebackend.interceptor.StompAuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableMethodSecurity
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtCookieHandshakeInterceptor jwtHandshakeInterceptor;
    private final StompAuthInterceptor stompAuthInterceptor;

    public WebSocketConfig(JwtCookieHandshakeInterceptor jwtHandshakeInterceptor, StompAuthInterceptor stompAuthInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
        this.stompAuthInterceptor = stompAuthInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
//                .setHandshakeHandler(new UserHandshakeHandler())
                .addInterceptors(jwtHandshakeInterceptor) // copies session → WS attrs
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/room");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
                stompAuthInterceptor,                      // 1. sets accessor.setUser(auth)
                new SecurityContextChannelInterceptor()    // 2. populates SecurityContext from that user
        );
    }

}

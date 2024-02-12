package com.shoping.flipkart.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shoping.flipkart.Repository.AccessTokenRepo;
import com.shoping.flipkart.entity.AccessToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter{

	private AccessTokenRepo accessTokenRepo;
	private JwtService jwtService;
	private CustomUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String at = null;
		String rt = null;
		Cookie[] cookies = request.getCookies();
		if(cookies!=null) {
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("at"))
				at = cookie.getValue();
			if (cookie.getName().equals("rt"))
				rt = cookie.getValue();
		}
		String username = null;
		if (at != null || rt != null)
		{
		Optional<AccessToken> accessToken = accessTokenRepo.findByTokenAndIsBlocked(at, false);
		if (accessToken == null)
			throw new UsernameNotFoundException("user not logged in");
		else {
			log.info("Authenticating the token");
			username = jwtService.extractUsername(at);
			if (username == null)
				throw new UsernameNotFoundException("User failed to authenticate");
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null,
					userDetails.getAuthorities());
			token.setDetails(new WebAuthenticationDetails(request));
			SecurityContextHolder.getContext().setAuthentication(token);
			log.info("Authenticated successfully");
		}
	}
}
       filterChain.doFilter(request, response);
	}

	}

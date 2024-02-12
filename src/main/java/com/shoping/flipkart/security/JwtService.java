package com.shoping.flipkart.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;



@Service
public class JwtService {
	@Value("${myapp.secret}")
    private String secret;	
	
	@Value("${myapp.access.expiry}")
	private Long accessExpirationInseconds;	
	
	@Value("${myapp.refresh.expiry}")
	private Long refreshExpirationInSeconds;
	
	
	public String generateAccessToken(String username) {
		return generateJWT(new HashMap<String,Object>(), username,accessExpirationInseconds*10001);
	}
	
	public String generateRefreshToken(String username) {
		return generateJWT(new HashMap<String,Object>(), username,refreshExpirationInSeconds*10001);
	}
	
	
	private String generateJWT(Map<String,Object> claims,String username,Long expiry) {
		return Jwts.builder().setClaims(claims).setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis()+expiry))
				.signWith(getSignature(), SignatureAlgorithm.HS512)// signing the JWT with key
				.compact();
	}
	private Key getSignature() {
		byte[] secretbytes=Decoders.BASE64.decode(secret);
	    return Keys.hmacShaKeyFor(secretbytes);
	}
	public Claims jwtParser(String token) {
		JwtParser jwtParser=Jwts.parserBuilder().setSigningKey(getSignature()).build();
		return jwtParser.parseClaimsJwt(token).getBody();
		
	}
	
	
	public String extractUsername(String token) {
		return jwtParser(token).getSubject();
	}
	
}

package com.example.mini.global.auth.external;

import com.example.mini.global.api.exception.GlobalException;
import com.example.mini.global.api.exception.error.AuthErrorCode;
import com.example.mini.global.auth.model.GoogleUserInfo;
import com.example.mini.global.auth.model.TokenResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public abstract class OAuthApiClient {

    protected final RestTemplate restTemplate = new RestTemplate();

    protected abstract String getClientId();

    protected abstract String getClientSecret();
    protected abstract String getRedirectUri();
    protected abstract String getTokenUri();

    protected abstract String getUserInfoUri();

    public TokenResponse getToken(String code){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", getClientId());
        params.add("client_secret", getClientSecret());
        params.add("redirect_uri", getRedirectUri());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(getTokenUri(), request, TokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new GlobalException(AuthErrorCode.TOKEN_FETCH_FAILED);
        }
    }

    public Map<String, Object> getUserInfo(String accessToken){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                getUserInfoUri(),
                HttpMethod.GET,
                entity,
                Map.class
        );

        return response.getBody();
    }

    public TokenResponse getRefreshedToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", getClientId());
        params.add("client_secret", getClientSecret());
        params.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(getTokenUri(), request, TokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new GlobalException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
    }
}

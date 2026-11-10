package com.gk.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackendApplicationTests {

    @Autowired
    private TestRestTemplate http;

    @Test
    void helloRequiresAuthentication() {
        assertThat(http.getForEntity("/hello", String.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void wrongPasswordDoesNotCreateSession() {
        ResponseEntity<Void> response = login("""
                {"username":"test","password":"wrong"}
                """);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void missingPasswordIsRejected() {
        assertThat(login("""
                {"username":"test"}
                """).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void loginRequiresJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> form = new HttpEntity<>("username=test&password=123456", headers);

        assertThat(http.postForEntity("/login", form, Void.class).getStatusCode())
                .isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void validLoginGrantsAccessUsingSessionCookie() {
        ResponseEntity<Void> loginResponse = login("""
                {"username":"test","password":"123456"}
                """);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        String cookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookie).startsWith("JSESSIONID=").containsIgnoringCase("HttpOnly");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookie.split(";", 2)[0]);
        ResponseEntity<String> response =
                http.exchange("/hello", HttpMethod.GET, new HttpEntity<Void>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_PLAIN)).isTrue();
        assertThat(response.getBody()).isEqualTo("Hello World");
    }

    private ResponseEntity<Void> login(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return http.postForEntity("/login", new HttpEntity<>(json, headers), Void.class);
    }
}

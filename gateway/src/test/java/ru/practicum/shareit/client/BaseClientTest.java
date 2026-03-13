package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BaseClientTest {

    private RestTemplate restTemplate;
    private TestClient client;

    private static class TestClient extends BaseClient {
        public TestClient(RestTemplate rest) {
            super(rest);
        }

        public ResponseEntity<Object> testGet(String path, Long userId, Map<String, Object> parameters) {
            return get(path, userId, parameters);
        }

        public <T> ResponseEntity<Object> testPost(String path, Long userId, Map<String, Object> parameters, T body) {
            return post(path, userId, parameters, body);
        }
    }

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        client = new TestClient(restTemplate);
    }

    @Test
    void get_whenSuccessful_thenReturnResponse() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("Success");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = client.testGet("/test", 1L, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody());
    }

    @Test
    void get_whenHttpStatusCodeException_thenHandleError() {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Error".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = client.testGet("/test", 1L, null);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void post_whenWithParameters_thenSuccess() {
        Map<String, Object> parameters = Map.of("param", "value");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("Created");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = client.testPost("/test?param={param}", 1L, parameters, "body");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Created", response.getBody());

        verify(restTemplate).exchange(eq("/test?param={param}"), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(Object.class), eq(parameters));
    }

    @Test
    void post_whenWithoutUserId_thenSuccess() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("Created");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = client.testPost("/test", null, null, "body");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
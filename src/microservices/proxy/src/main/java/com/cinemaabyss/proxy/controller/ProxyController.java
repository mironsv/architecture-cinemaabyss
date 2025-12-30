package com.cinemaabyss.proxy.controller;

import com.cinemaabyss.proxy.service.FeatureFlagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Main controller for the API Gateway.
 * Handles all incoming requests and routes them to appropriate backend services
 * based on feature flags and path patterns.
 */
@RestController
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);
    
    private final FeatureFlagService featureFlagService;
    private final RestTemplate restTemplate;

    public ProxyController(FeatureFlagService featureFlagService, RestTemplate restTemplate) {
        this.featureFlagService = featureFlagService;
        this.restTemplate = restTemplate;
        logger.debug("RestTemplate instance: {}", restTemplate.getClass().getName());
    }

    /**
     * Handles all requests to /api/* paths and routes them to appropriate services.
     */
    @RequestMapping("/api/**")
    public ResponseEntity<String> proxyRequest(HttpServletRequest request) {
        try {
            String path = request.getRequestURI();
            String method = request.getMethod();
            String queryString = request.getQueryString();
            int contentLength = request.getContentLength();
            
            logger.info("Received {} request to: {}", method, path);
            if (queryString != null) {
                logger.info("Query string: {}", queryString);
            }
            logger.info("Content-Length: {}", contentLength);
            String contentType = request.getContentType();
            if (contentType != null) {
                logger.info("Content-Type: {}", contentType);
            } else {
                logger.info("Content-Type: null");
            }
            
            // Log all request headers
            logger.debug("Request headers:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                logger.debug("  {}: {}", headerName, headerValue);
            }
            
            String targetService = featureFlagService.getTargetService(path);
            
            logger.info("Proxying request: {} -> {}", path, targetService);
            
            // Build target URL
            String targetUrl = targetService + path;
            if (request.getQueryString() != null) {
                targetUrl += "?" + request.getQueryString();
            }
            logger.debug("Target URL: {}", targetUrl);
            
            // Create headers
            HttpHeaders headers = createHeaders(request);
            
            // Log all headers that will be sent
            logger.debug("Headers that will be sent to target service:");
            for (String headerName : headers.keySet()) {
                logger.debug("  {}: {}", headerName, headers.get(headerName));
            }
            
            // Create request entity
            String body = getBody(request);
            HttpEntity<String> entity;
            if (body != null) {
                logger.debug("Request body length: {}", body.length());
                logger.debug("Request body: {}", body);
                logger.debug("Creating HttpEntity with body and headers");
                entity = new HttpEntity<>(body, headers);
            } else {
                logger.debug("No request body");
                logger.debug("Creating HttpEntity with headers only");
                entity = new HttpEntity<>(headers);
            }
            
            // Make the request
            HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
            logger.debug("Making request to: {} with method: {}, headers: {}, body: {}", 
                        targetUrl, 
                        httpMethod, 
                        entity.getHeaders(), 
                        entity.getBody());
            
            logger.debug("Calling restTemplate.exchange with: URL={}, Method={}, Entity={}, ResponseType={}", 
                        targetUrl, 
                        HttpMethod.valueOf(request.getMethod()), 
                        entity, 
                        String.class);
            
            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.valueOf(request.getMethod()),
                entity,
                String.class
            );
            
            logger.debug("Response received: class={}, status={}, headers={}, bodyLength={}", 
                        response.getClass().getName(), 
                        response.getStatusCode(), 
                        response.getHeaders(), 
                        response.getBody() != null ? response.getBody().length() : 0);
            
            logger.info("Request completed: {} -> {} (status: {})", 
                       path, targetService, response.getStatusCode());
            
            return ResponseEntity
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
                
        } catch (Exception e) {
            logger.error("Error proxying request: {}", request.getRequestURI(), e);
            logger.error("Exception type: {}", e.getClass().getName());
            logger.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Root cause: {}", e.getCause().getMessage());
            }
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint for the proxy service.
     */
    @RequestMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Strangler Fig Proxy is healthy");
    }

    /**
     * Creates HTTP headers from the incoming request.
     */
    private HttpHeaders createHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Skip hop-by-hop headers
            if (!isHopByHopHeader(headerName)) {
                headers.set(headerName, headerValue);
            }
        }
        
        // Handle Content-Length header properly for POST requests
        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getContentLength() > 0) {
            String contentLength = String.valueOf(request.getContentLength());
            logger.debug("Setting Content-Length: {}", contentLength);
            headers.set("Content-Length", contentLength);
            logger.debug("Content-Length header will be sent: {}", headers.get("Content-Length"));
        }
        
        // Ensure Content-Type header is properly set for POST requests with body
        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getContentLength() > 0) {
            if (headers.getContentType() == null) {
                // If no Content-Type specified, try to get it from request
                String contentType = request.getContentType();
                if (contentType != null) {
                    logger.debug("Setting Content-Type from request: {}", contentType);
                    headers.set("Content-Type", contentType);
                } else {
                    // Default to application/json if no content type specified
                    logger.debug("Setting default Content-Type: application/json");
                    headers.set("Content-Type", "application/json");
                }
            } else {
                logger.debug("Content-Type already set: {}", headers.getContentType());
            }
            logger.debug("Content-Type header will be sent: {}", headers.getContentType());
        }
        
        return headers;
    }

    /**
     * Gets the request body from HttpServletRequest.
     */
    private String getBody(HttpServletRequest request) {
        try {
            // Check if there's content to read
            if (request.getContentLength() <= 0) {
                return null;
            }
            
            // Read the entire request body as a string
            java.io.InputStream inputStream = request.getInputStream();
            java.util.Scanner scanner = new java.util.Scanner(inputStream, "UTF-8").useDelimiter("\\A");
            String body = scanner.hasNext() ? scanner.next() : "";
            
            // If body is empty after reading, return null
            return body.isEmpty() ? null : body;
        } catch (Exception e) {
            logger.debug("Error reading request body", e);
            return null;
        }
    }

    /**
     * Checks if a header is a hop-by-hop header that shouldn't be forwarded.
     */
    private boolean isHopByHopHeader(String headerName) {
        switch (headerName.toLowerCase()) {
            case "connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "te", "trailers", "transfer-encoding", "upgrade":
                return true;
            default:
                return false;
        }
    }
}

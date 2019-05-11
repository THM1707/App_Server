package com.thm.app_server.service;

import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class FirebaseService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);
    @Value("${app.firebaseKey}")
    private String firebaseKey;

    @Async
    public void setAvailable(Long id, int available) {
        final String uri = "https://gr-project-1707.firebaseio.com/parking/" + id + "/available.json";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(String.valueOf(available), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);
        logger.info(result.toString());
    }

    @Async
    public void setStar(Long id, float star) {
        final String uri = "https://gr-project-1707.firebaseio.com/parking/" + id + "/star.json";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Float> entity = new HttpEntity<>(star, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);
        logger.info(result.toString());
    }

    @Async
    public void addParkingLot(Long id, String name, double latitude, double longitude, float star, int available, int price) {
        final String uri = "https://gr-project-1707.firebaseio.com/parking/" + id + ".json";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject content = new JSONObject();
        content.put("name", name);
        content.put("id", id);
        content.put("latitude", latitude);
        content.put("longitude", longitude);
        content.put("star", star);
        content.put("available", available);
        content.put("price", price);
        HttpEntity<String> entity = new HttpEntity<>(content.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        ResponseEntity result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);
        logger.info(result.toString());
    }

    @Async
    public void setPending(Long id, int value) {
        final String uri = "https://gr-project-1707.firebaseio.com/pending/" + id + ".json";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Integer> entity = new HttpEntity<>(value, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);
        logger.info(result.toString());
    }

    @Async
    /*
      type:
        0 - add
        1 - minus
     */
    public void sendWalletChangeMessage(String token, int value, int type) {
        final String uri = "https://fcm.googleapis.com/fcm/send";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "key=" + firebaseKey);
        JSONObject content = new JSONObject();
        JSONObject notification = new JSONObject();
        notification.put("title", "Transaction notice");
        String body = String.format(Locale.getDefault(), "Your wallet have been %s %s",
                type == 0 ? "added" : "decreased", getAmountNumber(value));
        notification.put("body", body);
        content.put("to", token);
        content.put("notification", notification);
        HttpEntity<String> entity = new HttpEntity<>(content.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        ResponseEntity result = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        logger.info(result.toString());
    }

    private String getAmountNumber(int price) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        return numberFormat.format(price) + " đ";
    }
}

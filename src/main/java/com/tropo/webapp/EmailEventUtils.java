package com.tropo.webapp;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.utils.DateUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Strings;

public final class EmailEventUtils {
    
    private static final ObjectMapper mapper = new ObjectMapper();

    private EmailEventUtils() {
    }

    public static Map<String, String> parseUserVariables(String userVariables) {
        Map<String, String> userVariablesMap = null;

        if (!Strings.isNullOrEmpty(userVariables)) {
            try {
                userVariablesMap = mapper
                    .readValue(userVariables, new TypeReference<Map<String, String>>() {
                    });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return userVariablesMap;
    }

        public static InternalEmailEvent parseMessageHeaders(String messageHeaders) {
        String serviceName = "UNKNOWN";
        Map<String, String> userVariables = null;

        JsonNode jsonNode = null;

        try {
            jsonNode = mapper.readTree(messageHeaders);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> messageHeadersMap = new HashMap<String, String>();

        for (JsonNode json : jsonNode) {
            ArrayNode arrayNode = (ArrayNode) json;

            String messageHeadersKey = arrayNode.get(0).asText();
            String messageHeadersValue = arrayNode.get(1).asText();

            messageHeadersMap.put(messageHeadersKey, messageHeadersValue);
        }

        String variables = messageHeadersMap.get("X-Mailgun-Variables");
        if (!Strings.isNullOrEmpty(variables)) {
            JsonNode variablesJsonNode = null;

            try {
                variablesJsonNode = mapper.readTree(variables);
            } catch (IOException e) {e.printStackTrace();
            }

            JsonNode serviceNameJsonNode = variablesJsonNode.get("serviceName");
            if (serviceNameJsonNode != null) {
                serviceName = serviceNameJsonNode.asText();
            } 

            JsonNode userVariablesNode = variablesJsonNode.get("userVariables");
            if (userVariablesNode != null) {
                try {
                    userVariables = mapper.readValue(userVariablesNode.asText(),
                                                           new TypeReference<Map<String, String>>() {
                                                           });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
        } 
        String subject = messageHeadersMap.get("Subject");
        String sender = messageHeadersMap.get("From");

        InternalEmailEvent emailEvent = new InternalEmailEvent();
        emailEvent.setServiceName(serviceName);
        emailEvent.setSubject(subject);
        emailEvent.setSender(sender);
        emailEvent.setUserVariables(userVariables);
        if (messageHeadersMap.get("Date") != null) {
            Date sendDate = DateUtils.parseDate(messageHeadersMap.get("Date"), new String[] {
                                                DateUtils.PATTERN_RFC1123 });
            emailEvent.setSendDate(sendDate);
        } else {
            System.out.println("Fail to get \"Date\" field from message header");
        }
        return emailEvent;
    }

    private static String encode(String apiKey, String data) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(apiKey.getBytes(), "HmacSHA256");

            sha256HMAC.init(secretKey);

            return Hex.encodeHexString(sha256HMAC.doFinal(data.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isSecure(String apiKey, String timestamp, String token,
                                   String signature) {
        return encode(apiKey, timestamp.concat(token)).equals(signature);
    }

}

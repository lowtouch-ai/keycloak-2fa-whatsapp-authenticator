package com.lowtouch.keycloak.auth.whatsapp;

import lombok.extern.jbosslog.JBossLog;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@JBossLog
public class WhatsAppService {

    private final String apiToken;
    private final String phoneNumberId;
    private final String templateName;

    public WhatsAppService(String apiToken, String phoneNumberId, String templateName) {
        this.apiToken = apiToken;
        this.phoneNumberId = phoneNumberId;
        this.templateName = templateName;
    }

    /**
     * Sends a WhatsApp OTP message using Meta Cloud API template messaging.
     * The template must be approved in Meta Business Manager with one body parameter: the OTP code.
     *
     * @param toPhone phone number in E.164 format, e.g. +14155551234
     * @param code    the OTP code to send
     * @param ttl     validity in seconds (included in template if it supports two parameters)
     */
    public void sendOtp(String toPhone, String code, int ttl) throws IOException {
        String endpoint = WhatsAppConstants.GRAPH_API_URL + "/" + phoneNumberId + "/messages";

        // Normalize phone: remove leading '+' as Meta API expects digits only
        String normalizedPhone = toPhone.startsWith("+") ? toPhone.substring(1) : toPhone;

        String body = buildTemplatePayload(normalizedPhone, code, ttl);

        log.debugf("Sending WhatsApp OTP to %s via template '%s'", normalizedPhone, templateName);

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                log.errorf("WhatsApp API returned HTTP %d for phone %s", status, normalizedPhone);
                throw new IOException("WhatsApp API error: HTTP " + status);
            }

            log.infof("WhatsApp OTP sent successfully to %s", normalizedPhone);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Builds a Meta Cloud API template message payload.
     * Supports both single-parameter templates (code only) and
     * dual-parameter templates (code + ttl).
     */
    private String buildTemplatePayload(String phone, String code, int ttl) {
        return "{"
                + "\"messaging_product\":\"whatsapp\","
                + "\"to\":\"" + phone + "\","
                + "\"type\":\"template\","
                + "\"template\":{"
                + "  \"name\":\"" + templateName + "\","
                + "  \"language\":{\"code\":\"en_US\"},"
                + "  \"components\":[{"
                + "    \"type\":\"body\","
                + "    \"parameters\":["
                + "      {\"type\":\"text\",\"text\":\"" + code + "\"},"
                + "      {\"type\":\"text\",\"text\":\"" + ttl + "\"}"
                + "    ]"
                + "  }]"
                + "}"
                + "}";
    }
}

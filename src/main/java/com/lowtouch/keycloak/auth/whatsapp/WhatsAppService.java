package com.lowtouch.keycloak.auth.whatsapp;

import lombok.extern.jbosslog.JBossLog;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@JBossLog
public class WhatsAppService {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    public WhatsAppService(String accountSid, String authToken, String fromNumber) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
    }

    /**
     * Sends a WhatsApp OTP message via Twilio Messaging API.
     *
     * @param toPhone phone number in E.164 format, e.g. +14155551234
     * @param code    the OTP code to send
     * @param ttl     validity in seconds, included in the message body
     */
    public void sendOtp(String toPhone, String code, int ttl) throws IOException {
        String endpoint = WhatsAppConstants.TWILIO_API_URL + "/" + accountSid + "/Messages.json";

        String to = "whatsapp:" + toPhone;
        String from = fromNumber.startsWith("whatsapp:") ? fromNumber : "whatsapp:" + fromNumber;
        String messageBody = "Your lowtouch.ai verification code is *" + code + "*. "
                + "Valid for " + ttl + " seconds. Do not share this code with anyone.";

        String formData = "To=" + URLEncoder.encode(to, StandardCharsets.UTF_8)
                + "&From=" + URLEncoder.encode(from, StandardCharsets.UTF_8)
                + "&Body=" + URLEncoder.encode(messageBody, StandardCharsets.UTF_8);

        String credentials = Base64.getEncoder()
                .encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));

        log.debugf("Sending WhatsApp OTP via Twilio to %s", toPhone);

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Basic " + credentials);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(formData.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                log.errorf("Twilio API returned HTTP %d for phone %s", status, toPhone);
                throw new IOException("Twilio API error: HTTP " + status);
            }

            log.infof("WhatsApp OTP sent successfully via Twilio to %s", toPhone);
        } finally {
            conn.disconnect();
        }
    }
}

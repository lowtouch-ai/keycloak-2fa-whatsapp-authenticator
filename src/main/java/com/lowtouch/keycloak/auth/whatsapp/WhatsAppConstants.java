package com.lowtouch.keycloak.auth.whatsapp;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WhatsAppConstants {
    public String CODE = "whatsappCode";
    public String CODE_LENGTH = "length";
    public String CODE_TTL = "ttl";
    public String API_TOKEN = "apiToken";
    public String PHONE_NUMBER_ID = "phoneNumberId";
    public String TEMPLATE_NAME = "templateName";
    public String PHONE_ATTRIBUTE = "phoneAttribute";

    public int DEFAULT_LENGTH = 6;
    public int DEFAULT_TTL = 300;
    public String DEFAULT_TEMPLATE_NAME = "otp_authentication";
    public String DEFAULT_PHONE_ATTRIBUTE = "phoneNumber";
    public String GRAPH_API_URL = "https://graph.facebook.com/v17.0";
}

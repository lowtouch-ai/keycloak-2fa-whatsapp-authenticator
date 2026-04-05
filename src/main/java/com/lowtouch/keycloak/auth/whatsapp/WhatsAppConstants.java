package com.lowtouch.keycloak.auth.whatsapp;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WhatsAppConstants {
    public String CODE = "whatsappCode";
    public String CODE_LENGTH = "length";
    public String CODE_TTL = "ttl";
    public String ACCOUNT_SID = "accountSid";
    public String AUTH_TOKEN = "authToken";
    public String FROM_NUMBER = "fromNumber";
    public String PHONE_ATTRIBUTE = "phoneAttribute";

    public int DEFAULT_LENGTH = 6;
    public int DEFAULT_TTL = 300;
    public String DEFAULT_PHONE_ATTRIBUTE = "phoneNumber";
    public String TWILIO_API_URL = "https://api.twilio.com/2010-04-01/Accounts";
}

package com.lowtouch.keycloak.auth.whatsapp;

import static com.lowtouch.keycloak.auth.whatsapp.ConditionalWhatsAppAuthenticatorForm.DEFAULT_OTP_OUTCOME;
import static com.lowtouch.keycloak.auth.whatsapp.ConditionalWhatsAppAuthenticatorForm.FORCE;
import static com.lowtouch.keycloak.auth.whatsapp.ConditionalWhatsAppAuthenticatorForm.FORCE_OTP_FOR_HTTP_HEADER;
import static com.lowtouch.keycloak.auth.whatsapp.ConditionalWhatsAppAuthenticatorForm.FORCE_OTP_ROLE;
import static com.lowtouch.keycloak.auth.whatsapp.ConditionalWhatsAppAuthenticatorForm.OTP_CONTROL_USER_ATTRIBUTE;
import static com.lowtouch.keycloak.auth.whatsapp.ConditionalWhatsAppAuthenticatorForm.SKIP;
import static com.lowtouch.keycloak.auth.whatsapp.ConditionalWhatsAppAuthenticatorForm.SKIP_OTP_FOR_HTTP_HEADER;
import static com.lowtouch.keycloak.auth.whatsapp.ConditionalWhatsAppAuthenticatorForm.SKIP_OTP_ROLE;
import static java.util.Arrays.asList;
import static org.keycloak.provider.ProviderConfigProperty.LIST_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.ROLE_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

public class ConditionalWhatsAppAuthenticatorFormFactory extends WhatsAppAuthenticatorFormFactory {

    public static final String PROVIDER_ID = "whatsapp-conditional-authenticator";
    public static final ConditionalWhatsAppAuthenticatorForm SINGLETON = new ConditionalWhatsAppAuthenticatorForm();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Conditional WhatsApp OTP";
    }

    @Override
    public String getHelpText() {
        return "Conditionally sends a one-time password via WhatsApp based on user roles, attributes, or HTTP headers.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> list = new ArrayList<>(super.getConfigProperties());

        ProviderConfigProperty forceOtpUserAttribute = new ProviderConfigProperty();
        forceOtpUserAttribute.setType(STRING_TYPE);
        forceOtpUserAttribute.setName(OTP_CONTROL_USER_ATTRIBUTE);
        forceOtpUserAttribute.setLabel("OTP Control User Attribute");
        forceOtpUserAttribute.setHelpText("User attribute to explicitly control OTP. 'force' always requires OTP; 'skip' always bypasses it.");
        list.add(forceOtpUserAttribute);

        ProviderConfigProperty skipOtpRole = new ProviderConfigProperty();
        skipOtpRole.setType(ROLE_TYPE);
        skipOtpRole.setName(SKIP_OTP_ROLE);
        skipOtpRole.setLabel("Skip OTP for Role");
        skipOtpRole.setHelpText("OTP is always skipped if the user has this role.");
        list.add(skipOtpRole);

        ProviderConfigProperty forceOtpRole = new ProviderConfigProperty();
        forceOtpRole.setType(ROLE_TYPE);
        forceOtpRole.setName(FORCE_OTP_ROLE);
        forceOtpRole.setLabel("Force OTP for Role");
        forceOtpRole.setHelpText("OTP is always required if the user has this role.");
        list.add(forceOtpRole);

        ProviderConfigProperty skipOtpForHttpHeader = new ProviderConfigProperty();
        skipOtpForHttpHeader.setType(STRING_TYPE);
        skipOtpForHttpHeader.setName(SKIP_OTP_FOR_HTTP_HEADER);
        skipOtpForHttpHeader.setLabel("Skip OTP for Header");
        skipOtpForHttpHeader.setHelpText("OTP skipped if an HTTP header matches this regex pattern (e.g. X-Forwarded-Host: (1.2.3.4|1.2.3.5)).");
        skipOtpForHttpHeader.setDefaultValue("");
        list.add(skipOtpForHttpHeader);

        ProviderConfigProperty forceOtpForHttpHeader = new ProviderConfigProperty();
        forceOtpForHttpHeader.setType(STRING_TYPE);
        forceOtpForHttpHeader.setName(FORCE_OTP_FOR_HTTP_HEADER);
        forceOtpForHttpHeader.setLabel("Force OTP for Header");
        forceOtpForHttpHeader.setHelpText("OTP required if an HTTP header matches this regex pattern.");
        forceOtpForHttpHeader.setDefaultValue("");
        list.add(forceOtpForHttpHeader);

        ProviderConfigProperty defaultOutcome = new ProviderConfigProperty();
        defaultOutcome.setType(LIST_TYPE);
        defaultOutcome.setName(DEFAULT_OTP_OUTCOME);
        defaultOutcome.setLabel("Fallback OTP handling");
        defaultOutcome.setOptions(asList(SKIP, FORCE));
        defaultOutcome.setHelpText("What to do when all checks abstain. Defaults to force OTP.");
        list.add(defaultOutcome);

        return Collections.unmodifiableList(list);
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }
}

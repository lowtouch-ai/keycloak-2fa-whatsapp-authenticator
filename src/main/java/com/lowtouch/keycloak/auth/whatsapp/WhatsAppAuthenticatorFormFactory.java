package com.lowtouch.keycloak.auth.whatsapp;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

public class WhatsAppAuthenticatorFormFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "whatsapp-authenticator";
    public static final WhatsAppAuthenticatorForm SINGLETON = new WhatsAppAuthenticatorForm();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "WhatsApp OTP";
    }

    @Override
    public String getReferenceCategory() {
        return OTPCredentialModel.TYPE;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Sends a one-time password to the user via WhatsApp (Meta Cloud API).";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
                new ProviderConfigProperty(WhatsAppConstants.API_TOKEN, "WhatsApp API Token",
                        "Meta Cloud API access token (from Meta for Developers > WhatsApp > API Setup).",
                        ProviderConfigProperty.PASSWORD, ""),
                new ProviderConfigProperty(WhatsAppConstants.PHONE_NUMBER_ID, "Phone Number ID",
                        "The WhatsApp Phone Number ID from Meta Business Manager.",
                        ProviderConfigProperty.STRING_TYPE, ""),
                new ProviderConfigProperty(WhatsAppConstants.TEMPLATE_NAME, "Message Template Name",
                        "Approved WhatsApp message template name. Must have two body parameters: {{1}} = OTP code, {{2}} = TTL seconds.",
                        ProviderConfigProperty.STRING_TYPE, WhatsAppConstants.DEFAULT_TEMPLATE_NAME),
                new ProviderConfigProperty(WhatsAppConstants.PHONE_ATTRIBUTE, "User Phone Attribute",
                        "Keycloak user attribute that holds the phone number in E.164 format (e.g. +14155551234).",
                        ProviderConfigProperty.STRING_TYPE, WhatsAppConstants.DEFAULT_PHONE_ATTRIBUTE),
                new ProviderConfigProperty(WhatsAppConstants.CODE_LENGTH, "Code length",
                        "The number of digits of the generated OTP code.",
                        ProviderConfigProperty.STRING_TYPE, String.valueOf(WhatsAppConstants.DEFAULT_LENGTH)),
                new ProviderConfigProperty(WhatsAppConstants.CODE_TTL, "Time-to-live",
                        "The time to live in seconds for the OTP code to be valid.",
                        ProviderConfigProperty.STRING_TYPE, String.valueOf(WhatsAppConstants.DEFAULT_TTL)));
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }
}

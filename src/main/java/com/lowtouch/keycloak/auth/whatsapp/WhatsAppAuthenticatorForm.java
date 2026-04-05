package com.lowtouch.keycloak.auth.whatsapp;

import lombok.extern.jbosslog.JBossLog;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

@JBossLog
public class WhatsAppAuthenticatorForm extends AbstractUsernameFormAuthenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        challenge(context, null);
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        generateAndSendWhatsAppCode(context);

        LoginFormsProvider form = context.form().setExecution(context.getExecution().getId());
        if (error != null) {
            if (field != null) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }
        Response response = form.createForm("whatsapp-code-form.ftl");
        context.challenge(response);
        return response;
    }

    private void generateAndSendWhatsAppCode(AuthenticationFlowContext context) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        AuthenticationSessionModel session = context.getAuthenticationSession();

        if (session.getAuthNote(WhatsAppConstants.CODE) != null) {
            // code already sent, skip resend
            return;
        }

        int length = WhatsAppConstants.DEFAULT_LENGTH;
        int ttl = WhatsAppConstants.DEFAULT_TTL;
        String accountSid = null;
        String authToken = null;
        String fromNumber = null;
        String phoneAttribute = WhatsAppConstants.DEFAULT_PHONE_ATTRIBUTE;

        if (config != null) {
            Map<String, String> cfg = config.getConfig();
            length = Integer.parseInt(cfg.getOrDefault(WhatsAppConstants.CODE_LENGTH, String.valueOf(WhatsAppConstants.DEFAULT_LENGTH)));
            ttl = Integer.parseInt(cfg.getOrDefault(WhatsAppConstants.CODE_TTL, String.valueOf(WhatsAppConstants.DEFAULT_TTL)));
            accountSid = cfg.get(WhatsAppConstants.ACCOUNT_SID);
            authToken = cfg.get(WhatsAppConstants.AUTH_TOKEN);
            fromNumber = cfg.get(WhatsAppConstants.FROM_NUMBER);
            phoneAttribute = cfg.getOrDefault(WhatsAppConstants.PHONE_ATTRIBUTE, WhatsAppConstants.DEFAULT_PHONE_ATTRIBUTE);
        }

        String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
        sendWhatsAppCode(context.getUser(), code, ttl, accountSid, authToken, fromNumber, phoneAttribute);

        session.setAuthNote(WhatsAppConstants.CODE, code);
        session.setAuthNote(WhatsAppConstants.CODE_TTL, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        UserModel userModel = context.getUser();
        if (!enabledUser(context, userModel)) {
            return;
        }

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        if (formData.containsKey("resend")) {
            resetWhatsAppCode(context);
            challenge(context, null);
            return;
        }

        if (formData.containsKey("cancel")) {
            resetWhatsAppCode(context);
            context.resetFlow();
            return;
        }

        AuthenticationSessionModel session = context.getAuthenticationSession();
        String code = session.getAuthNote(WhatsAppConstants.CODE);
        String ttl = session.getAuthNote(WhatsAppConstants.CODE_TTL);
        String enteredCode = formData.getFirst(WhatsAppConstants.CODE);

        if (enteredCode.equals(code)) {
            if (Long.parseLong(ttl) < System.currentTimeMillis()) {
                context.getEvent().user(userModel).error(Errors.EXPIRED_CODE);
                Response challengeResponse = challenge(context, Messages.EXPIRED_ACTION_TOKEN_SESSION_EXISTS, WhatsAppConstants.CODE);
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, challengeResponse);
            } else {
                resetWhatsAppCode(context);
                context.success();
            }
        } else {
            AuthenticationExecutionModel execution = context.getExecution();
            if (execution.isRequired()) {
                context.getEvent().user(userModel).error(Errors.INVALID_USER_CREDENTIALS);
                Response challengeResponse = challenge(context, Messages.INVALID_ACCESS_CODE, WhatsAppConstants.CODE);
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
            } else if (execution.isConditional() || execution.isAlternative()) {
                context.attempted();
            }
        }
    }

    @Override
    protected String disabledByBruteForceError() {
        return Messages.INVALID_ACCESS_CODE;
    }

    private void resetWhatsAppCode(AuthenticationFlowContext context) {
        context.getAuthenticationSession().removeAuthNote(WhatsAppConstants.CODE);
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.getFirstAttribute(WhatsAppConstants.DEFAULT_PHONE_ATTRIBUTE) != null;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }

    private void sendWhatsAppCode(UserModel user, String code, int ttl,
                                  String accountSid, String authToken,
                                  String fromNumber, String phoneAttribute) {
        String phone = user.getFirstAttribute(phoneAttribute);
        if (phone == null || phone.isBlank()) {
            log.warnf("Cannot send WhatsApp OTP: user '%s' has no '%s' attribute", user.getUsername(), phoneAttribute);
            throw new AuthenticationFlowException(AuthenticationFlowError.INVALID_USER);
        }

        if (accountSid == null || authToken == null || fromNumber == null) {
            log.error("WhatsApp authenticator is not configured: accountSid, authToken, or fromNumber is missing");
            throw new AuthenticationFlowException(AuthenticationFlowError.INTERNAL_ERROR);
        }

        WhatsAppService service = new WhatsAppService(accountSid, authToken, fromNumber);
        try {
            service.sendOtp(phone, code, ttl);
        } catch (IOException e) {
            log.errorf(e, "Failed to send WhatsApp OTP via Twilio to user '%s'", user.getUsername());
            throw new AuthenticationFlowException(AuthenticationFlowError.INTERNAL_ERROR);
        }
    }
}

# Keycloak 2FA WhatsApp Authenticator

Keycloak Authentication Provider that sends a two-factor OTP (One-Time Password) via WhatsApp using **Twilio**.

Mirrors the structure of [keycloak-2fa-email-authenticator](https://github.com/lowtouch-ai/keycloak-2fa-email-authenticator). Tested with Keycloak 26.1.x.

## Prerequisites

- A **Twilio** account with WhatsApp messaging enabled
- Users must have a `phoneNumber` attribute in Keycloak with an **E.164** formatted number (e.g. `+14155551234`)

## Twilio Setup

### Sandbox (Testing)
1. Sign up at [twilio.com](https://twilio.com)
2. Go to **Messaging → Try it out → Send a WhatsApp message**
3. Have users text `join <sandbox-word>` to `+14155238886` to opt in
4. Use `+14155238886` as the From number

### Production
1. Go to **Messaging → Senders → WhatsApp Senders**
2. Request and verify a WhatsApp Business number
3. Use your verified number as the From number (no template required for session messages)

## Build

```bash
mvn clean package
```

Copy `target/keycloak-2fa-whatsapp-authenticator-*.jar` to `/opt/keycloak/providers/` then rebuild:

```bash
bin/kc.sh build
```

## Configuration (Keycloak Admin Console)

After adding the authenticator to your authentication flow, configure:

| Property | Description |
|----------|-------------|
| **Twilio Account SID** | From [console.twilio.com](https://console.twilio.com) dashboard |
| **Twilio Auth Token** | From [console.twilio.com](https://console.twilio.com) dashboard |
| **From WhatsApp Number** | Your Twilio WhatsApp number in E.164 (e.g. `+14155238886`) |
| **User Phone Attribute** | Keycloak user attribute for the phone number (default: `phoneNumber`) |
| **Code length** | Digits in the OTP (default: 6) |
| **Time-to-live** | OTP validity in seconds (default: 300) |

## Authentication Flow Setup

1. Go to **Authentication** → **Flows** → **Browser** → **Copy**
2. Add **WhatsApp OTP** (or **Conditional WhatsApp OTP**) after Username Password Form
3. Set execution to **Required**
4. Click the gear icon and fill in your Twilio credentials

## Conditional Authenticator

**Conditional WhatsApp OTP** supports bypass rules identical to the email variant:
- Skip/force by **user attribute** (`otpControlAttribute`)
- Skip/force by **user role** (`skipOtpRole` / `forceOtpRole`)
- Skip/force by **HTTP header pattern**
- Default fallback: `skip` or `force`

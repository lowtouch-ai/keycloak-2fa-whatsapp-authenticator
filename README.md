# Keycloak 2FA WhatsApp Authenticator

Keycloak Authentication Provider that sends a two-factor OTP (One-Time Password) via WhatsApp using the Meta Cloud API.

Mirrors the structure of [keycloak-2fa-email-authenticator](https://github.com/lowtouch-ai/keycloak-2fa-email-authenticator). Tested with Keycloak 26.1.x.

## Prerequisites

- A **Meta (Facebook) Developer** account with a WhatsApp Business App
- An approved **WhatsApp message template** with two body parameters:
  - `{{1}}` — the OTP code
  - `{{2}}` — TTL in seconds
- Users must have a `phoneNumber` attribute in Keycloak with an **E.164** formatted number (e.g. `+14155551234`)

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
| **WhatsApp API Token** | Meta Cloud API access token |
| **Phone Number ID** | WhatsApp Phone Number ID from Meta Business Manager |
| **Message Template Name** | Approved template name (default: `otp_authentication`) |
| **User Phone Attribute** | Keycloak user attribute for the phone number (default: `phoneNumber`) |
| **Code length** | Digits in the OTP (default: 6) |
| **Time-to-live** | OTP validity in seconds (default: 300) |

## Authentication Flow Setup

1. Go to **Authentication** → **Flows** → **Browser**
2. Add **WhatsApp OTP** (or **Conditional WhatsApp OTP**) after Username Password Form
3. Configure the execution with your Meta API credentials

## Conditional Authenticator

**Conditional WhatsApp OTP** supports all the same bypass rules as the email variant:
- Skip/force by **user attribute** (`otpControlAttribute`)
- Skip/force by **user role** (`skipOtpRole` / `forceOtpRole`)
- Skip/force by **HTTP header pattern** (useful for trusted IPs via `X-Forwarded-Host`)
- Default fallback: `skip` or `force`

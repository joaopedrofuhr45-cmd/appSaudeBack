# Deploy configuration

Set `CORS_ALLOWED_ORIGINS` to the exact frontend origin, for example `https://app.example.com` (no trailing slash). CORS allows credentials, so do not use `*`.

The authentication cookie uses `COOKIE_SAME_SITE=Lax` by default. For frontend and backend hosted on different sites, set both:

```text
COOKIE_SAME_SITE=None
COOKIE_SECURE=true
```

`SameSite=None` requires `Secure`, and `Secure` requires HTTPS. For the same site, `COOKIE_SAME_SITE=Lax` is sufficient. Also configure `SPRING_PROFILES_ACTIVE=prod`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET` (Base64, at least 32 bytes: `openssl rand -base64 48`), and `COOKIE_SECURE` for the hosting environment. The Dockerfile already defaults to the `prod` profile and the `America/Sao_Paulo` time zone.

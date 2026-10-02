# Deploy configuration

Set `CORS_ALLOWED_ORIGINS` to the exact frontend origin, for example `https://app.example.com` (no trailing slash). CORS allows credentials, so do not use `*`.

The authentication cookie uses `COOKIE_SAME_SITE=Lax` by default. For frontend and backend hosted on different sites, set both:

```text
COOKIE_SAME_SITE=None
COOKIE_SECURE=true
```

`SameSite=None` requires `Secure`, and `Secure` requires HTTPS. For the same site, `COOKIE_SAME_SITE=Lax` is sufficient. Also configure the existing `DB_URL`, `DB_DRIVER`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `COOKIE_SECURE` values for the hosting environment.

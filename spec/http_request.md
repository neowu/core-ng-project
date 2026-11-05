# HTTP request

- For proxied requests, a port present in both `X-Forwarded-Host` and `X-Forwarded-Port` must appear only once in `Request.requestURL()`.
- `Request.hostname()` never contains port, port in `X-Forwarded-Host` is stripped; port is determined by `X-Forwarded-Port` or `Host` header.

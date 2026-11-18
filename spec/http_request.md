# HTTP request

- For proxied requests, a port present in both `X-Forwarded-Host` and `X-Forwarded-Port` must appear only once in `Request.requestURL()`.
- `Request.hostname()` never contains port, port in `X-Forwarded-Host` is stripped; port is determined by `X-Forwarded-Port` or `Host` header.

## internal sys controllers (`/_sys/`)

- Only for human troubleshooting, via `kubectl port-forward` or on the same host.
- Allowed only when `Request.remoteIP()` is loopback and no `X-Forwarded-For` / `Forwarded` / `X-Real-IP` header is present.
    - site local ip is not trusted, LB / ingress / proxy-only subnet / SNAT node ip can be private ip.
    - forwarded headers mean request went through a proxy on the same host/pod (e.g. sidecar, local nginx).
- Known limitation: an L4 proxy on the same host/pod forwarding from loopback without headers can't be detected.

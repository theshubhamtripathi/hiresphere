# Deployment guide

HireSphere deploys as two pieces: the **backend** (Docker Compose on a VPS) and
the **frontend** (a static SPA on Vercel).

## 1. Backend — Ubuntu VPS

### Prerequisites
- Ubuntu 22.04+ with 4 GB RAM minimum (8 GB recommended for the full stack).
- A domain (e.g. `api.example.com`) pointed at the server's IP.

### Install Docker
```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER   # re-login afterwards
```

### Clone and configure
```bash
git clone https://github.com/theshubhamtripathi/hiresphere.git
cd hiresphere
cp .env.example .env
nano .env   # set strong JWT_SECRET, POSTGRES_PASSWORD, ADMIN_PASSWORD,
            # DOMAIN, FRONTEND_URL (your Vercel URL), and optional GEMINI/RAZORPAY keys
```

### Firewall — expose only 80/443 and SSH
```bash
sudo ufw allow OpenSSH
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable
```
The per-service ports in `docker-compose.yml` are for local development; in
production only Caddy (80/443) should be reachable.

### Start the stack
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```
Caddy automatically provisions a TLS certificate for `DOMAIN`. The API is then
available at `https://<DOMAIN>`.

### Operate
```bash
docker compose ps                      # health
docker compose logs -f api-gateway     # tail a service
docker compose pull && \
  docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build   # update
docker compose down                    # stop (add -v to wipe data)
```

## 2. Frontend — Vercel

1. Import the repository in Vercel and set the **root directory** to `frontend`.
   `frontend/vercel.json` configures the Vite build and SPA rewrites.
2. Add an environment variable:
   - `VITE_API_BASE_URL = https://<DOMAIN>` (your backend domain).
3. Deploy. Vercel builds with `npm run build` and serves `dist/`.

## 3. Connect the two

Set `FRONTEND_URL` in the backend `.env` to the Vercel domain (comma-separated
for multiple), then restart the gateway so CORS allows the SPA origin:
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d api-gateway
```

## Notes
- Without `GEMINI_API_KEY`, AI features use a deterministic mock — the app is
  fully functional. Set the key to enable real Gemini.
- Without Razorpay keys, payment endpoints return `503` and the UI hides
  checkout. Add test keys to enable the full flow.
- Emails go to the bundled Mailpit in dev. For production, point the `MAIL_*`
  variables at a real SMTP provider.

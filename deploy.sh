#!/bin/bash
# ============================================================
#  CMS Cloud Management System — EC2 Deployment Script
#  Option A: No Docker — Direct Deploy
#
#  Run this script on your AWS EC2 Ubuntu instance:
#    chmod +x deploy.sh
#    ./deploy.sh
# ============================================================

set -e  # Stop on any error

# ── Colours for output ──────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; NC='\033[0m' # No Colour

log()     { echo -e "${GREEN}[✓] $1${NC}"; }
warn()    { echo -e "${YELLOW}[!] $1${NC}"; }
info()    { echo -e "${BLUE}[→] $1${NC}"; }
error()   { echo -e "${RED}[✗] $1${NC}"; exit 1; }

echo ""
echo -e "${BLUE}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     CMS Enterprise — EC2 Deployment (No Docker)  ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════╝${NC}"
echo ""

# ── Step 1: System Update & Dependencies ────────────────────
info "Step 1/6 — Updating system packages..."

# Wait for unattended-upgrades to finish (common on fresh EC2 boot)
# This process holds the dpkg lock and causes "Could not get lock" errors
wait_for_apt_lock() {
    local MAX_WAIT=120  # Wait up to 2 minutes
    local WAITED=0
    while sudo fuser /var/lib/dpkg/lock-frontend >/dev/null 2>&1; do
        if [ $WAITED -ge $MAX_WAIT ]; then
            warn "Apt lock held too long — forcing release..."
            sudo killall unattended-upgrades apt apt-get 2>/dev/null || true
            sudo rm -f /var/lib/dpkg/lock-frontend /var/lib/dpkg/lock /var/cache/apt/archives/lock
            sudo dpkg --configure -a -q
            break
        fi
        info "Waiting for apt lock (held by unattended-upgrades)... ${WAITED}s"
        sleep 5
        WAITED=$((WAITED + 5))
    done
}

wait_for_apt_lock
sudo apt-get update -y -q
sudo apt-get install -y -q openjdk-17-jdk nginx curl

JAVA_VER=$(java -version 2>&1 | head -1)
log "Java installed: $JAVA_VER"
log "Nginx installed"

# ── Step 2: Build the Spring Boot JAR locally ───────────────
info "Step 2/6 — Building Spring Boot JAR..."
if [ ! -f "mvnw" ]; then
    error "mvnw not found. Run this script from the project root (d:/CBMS/CMS/CMS)"
fi

chmod +x mvnw
./mvnw clean package -DskipTests -q

JAR_FILE=$(find target -name "*.jar" ! -name "*sources*" | head -1)
if [ -z "$JAR_FILE" ]; then
    error "JAR file not found in target/. Build may have failed."
fi
log "JAR built: $JAR_FILE"

# ── Step 3: Deploy Backend JAR ──────────────────────────────
info "Step 3/6 — Deploying backend JAR to /opt/cms..."
sudo mkdir -p /opt/cms
sudo cp "$JAR_FILE" /opt/cms/app.jar
sudo chown ubuntu:ubuntu /opt/cms/app.jar
log "JAR deployed to /opt/cms/app.jar"

# Install and start the systemd service
if [ -f "cms-backend.service" ]; then
    sudo cp cms-backend.service /etc/systemd/system/
    sudo systemctl daemon-reload
    sudo systemctl enable cms-backend
    sudo systemctl restart cms-backend
    sleep 3
    if systemctl is-active --quiet cms-backend; then
        log "Backend service started and enabled on boot"
    else
        warn "Backend service may have failed. Check: sudo journalctl -u cms-backend -n 50"
    fi
else
    warn "cms-backend.service not found — starting JAR directly (not auto-start on boot)"
    nohup java -jar /opt/cms/app.jar > /opt/cms/backend.log 2>&1 &
    log "Backend started (PID: $!). Logs at /opt/cms/backend.log"
fi

# ── Step 4: Deploy Frontend Static Files ────────────────────
info "Step 4/6 — Deploying frontend files to /var/www/cms..."
sudo mkdir -p /var/www/cms
sudo cp frontend/index.html /var/www/cms/
sudo cp frontend/styles.css /var/www/cms/
sudo cp frontend/app.js     /var/www/cms/
sudo chown -R www-data:www-data /var/www/cms
log "Frontend files deployed to /var/www/cms"

# ── Step 5: Configure Nginx ─────────────────────────────────
info "Step 5/6 — Configuring Nginx..."
sudo cp frontend/nginx.conf /etc/nginx/sites-available/cms

# Remove default Nginx site if it exists
if [ -f /etc/nginx/sites-enabled/default ]; then
    sudo rm -f /etc/nginx/sites-enabled/default
    warn "Removed default Nginx site"
fi

# Enable CMS site
sudo ln -sf /etc/nginx/sites-available/cms /etc/nginx/sites-enabled/cms

# Test Nginx config
if sudo nginx -t 2>/dev/null; then
    sudo systemctl enable nginx
    sudo systemctl restart nginx
    log "Nginx configured and restarted"
else
    error "Nginx configuration test failed. Check /etc/nginx/sites-available/cms"
fi

# ── Step 6: Verify Deployment ───────────────────────────────
info "Step 6/6 — Verifying deployment..."
sleep 5

EC2_IP=$(curl -s --max-time 5 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "your-ec2-ip")

# Check backend health
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 http://localhost:8081/api/health 2>/dev/null || echo "000")
if [ "$BACKEND_STATUS" = "200" ]; then
    log "Backend health check: OK (HTTP $BACKEND_STATUS)"
else
    warn "Backend health check returned HTTP $BACKEND_STATUS — may still be starting up (takes ~30s)"
    warn "Check logs: sudo journalctl -u cms-backend -n 30"
fi

# Check frontend
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 http://localhost/ 2>/dev/null || echo "000")
if [ "$FRONTEND_STATUS" = "200" ]; then
    log "Frontend health check: OK (HTTP $FRONTEND_STATUS)"
else
    warn "Frontend check returned HTTP $FRONTEND_STATUS — check nginx: sudo nginx -t"
fi

echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║              DEPLOYMENT COMPLETE! 🚀                  ║${NC}"
echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  Frontend URL : http://${EC2_IP}/              ${NC}"
echo -e "${GREEN}║  Backend API  : http://${EC2_IP}:8081/api/     ${NC}"
echo -e "${GREEN}║  Health Check : http://${EC2_IP}/api/health    ${NC}"
echo -e "${GREEN}╠══════════════════════════════════════════════════════╣${NC}"
echo -e "${GREEN}║  Useful Commands:                                     ${NC}"
echo -e "${GREEN}║  sudo systemctl status cms-backend                    ${NC}"
echo -e "${GREEN}║  sudo journalctl -u cms-backend -f   (live logs)      ${NC}"
echo -e "${GREEN}║  sudo systemctl restart cms-backend                   ${NC}"
echo -e "${GREEN}║  sudo systemctl status nginx                          ${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════╝${NC}"
echo ""

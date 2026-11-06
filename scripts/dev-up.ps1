# Local-এ যা যা লাগে সব চালু করে: Prometheus, Loki, Grafana (Docker) আর Mailpit
# চালাতে: project folder থেকে  .\scripts\dev-up.ps1
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent

function Test-Port($port) {
    [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

# 1. Docker
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "docker পাওয়া যায়নি। Docker Desktop install আছে কিনা দেখ, তারপর নতুন terminal খোল।"
}
docker info *> $null
if ($LASTEXITCODE -ne 0) {
    throw "Docker engine চলছে না। Docker Desktop খোল, 'Engine running' দেখা পর্যন্ত অপেক্ষা কর, তারপর আবার চালা।"
}
docker compose -f "$root\infra\docker-compose.yml" up -d
if ($LASTEXITCODE -ne 0) { throw "docker compose ব্যর্থ হলো" }

# 2. Mailpit (আলাদা window-এ চলে)
if (Test-Port 1025) {
    Write-Host "Mailpit আগে থেকেই চলছে (port 1025)" -ForegroundColor Yellow
} elseif (-not (Get-Command mailpit -ErrorAction SilentlyContinue)) {
    Write-Host "mailpit পাওয়া যায়নি: winget install axllent.mailpit দিয়ে install কর" -ForegroundColor Red
} else {
    Start-Process mailpit -ArgumentList "--listen 127.0.0.1:8025 --smtp 127.0.0.1:1025" -WindowStyle Minimized
    Write-Host "Mailpit চালু হলো" -ForegroundColor Green
}

Write-Host ""
Write-Host "সব তৈরি:" -ForegroundColor Green
Write-Host "  Grafana    http://localhost:3000   (admin / admin)"
Write-Host "  Prometheus http://localhost:9090"
Write-Host "  Mailpit    http://127.0.0.1:8025"
Write-Host ""
Write-Host "এবার app চালা observability profile দিয়ে:" -ForegroundColor Cyan
Write-Host "  .\scripts\run-app.ps1"

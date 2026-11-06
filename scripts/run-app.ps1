# App চালায় observability profile দিয়ে (log Loki-তে যায়)
# চালাতে: project folder থেকে  .\scripts\run-app.ps1
$root = Split-Path $PSScriptRoot -Parent

if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    Write-Host "Port 8080-এ আগে থেকেই কিছু চলছে। আগের app বন্ধ কর, তারপর আবার চালা।" -ForegroundColor Red
    exit 1
}

Set-Location $root
# PowerShell-এ -D... argument quote-এর ভেতরে রাখতে হয়, নাহলে '.' এর জায়গায় ভেঙে যায়
& .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=observability"

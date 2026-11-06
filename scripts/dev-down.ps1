# dev-up.ps1 যা চালু করেছিল সব বন্ধ করে (data মোছে না)
$root = Split-Path $PSScriptRoot -Parent

docker compose -f "$root\infra\docker-compose.yml" stop

$mailpit = Get-Process mailpit -ErrorAction SilentlyContinue
if ($mailpit) {
    $mailpit | Stop-Process -Force
    Write-Host "Mailpit বন্ধ হলো"
}

Write-Host "সব বন্ধ। আবার চালাতে: .\scripts\dev-up.ps1" -ForegroundColor Green

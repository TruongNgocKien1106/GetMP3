$ErrorActionPreference = "Stop"

$ScriptDirectory =
Split-Path `
    -Parent `
    $MyInvocation.MyCommand.Path

$ProjectRoot =
Split-Path `
    -Parent `
    $ScriptDirectory

$Gradlew =
Join-Path `
    $ProjectRoot `
    "gradlew.bat"

$Apk =
Join-Path `
    $ProjectRoot `
    "app\build\outputs\apk\debug\app-debug.apk"

if (-not (Test-Path $Gradlew)) {
    throw "gradlew.bat was not found: $Gradlew"
}

Write-Host "Project root: $ProjectRoot" -ForegroundColor DarkGray
Write-Host "Building debug APK..." -ForegroundColor Cyan
Write-Host ""

Push-Location $ProjectRoot

try {
    & $Gradlew assembleDebug

    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed."
    }
}
finally {
    Pop-Location
}

if (-not (Test-Path $Apk)) {
    throw "The build completed, but the APK was not found: $Apk"
}

$ApkInfo =
Get-Item $Apk

$SizeMb =
[math]::Round(
    $ApkInfo.Length / 1MB,
    2
)

Write-Host ""
Write-Host "Build successful." -ForegroundColor Green
Write-Host "APK: $($ApkInfo.FullName)" -ForegroundColor Green
Write-Host "Size: $SizeMb MB" -ForegroundColor Green
Write-Host "Updated: $($ApkInfo.LastWriteTime)" -ForegroundColor Green
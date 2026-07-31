$ErrorActionPreference = "Stop"

$Adb =
"$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

$PackageName =
"com.ngoctien.getmp3"

$Activity =
"$PackageName/.MainActivity"

$ScriptDirectory =
Split-Path `
    -Parent `
    $MyInvocation.MyCommand.Path

$ProjectRoot =
Split-Path `
    -Parent `
    $ScriptDirectory

$TargetFile =
Join-Path `
    $ScriptDirectory `
    ".adb-target"

$Apk =
Join-Path `
    $ProjectRoot `
    "app\build\outputs\apk\debug\app-debug.apk"

if (-not (Test-Path $Adb)) {
    throw "ADB was not found: $Adb"
}

if (-not (Test-Path $TargetFile)) {
    throw "No saved Wi-Fi target was found. Run scripts\connect_by_wifi.ps1 first."
}

if (-not (Test-Path $Apk)) {
    throw "APK was not found. Run scripts\build.ps1 first."
}

$Target = (
    Get-Content `
        -Path $TargetFile `
        -Raw
).Trim()

if ($Target -notmatch "^[^:]+:\d+$") {
    throw "The saved ADB target is invalid: $Target"
}

Write-Host "Starting ADB server..." -ForegroundColor Cyan

& $Adb start-server |
Out-Null

Write-Host "Connecting to $Target..." -ForegroundColor Cyan

$ConnectOutput =
& $Adb connect $Target 2>&1

$ConnectOutput |
ForEach-Object {
    Write-Host $_
}

$DeviceState = (
    & $Adb -s $Target get-state 2>$null
).Trim()

if ($DeviceState -ne "device") {
    Write-Host ""
    Write-Host "Available ADB devices:" -ForegroundColor Yellow

    & $Adb devices -l

    throw "The phone is not connected as an ADB device."
}

$Model = (
    & $Adb `
        -s $Target `
        shell getprop ro.product.model
).Trim()

$ApkInfo =
Get-Item $Apk

$SizeMb =
[math]::Round(
    $ApkInfo.Length / 1MB,
    2
)

Write-Host ""
Write-Host "Connected device: $Model" -ForegroundColor Green
Write-Host "ADB target: $Target" -ForegroundColor Green
Write-Host "APK size: $SizeMb MB" -ForegroundColor DarkGray

Write-Host ""
Write-Host "Installing APK over Wi-Fi..." -ForegroundColor Yellow
Write-Host "Unlock the phone and approve any installation prompt." -ForegroundColor Yellow
Write-Host ""

$InstallOutput =
& $Adb `
    -s $Target `
    install `
    -r `
    -d `
    $Apk `
    2>&1

$InstallExitCode =
$LASTEXITCODE

$InstallText =
$InstallOutput -join "`n"

$InstallOutput |
ForEach-Object {
    Write-Host $_
}

if ($InstallExitCode -ne 0) {
    if (
        $InstallText -match
        "INSTALL_FAILED_ABORTED"
    ) {
        throw @"
The phone rejected the installation.

Unlock the phone and approve the installation prompt.
On Vivo devices, also check Developer options:
- Wireless debugging
- Install via USB
- USB debugging security settings
"@
    }

    if (
        $InstallText -match
        "INSTALL_FAILED_VERSION_DOWNGRADE"
    ) {
        throw "The installed app has a newer version code. Increase versionCode or uninstall the current app."
    }

    if (
        $InstallText -match
        "INSTALL_FAILED_UPDATE_INCOMPATIBLE"
    ) {
        throw "The installed app was signed with a different certificate."
    }

    throw "APK installation failed."
}

Write-Host ""
Write-Host "Stopping the current app process..." -ForegroundColor Yellow

& $Adb `
    -s $Target `
    shell am force-stop `
    $PackageName

if ($LASTEXITCODE -ne 0) {
    throw "The app process could not be stopped."
}

Write-Host "Launching GetMP3..." -ForegroundColor Yellow

$LaunchOutput =
& $Adb `
    -s $Target `
    shell am start `
    -n $Activity `
    2>&1

$LaunchExitCode =
$LASTEXITCODE

$LaunchOutput |
ForEach-Object {
    Write-Host $_
}

if ($LaunchExitCode -ne 0) {
    throw "The APK was installed, but the app could not be launched."
}

Write-Host ""
Write-Host "GetMP3 was installed and launched successfully." `
    -ForegroundColor Green
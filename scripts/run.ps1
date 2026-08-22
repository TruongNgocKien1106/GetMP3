param(
    [switch]$UsbOnly,
    [switch]$WifiOnly,
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest


# ============================================================
# GETMP3 - BUILD + AUTO DEVICE + SAFE DEPLOY + LAUNCH
#
# Default:
#
#   .\scripts\run.ps1
#
# Device priority:
#
#   1. USB ADB device
#   2. Existing connected Wi-Fi ADB device
#   3. Saved .adb-target
#   4. connect_by_wifi.ps1
#
# Optional:
#
#   .\scripts\run.ps1 -UsbOnly
#   .\scripts\run.ps1 -WifiOnly
#   .\scripts\run.ps1 -SkipBuild
#
# Safety:
#
# - Never adb uninstall
# - Never pm clear
# - Uses adb install -r -d
# - Existing app data/settings are preserved
# ============================================================


# ============================================================
# VALIDATE PARAMETERS
# ============================================================

if (
    $UsbOnly -and
    $WifiOnly
) {
    throw "Khong the dung dong thoi -UsbOnly va -WifiOnly."
}


# ============================================================
# CONSTANTS
# ============================================================

$PackageName =
    "com.ngoctien.getmp3"

$MainActivity =
    "$PackageName/.MainActivity"


# ============================================================
# PATHS
# ============================================================

$ProjectRoot =
    (
        Resolve-Path `
            -LiteralPath (
                Join-Path `
                    $PSScriptRoot `
                    ".."
            )
    ).Path


$Gradlew =
    Join-Path `
        $ProjectRoot `
        "gradlew.bat"


$ConnectScript =
    Join-Path `
        $PSScriptRoot `
        "connect_by_wifi.ps1"


$TargetFile =
    Join-Path `
        $PSScriptRoot `
        ".adb-target"


$ApkPath =
    Join-Path `
        $ProjectRoot `
        "app\build\outputs\apk\debug\app-debug.apk"


# ============================================================
# NATIVE COMMAND
# ============================================================

function Invoke-NativeCommand {

    param(
        [Parameter(Mandatory = $true)]
        [string]$Executable,

        [Parameter(Mandatory = $false)]
        [string[]]$Arguments = @()
    )


    $OldPreference =
        $ErrorActionPreference

    $ErrorActionPreference =
        "Continue"


    try {

        $Output = @(
            & $Executable @Arguments 2>&1
        )

        $ExitCode =
            $LASTEXITCODE

    }
    catch {

        $Output = @(
            $_.Exception.Message
        )

        $ExitCode =
            -1
    }
    finally {

        $ErrorActionPreference =
            $OldPreference
    }


    $Lines = @(
        $Output |
        ForEach-Object {
            $_.ToString()
        }
    )


    return [PSCustomObject]@{
        ExitCode = $ExitCode
        Lines    = $Lines
        Text     = ($Lines -join "`r`n").Trim()
    }
}


function Write-CommandOutput {

    param(
        [Parameter(Mandatory = $true)]
        $Result
    )


    foreach (
        $Line in
        $Result.Lines
    ) {
        Write-Host $Line
    }
}


# ============================================================
# BUILD
# ============================================================

function Build-LatestApk {

    if (
        -not (
            Test-Path `
                -LiteralPath $Gradlew `
                -PathType Leaf
        )
    ) {
        throw "gradlew.bat was not found: $Gradlew"
    }


    Write-Host ""
    Write-Host "========================================" `
        -ForegroundColor Cyan

    Write-Host "BUILDING LATEST APK" `
        -ForegroundColor Cyan

    Write-Host "========================================" `
        -ForegroundColor Cyan

    Write-Host ""

    Write-Host (
        "Project: {0}" -f
        $ProjectRoot
    ) -ForegroundColor DarkGray


    # --------------------------------------------------------
    # Delete only stale APK on PC.
    #
    # Does NOT touch installed Android app.
    # --------------------------------------------------------

    if (
        Test-Path `
            -LiteralPath $ApkPath `
            -PathType Leaf
    ) {

        $OldApk =
            Get-Item `
                -LiteralPath $ApkPath


        Write-Host (
            "Removing stale APK: {0}" -f
            $OldApk.LastWriteTime.ToString(
                "yyyy-MM-dd HH:mm:ss"
            )
        ) -ForegroundColor DarkYellow


        Remove-Item `
            -LiteralPath $ApkPath `
            -Force
    }


    $BuildStarted =
        Get-Date


    Write-Host ""
    Write-Host "Running Gradle assembleDebug..." `
        -ForegroundColor Yellow


    Push-Location `
        $ProjectRoot

    try {

        $Result =
            Invoke-NativeCommand `
                -Executable $Gradlew `
                -Arguments @(
                    "assembleDebug"
                )

    }
    finally {

        Pop-Location
    }


    Write-CommandOutput `
        -Result $Result


    if (
        $Result.ExitCode -ne 0
    ) {
        throw (
            "Gradle build failed.`r`n`r`n" +
            $Result.Text
        )
    }


    if (
        -not (
            Test-Path `
                -LiteralPath $ApkPath `
                -PathType Leaf
        )
    ) {
        throw (
            "Build completed but APK was not created: " +
            $ApkPath
        )
    }


    $Apk =
        Get-Item `
            -LiteralPath $ApkPath


    if (
        $Apk.Length -le 0
    ) {
        throw "Built APK is empty."
    }


    if (
        $Apk.LastWriteTime -lt
        $BuildStarted.AddSeconds(-5)
    ) {
        throw (
            "APK timestamp is stale. " +
            "Deployment stopped."
        )
    }


    $SizeMb =
        [math]::Round(
            $Apk.Length / 1MB,
            2
        )


    Write-Host ""
    Write-Host "BUILD VERIFIED" `
        -ForegroundColor Green

    Write-Host (
        "APK: {0}" -f
        $Apk.FullName
    ) -ForegroundColor Green

    Write-Host (
        "Size: {0} MB" -f
        $SizeMb
    ) -ForegroundColor Green

    Write-Host (
        "Updated: {0}" -f
        $Apk.LastWriteTime.ToString(
            "yyyy-MM-dd HH:mm:ss"
        )
    ) -ForegroundColor Green
}


function Test-ExistingApk {

    if (
        -not (
            Test-Path `
                -LiteralPath $ApkPath `
                -PathType Leaf
        )
    ) {
        throw (
            "-SkipBuild was used but APK does not exist: " +
            $ApkPath
        )
    }


    $Apk =
        Get-Item `
            -LiteralPath $ApkPath


    if (
        $Apk.Length -le 0
    ) {
        throw "Existing APK is empty."
    }


    Write-Host ""
    Write-Host "BUILD SKIPPED" `
        -ForegroundColor Yellow

    Write-Host (
        "Using APK: {0}" -f
        $Apk.FullName
    ) -ForegroundColor Yellow
}


# ============================================================
# FIND ADB
# ============================================================

function Find-Adb {

    $Candidates =
        @()


    # --------------------------------------------------------
    # PATH
    # --------------------------------------------------------

    $AdbCommand =
        Get-Command `
            "adb.exe" `
            -CommandType Application `
            -ErrorAction SilentlyContinue |
        Select-Object -First 1


    if (
        $null -ne
        $AdbCommand
    ) {
        $Candidates +=
            $AdbCommand.Source
    }


    # --------------------------------------------------------
    # Android Studio SDK
    # --------------------------------------------------------

    if (
        -not [string]::IsNullOrWhiteSpace(
            $env:LOCALAPPDATA
        )
    ) {

        $Candidates +=
            Join-Path `
                $env:LOCALAPPDATA `
                "Android\Sdk\platform-tools\adb.exe"
    }


    # --------------------------------------------------------
    # ANDROID_SDK_ROOT
    # --------------------------------------------------------

    if (
        -not [string]::IsNullOrWhiteSpace(
            $env:ANDROID_SDK_ROOT
        )
    ) {

        $Candidates +=
            Join-Path `
                $env:ANDROID_SDK_ROOT `
                "platform-tools\adb.exe"
    }


    # --------------------------------------------------------
    # ANDROID_HOME
    # --------------------------------------------------------

    if (
        -not [string]::IsNullOrWhiteSpace(
            $env:ANDROID_HOME
        )
    ) {

        $Candidates +=
            Join-Path `
                $env:ANDROID_HOME `
                "platform-tools\adb.exe"
    }


    # --------------------------------------------------------
    # Known local scrcpy location
    # --------------------------------------------------------

    $Candidates +=
        "C:\Tools\scrcpy-win64-v3.3.4\adb.exe"


    # --------------------------------------------------------
    # Resolve candidates
    # --------------------------------------------------------

    foreach (
        $Candidate in
        $Candidates
    ) {

        if (
            -not [string]::IsNullOrWhiteSpace(
                [string]$Candidate
            ) -and
            (
                Test-Path `
                    -LiteralPath $Candidate `
                    -PathType Leaf
            )
        ) {

            return (
                Resolve-Path `
                    -LiteralPath $Candidate
            ).Path
        }
    }


    # --------------------------------------------------------
    # Last fallback: C:\Tools recursive search
    # --------------------------------------------------------

    if (
        Test-Path `
            -LiteralPath "C:\Tools" `
            -PathType Container
    ) {

        $Found =
            Get-ChildItem `
                -LiteralPath "C:\Tools" `
                -Filter "adb.exe" `
                -File `
                -Recurse `
                -ErrorAction SilentlyContinue |
            Sort-Object `
                LastWriteTime `
                -Descending |
            Select-Object -First 1


        if (
            $null -ne
            $Found
        ) {

            return $Found.FullName
        }
    }


    throw (
        "adb.exe was not found. " +
        "Install Android Platform Tools or check C:\Tools."
    )
}


# ============================================================
# ADB DEVICE DISCOVERY
# ============================================================

function Test-IsNetworkSerial {

    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial
    )


    if (
        $Serial -match
        '^(?:\d{1,3}\.){3}\d{1,3}:\d+$'
    ) {
        return $true
    }


    if (
        $Serial -match
        '^\[.+\]:\d+$'
    ) {
        return $true
    }


    if (
        $Serial -match
        '^adb-.+\._adb-tls-connect\._tcp\.?$'
    ) {
        return $true
    }


    return $false
}


function Get-AdbDevices {

    param(
        [Parameter(Mandatory = $true)]
        [string]$Adb
    )


    $Result =
        Invoke-NativeCommand `
            -Executable $Adb `
            -Arguments @(
                "devices",
                "-l"
            )


    if (
        $Result.ExitCode -ne 0
    ) {
        throw (
            "adb devices failed.`r`n" +
            $Result.Text
        )
    }


    $Devices =
        @()


    foreach (
        $RawLine in
        $Result.Lines
    ) {

        $Line =
            $RawLine.Trim()


        if (
            [string]::IsNullOrWhiteSpace(
                $Line
            )
        ) {
            continue
        }


        if (
            $Line -like
            "List of devices attached*"
        ) {
            continue
        }


        if (
            $Line -like
            "*daemon*"
        ) {
            continue
        }


        $Parts =
            $Line -split '\s+', 3


        if (
            $Parts.Count -lt 2
        ) {
            continue
        }


        $Serial =
            [string]$Parts[0]

        $State =
            [string]$Parts[1]

        $Details =
            ""


        if (
            $Parts.Count -ge 3
        ) {
            $Details =
                [string]$Parts[2]
        }


        $Transport =
            "USB"


        if (
            Test-IsNetworkSerial `
                -Serial $Serial
        ) {

            $Transport =
                "Wi-Fi"

        }
        elseif (
            $Serial -like
            "emulator-*"
        ) {

            $Transport =
                "Emulator"

        }
        elseif (
            $Details -match
            '(^|\s)usb:'
        ) {

            $Transport =
                "USB"
        }


        $Devices +=
            [PSCustomObject]@{
                Serial    = $Serial
                State     = $State
                Details   = $Details
                Transport = $Transport
            }
    }


    return $Devices
}


function Show-AdbDevices {

    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Devices
    )


    Write-Host ""
    Write-Host "ADB devices:" `
        -ForegroundColor Cyan


    if (
        $Devices.Count -eq 0
    ) {

        Write-Host "  (none)" `
            -ForegroundColor DarkGray

        return
    }


    foreach (
        $Device in
        $Devices
    ) {

        $Color =
            "DarkGray"


        if (
            $Device.State -eq
            "device"
        ) {

            $Color =
                "Green"

        }
        elseif (
            $Device.State -eq
            "unauthorized"
        ) {

            $Color =
                "Yellow"

        }
        elseif (
            $Device.State -eq
            "offline"
        ) {

            $Color =
                "Red"
        }


        Write-Host (
            "  [{0}] {1}  state={2}" -f
            $Device.Transport,
            $Device.Serial,
            $Device.State
        ) -ForegroundColor $Color
    }
}


# ============================================================
# SAVED WI-FI TARGET
# ============================================================

function Get-SavedWifiTarget {

    if (
        -not (
            Test-Path `
                -LiteralPath $TargetFile `
                -PathType Leaf
        )
    ) {
        return $null
    }


    $Value =
        [System.IO.File]::ReadAllText(
            $TargetFile
        ).Trim()


    if (
        [string]::IsNullOrWhiteSpace(
            $Value
        )
    ) {
        return $null
    }


    if (
        $Value -notmatch
        '^(?:\d{1,3}\.){3}\d{1,3}:\d{1,5}$'
    ) {
        return $null
    }


    return [string]$Value
}


# ============================================================
# TEST ADB TARGET
# ============================================================

function Test-PhoneConnected {

    param(
        [Parameter(Mandatory = $true)]
        [string]$Adb,

        [Parameter(Mandatory = $true)]
        [string]$Target
    )


    if (
        [string]::IsNullOrWhiteSpace(
            $Target
        )
    ) {
        return $false
    }


    $Result =
        Invoke-NativeCommand `
            -Executable $Adb `
            -Arguments @(
                "-s",
                $Target,
                "get-state"
            )


    return (
        $Result.ExitCode -eq 0 -and
        $Result.Text.Trim() -eq "device"
    )
}


# ============================================================
# WI-FI CONNECTION TOOL
# ============================================================

function Invoke-WifiConnectionTool {

    if (
        -not (
            Test-Path `
                -LiteralPath $ConnectScript `
                -PathType Leaf
        )
    ) {
        throw (
            "Wi-Fi connection script was not found: " +
            $ConnectScript
        )
    }


    Write-Host ""
    Write-Host "No usable USB device was found." `
        -ForegroundColor Yellow

    Write-Host "Trying Wi-Fi ADB..." `
        -ForegroundColor Cyan


    $Result =
        Invoke-NativeCommand `
            -Executable "powershell.exe" `
            -Arguments @(
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                $ConnectScript
            )


    Write-CommandOutput `
        -Result $Result


    if (
        $Result.ExitCode -ne 0
    ) {
        throw (
            "Wi-Fi connection failed.`r`n`r`n" +
            $Result.Text
        )
    }
}


# ============================================================
# SELECT ONE TARGET
# ============================================================

function Select-SingleTarget {

    param(
        [Parameter(Mandatory = $true)]
        [object[]]$Devices,

        [Parameter(Mandatory = $true)]
        [string]$Transport
    )


    $Candidates =
        @(
            $Devices |
            Where-Object {
                $_.State -eq "device" -and
                $_.Transport -eq $Transport
            }
        )


    if (
        $Candidates.Count -eq 0
    ) {
        return $null
    }


    if (
        $Candidates.Count -gt 1
    ) {

        $Names =
            (
                $Candidates |
                ForEach-Object {
                    $_.Serial
                }
            ) -join ", "


        throw (
            "Found multiple $Transport ADB devices: " +
            $Names +
            ". Disconnect extra devices or use only one target."
        )
    }


    return $Candidates[0]
}


# ============================================================
# RESOLVE TARGET
#
# Priority:
#
# 1. USB
# 2. Existing saved Wi-Fi
# 3. Existing connected Wi-Fi
# 4. connect_by_wifi.ps1
# ============================================================

function Resolve-PhoneTarget {

    param(
        [Parameter(Mandatory = $true)]
        [string]$Adb
    )


    $Devices =
        @(
            Get-AdbDevices `
                -Adb $Adb
        )


    Show-AdbDevices `
        -Devices $Devices


    # ========================================================
    # USB FIRST
    # ========================================================

    if (
        -not $WifiOnly
    ) {

        $UsbDevice =
            Select-SingleTarget `
                -Devices $Devices `
                -Transport "USB"


        if (
            $null -ne
            $UsbDevice
        ) {

            Write-Host ""
            Write-Host "USB DEVICE FOUND" `
                -ForegroundColor Green

            Write-Host (
                "Serial: {0}" -f
                $UsbDevice.Serial
            ) -ForegroundColor Green


            return [PSCustomObject]@{
                Target = $UsbDevice.Serial
                Mode   = "USB"
            }
        }


        $UnauthorizedUsb =
            @(
                $Devices |
                Where-Object {
                    $_.Transport -eq "USB" -and
                    $_.State -eq "unauthorized"
                }
            )


        if (
            $UnauthorizedUsb.Count -gt 0
        ) {

            Write-Host ""
            Write-Host (
                "USB device is unauthorized. " +
                "Unlock the phone and accept the USB debugging RSA dialog."
            ) -ForegroundColor Yellow
        }


        $OfflineUsb =
            @(
                $Devices |
                Where-Object {
                    $_.Transport -eq "USB" -and
                    $_.State -eq "offline"
                }
            )


        if (
            $OfflineUsb.Count -gt 0
        ) {

            Write-Host ""
            Write-Host "USB ADB device is offline." `
                -ForegroundColor Yellow
        }


        if (
            $UsbOnly
        ) {

            throw (
                "No authorized USB ADB device was found.`r`n" +
                "Enable USB debugging, unlock the phone, " +
                "accept the RSA dialog, then run again."
            )
        }
    }


    # ========================================================
    # SAVED WI-FI TARGET
    # ========================================================

    if (
        -not $UsbOnly
    ) {

        $SavedTarget =
            Get-SavedWifiTarget


        if (
            $null -ne
            $SavedTarget
        ) {

            Write-Host ""
            Write-Host (
                "Checking saved Wi-Fi target: {0}" -f
                $SavedTarget
            ) -ForegroundColor DarkGray


            if (
                Test-PhoneConnected `
                    -Adb $Adb `
                    -Target $SavedTarget
            ) {

                Write-Host "WI-FI DEVICE FOUND" `
                    -ForegroundColor Green


                return [PSCustomObject]@{
                    Target = $SavedTarget
                    Mode   = "Wi-Fi"
                }
            }
        }


        # ====================================================
        # ALREADY CONNECTED WI-FI DEVICE
        # ====================================================

        $WifiDevice =
            Select-SingleTarget `
                -Devices $Devices `
                -Transport "Wi-Fi"


        if (
            $null -ne
            $WifiDevice
        ) {

            Write-Host ""
            Write-Host "CONNECTED WI-FI DEVICE FOUND" `
                -ForegroundColor Green

            Write-Host (
                "Target: {0}" -f
                $WifiDevice.Serial
            ) -ForegroundColor Green


            return [PSCustomObject]@{
                Target = $WifiDevice.Serial
                Mode   = "Wi-Fi"
            }
        }


        # ====================================================
        # TRY WI-FI CONNECTION SCRIPT
        # ====================================================

        Invoke-WifiConnectionTool


        # ----------------------------------------------------
        # Refresh device list after connection tool.
        # ----------------------------------------------------

        $Devices =
            @(
                Get-AdbDevices `
                    -Adb $Adb
            )


        Show-AdbDevices `
            -Devices $Devices


        # ----------------------------------------------------
        # USB may have become authorized while waiting.
        # In Auto mode, still prefer USB.
        # ----------------------------------------------------

        if (
            -not $WifiOnly
        ) {

            $UsbDevice =
                Select-SingleTarget `
                    -Devices $Devices `
                    -Transport "USB"


            if (
                $null -ne
                $UsbDevice
            ) {

                Write-Host ""
                Write-Host "USB DEVICE FOUND AFTER RETRY" `
                    -ForegroundColor Green


                return [PSCustomObject]@{
                    Target = $UsbDevice.Serial
                    Mode   = "USB"
                }
            }
        }


        $SavedTarget =
            Get-SavedWifiTarget


        if (
            $null -ne
            $SavedTarget
        ) {

            if (
                Test-PhoneConnected `
                    -Adb $Adb `
                    -Target $SavedTarget
            ) {

                return [PSCustomObject]@{
                    Target = $SavedTarget
                    Mode   = "Wi-Fi"
                }
            }
        }


        $WifiDevice =
            Select-SingleTarget `
                -Devices $Devices `
                -Transport "Wi-Fi"


        if (
            $null -ne
            $WifiDevice
        ) {

            return [PSCustomObject]@{
                Target = $WifiDevice.Serial
                Mode   = "Wi-Fi"
            }
        }
    }


    throw (
        "No usable Android ADB device was found.`r`n`r`n" +
        "USB: enable USB debugging and accept RSA authorization.`r`n" +
        "Wi-Fi: enable Wireless debugging and connect/pair the device."
    )
}


# ============================================================
# PACKAGE CHECK
# ============================================================

function Test-AppInstalled {

    param(
        [Parameter(Mandatory = $true)]
        [string]$Adb,

        [Parameter(Mandatory = $true)]
        [string]$Target
    )


    $Result =
        Invoke-NativeCommand `
            -Executable $Adb `
            -Arguments @(
                "-s",
                $Target,
                "shell",
                "pm",
                "path",
                $PackageName
            )


    return (
        $Result.ExitCode -eq 0 -and
        $Result.Text -match
        '(?m)^\s*package:'
    )
}


# ============================================================
# SAFE INSTALL
# ============================================================

function Install-App {

    param(
        [Parameter(Mandatory = $true)]
        [string]$Adb,

        [Parameter(Mandatory = $true)]
        [string]$Target
    )


    $WasInstalled =
        Test-AppInstalled `
            -Adb $Adb `
            -Target $Target


    Write-Host ""
    Write-Host "========================================" `
        -ForegroundColor Cyan


    if (
        $WasInstalled
    ) {

        Write-Host "UPDATE MODE - PRESERVE DATA" `
            -ForegroundColor Green

        Write-Host (
            "Existing app found. " +
            "Using adb install -r -d."
        ) -ForegroundColor Green

    }
    else {

        Write-Host "FIRST INSTALL MODE" `
            -ForegroundColor Yellow
    }


    Write-Host "========================================" `
        -ForegroundColor Cyan

    Write-Host ""
    Write-Host "Installing APK..." `
        -ForegroundColor Yellow


    # --------------------------------------------------------
    # -r = replace existing package, preserve app data
    # -d = allow version code downgrade
    #
    # No uninstall fallback.
    # No pm clear.
    # --------------------------------------------------------

    $Result =
        Invoke-NativeCommand `
            -Executable $Adb `
            -Arguments @(
                "-s",
                $Target,
                "install",
                "-r",
                "-d",
                $ApkPath
            )


    Write-CommandOutput `
        -Result $Result


    if (
        $Result.ExitCode -ne 0
    ) {

        Write-Host ""
        Write-Host "INSTALL FAILED" `
            -ForegroundColor Red

        Write-Host "Existing app was NOT uninstalled." `
            -ForegroundColor Yellow

        Write-Host "App data was NOT cleared." `
            -ForegroundColor Yellow


        throw (
            "ADB installation failed.`r`n`r`n" +
            $Result.Text
        )
    }


    if (
        $Result.Text -notmatch
        '(?im)^\s*Success\s*$'
    ) {

        throw (
            "ADB did not report installation success.`r`n`r`n" +
            $Result.Text
        )
    }


    if (
        -not (
            Test-AppInstalled `
                -Adb $Adb `
                -Target $Target
        )
    ) {

        throw (
            "Package was not found after installation."
        )
    }


    Write-Host ""
    Write-Host "INSTALL SUCCESS" `
        -ForegroundColor Green


    if (
        $WasInstalled
    ) {

        Write-Host (
            "Existing app data was preserved."
        ) -ForegroundColor Green
    }
}


# ============================================================
# STOP OLD PROCESS
# ============================================================

function Stop-App {

    param(
        [Parameter(Mandatory = $true)]
        [string]$Adb,

        [Parameter(Mandatory = $true)]
        [string]$Target
    )


    Write-Host ""
    Write-Host "Stopping old app process..." `
        -ForegroundColor Yellow


    $Result =
        Invoke-NativeCommand `
            -Executable $Adb `
            -Arguments @(
                "-s",
                $Target,
                "shell",
                "am",
                "force-stop",
                $PackageName
            )


    if (
        $Result.ExitCode -ne 0
    ) {

        throw (
            "force-stop failed.`r`n" +
            $Result.Text
        )
    }
}


# ============================================================
# LAUNCH APP
# ============================================================

function Start-App {

    param(
        [Parameter(Mandatory = $true)]
        [string]$Adb,

        [Parameter(Mandatory = $true)]
        [string]$Target
    )


    Write-Host "Launching GetMP3..." `
        -ForegroundColor Yellow


    $Result =
        Invoke-NativeCommand `
            -Executable $Adb `
            -Arguments @(
                "-s",
                $Target,
                "shell",
                "am",
                "start",
                "-n",
                $MainActivity
            )


    Write-CommandOutput `
        -Result $Result


    if (
        $Result.ExitCode -ne 0 -or
        $Result.Text -match
        '(?im)^\s*Error:'
    ) {

        throw (
            "App launch failed.`r`n" +
            $Result.Text
        )
    }
}


# ============================================================
# MAIN
# ============================================================

Write-Host ""
Write-Host "========================================" `
    -ForegroundColor Cyan

Write-Host "GETMP3 BUILD + AUTO DEPLOY" `
    -ForegroundColor Cyan

Write-Host "========================================" `
    -ForegroundColor Cyan


# ============================================================
# 1. BUILD
# ============================================================

if (
    $SkipBuild
) {

    Test-ExistingApk

}
else {

    Build-LatestApk
}


# ============================================================
# 2. FIND ADB
# ============================================================

$Adb =
    Find-Adb


Write-Host ""
Write-Host (
    "ADB: {0}" -f
    $Adb
) -ForegroundColor DarkGray


# ============================================================
# 3. START ADB SERVER
# ============================================================

$StartServer =
    Invoke-NativeCommand `
        -Executable $Adb `
        -Arguments @(
            "start-server"
        )


if (
    $StartServer.ExitCode -ne 0
) {

    throw (
        "ADB server failed to start.`r`n" +
        $StartServer.Text
    )
}


# ============================================================
# 4. AUTO RESOLVE TARGET
# ============================================================

$Resolved =
    Resolve-PhoneTarget `
        -Adb $Adb


$Target =
    [string]$Resolved.Target

$ConnectionMode =
    [string]$Resolved.Mode


if (
    [string]::IsNullOrWhiteSpace(
        $Target
    )
) {
    throw "Resolved ADB target is empty."
}


if (
    -not (
        Test-PhoneConnected `
            -Adb $Adb `
            -Target $Target
    )
) {

    throw (
        "Resolved device is no longer connected: " +
        $Target
    )
}


Write-Host ""
Write-Host "========================================" `
    -ForegroundColor Green

Write-Host "DEVICE READY" `
    -ForegroundColor Green

Write-Host "========================================" `
    -ForegroundColor Green

Write-Host (
    "Connection: {0}" -f
    $ConnectionMode
) -ForegroundColor Green

Write-Host (
    "Target: {0}" -f
    $Target
) -ForegroundColor Green


# ============================================================
# 5. INSTALL / UPDATE
# ============================================================

Install-App `
    -Adb $Adb `
    -Target $Target


# ============================================================
# 6. STOP OLD APP
# ============================================================

Stop-App `
    -Adb $Adb `
    -Target $Target


# ============================================================
# 7. LAUNCH
# ============================================================

Start-App `
    -Adb $Adb `
    -Target $Target


# ============================================================
# SUCCESS
# ============================================================

Write-Host ""
Write-Host "========================================" `
    -ForegroundColor Green

Write-Host "GETMP3 READY" `
    -ForegroundColor Green

Write-Host "========================================" `
    -ForegroundColor Green

Write-Host ""

Write-Host (
    "Connection used: {0}" -f
    $ConnectionMode
) -ForegroundColor Green

Write-Host (
    "Device target: {0}" -f
    $Target
) -ForegroundColor Green

Write-Host "Latest APK installed: YES" `
    -ForegroundColor Green

Write-Host "Data-preserving update: YES" `
    -ForegroundColor Green

Write-Host "adb uninstall used: NO" `
    -ForegroundColor Green

Write-Host "pm clear used: NO" `
    -ForegroundColor Green

Write-Host "App launched: YES" `
    -ForegroundColor Green
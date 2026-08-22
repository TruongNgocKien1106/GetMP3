param(
    [switch]$Register,
    [switch]$Reset,
    [string]$DeviceSerial
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName Microsoft.VisualBasic

[System.Windows.Forms.Application]::EnableVisualStyles()

$ScriptDirectory =
    Split-Path `
        -Parent `
        $MyInvocation.MyCommand.Path

$TargetFile =
    Join-Path `
        $ScriptDirectory `
        ".adb-target"

$DeviceProfileFile =
    Join-Path `
        $ScriptDirectory `
        ".adb-device.json"

$Utf8NoBom =
    New-Object `
        System.Text.UTF8Encoding(
            $false
        )

# ============================================================
# WINDOWS NETWORK HELPERS
# ============================================================

$NetworkHelperSource = @'
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Threading.Tasks;

public static class GetMp3NetworkTools
{
    public static string[] Ping24(
        string prefix,
        int timeoutMilliseconds,
        int maxConcurrency
    )
    {
        ConcurrentBag<string> reachable =
            new ConcurrentBag<string>();

        ParallelOptions options =
            new ParallelOptions
            {
                MaxDegreeOfParallelism =
                    Math.Max(
                        1,
                        maxConcurrency
                    )
            };

        Parallel.For(
            1,
            255,
            options,
            host =>
            {
                string address =
                    prefix + "." + host;

                try
                {
                    using (
                        Ping ping =
                            new Ping()
                    )
                    {
                        PingReply reply =
                            ping.Send(
                                address,
                                timeoutMilliseconds
                            );

                        if (
                            reply != null &&
                            reply.Status ==
                                IPStatus.Success
                        )
                        {
                            reachable.Add(
                                address
                            );
                        }
                    }
                }
                catch
                {
                    // Ignore unreachable hosts.
                }
            }
        );

        return reachable
            .Distinct(
                StringComparer.OrdinalIgnoreCase
            )
            .OrderBy(
                value => value,
                StringComparer.OrdinalIgnoreCase
            )
            .ToArray();
    }

    public static int[] ScanPorts(
        string ipAddress,
        int firstPort,
        int lastPort,
        int timeoutMilliseconds,
        int maxConcurrency
    )
    {
        ConcurrentBag<int> openPorts =
            new ConcurrentBag<int>();

        ParallelOptions options =
            new ParallelOptions
            {
                MaxDegreeOfParallelism =
                    Math.Max(
                        1,
                        maxConcurrency
                    )
            };

        Parallel.For(
            firstPort,
            lastPort + 1,
            options,
            port =>
            {
                try
                {
                    using (
                        TcpClient client =
                            new TcpClient()
                    )
                    {
                        IAsyncResult connection =
                            client.BeginConnect(
                                ipAddress,
                                port,
                                null,
                                null
                            );

                        bool completed =
                            connection
                                .AsyncWaitHandle
                                .WaitOne(
                                    timeoutMilliseconds
                                );

                        if (!completed)
                        {
                            return;
                        }

                        client.EndConnect(
                            connection
                        );

                        if (client.Connected)
                        {
                            openPorts.Add(
                                port
                            );
                        }
                    }
                }
                catch
                {
                    // Closed or non-ADB port.
                }
            }
        );

        return openPorts
            .Distinct()
            .OrderBy(
                port => port
            )
            .ToArray();
    }
}
'@

if (
    $null -eq
    (
        "GetMp3NetworkTools" -as
        [type]
    )
) {
    Add-Type `
        -TypeDefinition $NetworkHelperSource `
        -Language CSharp
}

# ============================================================
# UI HELPERS
# ============================================================

function Show-Message {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Text,

        [Parameter(Mandatory = $true)]
        [string]$Title,

        [System.Windows.Forms.MessageBoxIcon]$Icon =
            [System.Windows.Forms.MessageBoxIcon]::Information,

        [System.Windows.Forms.MessageBoxButtons]$Buttons =
            [System.Windows.Forms.MessageBoxButtons]::OK
    )

    return [System.Windows.Forms.MessageBox]::Show(
        $Text,
        $Title,
        $Buttons,
        $Icon
    )
}

# ============================================================
# ADB HELPERS
# ============================================================

function Resolve-AdbPath {
    $Candidates =
        @()

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

    foreach ($CandidatePath in $Candidates) {
        if (
            Test-Path `
                -LiteralPath $CandidatePath `
                -PathType Leaf
        ) {
            return (
                Resolve-Path `
                    -LiteralPath $CandidatePath
            ).Path
        }
    }

    $AdbCommand =
        Get-Command `
            -Name "adb.exe" `
            -CommandType Application `
            -ErrorAction SilentlyContinue |
        Select-Object -First 1

    if ($null -eq $AdbCommand) {
        $AdbCommand =
            Get-Command `
                -Name "adb" `
                -CommandType Application `
                -ErrorAction SilentlyContinue |
            Select-Object -First 1
    }

    if ($null -ne $AdbCommand) {
        return $AdbCommand.Source
    }

    throw (
        "ADB was not found. Install Android SDK " +
        "Platform-Tools or add adb.exe to PATH."
    )
}

$Adb =
    Resolve-AdbPath

function Invoke-Adb {
    param(
        [Parameter(Mandatory = $true)]
        [AllowEmptyCollection()]
        [string[]]$CommandArguments
    )

    $PreviousErrorPreference =
        $ErrorActionPreference

    $NativePreferenceVariable =
        Get-Variable `
            -Name "PSNativeCommandUseErrorActionPreference" `
            -ErrorAction SilentlyContinue

    $HasNativePreference =
        $null -ne
        $NativePreferenceVariable

    $PreviousNativePreference =
        $null

    if ($HasNativePreference) {
        $PreviousNativePreference =
            $PSNativeCommandUseErrorActionPreference
    }

    $RawOutput =
        @()

    $ExitCode =
        -1

    try {
        $ErrorActionPreference =
            "Continue"

        if ($HasNativePreference) {
            $PSNativeCommandUseErrorActionPreference =
                $false
        }

        $RawOutput =
            @(
                & $script:Adb @CommandArguments 2>&1
            )

        $ExitCode =
            $LASTEXITCODE
    }
    catch {
        $RawOutput =
            @(
                $_.Exception.Message
            )

        $ExitCode =
            -1
    }
    finally {
        $ErrorActionPreference =
            $PreviousErrorPreference

        if ($HasNativePreference) {
            $PSNativeCommandUseErrorActionPreference =
                $PreviousNativePreference
        }
    }

    $Lines =
        @(
            $RawOutput |
                ForEach-Object {
                    $_.ToString()
                }
        )

    return [PSCustomObject]@{
        ExitCode = $ExitCode
        Output   = $Lines
        Text     = (
            $Lines -join "`r`n"
        ).Trim()
    }
}

function Start-AdbServer {
    # Prefer the built-in open-screen mDNS backend.
    $env:ADB_MDNS_OPENSCREEN =
        "1"

    $Result =
        Invoke-Adb `
            -CommandArguments @(
                "start-server"
            )

    if ($Result.ExitCode -ne 0) {
        throw (
            "ADB server could not be started.`r`n`r`n" +
            $Result.Text
        )
    }
}

function Test-NetworkSerial {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial
    )

    return (
        $Serial -match
        "^(?:\d{1,3}\.){3}\d{1,3}:\d+$"
    )
}

function Test-UsableSerial {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial
    )

    return -not (
        $Serial -match "^emulator-" -or
        $Serial -match "^127\.0\.0\.1:" -or
        $Serial -match "^localhost:"
    )
}

function Test-AdbEndpoint {
    param(
        [AllowEmptyString()]
        [string]$Endpoint
    )

    if (
        $Endpoint -notmatch
        "^(?<ip>(?:\d{1,3}\.){3}\d{1,3}):(?<port>\d{1,5})$"
    ) {
        return $false
    }

    $Port =
        [int]$Matches["port"]

    return (
        $Port -ge 1 -and
        $Port -le 65535
    )
}

function Split-AdbEndpoint {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Endpoint
    )

    if (
        $Endpoint -notmatch
        "^(?<ip>(?:\d{1,3}\.){3}\d{1,3}):(?<port>\d{1,5})$"
    ) {
        return $null
    }

    return [PSCustomObject]@{
        IpAddress = $Matches["ip"]
        Port      = [int]$Matches["port"]
    }
}

function Get-ConnectedSerials {
    $Result =
        Invoke-Adb `
            -CommandArguments @(
                "devices",
                "-l"
            )

    if ($Result.ExitCode -ne 0) {
        return @()
    }

    $Serials =
        @()

    foreach ($Line in $Result.Output) {
        if (
            $Line -match
            "^(?<serial>\S+)\s+device(?:\s|$)"
        ) {
            $Serial =
                $Matches["serial"]

            if (
                Test-UsableSerial `
                    -Serial $Serial
            ) {
                $Serials +=
                    $Serial
            }
        }
    }

    return @(
        $Serials |
            Sort-Object -Unique
    )
}

function Wait-ForConnectedSerial {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial,

        [int]$TimeoutSeconds = 10
    )

    $Deadline =
        [DateTime]::UtcNow.AddSeconds(
            $TimeoutSeconds
        )

    while (
        [DateTime]::UtcNow -lt
        $Deadline
    ) {
        if (
            @(Get-ConnectedSerials) -contains
            $Serial
        ) {
            return $true
        }

        Start-Sleep `
            -Milliseconds 500
    }

    return $false
}

function Get-ShellValue {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial,

        [Parameter(Mandatory = $true)]
        [string[]]$ShellArguments
    )

    $Arguments =
        @(
            "-s",
            $Serial,
            "shell"
        ) +
        $ShellArguments

    $Result =
        Invoke-Adb `
            -CommandArguments $Arguments

    if ($Result.ExitCode -ne 0) {
        return ""
    }

    $Value =
        $Result.Text.Trim()

    if (
        $Value -eq "null" -or
        $Value -eq "unknown"
    ) {
        return ""
    }

    return $Value
}

# ============================================================
# DEVICE IDENTITY
# ============================================================

function Get-Sha256 {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Text
    )

    $Sha =
        [System.Security.Cryptography.SHA256]::Create()

    try {
        $Bytes =
            [System.Text.Encoding]::UTF8.GetBytes(
                $Text
            )

        $Hash =
            $Sha.ComputeHash(
                $Bytes
            )

        return (
            $Hash |
                ForEach-Object {
                    $_.ToString("x2")
                }
        ) -join ""
    }
    finally {
        $Sha.Dispose()
    }
}

function ConvertTo-NormalizedIdentityValue {
    param(
        [AllowEmptyString()]
        [string]$Value
    )

    return $Value.Trim().ToLowerInvariant()
}

function ConvertTo-NormalizedMacAddress {
    param(
        [AllowEmptyString()]
        [string]$MacAddress
    )

    if (
        [string]::IsNullOrWhiteSpace(
            $MacAddress
        )
    ) {
        return ""
    }

    return $MacAddress.Trim().ToLowerInvariant().Replace(
            "-",
            ":"
        )
}

function Get-WifiInformation {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial
    )

    $WifiIp =
        ""

    $WifiMac =
        ""

    $IpOutput =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "ip",
                "-f",
                "inet",
                "addr",
                "show",
                "wlan0"
            )

    if (
        $IpOutput -match
        "\binet\s+(?<ip>(?:\d{1,3}\.){3}\d{1,3})/"
    ) {
        $WifiIp =
            $Matches["ip"]
    }

    if (
        [string]::IsNullOrWhiteSpace(
            $WifiIp
        )
    ) {
        $RouteOutput =
            Get-ShellValue `
                -Serial $Serial `
                -ShellArguments @(
                    "ip",
                    "route",
                    "get",
                    "1.1.1.1"
                )

        if (
            $RouteOutput -match
            "\bsrc\s+(?<ip>(?:\d{1,3}\.){3}\d{1,3})"
        ) {
            $WifiIp =
                $Matches["ip"]
        }
    }

    $MacOutput =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "cat",
                "/sys/class/net/wlan0/address"
            )

    if (
        $MacOutput -match
        "^(?<mac>[0-9a-fA-F]{2}(?::[0-9a-fA-F]{2}){5})$"
    ) {
        $WifiMac =
            ConvertTo-NormalizedMacAddress `
                -MacAddress $Matches["mac"]
    }

    if (
        [string]::IsNullOrWhiteSpace(
            $WifiMac
        )
    ) {
        $LinkOutput =
            Get-ShellValue `
                -Serial $Serial `
                -ShellArguments @(
                    "ip",
                    "link",
                    "show",
                    "wlan0"
                )

        if (
            $LinkOutput -match
            "\blink/ether\s+(?<mac>[0-9a-fA-F]{2}(?::[0-9a-fA-F]{2}){5})"
        ) {
            $WifiMac =
                ConvertTo-NormalizedMacAddress `
                    -MacAddress $Matches["mac"]
        }
    }

    return [PSCustomObject]@{
        IpAddress  = $WifiIp
        MacAddress = $WifiMac
    }
}

function Get-WirelessDebugPort {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial
    )

    $DumpOutput =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "dumpsys",
                "adb"
            )

    $Patterns =
        @(
            "(?im)\bmConnectionPort\s*[:=]\s*(?<port>\d{4,5})",
            "(?im)\bconnection\s+port\s*[:=]\s*(?<port>\d{4,5})",
            "(?im)\badb.*wifi.*port\s*[:=]\s*(?<port>\d{4,5})"
        )

    foreach ($Pattern in $Patterns) {
        if (
            $DumpOutput -match
            $Pattern
        ) {
            $Port =
                [int]$Matches["port"]

            if (
                $Port -ge 1 -and
                $Port -le 65535
            ) {
                return $Port
            }
        }
    }

    return 0
}

function Get-DeviceIdentity {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial
    )

    if (
        @(Get-ConnectedSerials) -notcontains
        $Serial
    ) {
        throw (
            "ADB device is not connected: " +
            $Serial
        )
    }

    $AndroidId =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "settings",
                "get",
                "secure",
                "android_id"
            )

    $Manufacturer =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "getprop",
                "ro.product.manufacturer"
            )

    $Model =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "getprop",
                "ro.product.model"
            )

    $Device =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "getprop",
                "ro.product.device"
            )

    $AndroidVersion =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "getprop",
                "ro.build.version.release"
            )

    $SdkVersion =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "getprop",
                "ro.build.version.sdk"
            )

    $DeviceName =
        Get-ShellValue `
            -Serial $Serial `
            -ShellArguments @(
                "settings",
                "get",
                "global",
                "device_name"
            )

    if (
        [string]::IsNullOrWhiteSpace(
            $AndroidId
        ) -or
        [string]::IsNullOrWhiteSpace(
            $Model
        )
    ) {
        throw (
            "Could not read a stable identity from device: " +
            $Serial
        )
    }

    if (
        [string]::IsNullOrWhiteSpace(
            $DeviceName
        )
    ) {
        $DeviceName =
            (
                $Manufacturer +
                " " +
                $Model
            ).Trim()
    }

    $FingerprintSource =
        @(
            ConvertTo-NormalizedIdentityValue `
                -Value $AndroidId

            ConvertTo-NormalizedIdentityValue `
                -Value $Manufacturer

            ConvertTo-NormalizedIdentityValue `
                -Value $Model

            ConvertTo-NormalizedIdentityValue `
                -Value $Device
        ) -join "|"

    $WifiInformation =
        Get-WifiInformation `
            -Serial $Serial

    return [PSCustomObject]@{
        Serial         = $Serial
        IsNetwork      = (
            Test-NetworkSerial `
                -Serial $Serial
        )
        DeviceName     = $DeviceName
        Manufacturer   = $Manufacturer
        Model          = $Model
        Device         = $Device
        AndroidVersion = $AndroidVersion
        SdkVersion     = $SdkVersion
        WifiIp         = $WifiInformation.IpAddress
        WifiMac        = $WifiInformation.MacAddress
        Fingerprint    = (
            Get-Sha256 `
                -Text $FingerprintSource
        )
    }
}

# ============================================================
# PROFILE FILES
# ============================================================

function Remove-TargetFile {
    if (
        Test-Path `
            -LiteralPath $TargetFile `
            -PathType Leaf
    ) {
        Remove-Item `
            -LiteralPath $TargetFile `
            -Force
    }
}

function Set-TargetFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Target
    )

    [System.IO.File]::WriteAllText(
        $TargetFile,
        $Target,
        $Utf8NoBom
    )
}

function Save-DeviceProfile {
    param(
        [Parameter(Mandatory = $true)]
        [PSCustomObject]$DeviceProfile
    )

    $Json =
        $DeviceProfile |
        ConvertTo-Json `
            -Depth 5

    [System.IO.File]::WriteAllText(
        $DeviceProfileFile,
        $Json,
        $Utf8NoBom
    )
}

function Read-DeviceProfile {
    if (
        -not (
            Test-Path `
                -LiteralPath $DeviceProfileFile `
                -PathType Leaf
        )
    ) {
        return $null
    }

    try {
        return (
            [System.IO.File]::ReadAllText(
                $DeviceProfileFile
            ) |
            ConvertFrom-Json
        )
    }
    catch {
        throw (
            "The registered-device profile is invalid.`r`n" +
            "Run:`r`n" +
            ".\tools\phone\connect_by_wifi.ps1 -Reset"
        )
    }
}

function Test-IdentityMatches {
    param(
        [Parameter(Mandatory = $true)]
        [PSCustomObject]$Identity,

        [Parameter(Mandatory = $true)]
        [PSCustomObject]$DeviceProfile
    )

    return (
        -not [string]::IsNullOrWhiteSpace(
            [string]$Identity.Fingerprint
        ) -and
        [string]$Identity.Fingerprint -eq
        [string]$DeviceProfile.Fingerprint
    )
}

# ============================================================
# REGISTRATION
# ============================================================

function Get-ManualEndpoint {
    while ($true) {
        $Endpoint =
            [Microsoft.VisualBasic.Interaction]::InputBox(
                (
                    "No authorized device was detected automatically." +
                    "`r`n`r`n" +
                    "For one-time Wi-Fi registration, enter the value " +
                    "shown under Wireless debugging > IP address & port." +
                    "`r`n`r`n" +
                    "Example: 192.168.1.22:43713"
                ),
                "Register GetMP3 phone",
                ""
            ).Trim()

        if (
            [string]::IsNullOrWhiteSpace(
                $Endpoint
            )
        ) {
            return $null
        }

        if (
            Test-AdbEndpoint `
                -Endpoint $Endpoint
        ) {
            return $Endpoint
        }

        Show-Message `
            -Text (
                "Invalid endpoint.`r`n`r`n" +
                "Use IP:PORT, for example:`r`n" +
                "192.168.1.22:43713"
            ) `
            -Title "Invalid endpoint" `
            -Icon Warning |
        Out-Null
    }
}

function Connect-AdbEndpoint {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Endpoint,

        [int]$TimeoutSeconds = 10
    )

    $ConnectResult =
        Invoke-Adb `
            -CommandArguments @(
                "connect",
                $Endpoint
            )

    if (
        $ConnectResult.Text -match
        "(?i)failed|refused|unable|cannot|timed out|no route"
    ) {
        return $false
    }

    return (
        Wait-ForConnectedSerial `
            -Serial $Endpoint `
            -TimeoutSeconds $TimeoutSeconds
    )
}

function Register-ApprovedPhone {
    $ConnectedSerials =
        @(Get-ConnectedSerials)

    if (
        -not [string]::IsNullOrWhiteSpace(
            $DeviceSerial
        )
    ) {
        $ConnectedSerials =
            @(
                $ConnectedSerials |
                    Where-Object {
                        $_ -eq $DeviceSerial
                    }
            )
    }

    if ($ConnectedSerials.Count -eq 0) {
        $ManualEndpoint =
            Get-ManualEndpoint

        if ($null -eq $ManualEndpoint) {
            throw "Phone registration was cancelled."
        }

        Write-Host (
            "Connecting to " +
            $ManualEndpoint +
            "..."
        ) -ForegroundColor Cyan

        if (
            -not (
                Connect-AdbEndpoint `
                    -Endpoint $ManualEndpoint `
                    -TimeoutSeconds 12
            )
        ) {
            throw (
                "Could not connect to " +
                $ManualEndpoint +
                ".`r`n`r`n" +
                "Check that this computer is already paired with " +
                "the phone and Wireless debugging is enabled."
            )
        }

        $ConnectedSerials =
            @(
                $ManualEndpoint
            )
    }

    $Identities =
        @()

    foreach ($Serial in $ConnectedSerials) {
        try {
            $Identities +=
                Get-DeviceIdentity `
                    -Serial $Serial
        }
        catch {
            Write-Warning (
                "Could not read device ${Serial}: " +
                $_.Exception.Message
            )
        }
    }

    if ($Identities.Count -eq 0) {
        throw (
            "No authorized physical Android device was found."
        )
    }

    $FingerprintGroups =
        @(
            $Identities |
                Group-Object Fingerprint
        )

    if ($FingerprintGroups.Count -gt 1) {
        throw (
            "More than one different Android device is connected.`r`n" +
            "Disconnect the other devices or pass -DeviceSerial."
        )
    }

    $Identity =
        $FingerprintGroups[0].Group |
        Sort-Object @{
            Expression = {
                if ($_.IsNetwork) {
                    0
                }
                else {
                    1
                }
            }
        } |
        Select-Object -First 1

    $EndpointInformation =
        $null

    if ($Identity.IsNetwork) {
        $EndpointInformation =
            Split-AdbEndpoint `
                -Endpoint $Identity.Serial
    }

    $LastKnownIp =
        $Identity.WifiIp

    $LastKnownPort =
        0

    if ($null -ne $EndpointInformation) {
        $LastKnownIp =
            $EndpointInformation.IpAddress

        $LastKnownPort =
            $EndpointInformation.Port
    }
    else {
        $LastKnownPort =
            Get-WirelessDebugPort `
                -Serial $Identity.Serial
    }

    $Confirmation =
        Show-Message `
            -Text (
                "Register this phone for automatic Wireless ADB?`r`n`r`n" +
                "Name: $($Identity.DeviceName)`r`n" +
                "Model: $($Identity.Manufacturer) $($Identity.Model)`r`n" +
                "Android: $($Identity.AndroidVersion)`r`n" +
                "Wi-Fi IP: $LastKnownIp`r`n" +
                "Wi-Fi MAC: $($Identity.WifiMac)`r`n`r`n" +
                "Future connections will verify the saved fingerprint."
            ) `
            -Title "Register phone" `
            -Icon Question `
            -Buttons YesNo

    if (
        $Confirmation -ne
        [System.Windows.Forms.DialogResult]::Yes
    ) {
        throw "Phone registration was cancelled."
    }

    $DeviceProfile =
        [PSCustomObject][ordered]@{
            SchemaVersion = 2
            RegisteredAt  = (
                Get-Date
            ).ToString("o")
            DeviceName     = $Identity.DeviceName
            Manufacturer   = $Identity.Manufacturer
            Model          = $Identity.Model
            Device         = $Identity.Device
            Fingerprint    = $Identity.Fingerprint
            WifiMac        = $Identity.WifiMac
            LastKnownIp    = $LastKnownIp
            LastKnownPort  = $LastKnownPort
            LastConnected  = ""
        }

    Save-DeviceProfile `
        -DeviceProfile $DeviceProfile

    Remove-TargetFile

    if ($Identity.IsNetwork) {
        Set-TargetFile `
            -Target $Identity.Serial
    }

    Write-Host ""
    Write-Host "Phone registration completed." `
        -ForegroundColor Green

    Write-Host (
        "Device: " +
        $Identity.DeviceName
    ) -ForegroundColor Green

    Write-Host (
        "Wi-Fi IP: " +
        $LastKnownIp
    ) -ForegroundColor Green

    Write-Host (
        "Wi-Fi MAC: " +
        $Identity.WifiMac
    ) -ForegroundColor Green

    Show-Message `
        -Text (
            "Registration completed.`r`n`r`n" +
            "From now on, enable Wireless debugging and run:`r`n" +
            ".\tools\phone\connect_by_wifi.ps1"
        ) `
        -Title "Registration completed" `
        -Icon Information |
    Out-Null
}

# ============================================================
# MDNS DISCOVERY
# ============================================================

function Get-MdnsEndpoints {
    $Endpoints =
        @{}

    for (
        $Attempt = 1;
        $Attempt -le 6;
        $Attempt++
    ) {
        $Result =
            Invoke-Adb `
                -CommandArguments @(
                    "mdns",
                    "services"
                )

        foreach ($Line in $Result.Output) {
            if (
                $Line -match
                "_adb-tls-connect\._tcp\.?\s+(?<endpoint>(?:\d{1,3}\.){3}\d{1,3}:\d+)"
            ) {
                $Endpoint =
                    $Matches["endpoint"]

                if (
                    Test-AdbEndpoint `
                        -Endpoint $Endpoint
                ) {
                    $Endpoints[$Endpoint] =
                        $Endpoint
                }
            }
        }

        if ($Endpoints.Count -gt 0) {
            break
        }

        Start-Sleep `
            -Milliseconds 900
    }

    return @(
        $Endpoints.Values |
            Sort-Object -Unique
    )
}

# ============================================================
# MAC AND IP DISCOVERY
# ============================================================

function Get-ArpEntries {
    $RawOutput =
        @(
            arp.exe -a 2>$null
        )

    $Entries =
        @()

    foreach ($LineObject in $RawOutput) {
        $Line =
            $LineObject.ToString()

        if (
            $Line -match
            "^\s*(?<ip>(?:\d{1,3}\.){3}\d{1,3})\s+(?<mac>[0-9a-fA-F]{2}(?:-[0-9a-fA-F]{2}){5})\s+"
        ) {
            $Entries +=
                [PSCustomObject]@{
                    IpAddress =
                        $Matches["ip"]

                    MacAddress =
                        ConvertTo-NormalizedMacAddress `
                            -MacAddress $Matches["mac"]
                }
        }
    }

    return @($Entries)
}

function Find-IpByMac {
    param(
        [Parameter(Mandatory = $true)]
        [string]$MacAddress
    )

    $NormalizedMac =
        ConvertTo-NormalizedMacAddress `
            -MacAddress $MacAddress

    if (
        [string]::IsNullOrWhiteSpace(
            $NormalizedMac
        )
    ) {
        return ""
    }

    $Entry =
        Get-ArpEntries |
        Where-Object {
            $_.MacAddress -eq
            $NormalizedMac
        } |
        Select-Object -First 1

    if ($null -eq $Entry) {
        return ""
    }

    return $Entry.IpAddress
}

function Get-Private24Prefixes {
    param(
        [AllowEmptyString()]
        [string]$LastKnownIp
    )

    $Prefixes =
        @()

    $CandidateAddresses =
        @()

    if (
        -not [string]::IsNullOrWhiteSpace(
            $LastKnownIp
        )
    ) {
        $CandidateAddresses +=
            $LastKnownIp
    }

    try {
        $CandidateAddresses +=
            @(
                Get-NetIPAddress `
                    -AddressFamily IPv4 `
                    -ErrorAction Stop |
                Where-Object {
                    $_.IPAddress -notmatch "^127\." -and
                    $_.IPAddress -notmatch "^169\.254\."
                } |
                ForEach-Object {
                    $_.IPAddress
                }
            )
    }
    catch {
        # Get-NetIPAddress may be unavailable in old shells.
    }

    foreach ($Address in $CandidateAddresses) {
        if (
            $Address -match
            "^(?<prefix>(?:\d{1,3}\.){2}\d{1,3})\.\d{1,3}$"
        ) {
            $Prefixes +=
                $Matches["prefix"]
        }
    }

    return @(
        $Prefixes |
            Sort-Object -Unique
    )
}

function Update-ArpCache {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Prefixes
    )

    foreach ($Prefix in $Prefixes) {
        Write-Host (
            "Scanning local subnet " +
            $Prefix +
            ".0/24..."
        ) -ForegroundColor DarkCyan

        [GetMp3NetworkTools]::Ping24(
            $Prefix,
            120,
            64
        ) |
        Out-Null
    }
}

# ============================================================
# CONNECTION AND VERIFICATION
# ============================================================

function Complete-MatchingConnection {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Endpoint,

        [Parameter(Mandatory = $true)]
        [PSCustomObject]$Identity,

        [Parameter(Mandatory = $true)]
        [PSCustomObject]$DeviceProfile
    )

    $EndpointInformation =
        Split-AdbEndpoint `
            -Endpoint $Endpoint

    $WifiInformation =
        Get-WifiInformation `
            -Serial $Endpoint

    if ($null -ne $EndpointInformation) {
        $DeviceProfile.LastKnownIp =
            $EndpointInformation.IpAddress

        $DeviceProfile.LastKnownPort =
            $EndpointInformation.Port
    }

    if (
        -not [string]::IsNullOrWhiteSpace(
            $WifiInformation.MacAddress
        )
    ) {
        $DeviceProfile.WifiMac =
            $WifiInformation.MacAddress
    }

    $DeviceProfile.LastConnected =
        (Get-Date).ToString("o")

    Save-DeviceProfile `
        -DeviceProfile $DeviceProfile

    Set-TargetFile `
        -Target $Endpoint

    Write-Host ""
    Write-Host "Wireless ADB connection successful." `
        -ForegroundColor Green

    Write-Host (
        "Device: " +
        $Identity.DeviceName
    ) -ForegroundColor Green

    Write-Host (
        "Target: " +
        $Endpoint
    ) -ForegroundColor Green


}

function Connect-VerifiedEndpoint {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Endpoint,

        [Parameter(Mandatory = $true)]
        [PSCustomObject]$DeviceProfile
    )

    if (
        -not (
            Test-AdbEndpoint `
                -Endpoint $Endpoint
        )
    ) {
        return $false
    }

    Write-Host (
        "Checking " +
        $Endpoint +
        "..."
    ) -ForegroundColor DarkCyan

    if (
        -not (
            Connect-AdbEndpoint `
                -Endpoint $Endpoint `
                -TimeoutSeconds 8
        )
    ) {
        return $false
    }

    try {
        $Identity =
            Get-DeviceIdentity `
                -Serial $Endpoint
    }
    catch {
        Invoke-Adb `
            -CommandArguments @(
                "disconnect",
                $Endpoint
            ) |
        Out-Null

        return $false
    }

    if (
        -not (
            Test-IdentityMatches `
                -Identity $Identity `
                -DeviceProfile $DeviceProfile
        )
    ) {
        Write-Host (
            "Rejected another device at " +
            $Endpoint
        ) -ForegroundColor Yellow

        Invoke-Adb `
            -CommandArguments @(
                "disconnect",
                $Endpoint
            ) |
        Out-Null

        return $false
    }

    Complete-MatchingConnection `
        -Endpoint $Endpoint `
        -Identity $Identity `
        -DeviceProfile $DeviceProfile

    return $true
}

function Find-AlreadyConnectedPhone {
    param(
        [Parameter(Mandatory = $true)]
        [PSCustomObject]$DeviceProfile
    )

    foreach ($Serial in @(Get-ConnectedSerials)) {
        if (
            -not (
                Test-NetworkSerial `
                    -Serial $Serial
            )
        ) {
            continue
        }

        try {
            $Identity =
                Get-DeviceIdentity `
                    -Serial $Serial

            if (
                Test-IdentityMatches `
                    -Identity $Identity `
                    -DeviceProfile $DeviceProfile
            ) {
                return [PSCustomObject]@{
                    Endpoint = $Serial
                    Identity = $Identity
                }
            }
        }
        catch {
            # Ignore stale ADB entries.
        }
    }

    return $null
}

function Connect-WirelessPorts {
    param(
        [Parameter(Mandatory = $true)]
        [string]$IpAddress,

        [Parameter(Mandatory = $true)]
        [PSCustomObject]$DeviceProfile
    )

    $CandidatePorts =
        @()

    $LastKnownPort =
        [int]$DeviceProfile.LastKnownPort

    if (
        $LastKnownPort -ge 1 -and
        $LastKnownPort -le 65535
    ) {
        $CandidatePorts +=
            $LastKnownPort
    }

    Write-Host (
        "Searching Wireless ADB port on " +
        $IpAddress +
        "..."
    ) -ForegroundColor Cyan

    $CommonOpenPorts =
        @(
            [GetMp3NetworkTools]::ScanPorts(
                $IpAddress,
                30000,
                49999,
                90,
                256
            )
        )

    $CandidatePorts +=
        $CommonOpenPorts

    $CandidatePorts =
        @(
            $CandidatePorts |
                Sort-Object -Unique
        )

    foreach ($Port in $CandidatePorts) {
        $Endpoint =
            "${IpAddress}:${Port}"

        if (
            Connect-VerifiedEndpoint `
                -Endpoint $Endpoint `
                -DeviceProfile $DeviceProfile
        ) {
            return $true
        }
    }

    Write-Host (
        "No matching ADB service in common port range. " +
        "Running extended scan..."
    ) -ForegroundColor DarkCyan

    $LowerOpenPorts =
        @(
            [GetMp3NetworkTools]::ScanPorts(
                $IpAddress,
                1024,
                29999,
                75,
                256
            )
        )

    $UpperOpenPorts =
        @(
            [GetMp3NetworkTools]::ScanPorts(
                $IpAddress,
                50000,
                65535,
                75,
                256
            )
        )

    $ExtendedPorts =
        @(
            $LowerOpenPorts +
            $UpperOpenPorts |
                Sort-Object -Unique
        )

    foreach ($Port in $ExtendedPorts) {
        $Endpoint =
            "${IpAddress}:${Port}"

        if (
            Connect-VerifiedEndpoint `
                -Endpoint $Endpoint `
                -DeviceProfile $DeviceProfile
        ) {
            return $true
        }
    }

    return $false
}

# ============================================================
# MAIN
# ============================================================

try {
    Write-Host (
        "ADB: " +
        $Adb
    ) -ForegroundColor DarkGray

    Start-AdbServer

    if ($Reset) {
        Remove-TargetFile

        if (
            Test-Path `
                -LiteralPath $DeviceProfileFile `
                -PathType Leaf
        ) {
            Remove-Item `
                -LiteralPath $DeviceProfileFile `
                -Force
        }

        Invoke-Adb `
            -CommandArguments @(
                "disconnect"
            ) |
        Out-Null

        Write-Host ""
        Write-Host "Registration state removed." `
            -ForegroundColor Green

        Write-Host (
            "Deleted: " +
            $TargetFile
        ) -ForegroundColor DarkGray

        Write-Host (
            "Deleted: " +
            $DeviceProfileFile
        ) -ForegroundColor DarkGray

        exit 0
    }

    if ($Register) {
        Remove-TargetFile
        Register-ApprovedPhone
        exit 0
    }

    $DeviceProfile =
        Read-DeviceProfile

    if ($null -eq $DeviceProfile) {
        throw (
            "No registered phone profile exists.`r`n`r`n" +
            "Register once with:`r`n" +
            ".\tools\phone\connect_by_wifi.ps1 -Register"
        )
    }

    Remove-TargetFile

    Write-Host (
        "Looking for registered phone: " +
        $DeviceProfile.DeviceName +
        " (" +
        $DeviceProfile.Manufacturer +
        " " +
        $DeviceProfile.Model +
        ")"
    ) -ForegroundColor Cyan

    $ConnectedPhone =
        Find-AlreadyConnectedPhone `
            -DeviceProfile $DeviceProfile

    if ($null -ne $ConnectedPhone) {
        Complete-MatchingConnection `
            -Endpoint $ConnectedPhone.Endpoint `
            -Identity $ConnectedPhone.Identity `
            -DeviceProfile $DeviceProfile

        exit 0
    }

    $Endpoints =
        @()

    if (
        -not [string]::IsNullOrWhiteSpace(
            [string]$DeviceProfile.LastKnownIp
        ) -and
        [int]$DeviceProfile.LastKnownPort -gt 0
    ) {
        $Endpoints +=
            (
                [string]$DeviceProfile.LastKnownIp +
                ":" +
                [string]$DeviceProfile.LastKnownPort
            )
    }

    Write-Host "Trying ADB mDNS discovery..." `
        -ForegroundColor Cyan

    $Endpoints +=
        @(Get-MdnsEndpoints)

    $Endpoints =
        @(
            $Endpoints |
                Sort-Object -Unique
        )

    foreach ($Endpoint in $Endpoints) {
        if (
            Connect-VerifiedEndpoint `
                -Endpoint $Endpoint `
                -DeviceProfile $DeviceProfile
        ) {
            exit 0
        }
    }

    Write-Host (
        "mDNS did not provide a usable endpoint. " +
        "Using MAC and local-network discovery..."
    ) -ForegroundColor Cyan

    $CandidateIps =
        @()

    if (
        -not [string]::IsNullOrWhiteSpace(
            [string]$DeviceProfile.LastKnownIp
        )
    ) {
        $CandidateIps +=
            [string]$DeviceProfile.LastKnownIp
    }

    $MacIp =
        Find-IpByMac `
            -MacAddress (
                [string]$DeviceProfile.WifiMac
            )

    if (
        -not [string]::IsNullOrWhiteSpace(
            $MacIp
        )
    ) {
        $CandidateIps +=
            $MacIp
    }

    if (
        [string]::IsNullOrWhiteSpace(
            $MacIp
        ) -and
        -not [string]::IsNullOrWhiteSpace(
            [string]$DeviceProfile.WifiMac
        )
    ) {
        $Prefixes =
            @(
                Get-Private24Prefixes `
                    -LastKnownIp (
                        [string]$DeviceProfile.LastKnownIp
                    )
            )

        Update-ArpCache `
            -Prefixes $Prefixes

        $MacIp =
            Find-IpByMac `
                -MacAddress (
                    [string]$DeviceProfile.WifiMac
                )

        if (
            -not [string]::IsNullOrWhiteSpace(
                $MacIp
            )
        ) {
            $CandidateIps +=
                $MacIp
        }
    }

    $CandidateIps =
        @(
            $CandidateIps |
                Where-Object {
                    -not [string]::IsNullOrWhiteSpace(
                        $_
                    )
                } |
                Sort-Object -Unique
        )

    foreach ($CandidateIp in $CandidateIps) {
        if (
            Connect-WirelessPorts `
                -IpAddress $CandidateIp `
                -DeviceProfile $DeviceProfile
        ) {
            exit 0
        }
    }

    throw (
        "The registered phone could not be found automatically.`r`n`r`n" +
        "Check that:`r`n" +
        "- Wireless debugging is enabled.`r`n" +
        "- The phone and computer are on the same Wi-Fi network.`r`n" +
        "- This computer is still paired with the phone.`r`n" +
        "- The phone is awake while discovery is running.`r`n`r`n" +
        "The saved fingerprint was preserved and no other phone was accepted."
    )
}
catch {
    Remove-TargetFile

    $ErrorMessage =
        $_.Exception.Message

    Write-Host ""
    Write-Host $ErrorMessage `
        -ForegroundColor Red

    Show-Message `
        -Text $ErrorMessage `
        -Title "Wireless ADB connection failed" `
        -Icon Error |
    Out-Null

    exit 1
}
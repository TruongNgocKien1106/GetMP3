$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

[System.Windows.Forms.Application]::EnableVisualStyles()

$Adb =
"$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

$ScriptDirectory =
Split-Path `
    -Parent `
    $MyInvocation.MyCommand.Path

$TargetFile =
Join-Path `
    $ScriptDirectory `
    ".adb-target"

if (-not (Test-Path $Adb)) {
    [System.Windows.Forms.MessageBox]::Show(
        "ADB was not found:`n$Adb",
        "ADB not found",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Error
    ) | Out-Null

    exit 1
}

# ============================================================
# Load the previously saved IP and port
# ============================================================

$DefaultIp = ""
$DefaultPort = ""

if (Test-Path $TargetFile) {
    $SavedTarget = (
        Get-Content `
            -Path $TargetFile `
            -Raw `
            -ErrorAction SilentlyContinue
    ).Trim()

    if (
        $SavedTarget -match
        "^(?<ip>[^:]+):(?<port>\d+)$"
    ) {
        $DefaultIp =
        $Matches["ip"]

        $DefaultPort =
        $Matches["port"]
    }
}

# ============================================================
# Create the IP and port input window
# ============================================================

$Form =
New-Object `
    System.Windows.Forms.Form

$Form.Text =
"Connect Android over Wi-Fi"

$Form.StartPosition =
[System.Windows.Forms.FormStartPosition]::CenterScreen

$Form.FormBorderStyle =
[System.Windows.Forms.FormBorderStyle]::FixedDialog

$Form.MaximizeBox = $false
$Form.MinimizeBox = $false
$Form.ShowInTaskbar = $true
$Form.TopMost = $true

$Form.ClientSize =
New-Object `
    System.Drawing.Size(
    420,
    225
)

$TitleLabel =
New-Object `
    System.Windows.Forms.Label

$TitleLabel.Text =
"Wireless ADB connection"

$TitleLabel.Font =
New-Object `
    System.Drawing.Font(
    "Segoe UI",
    14,
    [System.Drawing.FontStyle]::Bold
)

$TitleLabel.AutoSize = $true
$TitleLabel.Location =
New-Object `
    System.Drawing.Point(
    24,
    18
)

$DescriptionLabel =
New-Object `
    System.Windows.Forms.Label

$DescriptionLabel.Text =
"Enter the IP address and port shown in Wireless debugging."

$DescriptionLabel.Font =
New-Object `
    System.Drawing.Font(
    "Segoe UI",
    9
)

$DescriptionLabel.AutoSize = $true
$DescriptionLabel.Location =
New-Object `
    System.Drawing.Point(
    26,
    52
)

$IpLabel =
New-Object `
    System.Windows.Forms.Label

$IpLabel.Text =
"Phone IP"

$IpLabel.Font =
New-Object `
    System.Drawing.Font(
    "Segoe UI",
    9
)

$IpLabel.AutoSize = $true
$IpLabel.Location =
New-Object `
    System.Drawing.Point(
    26,
    88
)

$IpTextBox =
New-Object `
    System.Windows.Forms.TextBox

$IpTextBox.Font =
New-Object `
    System.Drawing.Font(
    "Segoe UI",
    10
)

$IpTextBox.Location =
New-Object `
    System.Drawing.Point(
    110,
    84
)

$IpTextBox.Size =
New-Object `
    System.Drawing.Size(
    180,
    28
)

$IpTextBox.Text =
$DefaultIp

$PortLabel =
New-Object `
    System.Windows.Forms.Label

$PortLabel.Text =
"ADB port"

$PortLabel.Font =
New-Object `
    System.Drawing.Font(
    "Segoe UI",
    9
)

$PortLabel.AutoSize = $true
$PortLabel.Location =
New-Object `
    System.Drawing.Point(
    26,
    126
)

$PortTextBox =
New-Object `
    System.Windows.Forms.TextBox

$PortTextBox.Font =
New-Object `
    System.Drawing.Font(
    "Segoe UI",
    10
)

$PortTextBox.Location =
New-Object `
    System.Drawing.Point(
    110,
    122
)

$PortTextBox.Size =
New-Object `
    System.Drawing.Size(
    100,
    28
)

$PortTextBox.Text =
$DefaultPort

$ExampleLabel =
New-Object `
    System.Windows.Forms.Label

$ExampleLabel.Text =
"Example: 192.168.1.9 : 36285"

$ExampleLabel.ForeColor =
[System.Drawing.Color]::Gray

$ExampleLabel.Font =
New-Object `
    System.Drawing.Font(
    "Segoe UI",
    8
)

$ExampleLabel.AutoSize = $true
$ExampleLabel.Location =
New-Object `
    System.Drawing.Point(
    220,
    128
)

$ConnectButton =
New-Object `
    System.Windows.Forms.Button

$ConnectButton.Text =
"Connect"

$ConnectButton.Font =
New-Object `
    System.Drawing.Font(
    "Segoe UI",
    9,
    [System.Drawing.FontStyle]::Bold
)

$ConnectButton.Size =
New-Object `
    System.Drawing.Size(
    100,
    34
)

$ConnectButton.Location =
New-Object `
    System.Drawing.Point(
    206,
    172
)

$CancelButton =
New-Object `
    System.Windows.Forms.Button

$CancelButton.Text =
"Cancel"

$CancelButton.Font =
New-Object `
    System.Drawing.Font(
    "Segoe UI",
    9
)

$CancelButton.Size =
New-Object `
    System.Drawing.Size(
    88,
    34
)

$CancelButton.Location =
New-Object `
    System.Drawing.Point(
    312,
    172
)

$CancelButton.DialogResult =
[System.Windows.Forms.DialogResult]::Cancel

$Form.AcceptButton =
$ConnectButton

$Form.CancelButton =
$CancelButton

$Form.Controls.AddRange(
    @(
        $TitleLabel,
        $DescriptionLabel,
        $IpLabel,
        $IpTextBox,
        $PortLabel,
        $PortTextBox,
        $ExampleLabel,
        $ConnectButton,
        $CancelButton
    )
)

# ============================================================
# Validate the IP and port before closing the window
# ============================================================

$ConnectButton.Add_Click(
    {
        $Ip =
        $IpTextBox.Text.Trim()

        $PortText =
        $PortTextBox.Text.Trim()

        $ParsedIp =
        $null

        $ValidIp =
        [System.Net.IPAddress]::TryParse(
            $Ip,
            [ref]$ParsedIp
        )

        $Port = 0

        $ValidPort =
        [int]::TryParse(
            $PortText,
            [ref]$Port
        ) -and
        $Port -ge 1 -and
        $Port -le 65535

        if (-not $ValidIp) {
            [System.Windows.Forms.MessageBox]::Show(
                $Form,
                "Enter a valid phone IP address.",
                "Invalid IP address",
                [System.Windows.Forms.MessageBoxButtons]::OK,
                [System.Windows.Forms.MessageBoxIcon]::Warning
            ) | Out-Null

            $IpTextBox.Focus()
            $IpTextBox.SelectAll()

            return
        }

        if (-not $ValidPort) {
            [System.Windows.Forms.MessageBox]::Show(
                $Form,
                "Enter a port number from 1 to 65535.",
                "Invalid port",
                [System.Windows.Forms.MessageBoxButtons]::OK,
                [System.Windows.Forms.MessageBoxIcon]::Warning
            ) | Out-Null

            $PortTextBox.Focus()
            $PortTextBox.SelectAll()

            return
        }

        $Form.Tag =
        [PSCustomObject]@{
            Ip   = $Ip
            Port = $Port
        }

        $Form.DialogResult =
        [System.Windows.Forms.DialogResult]::OK

        $Form.Close()
    }
)

$Form.Add_Shown(
    {
        $IpTextBox.Focus()

        if ($IpTextBox.Text.Length -gt 0) {
            $IpTextBox.SelectionStart =
            $IpTextBox.Text.Length
        }
    }
)

$DialogResult =
$Form.ShowDialog()

if (
    $DialogResult -ne
    [System.Windows.Forms.DialogResult]::OK
) {
    Write-Host "Connection cancelled." -ForegroundColor Yellow
    exit 0
}

$Target =
"$($Form.Tag.Ip):$($Form.Tag.Port)"

# ============================================================
# Connect to the phone
# ============================================================

Write-Host "Starting ADB server..." -ForegroundColor Cyan

& $Adb start-server |
Out-Null

Write-Host "Connecting to $Target..." -ForegroundColor Cyan

$ConnectOutput =
@(
    & $Adb connect $Target 2>&1
)

$ConnectOutput |
ForEach-Object {
    Write-Host $_
}

$StateOutput =
@(
    & $Adb `
        -s $Target `
        get-state `
        2>$null
)

$DeviceState =
(
    $StateOutput -join ""
).Trim()

if ($DeviceState -ne "device") {
    $DeviceList =
    @(
        & $Adb devices -l
    ) -join "`r`n"

    [System.Windows.Forms.MessageBox]::Show(
        "The phone could not be connected.`n`nTarget: $Target`n`n$DeviceList",
        "Connection failed",
        [System.Windows.Forms.MessageBoxButtons]::OK,
        [System.Windows.Forms.MessageBoxIcon]::Error
    ) | Out-Null

    exit 1
}

$Model =
(
    @(
        & $Adb `
            -s $Target `
            shell getprop ro.product.model
    ) -join ""
).Trim()

$AndroidVersion =
(
    @(
        & $Adb `
            -s $Target `
            shell getprop ro.build.version.release
    ) -join ""
).Trim()

$SdkVersion =
(
    @(
        & $Adb `
            -s $Target `
            shell getprop ro.build.version.sdk
    ) -join ""
).Trim()

# Save the successful connection for run.ps1
[System.IO.File]::WriteAllText(
    $TargetFile,
    $Target,
    [System.Text.UTF8Encoding]::new($false)
)

Write-Host ""
Write-Host "Wi-Fi ADB connection successful." -ForegroundColor Green
Write-Host "Device: $Model" -ForegroundColor Green
Write-Host "Android: $AndroidVersion (API $SdkVersion)" -ForegroundColor Green
Write-Host "Target: $Target" -ForegroundColor Green

[System.Windows.Forms.MessageBox]::Show(
    "Connected successfully.`n`nDevice: $Model`nAndroid: $AndroidVersion (API $SdkVersion)`nTarget: $Target",
    "Wireless ADB connected",
    [System.Windows.Forms.MessageBoxButtons]::OK,
    [System.Windows.Forms.MessageBoxIcon]::Information
) | Out-Null
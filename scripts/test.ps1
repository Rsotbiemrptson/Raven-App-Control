[CmdletBinding()]
param([string]$AndroidSdk=$env:ANDROID_SDK_ROOT,[string]$JavaHome=$env:JAVA_HOME,[string]$Toolchain)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot
$args=@{}
if($AndroidSdk){$args.AndroidSdk=$AndroidSdk}
if($JavaHome){$args.JavaHome=$JavaHome}
if($Toolchain){$args.Toolchain=$Toolchain}
foreach($target in @('app','fixture','fixture2','tests')){& (Join-Path $root 'build.ps1') @args -Target $target}
Write-Output 'All four targets built. See docs/TESTING.md before running on a test device.'

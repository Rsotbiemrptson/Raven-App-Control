[CmdletBinding()]
param(
 [string]$AndroidSdk=$env:ANDROID_SDK_ROOT,
 [string]$BuildToolsVersion='35.0.0',
 [string]$JavaHome=$env:JAVA_HOME,
 [string]$Toolchain,
 [ValidateSet('app','fixture','fixture2','tests')][string]$Target='app',
 [string]$SigningKey,
 [string]$KeyAlias='raven-app-control',
 [string]$BuildDirectory,
 [string]$OutputDirectory
)
$ErrorActionPreference='Stop'
$project=$PSScriptRoot
function Run([string]$Program,[string[]]$Arguments){& $Program @Arguments;if($LASTEXITCODE -ne 0){throw "Build tool failed: $([IO.Path]::GetFileName($Program)) (exit $LASTEXITCODE)"}}
if($Toolchain){
 $tools=Join-Path $Toolchain 'Android-Build-Tools'
 $platform=Join-Path $Toolchain 'Android-SDK/platforms/android-35/android.jar'
 if(!$JavaHome){$JavaHome=Join-Path $Toolchain 'Java'}
}else{
 if(!$AndroidSdk){$AndroidSdk=$env:ANDROID_HOME}
 if(!$AndroidSdk){throw 'Set ANDROID_SDK_ROOT or pass -AndroidSdk. See docs/BUILD.md.'}
 $tools=Join-Path $AndroidSdk "build-tools/$BuildToolsVersion"
 $platform=Join-Path $AndroidSdk 'platforms/android-35/android.jar'
}
if(!$JavaHome){$compiler=Get-Command javac.exe -ErrorAction SilentlyContinue;if($compiler){$JavaHome=Split-Path (Split-Path $compiler.Source)}}
if(!$JavaHome){throw 'Set JAVA_HOME or pass -JavaHome pointing to a JDK.'}
$javaBin=Join-Path $JavaHome 'bin'
$java=Join-Path $javaBin 'java.exe'
foreach($file in @($java,(Join-Path $javaBin 'javac.exe'),(Join-Path $javaBin 'keytool.exe'),$platform,(Join-Path $tools 'aapt.exe'),(Join-Path $tools 'zipalign.exe'),(Join-Path $tools 'lib/d8.jar'),(Join-Path $tools 'lib/apksigner.jar'))){if(!(Test-Path -LiteralPath $file -PathType Leaf)){throw "Required build tool missing: $file"}}
if(!$BuildDirectory){$BuildDirectory=Join-Path $project '.build'}
if(!$OutputDirectory){$OutputDirectory=Join-Path $project 'dist'}
$build=Join-Path $BuildDirectory ($Target+'/'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path (Join-Path $build 'classes'),(Join-Path $build 'dex'),$OutputDirectory | Out-Null
$oldStore=$env:RAVEN_CONTROL_STORE_PASS;$oldKey=$env:RAVEN_CONTROL_KEY_PASS
try{
 if(!$SigningKey){
  $local=Join-Path $project '.local';New-Item -ItemType Directory -Force -Path $local | Out-Null
  $SigningKey=Join-Path $local 'development.jks';$KeyAlias='raven-control-development'
  $env:RAVEN_CONTROL_STORE_PASS='android';$env:RAVEN_CONTROL_KEY_PASS='android'
  if(!(Test-Path -LiteralPath $SigningKey)){Run (Join-Path $javaBin 'keytool.exe') @('-genkeypair','-keystore',$SigningKey,'-alias',$KeyAlias,'-storepass:env','RAVEN_CONTROL_STORE_PASS','-keypass:env','RAVEN_CONTROL_KEY_PASS','-keyalg','RSA','-keysize','3072','-validity','3650','-dname','CN=Raven App Control Development','-noprompt')}
  Write-Output 'Using a local development key. This APK cannot update the published release.'
 }else{
  if(!(Test-Path -LiteralPath $SigningKey)){throw 'Signing keystore not found.'}
  if(!$env:RAVEN_CONTROL_STORE_PASS){throw 'Set RAVEN_CONTROL_STORE_PASS for the custom keystore.'}
  if(!$env:RAVEN_CONTROL_KEY_PASS){$env:RAVEN_CONTROL_KEY_PASS=$env:RAVEN_CONTROL_STORE_PASS}
 }
 $manifest=if($Target -eq 'app'){Join-Path $project 'AndroidManifest.xml'}else{Join-Path $project "tests/$($Target -replace 'tests','instrumentation')/AndroidManifest.xml"}
 $sources=if($Target -like 'fixture*'){@(Join-Path $project 'tests/fixture/SeedActivity.java')}else{@(Get-ChildItem -LiteralPath (Join-Path $project 'src') -Recurse -Filter '*.java' | ForEach-Object FullName)}
 if($Target -eq 'tests'){$sources+=Join-Path $project 'tests/instrumentation/SmokeTest.java'}
 Run (Join-Path $javaBin 'javac.exe') (@('-encoding','UTF-8','-source','8','-target','8','-classpath',$platform,'-d',(Join-Path $build 'classes'))+$sources)
 $compiled=@(Get-ChildItem -LiteralPath (Join-Path $build 'classes') -Recurse -Filter '*.class' | Where-Object {$Target -ne 'tests' -or $_.Name -like 'SmokeTest*'} | ForEach-Object FullName)
 Run $java (@('-cp',(Join-Path $tools 'lib/d8.jar'),'com.android.tools.r8.D8','--min-api','30','--lib',$platform,'--output',(Join-Path $build 'dex'))+$compiled)
 $unsigned=Join-Path $build 'unsigned.apk';$resourceArgs=@('package','-f','-M',$manifest,'-I',$platform,'-F',$unsigned)
 if($Target -eq 'app'){$resourceArgs+=@('-S',(Join-Path $project 'res'))}
 Run (Join-Path $tools 'aapt.exe') $resourceArgs
 Push-Location (Join-Path $build 'dex');try{Run (Join-Path $tools 'aapt.exe') @('add',$unsigned,'classes.dex')}finally{Pop-Location}
 $aligned=Join-Path $build 'aligned.apk';Run (Join-Path $tools 'zipalign.exe') @('-f','4',$unsigned,$aligned)
 $name=if($Target -eq 'app'){'Raven-App-Control-1.2.0-local.apk'}else{"Raven-App-Control-$Target.apk"}
 $apk=Join-Path $OutputDirectory $name
 Run $java @('-jar',(Join-Path $tools 'lib/apksigner.jar'),'sign','--ks',$SigningKey,'--ks-key-alias',$KeyAlias,'--ks-pass','env:RAVEN_CONTROL_STORE_PASS','--key-pass','env:RAVEN_CONTROL_KEY_PASS','--out',$apk,$aligned)
 Run $java @('-jar',(Join-Path $tools 'lib/apksigner.jar'),'verify',$apk)
 Run (Join-Path $tools 'zipalign.exe') @('-c','4',$apk)
 [IO.File]::WriteAllText($apk+'.sha256',(Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash.ToLowerInvariant()+'  '+$name+"`n")
 Write-Output "Built: $apk"
}finally{$env:RAVEN_CONTROL_STORE_PASS=$oldStore;$env:RAVEN_CONTROL_KEY_PASS=$oldKey}

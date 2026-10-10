param(
    [string]$EnvFile = (Join-Path $env:USERPROFILE 'Downloads/atlas-credentials.env'),
    [string]$Database = 'kairos_ai',
    [switch]$PromptUri,
    [switch]$PromptToken,
    [switch]$ResolveSrv
)
$ErrorActionPreference = 'Stop'
$projectPath = Split-Path $PSScriptRoot -Parent
$resolvedEnvPath = (Resolve-Path -LiteralPath $EnvFile).Path.Replace('\', '/')
$previous=@{}
foreach ($name in @('APP_ENV_FILE','MONGODB_DATABASE','MONGODB_URI','API_ACCESS_TOKEN')) {
    $previous[$name]=[Environment]::GetEnvironmentVariable($name,'Process')
}
try {
    $env:APP_ENV_FILE = 'file:' + $resolvedEnvPath + '[.properties]'
    if ($Database) { $env:MONGODB_DATABASE = $Database }
    if ($PromptUri) {
        $secureUri=Read-Host 'URI MongoDB válida (entrada oculta)' -AsSecureString
        $env:MONGODB_URI=[Net.NetworkCredential]::new('', $secureUri).Password
        $secureUri.Dispose()
    }
    if ($PromptToken) {
        $secureToken=Read-Host 'Token da API (mínimo 32 caracteres; entrada oculta)' -AsSecureString
        $env:API_ACCESS_TOKEN=[Net.NetworkCredential]::new('', $secureToken).Password
        $secureToken.Dispose()
    }
    if ($ResolveSrv) {
        $connectionUri=$env:MONGODB_URI
        if (!$connectionUri) {
            $line=Get-Content -LiteralPath $resolvedEnvPath | Where-Object { $_ -match '^\s*MONGODB_URI\s*=' } | Select-Object -Last 1
            if (!$line) { throw 'MONGODB_URI ausente.' }
            $connectionUri=($line -split '=',2)[1].Trim().Trim('"',"'")
        }
        . (Join-Path $PSScriptRoot 'Resolve-MongoSrv.ps1')
        $env:MONGODB_URI=Resolve-MongoSrvUri -ConnectionUri $connectionUri
        $connectionUri=$null
    }
    Push-Location -LiteralPath $projectPath
    try {
        & ./mvnw.cmd spring-boot:run
        if ($LASTEXITCODE -ne 0) { throw 'A API não iniciou. Consulte o código de erro sanitizado no log.' }
    } finally { Pop-Location }
} finally {
    foreach ($name in $previous.Keys) { [Environment]::SetEnvironmentVariable($name,$previous[$name],'Process') }
}

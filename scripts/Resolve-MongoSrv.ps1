function Resolve-MongoSrvUri {
    param([Parameter(Mandatory=$true)][string]$ConnectionUri)
    if ($ConnectionUri.StartsWith('mongodb://')) { return $ConnectionUri }
    $parts = [regex]::Match($ConnectionUri, '^mongodb\+srv://(?<auth>[^@/]+@)?(?<host>[A-Za-z0-9.-]+)(?<path>/[^?]*)?(?:\?(?<query>.*))?$')
    if (!$parts.Success) { throw 'Formato de URI SRV inválido.' }
    $clusterHost=$parts.Groups['host'].Value
    $parentDomain=$clusterHost.Substring($clusterHost.IndexOf('.')+1)
    $options=@{}
    try {
        $txt=@(Resolve-DnsName -Name $clusterHost -Type TXT -ErrorAction Stop | Where-Object Type -eq TXT)
        if ($txt.Count -gt 1) { throw 'DNS TXT ambíguo.' }
        $query=if ($txt.Count) { $txt[0].Strings -join '' } else { '' }
        foreach ($entry in ($query -split '&')) {
            if (!$entry) { continue }
            $pair=$entry -split '=',2
            if ($pair.Length -ne 2 -or $pair[0] -notin @('authSource','replicaSet','loadBalanced')) { throw 'Opção TXT inválida.' }
            $options[$pair[0]]=$pair[1]
        }
        foreach ($entry in ($parts.Groups['query'].Value -split '&')) {
            if (!$entry) { continue }
            $pair=$entry -split '=',2
            if ($pair.Length -ne 2 -or $pair[0] -match '^srv') { throw 'Opção SRV não suportada pelo resolver local.' }
            $options[$pair[0]]=$pair[1]
        }
        if (($options.ContainsKey('tls') -and $options['tls'] -ne 'true') -or ($options.ContainsKey('ssl') -and $options['ssl'] -ne 'true')) { throw 'TLS obrigatório.' }
        $options['tls']='true'
        $records=@(Resolve-DnsName -Name ("_mongodb._tcp."+ $clusterHost) -Type SRV -ErrorAction Stop | Where-Object Type -eq SRV)
        if (!$records.Count) { throw 'SRV vazio.' }
        $targets=@($records | ForEach-Object {
            $target=$_.NameTarget.TrimEnd('.')
            if (!$target.EndsWith('.'+$parentDomain,[StringComparison]::OrdinalIgnoreCase)) { throw 'Host fora do domínio SRV.' }
            $target+':'+$_.Port
        })
        $parameters=($options.GetEnumerator() | Sort-Object Key | ForEach-Object { $_.Key+'='+$_.Value }) -join '&'
        $databasePath=$parts.Groups['path'].Value
        if (!$databasePath) { $databasePath='/' }
        return 'mongodb://'+$parts.Groups['auth'].Value+($targets -join ',')+$databasePath+'?'+$parameters
    } catch { throw 'Falha na resolução DNS segura do MongoDB. Nenhuma credencial foi registrada.' }
}

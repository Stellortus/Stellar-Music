$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$sourceRoot = Join-Path $projectRoot 'app/src/main/java/top/stellortus/stellar_music'
$featureFiles = @(Get-ChildItem (Join-Path $sourceRoot 'ui/screens') -Recurse -Filter '*.kt') +
    @(Get-ChildItem (Join-Path $sourceRoot 'ui/player') -Recurse -Filter '*.kt') +
    @(Get-Item (Join-Path $sourceRoot 'MainActivity.kt'))
$rules = @{
    'Material visual controls must stay in components or theme' = 'androidx\.compose\.material3|\bMaterialTheme\.'
    'Features must express visual intent through component roles' = 'StellarTheme\.(colors|shapes|typography)\b'
    'Raw sizes and colors belong to theme' = '\b\d+(?:\.\d+)?\.(?:dp|sp)\b|\bColor\('
    'Use shared visual components' = '\b(?:Text|Button|TextButton|OutlinedButton|FilledTonalButton|OutlinedTextField|Icon|IconButton|Checkbox|CircularProgressIndicator|HorizontalDivider|Surface|DropdownMenu|AlertDialog|ModalBottomSheet|NavigationBar|Scaffold)\('
}
$violations = @()
foreach ($file in $featureFiles) {
    $lines = [IO.File]::ReadAllLines($file.FullName)
    for ($i = 0; $i -lt $lines.Length; $i++) {
        foreach ($rule in $rules.GetEnumerator()) {
            if ($lines[$i] -match $rule.Value) {
                $violations += "$($file.FullName):$($i + 1): $($rule.Key)"
            }
        }
    }
}
if ($violations.Count -gt 0) {
    $violations | ForEach-Object { Write-Output $_ }
    throw "Design system boundary check failed: $($violations.Count) violation(s)."
}
Write-Output "Design system boundary check passed ($($featureFiles.Count) feature files)."

# CLEANUP PREVIOUS TEMP LOGS
$logDirectory = Join-Path $PSScriptRoot "logs"

if (Test-Path $logDirectory) {
    Remove-Item -Path $logDirectory -Recurse -Force
}

$mavenProjects = Get-ChildItem -Path $PSScriptRoot -Directory |
    Where-Object {
        Test-Path (Join-Path $_.FullName "pom.xml")
    }

if (-not $mavenProjects) {
    Write-Host "`n[ERROR] No Maven projects found in the script directory." -ForegroundColor Red
    Write-Host "Make sure the script is located in the root directory containing Maven projects." -ForegroundColor Yellow
    Read-Host "`nPress Enter to exit"
    exit 1
}

# MAVEN COMMANDS
$commands = @{
    "1" = @{
        Name = "mvn clean install"
        Args = @("clean", "install")
    }
    "2" = @{
        Name = "mvn clean install -Dmaven.test.skip=true"
        Args = @("clean", "install", "-Dmaven.test.skip=true")
    }
    "3" = @{
        Name = "mvn install"
        Args = @("install")
    }
    "4" = @{
        Name = "mvn install -Dmaven.test.skip=true"
        Args = @("install", "-Dmaven.test.skip=true")
    }
    "5" = @{
        Name = "mvn clean generate-sources"
        Args = @("clean", "generate-sources")
    }
    "6" = @{
        Name = "mvn clean"
        Args = @("clean")
    }
}

# MAVEN COMMAND MENU
Write-Host "`n========== MAVEN BUILD MENU ==========" -ForegroundColor Cyan

Write-Host "1. mvn clean install"
Write-Host "2. mvn clean install -Dmaven.test.skip=true"
Write-Host "3. mvn install"
Write-Host "4. mvn install -Dmaven.test.skip=true"
Write-Host "5. mvn clean generate-sources"
Write-Host "6. mvn clean"
Write-Host "`nPress Ctrl + C to exit." -ForegroundColor DarkGray

do {
    $choice = Read-Host "`nSelect command (1-6)"
} while (-not $commands.ContainsKey($choice))

$selectedCommand = $commands[$choice]
$testsEnabled = $choice -in @("1", "3")

Write-Host "`nSelected: $($selectedCommand.Name)" -ForegroundColor Yellow

# PROJECT SELECTION
Write-Host "`n========== PROJECT SELECTION ==========" -ForegroundColor Cyan

Write-Host "1. All services"

for ($i = 0; $i -lt $mavenProjects.Count; $i++) {
    Write-Host "$($i + 2). $($mavenProjects[$i].Name)"
}

do {
    $projectChoice = Read-Host "`nSelect project (1-$($mavenProjects.Count + 1))"

    $validProjectChoice = $projectChoice -match '^\d+$' -and
        [int]$projectChoice -ge 1 -and
        [int]$projectChoice -le ($mavenProjects.Count + 1)

} while (-not $validProjectChoice)

if ([int]$projectChoice -eq 1) {
    $selectedProjects = $mavenProjects
    $selectedProjectName = "All services"
}
else {
    $selectedProjects = @($mavenProjects[[int]$projectChoice - 2])
    $selectedProjectName = $selectedProjects[0].Name
}

Write-Host "`nSelected project: $selectedProjectName" -ForegroundColor Yellow

# CREATE TEMP LOG DIRECTORY
New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null

# BUILD
$results = @()
$totalStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

$selectedProjects | ForEach-Object {
    $serviceName = $_.Name
    $servicePath = $_.FullName
    $pom = Join-Path $servicePath "pom.xml"

    if (Test-Path $pom) {
        Write-Host "`n===== Building: $serviceName =====" -ForegroundColor Cyan

        $tempLogPath = Join-Path $logDirectory "maven-$serviceName.log"
        $logPath = Join-Path $servicePath "target\build.log"

        Push-Location $servicePath

        $serviceStopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        $status = "BUILD FAILURE"

        try {
            $mavenArgs = $selectedCommand.Args

            mvn @mavenArgs 2>&1 | Tee-Object -FilePath $tempLogPath
            $exitCode = $LASTEXITCODE

            if ($exitCode -eq 0) {
                $status = "BUILD SUCCESS"
            }
            elseif ($testsEnabled) {
                $testFailure = Select-String -Path $tempLogPath -Pattern @(
                    'Tests run:\s*\d+,\s*Failures:\s*[1-9]\d*'
                    'Tests run:\s*\d+,\s*Failures:\s*\d+,\s*Errors:\s*[1-9]\d*'
                    'There are test failures'
                    'Failed to execute goal .*maven-(surefire|failsafe)-plugin'
                ) -Quiet

                if ($testFailure) {
                    $status = "TEST FAILURE"
                }
            }
        }
        catch {
            Write-Host "ERROR: $_" -ForegroundColor Red
            $status = "BUILD FAILURE"
        }
        finally {
            $serviceStopwatch.Stop()
            Pop-Location
        }

        # MOVE LOG AFTER MAVEN FINISHES
        try {
            $targetDirectory = Join-Path $servicePath "target"
            New-Item -ItemType Directory -Path $targetDirectory -Force | Out-Null

            Move-Item -Path $tempLogPath -Destination $logPath -Force
        }
        catch {
            Write-Host "WARNING: Could not move log to target: $_" -ForegroundColor Yellow
            $logPath = $tempLogPath
        }

        $results += [PSCustomObject]@{
            Service  = $serviceName
            Status   = $status
            Duration = $serviceStopwatch.Elapsed
            Log      = $logPath
        }
    }
}

# CLEANUP TEMP LOG DIRECTORY
if (Test-Path $logDirectory) {
    Remove-Item -Path $logDirectory -Recurse -Force
}

$totalStopwatch.Stop()

# SUMMARY
Write-Host "`n========== BUILD SUMMARY ==========" -ForegroundColor Cyan
Write-Host "Command: $($selectedCommand.Name)`n" -ForegroundColor Yellow

$index = 1

foreach ($result in $results) {
    $color = switch ($result.Status) {
        "BUILD SUCCESS" { "Green" }
        "TEST FAILURE"  { "Yellow" }
        "BUILD FAILURE" { "Red" }
    }

    $elapsed = $result.Duration

    $duration = "{0:00}:{1:00}:{2:00}.{3:000}" -f `
        [int]$elapsed.TotalHours, `
        $elapsed.Minutes, `
        $elapsed.Seconds, `
        $elapsed.Milliseconds

    Write-Host ("{0}. {1,-30}" -f $index, $result.Service) -NoNewline
    Write-Host ("{0,-17}" -f $result.Status) -ForegroundColor $color -NoNewline
    Write-Host $duration

    if ($result.Status -ne "BUILD SUCCESS") {
        Write-Host "   Log: $($result.Log)" -ForegroundColor DarkGray
    }

    $index++
}

# STATISTICS
$successCount = @($results | Where-Object { $_.Status -eq "BUILD SUCCESS" }).Count
$testFailCount = @($results | Where-Object { $_.Status -eq "TEST FAILURE" }).Count
$buildFailCount = @($results | Where-Object { $_.Status -eq "BUILD FAILURE" }).Count

Write-Host "`nSUCCESS:       $successCount" -ForegroundColor Green

if ($testsEnabled) {
    Write-Host "TEST FAILURE:  $testFailCount" -ForegroundColor Yellow
}

Write-Host "BUILD FAILURE: $buildFailCount" -ForegroundColor Red

# TOTAL BUILD TIME
$totalElapsed = $totalStopwatch.Elapsed

$totalDuration = "{0:00}:{1:00}:{2:00}.{3:000}" -f `
    [int]$totalElapsed.TotalHours, `
    $totalElapsed.Minutes, `
    $totalElapsed.Seconds, `
    $totalElapsed.Milliseconds

Write-Host "`nTOTAL BUILD TIME: $totalDuration" -ForegroundColor Cyan

# LOG INSTRUCTIONS
$failedResults = @($results | Where-Object { $_.Status -ne "BUILD SUCCESS" })

if ($failedResults.Count -gt 0) {
    Write-Host 'TIP: To inspect a build log, run: notepad "<log_path>"' -ForegroundColor DarkGray
}

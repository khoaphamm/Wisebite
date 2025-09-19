# PowerShell script to run tests for WiseBite Backend

param(
    [string]$TestType = "all",
    [switch]$Coverage = $true,
    [switch]$Verbose = $true,
    [switch]$Install = $false
)

Write-Host "WiseBite Backend Test Runner" -ForegroundColor Green
Write-Host "=============================" -ForegroundColor Green

# Ensure we're in the correct directory
$BackendDir = Split-Path $MyInvocation.MyCommand.Path -Parent
Set-Location $BackendDir

# Install test dependencies if requested
if ($Install) {
    Write-Host "Installing test dependencies..." -ForegroundColor Yellow
    pip install -r requirements-test.txt
    Write-Host "Dependencies installed!" -ForegroundColor Green
}

# Check if pytest is available
$PytestAvailable = $null
try {
    $PytestAvailable = Get-Command pytest -ErrorAction Stop
} catch {
    Write-Host "pytest not found. Installing test dependencies..." -ForegroundColor Yellow
    pip install -r requirements-test.txt
}

# Build test command
$TestCommand = "pytest"

# Add coverage if requested
if ($Coverage) {
    $TestCommand += " --cov=app --cov-report=html --cov-report=term-missing"
}

# Add verbose if requested
if ($Verbose) {
    $TestCommand += " -v"
}

# Add test type filter
switch ($TestType) {
    "unit" { 
        $TestCommand += " -m unit"
        Write-Host "Running unit tests only..." -ForegroundColor Cyan
    }
    "integration" { 
        $TestCommand += " -m integration"
        Write-Host "Running integration tests only..." -ForegroundColor Cyan
    }
    "auth" { 
        $TestCommand += " tests/api/endpoints/test_auth.py"
        Write-Host "Running authentication tests..." -ForegroundColor Cyan
    }
    "user" { 
        $TestCommand += " tests/api/endpoints/test_user.py"
        Write-Host "Running user tests..." -ForegroundColor Cyan
    }
    "store" { 
        $TestCommand += " tests/api/endpoints/test_store.py"
        Write-Host "Running store tests..." -ForegroundColor Cyan
    }
    "order" { 
        $TestCommand += " tests/api/endpoints/test_order.py"
        Write-Host "Running order tests..." -ForegroundColor Cyan
    }
    "all" { 
        Write-Host "Running all tests..." -ForegroundColor Cyan
    }
    default { 
        Write-Host "Invalid test type. Available options:" -ForegroundColor Red
        Write-Host "  all, unit, integration, auth, user, store, order" -ForegroundColor Red
        exit 1
    }
}

Write-Host "Executing: $TestCommand" -ForegroundColor Yellow

# Set environment to test
$env:ENVIRONMENT = "test"

# Run the tests
try {
    Invoke-Expression $TestCommand
    $ExitCode = $LASTEXITCODE
} catch {
    Write-Host "Error running tests: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Display results
if ($ExitCode -eq 0) {
    Write-Host "" -ForegroundColor Green
    Write-Host "✅ All tests passed!" -ForegroundColor Green
    if ($Coverage) {
        Write-Host "📊 Coverage report generated in htmlcov/" -ForegroundColor Cyan
    }
} else {
    Write-Host "" -ForegroundColor Red
    Write-Host "❌ Some tests failed!" -ForegroundColor Red
    Write-Host "Please check the output above for details." -ForegroundColor Red
}

Write-Host "" -ForegroundColor White
Write-Host "Test run completed." -ForegroundColor White

exit $ExitCode

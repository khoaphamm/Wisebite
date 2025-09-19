# Simple PowerShell test runner for WiseBite Backend
param(
    [string]$TestFile = "tests/test_setup.py"
)

Write-Host "WiseBite Backend Simple Test Runner" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green

# Set Python path
$PythonPath = "E:/DIN/note-taking-2025/side-project/backend_experiment/venv/Scripts/python.exe"

Write-Host "Running tests..." -ForegroundColor Yellow
Write-Host "Command: $PythonPath -m pytest $TestFile -v" -ForegroundColor Cyan

& $PythonPath -m pytest $TestFile -v

$ExitCode = $LASTEXITCODE

if ($ExitCode -eq 0) {
    Write-Host "✅ Tests passed!" -ForegroundColor Green
} else {
    Write-Host "❌ Tests failed!" -ForegroundColor Red
}

exit $ExitCode

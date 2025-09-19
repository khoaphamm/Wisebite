#!/usr/bin/env python3
"""
Comprehensive test runner for WiseBite Backend.
Runs all test suites with detailed reporting and coverage analysis.
"""

import subprocess
import sys
import os
from pathlib import Path
import time


def run_command(command, description):
    """Run a shell command and return the result."""
    print(f"\n{'='*60}")
    print(f"🚀 {description}")
    print(f"{'='*60}")
    
    start_time = time.time()
    
    try:
        result = subprocess.run(
            command, 
            shell=True, 
            capture_output=True, 
            text=True,
            cwd=Path(__file__).parent
        )
        
        duration = time.time() - start_time
        
        if result.returncode == 0:
            print(f"✅ SUCCESS ({duration:.2f}s)")
            if result.stdout:
                print(result.stdout)
        else:
            print(f"❌ FAILED ({duration:.2f}s)")
            if result.stderr:
                print("STDERR:", result.stderr)
            if result.stdout:
                print("STDOUT:", result.stdout)
        
        return result.returncode == 0
        
    except Exception as e:
        print(f"❌ ERROR: {e}")
        return False


def main():
    """Run comprehensive test suite."""
    
    print("🧪 WiseBite Backend - Comprehensive Test Suite")
    print("=" * 60)
    
    # Change to the backend directory
    os.chdir(Path(__file__).parent)
    
    # Check if virtual environment exists and activate it
    venv_path = Path(".venv")
    if venv_path.exists():
        if os.name == "nt":  # Windows
            activate_script = venv_path / "Scripts" / "activate.bat"
            python_path = venv_path / "Scripts" / "python.exe"
        else:  # Unix/Linux/Mac
            activate_script = venv_path / "bin" / "activate"
            python_path = venv_path / "bin" / "python"
        
        if python_path.exists():
            print(f"✅ Using virtual environment: {venv_path}")
            # Update the python command to use venv python
            global python_cmd
            python_cmd = str(python_path)
        else:
            print("⚠️  Virtual environment found but Python executable missing")
            python_cmd = "python"
    else:
        print("⚠️  No virtual environment found, using system Python")
        python_cmd = "python"
    
    # Setup test database
    print("\n🗄️  Setting up test database...")
    setup_success = run_command(
        "docker-compose -f docker-compose.test.yml up -d test_db",
        "Starting PostGIS test database"
    )
    
    if not setup_success:
        print("❌ Failed to start test database. Please check Docker setup.")
        return 1
    
    # Wait for database to be ready
    print("⏳ Waiting for database to be ready...")
    time.sleep(10)  # Give database time to start
    
    # Test categories to run
    test_suites = [
        {
            "command": f"{python_cmd} -m pytest tests/test_setup.py -v",
            "description": "Environment Setup Tests"
        },
        {
            "command": f"{python_cmd} -m pytest tests/api/endpoints/test_auth.py -v",
            "description": "Authentication Tests"
        },
        {
            "command": f"{python_cmd} -m pytest tests/api/endpoints/test_user.py -v",
            "description": "User Management Tests"
        },
        {
            "command": f"{python_cmd} -m pytest tests/api/endpoints/test_store.py -v",
            "description": "Store Management Tests"
        },
        {
            "command": f"{python_cmd} -m pytest tests/api/endpoints/test_food_item.py -v",
            "description": "Food Item Tests"
        },
        {
            "command": f"{python_cmd} -m pytest tests/api/endpoints/test_surprise_bag.py -v",
            "description": "Surprise Bag Tests"
        },
        {
            "command": f"{python_cmd} -m pytest tests/api/endpoints/test_order_comprehensive.py -v",
            "description": "Order Management Tests"
        },
        {
            "command": f"{python_cmd} -m pytest tests/api/endpoints/test_transaction.py -v",
            "description": "Transaction Tests"
        }
    ]
    
    # Integration test suites
    integration_suites = [
        {
            "command": f"{python_cmd} -m pytest tests/ -m integration -v",
            "description": "All Integration Tests"
        },
        {
            "command": f"{python_cmd} -m pytest tests/ -m unit -v",
            "description": "All Unit Tests"
        }
    ]
    
    # Coverage and performance tests
    advanced_suites = [
        {
            "command": f"{python_cmd} -m pytest tests/ --cov=app --cov-report=html --cov-report=term",
            "description": "Test Coverage Analysis"
        }
    ]
    
    results = {}
    
    # Run individual test suites
    print("\n🎯 Running Individual Test Suites")
    for suite in test_suites:
        success = run_command(suite["command"], suite["description"])
        results[suite["description"]] = success
    
    # Run integration tests
    print("\n🔗 Running Integration Test Suites")
    for suite in integration_suites:
        success = run_command(suite["command"], suite["description"])
        results[suite["description"]] = success
    
    # Run advanced tests (optional)
    print("\n📊 Running Advanced Analysis")
    for suite in advanced_suites:
        success = run_command(suite["command"], suite["description"])
        results[suite["description"]] = success
    
    # Generate summary report
    print("\n" + "="*60)
    print("📋 TEST SUMMARY REPORT")
    print("="*60)
    
    passed = 0
    failed = 0
    
    for test_name, success in results.items():
        status = "✅ PASSED" if success else "❌ FAILED"
        print(f"{status:12} {test_name}")
        if success:
            passed += 1
        else:
            failed += 1
    
    total = passed + failed
    success_rate = (passed / total * 100) if total > 0 else 0
    
    print(f"\n📈 OVERALL RESULTS:")
    print(f"   Total Tests: {total}")
    print(f"   Passed: {passed}")
    print(f"   Failed: {failed}")
    print(f"   Success Rate: {success_rate:.1f}%")
    
    if failed == 0:
        print("\n🎉 ALL TESTS PASSED! Your API is ready for production.")
        return 0
    else:
        print(f"\n⚠️  {failed} test suite(s) failed. Please check the output above.")
        return 1


if __name__ == "__main__":
    sys.exit(main())

"""
Simple tests to verify the testing setup works.
These tests don't depend on the full application setup.
"""
import pytest


@pytest.mark.unit
def test_python_setup():
    """Test that Python is working correctly."""
    assert 1 + 1 == 2


@pytest.mark.unit  
def test_pytest_markers():
    """Test that pytest markers are working."""
    import pytest
    # If this runs, pytest is working
    assert pytest.__version__ is not None


@pytest.mark.unit
def test_imports():
    """Test that essential testing imports work."""
    import httpx
    import sqlmodel
    # If these import without error, dependencies are installed
    assert httpx.__version__ is not None

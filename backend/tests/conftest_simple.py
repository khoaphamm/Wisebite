"""
Simplified conftest.py for basic testing without full app dependencies.
"""
import pytest


# Basic pytest configuration for simple tests
@pytest.fixture
def sample_data():
    """Provide sample test data."""
    return {
        "name": "Test User",
        "email": "test@example.com"
    }

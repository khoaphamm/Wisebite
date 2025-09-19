# /backend/Dockerfile

# 1. Use an official Python runtime as a parent image
# Using a specific version is good practice for reproducibility.
# The "-slim" variant is smaller and good for production.
FROM python:3.11-slim

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Set environment variables
# This prevents Python from writing .pyc files to disc (improves performance in containers)
ENV PYTHONDONTWRITEBYTECODE 1
# This ensures Python output is sent straight to the terminal without buffering
ENV PYTHONUNBUFFERED 1

# 4. Install system dependencies and uv
# We need build-essential and libpq-dev for some Python packages that compile from source, like psycopg2
RUN apt-get update && apt-get install -y build-essential libpq-dev curl && \
    curl -LsSf https://astral.sh/uv/install.sh | sh && \
    mv /root/.local/bin/uv /usr/local/bin/uv

# 5. Copy the pyproject.toml, uv.lock, and README.md files and install Python dependencies
# This is done in a separate step to leverage Docker's layer caching.
COPY pyproject.toml uv.lock README.md ./
RUN uv sync --frozen

# 6. Copy the entire application code into the container
COPY ./app /app/app

# 7. Expose the port the app runs on
EXPOSE 8000

# 8. Define the command to run the application
# Use uv to run uvicorn with the FastAPI app found in app/main.py
# --host 0.0.0.0 is CRITICAL to make the server accessible from outside the container.
CMD ["uv", "run", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]
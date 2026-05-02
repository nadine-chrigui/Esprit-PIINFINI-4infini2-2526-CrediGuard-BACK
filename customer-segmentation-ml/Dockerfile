FROM python:3.9-slim

# Set working directory
WORKDIR /app

# Install system dependencies (including curl for health checks)
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    g++ \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better layer caching
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir --upgrade pip \
    && pip install --no-cache-dir -r requirements.txt

# Copy application source code
COPY src/ ./src/
COPY models/ ./models/
COPY data/ ./data/
COPY reports/ ./reports/

# Expose Flask port
EXPOSE 5000

# Environment variables
ENV FLASK_APP=src/app.py
ENV FLASK_DEBUG=0
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=15s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# Run the Flask application
CMD ["python", "src/app.py"]

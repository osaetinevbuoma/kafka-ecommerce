# syntax=docker/dockerfile:1

FROM python:3.12.7-slim

RUN apt-get update
RUN apt-get install -y --no-install-recommends build-essential

WORKDIR /app
RUN python -m venv /app/venv

COPY requirements.txt .
RUN pip install --upgrade pip
RUN pip install --no-cache-dir --upgrade -r requirements.txt

WORKDIR /app

COPY . .

CMD [ "uvicorn", "main:app", "--host", "0.0.0.0", "--port", "6000", "--reload" ]


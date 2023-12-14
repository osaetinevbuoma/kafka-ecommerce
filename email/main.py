import asyncio
import os

import structlog
from confluent_kafka import Consumer
from fastapi import FastAPI

app = FastAPI()

configuration: dict = {
    "bootstrap.servers": os.getenv("KAFKA_BROKER"),
    "group.id": "KAFKA_EMAIL_CONSUMER",
    "enable.auto.commit": "true",
    "auto.commit.interval.ms": "1000",
    "auto.offset.reset": "earliest"
}
consumer = Consumer(configuration)
KAFKA_TOPIC = "KAFKA_ECOMMERCE_MARKETPLACE"
SERVICE_NAME = "email-consumer-service"


async def consume_events() -> None:
    try:
        consumer.subscribe([KAFKA_TOPIC])

        while True:
            records = consumer.poll(timeout=1.0)
            if records is None:
                # Initial message consumption may take up to
                # `session.timeout.ms` for the consumer group to
                # rebalance and start consuming
                await structlog.get_logger(SERVICE_NAME).ainfo("Awaiting events...")
            elif records.error():
                await structlog.get_logger(SERVICE_NAME).aerror(
                    "Consumer Error",
                    error="ERROR: %s".format(records.error())
                )
            else:
                await send_email(order=records.value().decode("UTF-8"))
    except KeyboardInterrupt:
        pass
    finally:
        consumer.close()


async def send_email(order: str) -> None:
    await structlog.get_logger(SERVICE_NAME).ainfo(
        "Sending email...",
        title="Order placed",
        message="Your order has been placed",
        order=order
    )


asyncio.create_task(consume_events())


@app.get("/")
async def root():
    return {"message": "Hello World"}

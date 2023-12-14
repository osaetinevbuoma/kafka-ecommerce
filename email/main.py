import asyncio
import json
import os

import structlog
from confluent_kafka import Consumer
from fastapi import FastAPI

from models.processed_order import ProcessedOrder

app = FastAPI()

configuration: dict = {
    "bootstrap.servers": os.getenv("KAFKA_BROKER"),
    "group.id": "KAFKA_EMAIL_CONSUMER",
    "enable.auto.commit": "true",
    "auto.commit.interval.ms": "1000",
    "auto.offset.reset": "earliest"
}
consumer = Consumer(configuration)
KAFKA_MARKETPLACE_TOPIC = "KAFKA_ECOMMERCE_MARKETPLACE"
KAFKA_FULFILMENT_TOPIC = "KAFKA_ECOMMERCE_FULFILMENT"
SERVICE_NAME = "email-consumer-service"


async def consume_events() -> None:
    try:
        consumer.subscribe([KAFKA_MARKETPLACE_TOPIC, KAFKA_FULFILMENT_TOPIC])

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
                print(records.value().decode("UTF-8"))
                processed_order = ProcessedOrder.model_validate(json.loads(records.value().decode("UTF-8")))
                await send_email(order=processed_order)
    except KeyboardInterrupt:
        pass
    finally:
        consumer.close()


async def send_email(order: ProcessedOrder) -> None:
    title: str
    message: str
    if order.is_fulfilled:
        title = "Order dispatched"
        message = "Your order has been dispatched"
    else:
        title = "Order placed"
        message = "Your order has been placed"

    await structlog.get_logger(SERVICE_NAME).ainfo(
        "Sending email...",
        title=title,
        message=message,
        order=order.model_dump_json(exclude={"is_fulfilled"})
    )


asyncio.create_task(consume_events())


@app.get("/")
async def root():
    return {"message": "Hello World"}

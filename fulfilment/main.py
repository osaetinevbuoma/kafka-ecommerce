import asyncio
import json
import os

import structlog
from confluent_kafka import Consumer
from fastapi import FastAPI, Response

from db_entities import create_db
from exceptions.order_not_found_exception import OrderNotFoundException
from models.fulfil_order import FulfilOrder
from models.processed_order import ProcessedOrder
from services.fulfilment_service import get_fulfilment_service

app = FastAPI()

create_db()

configuration: dict = {
    "bootstrap.servers": os.getenv("KAFKA_BROKER"),
    "group.id": "KAFKA_FULFILMENT_CONSUMER",
    "enable.auto.commit": "true",
    "auto.commit.interval.ms": "1000",
    "auto.offset.reset": "earliest"
}
consumer = Consumer(configuration)

KAFKA_PAYMENT_TOPIC = "KAFKA_ECOMMERCE_PAYMENT"
SERVICE_NAME = "fulfilment-consumer-service"


async def consume_events() -> None:
    try:
        consumer.subscribe([KAFKA_PAYMENT_TOPIC])

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
                    error=f"ERROR: {records.error()}"
                )
            else:
                processed_order = ProcessedOrder.model_validate(json.loads(records.value().decode("UTF-8")))
                fulfilment_service = get_fulfilment_service()
                await fulfilment_service.save_orders(processed_order=processed_order)
    except KeyboardInterrupt:
        pass
    finally:
        consumer.close()


asyncio.create_task(consume_events())


@app.get("/")
async def root():
    return {"message": "Hello World"}


@app.get("/fulfilment/list")
async def list_filtered_orders(is_fulfilled: bool | None = None) -> list[ProcessedOrder]:
    fulfilment_service = get_fulfilment_service()
    return await fulfilment_service.list_orders(is_fulfilled=is_fulfilled)


@app.post("/fulfilment/process")
async def fulfil_order(order: FulfilOrder) -> Response:
    try:
        fulfilment_service = get_fulfilment_service()
        await fulfilment_service.fulfil_order(order_id=order.order_id)
        return Response(status_code=200)
    except OrderNotFoundException as exception:
        return Response(status_code=404, content=exception)

import os
import socket

import structlog
from confluent_kafka import Producer
from sqlalchemy.engine.base import Engine
from sqlalchemy.engine.result import ScalarResult
from sqlmodel import Session, select

from db_entities import get_db_engine
from db_entities.entities import Order, Product
from exceptions.order_not_found_exception import OrderNotFoundException
from models.ordered_products import OrderedProducts
from models.processed_order import ProcessedOrder


class FulfilmentService:
    def __init__(self, db_engine: Engine) -> None:
        self._db_engine = db_engine

    async def save_orders(self, processed_order: ProcessedOrder) -> None:
        from main import SERVICE_NAME

        await structlog.get_logger(SERVICE_NAME).ainfo(
            "Saving order...",
            processed_order=processed_order
        )

        with Session(self._db_engine) as session:
            order = Order(
                user_id=processed_order.user_id,
                order_id=processed_order.order_id,
                total_price=processed_order.total_price,
                is_fulfilled=False
            )
            session.add(order)
            session.commit()

            for processed_product in processed_order.products:
                product = Product(
                    title=processed_product.title,
                    price=processed_product.price,
                    description=processed_product.description,
                    category=processed_product.category,
                    image=processed_product.image,
                    quantity=processed_product.quantity,
                    order_id=order.id
                )
                session.add(product)
                session.commit()

    async def list_orders(self, is_fulfilled: bool = None) -> list[ProcessedOrder]:
        with Session(self._db_engine) as session:
            orders: ScalarResult[Order]
            if is_fulfilled is not None:
                statement = select(Order).where(Order.is_fulfilled == is_fulfilled)
                orders = session.exec(statement)
            else:
                orders = session.exec(select(Order))

            processed_orders: list[ProcessedOrder] = []
            for order in orders:
                products: list[OrderedProducts] = []
                for ordered_product in order.products:
                    product = OrderedProducts.model_validate(ordered_product.model_dump())
                    products.append(product)

                processed_order = ProcessedOrder.model_validate(order.model_dump())
                processed_order.products = products
                processed_orders.append(processed_order)

            return processed_orders

    async def fulfil_order(self, order_id: str) -> None:
        with Session(self._db_engine) as session:
            statement = select(Order).where(Order.order_id == order_id)
            order = session.exec(statement).one()

            if order is None:
                raise OrderNotFoundException(f"Order with ID {order_id} was not found")

            if order.is_fulfilled is True:
                return

            order.is_fulfilled = True
            session.add(order)
            session.commit()

            products: list[OrderedProducts] = []
            for ordered_product in order.products:
                product = OrderedProducts.model_validate(ordered_product.model_dump())
                products.append(product)

            processed_order = ProcessedOrder.model_validate(order.model_dump())
            processed_order.products = products

            await self._send_event(order=processed_order)

    async def _send_event(self, order: ProcessedOrder) -> None:
        KAFKA_FULFILMENT_TOPIC = "KAFKA_ECOMMERCE_FULFILMENT"
        configuration: dict = {
            "bootstrap.servers": os.getenv("KAFKA_BROKER"),
            "client.id": socket.gethostname(),
            "linger.ms": 1
        }
        producer = Producer(configuration)
        producer.produce(
            topic=KAFKA_FULFILMENT_TOPIC,
            key=order.order_id,
            value=order.model_dump_json(),
            callback=self._delivery_callback
        )
        producer.flush()

    def _delivery_callback(self, error, message) -> None:
        from main import SERVICE_NAME

        if error:
            structlog.get_logger(SERVICE_NAME).error(
                "Delivery callback error",
                error=f"ERROR: Message failed delivery: {error}"
            )
            return

        structlog.get_logger(SERVICE_NAME).info(
            "Delivery callback response",
            topic=message.topic(),
            key=message.key().decode("UTF-8"),
            value=message.value().decode("UTF-8")
        )


def get_fulfilment_service(db_engine=get_db_engine()) -> FulfilmentService:
    return FulfilmentService(db_engine=db_engine)

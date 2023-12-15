from pydantic import BaseModel


class FulfilOrder(BaseModel):
    order_id: str

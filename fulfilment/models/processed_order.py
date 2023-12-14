from pydantic import BaseModel

from models.ordered_products import OrderedProducts


class ProcessedOrder(BaseModel):
    user_id: int
    order_id: str
    products: list[OrderedProducts]

import re

from pydantic import BaseModel, Field, model_validator

from models.ordered_products import OrderedProducts


class ProcessedOrder(BaseModel):
    user_id: int
    order_id: str
    total_price: float
    is_fulfilled: bool | None = False
    products: list[OrderedProducts] = Field(default_factory=list)

    @model_validator(mode="before")
    @classmethod
    def _prepare_data(cls, data: dict):
        return {
            re.sub(r'(?<!^)(?=[A-Z])', '_', key).lower(): value
            for key, value in data.items()
        }

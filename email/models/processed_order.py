import re

from pydantic import BaseModel, model_validator

from models.ordered_products import OrderedProducts


class ProcessedOrder(BaseModel):
    user_id: int
    order_id: str
    products: list[OrderedProducts]
    is_fulfilled: bool | None = None

    @model_validator(mode="before")
    @classmethod
    def _prepare_data(cls, data: dict):
        return {
            re.sub(r'(?<!^)(?=[A-Z])', '_', key).lower(): value
            for key, value in data.items()
        }

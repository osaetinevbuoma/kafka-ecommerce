from pydantic import BaseModel


class OrderedProducts(BaseModel):
    title: str
    price: float
    description: str
    category: str
    image: str
    quantity: int

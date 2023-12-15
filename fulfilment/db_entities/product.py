from sqlalchemy.dialects.mysql import TEXT
from sqlmodel import SQLModel, Field


class Product(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    title: str
    price: float
    description: str = Field(sa_type=TEXT)
    category: str
    image: str
    quantity: int
    order_id: int | None = Field(default=None, foreign_key="order.id")

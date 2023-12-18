from sqlalchemy.dialects.mysql import TEXT
from sqlmodel import Field, SQLModel, Relationship


class Order(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    user_id: int
    order_id: str
    total_price: float
    is_fulfilled: bool

    products: list["Product"] = Relationship(back_populates="order")


class Product(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    title: str
    price: float
    description: str = Field(sa_type=TEXT)
    category: str
    image: str
    quantity: int

    order_id: int | None = Field(default=None, foreign_key="order.id")
    order: Order | None = Relationship(back_populates="products")

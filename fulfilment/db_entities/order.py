from sqlmodel import Field, SQLModel


class Order(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    user_id: int
    order_id: str
    total_price: float
    is_fulfilled: bool

import os

from sqlalchemy.engine.base import Engine
from sqlmodel import SQLModel, create_engine


def get_db_engine() -> Engine:
    return create_engine(os.getenv("MYSQL_URL"))


def create_db() -> None:
    SQLModel.metadata.create_all(get_db_engine())

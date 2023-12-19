import os

import time
from sqlalchemy.engine.base import Engine
from sqlmodel import SQLModel, create_engine


def get_db_engine() -> Engine:
    time.sleep(5)  # wait till other services have started before creating engine
    return create_engine(os.getenv("MYSQL_URL"))


def create_db() -> None:
    SQLModel.metadata.create_all(get_db_engine())

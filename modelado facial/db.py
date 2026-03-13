import os
import psycopg2
import psycopg2.pool
import logging
from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger(__name__)

_pool = None


def get_pool():
    global _pool
    if _pool is None:
        _pool = psycopg2.pool.ThreadedConnectionPool(
            minconn=1,
            maxconn=10,
            host=os.getenv("DB_HOST", "localhost"),
            port=int(os.getenv("DB_PORT", 5432)),
            dbname=os.getenv("DB_NAME", "validaya_db"),
            user=os.getenv("DB_USER", "validayaadmin"),
            password=os.getenv("DB_PASSWORD", ""),
        )
        logger.info("Pool de conexiones PostgreSQL inicializado")
    return _pool


def get_connection():
    return get_pool().getconn()


def release_connection(conn):
    get_pool().putconn(conn)


def test_connection() -> bool:
    try:
        conn = get_connection()
        cur = conn.cursor()
        cur.execute("SELECT 1")
        cur.close()
        release_connection(conn)
        return True
    except Exception as e:
        logger.error(f"Error al conectar con PostgreSQL: {e}")
        return False
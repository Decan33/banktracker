import csv
import random
import sys
from decimal import Decimal
from pathlib import Path

TRANSACTION_TYPES = [
    "SALARY",
    "GROCERIES",
    "RENT",
    "TRANSPORT",
    "ENTERTAINMENT",
    "TRANSFER",
    "OTHER",
]

CURRENCIES = ["PLN"]

BAD_VALUES = [
    "",
    "NULL",
    "not-a-number",
    "???",
    "2026-99",
    "INVALID",
    "-",
]


def next_available_path(output_path: str) -> Path:
    path = Path(output_path)

    if not path.exists():
        return path

    stem = path.stem
    suffix = path.suffix
    parent = path.parent

    counter = 1

    while True:
        candidate = parent / f"{stem}_{counter}{suffix}"
        if not candidate.exists():
            return candidate

        counter += 1


def random_iban() -> str:
    return "PL" + "".join(str(random.randint(0, 9)) for _ in range(26))


def generate_iban_pool(size: int) -> list[str]:
    return [random_iban() for _ in range(size)]

def year_month() -> str:
    year = int(sys.argv[1])
    month = int(sys.argv[2])
    return f"{year}-{month:02d}"

def random_year_month() -> str:
    year = random.randint(2020, 2026)
    month = random.randint(1, 12)
    return f"{year}-{month:02d}"


def random_amount(transaction_type: str) -> str:
    if transaction_type == "SALARY":
        value = Decimal(random.randint(4_000_00, 25_000_00)) / Decimal("100")

    elif transaction_type in {"GROCERIES", "RENT", "TRANSPORT", "ENTERTAINMENT"}:
        value = -(Decimal(random.randint(10_00, 5_000_00)) / Decimal("100"))

    else:
        value = Decimal(random.randint(-10_000_00, 10_000_00)) / Decimal("100")

    return str(value)


def maybe_corrupt(value: str, corruption_rate: float) -> str:
    if random.random() < corruption_rate:
        return random.choice(BAD_VALUES)

    return value


def generate_row(
        iban_pool: list[str],
        corruption_rate: float,
) -> dict:
    transaction_type = random.choice(TRANSACTION_TYPES)

    row = {
        "iban": random.choice(iban_pool),
        "transactionDate": year_month(),
        "currency": random.choice(CURRENCIES),
        "transactionType": transaction_type,
        "amount": random_amount(transaction_type),
    }

    return {
        key: maybe_corrupt(value, corruption_rate)
        for key, value in row.items()
    }


def generate_csv(
        output_path: str = "bank_transactions.csv",
        row_count: int = 10_000,
        corruption_rate: float = 0.03,
        iban_pool_size: int = 20,
) -> Path:
    headers = [
        "iban",
        "transactionDate",
        "currency",
        "transactionType",
        "amount",
    ]

    output = next_available_path(output_path)
    iban_pool = generate_iban_pool(iban_pool_size)

    with output.open("w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=headers)
        writer.writeheader()

        for i in range(row_count):
            writer.writerow(
                generate_row(
                    iban_pool=iban_pool,
                    corruption_rate=corruption_rate,
                )
            )

            if i > 0 and i % 100_000 == 0:
                print(f"Generated {i:,} rows...")

    print(f"Generated file: {output}")
    print(f"Rows: {row_count:,}")
    print(f"IBAN pool size: {iban_pool_size}")
    print(f"Approx rows per IBAN: {row_count // iban_pool_size:,}")

    return output


if __name__ == "__main__":
    generate_csv(
        output_path="bank_transactions.csv",
        row_count=10_000,
        corruption_rate=0.00,
        iban_pool_size=1000,
    )
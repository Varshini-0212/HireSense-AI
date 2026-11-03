from __future__ import annotations

import argparse
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from app.attrition_model import generate_synthetic_data

parser = argparse.ArgumentParser(description="Generate deterministic synthetic employee attrition data")
parser.add_argument("--records", type=int, default=2000)
parser.add_argument("--output", default="data/attrition_synthetic.csv")
parser.add_argument("--seed", type=int, default=42)
args = parser.parse_args()
if args.records < 1000:
    parser.error("--records must be at least 1000")
output = Path(args.output); output.parent.mkdir(parents=True, exist_ok=True)
generate_synthetic_data(args.records, args.seed).to_csv(output, index=False)
print(f"wrote {args.records} records to {output}")

from __future__ import annotations

import argparse
import json
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from app.attrition_model import train_and_save

parser = argparse.ArgumentParser(description="Train and compare HireSense attrition models")
parser.add_argument("--records", type=int, default=2000)
parser.add_argument("--output", default="data/attrition_synthetic.csv")
parser.add_argument("--model", default="models/attrition_model.joblib")
parser.add_argument("--seed", type=int, default=42)
args = parser.parse_args()
if args.records < 1000:
    parser.error("--records must be at least 1000")
print(json.dumps(train_and_save(args.records, args.output, args.model, args.seed), indent=2))

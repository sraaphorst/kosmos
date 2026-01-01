#!/usr/bin/env python3

import re
import sys
from pathlib import Path

SKIP_DIRS = {"build", ".gradle", "out", ".idea", ".git"}

NITS = [
    (re.compile(r"\beq\.eqv\("), "eq.eqv("),
    (re.compile(r"\bpr\.render\("), "pr.render("),
    (re.compile(r"\bPrintable\.Companion\b"), "Printable.Companion"),
]

RED = "\033[91m"
RESET = "\033[0m"

def main() -> int:
    root = Path(".")
    color = sys.stdout.isatty()

    kosmos_dirs = [d for d in root.iterdir() if d.is_dir() and d.name.startswith("kosmos")]

    any_hits = False

    for kosmos_dir in sorted(kosmos_dirs):
        for file_path in sorted(kosmos_dir.rglob("*.kt")):
            if any(part in SKIP_DIRS for part in file_path.parts):
                continue

            try:
                lines = file_path.read_text(encoding="utf-8").splitlines()
            except (UnicodeDecodeError, PermissionError):
                continue

            matches = []
            for line_num, line in enumerate(lines, start=1):
                hit_patterns = [pat for pat, _ in NITS if pat.search(line)]
                if hit_patterns:
                    matches.append((line_num, line, hit_patterns))

            if not matches:
                continue

            any_hits = True
            print(file_path.relative_to(root))

            for line_num, line, hit_patterns in matches:
                highlighted = line
                for pat in hit_patterns:
                    # highlight *matches*, not substrings
                    highlighted = pat.sub(lambda m: f"{RED}{m.group(0)}{RESET}" if color else m.group(0), highlighted)
                print(f"{line_num}: {highlighted}")
            print()

    return 1 if any_hits else 0

if __name__ == "__main__":
    raise SystemExit(main())
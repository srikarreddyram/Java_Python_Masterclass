"""
============================================================================
 SECTIONS 01-02 : REAL-WORLD APP - "Environment Doctor" (Python edition)
============================================================================

 Same job as SystemInfoApp.java: inspect the interpreter and machine before a
 build and report problems. Python-specific checks matter more here because
 Python projects break when the WRONG interpreter or a missing venv is used.

 CONCEPTS USED
  - sys.version_info      : interpreter version as a comparable tuple
  - platform / os modules : OS, CPU count, environment variables
  - sys.prefix vs sys.base_prefix : are we inside a virtual environment?
  - importlib.util.find_spec : is a package installed? (without importing it)
  - time.perf_counter     : high-resolution timing
  - shutil.disk_usage     : free disk space

 HOW TO RUN:  python3 system_info_app.py
============================================================================
"""
import importlib.util
import os
import platform
import shutil
import sys
import time

REQUIRED = (3, 11)        # match-case (3.10), typing.assert_never (3.11)


def row(key: str, value: object) -> None:
    print(f"  {key:<15}: {value}")


def check(label: str, ok: bool) -> None:
    print(f"  [{' OK ' if ok else 'FAIL'}] {label}")


def main() -> None:
    start = time.perf_counter()
    print("=== ENVIRONMENT DOCTOR (Python) ===")
    row("Python", platform.python_version())
    row("Implementation", platform.python_implementation())
    row("Executable", sys.executable)
    row("OS", f"{platform.system()} {platform.release()} ({platform.machine()})")
    row("CPU cores", os.cpu_count())
    in_venv = sys.prefix != sys.base_prefix
    row("Virtual env", sys.prefix if in_venv else "(none - using the system interpreter)")
    total, used, free = shutil.disk_usage("/")
    row("Disk free", f"{free // 2**30:,} GB of {total // 2**30:,} GB")
    row("PATH entries", len(os.environ.get("PATH", "").split(os.pathsep)))

    print("\n  Optional packages:")
    for pkg in ("numpy", "pandas", "requests", "pytest"):
        found = importlib.util.find_spec(pkg) is not None
        print(f"    {pkg:<9} {'installed' if found else 'missing  (pip install ' + pkg + ')'}")

    print()
    check(f"Python {REQUIRED[0]}.{REQUIRED[1]}+ (match-case, X | Y unions, assert_never)", sys.version_info >= REQUIRED)
    check("Running inside a virtual environment (recommended)", in_venv)
    check("At least 2 CPU cores", (os.cpu_count() or 1) >= 2)
    check("At least 1 GB free disk", free >= 2**30)
    print(f"\nReport generated in {(time.perf_counter() - start) * 1000:.2f} ms")


if __name__ == "__main__":
    main()

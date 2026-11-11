#!/usr/bin/env python3
# SPDX-License-Identifier: EUPL-1.2
# SPDX-FileCopyrightText: 2025-2026 Damian Fajfer <damian@fajfer.org>
"""Restore directory structure from filenames containing '__' separators.

The organizer never writes outside the resolved output directory and refuses
to overwrite existing files. Files beginning with '_' or '.' are skipped.
"""

import shutil
import sys
from pathlib import Path


def _safe_relative_path(filename: str) -> Path:
    """Convert an encoded filename to a safe relative path."""
    if not filename or "\x00" in filename:
        raise ValueError(f"Unsafe encoded path: {filename!r}")

    parts = filename.split("__") if "__" in filename else [filename]
    if not parts or any(
        part in ("", ".", "..")
        or "/" in part
        or "\\" in part
        or ":" in part
        for part in parts
    ):
        raise ValueError(f"Unsafe encoded path: {filename!r}")

    relative = Path(*parts)
    if relative.is_absolute() or relative.drive:
        raise ValueError(f"Absolute paths are not allowed: {filename!r}")
    return relative


def organize_files(source_dir: str = ".", output_dir: str = "organized") -> dict:
    """Copy source files into output_dir, rejecting traversal and collisions."""
    source_path = Path(source_dir).resolve()
    output_path = Path(output_dir).resolve()

    if not source_path.exists() or not source_path.is_dir():
        raise NotADirectoryError(f"Source directory does not exist: {source_path}")

    output_path.mkdir(parents=True, exist_ok=True)
    processed = 0
    skipped = 0

    for source_file in sorted(source_path.iterdir()):
        if source_file.name.startswith(("_", ".")):
            skipped += 1
            continue
        if source_file.is_symlink() or not source_file.is_file():
            skipped += 1
            continue

        relative_path = _safe_relative_path(source_file.name)
        target_path = (output_path / relative_path).resolve()

        try:
            target_path.relative_to(output_path)
        except ValueError as exc:
            raise ValueError(
                f"Refusing to write outside output directory: {source_file.name!r}"
            ) from exc

        target_path.parent.mkdir(parents=True, exist_ok=True)
        if target_path.exists() or target_path.is_symlink():
            raise FileExistsError(f"Refusing to overwrite existing file: {target_path}")

        shutil.copy2(source_file, target_path)
        processed += 1

    return {"processed": processed, "skipped": skipped, "output": str(output_path)}


def main() -> int:
    source_dir = sys.argv[1] if len(sys.argv) > 1 else "."
    output_dir = sys.argv[2] if len(sys.argv) > 2 else "organized"
    try:
        result = organize_files(source_dir, output_dir)
    except (OSError, ValueError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1

    print(f"Processed: {result['processed']}; skipped: {result['skipped']}")
    print(f"Output: {result['output']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

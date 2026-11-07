#!/usr/bin/env python3
# SPDX-License-Identifier: EUPL-1.2
# SPDX-FileCopyrightText: 2025-2026 Damian Fajfer <damian@fajfer.org>

"""Safely remove sequential line-number prefixes from text files.

The default mode is a read-only preview. Use --write to apply changes.
Changed files receive a sibling .bak copy before replacement.
"""

from __future__ import annotations

import argparse
import os
import re
import shutil
import tempfile
from pathlib import Path

SKIP_DIRS = {".git", "__pycache__", "node_modules", ".venv", "venv", "build", "dist"}
BINARY_EXTENSIONS = {
    ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".ico", ".svg", ".pdf",
    ".zip", ".tar", ".gz", ".bz2", ".xz", ".exe", ".dll", ".so",
    ".dylib", ".a", ".mp3", ".mp4", ".avi", ".mov", ".wav",
    ".ttf", ".otf", ".woff", ".woff2", ".eot", ".class", ".jar",
    ".war", ".ear", ".pyc", ".pyo", ".pyd",
}
PREFIX_RE = re.compile(r"^(\d+)([ \t]+)(.*)$")


def is_text_file(file_path: Path) -> bool:
    """Return whether a file can be decoded as UTF-8 and is not a known binary."""
    if file_path.suffix.lower() in BINARY_EXTENSIONS or file_path.is_symlink():
        return False
    try:
        with file_path.open("r", encoding="utf-8") as stream:
            stream.read(1024)
        return True
    except (UnicodeDecodeError, PermissionError, OSError):
        return False


def _numbered_prefixes(content: str) -> tuple[list[str], int] | None:
    """Return content lines and common prefix width only for strict 1..N numbering."""
    lines = content.splitlines(keepends=True)
    if len(lines) < 5:
        return None

    bodies = [line.rstrip("\r\n") for line in lines]
    separator_widths: list[int] = []
    for expected, body in enumerate(bodies, start=1):
        match = PREFIX_RE.match(body)
        if match:
            number, separator, _ = match.groups()
            if int(number) != expected or len(separator) < 2:
                return None
            separator_widths.append(len(separator))
            continue

        # A numbered blank line may contain only its line number.
        if body == str(expected):
            continue
        return None

    # Numbered exports use at least two separator spaces. Use the minimum
    # separator width as the line-number field and preserve any additional
    # indentation present in the source text.
    if len(separator_widths) < 5:
        return None
    return bodies, min(separator_widths)


def has_line_numbers(content: str) -> bool:
    """Detect a complete, sequential numbered listing (minimum five lines)."""
    return _numbered_prefixes(content) is not None


def remove_line_numbers(content: str) -> str:
    """Remove only a verified sequential line-number field; preserve all other text."""
    detected = _numbered_prefixes(content)
    if detected is None:
        return content
    bodies, prefix_width = detected
    output: list[str] = []
    original_lines = content.splitlines(keepends=True)
    for body, original in zip(bodies, original_lines):
        ending = original[len(original.rstrip("\r\n")):]
        match = PREFIX_RE.match(body)
        if match:
            number = match.group(1)
            output.append(body[len(number) + prefix_width:])
        else:
            output.append("")
        output[-1] += ending
    return "".join(output)


def process_file(file_path: Path, dry_run: bool = True) -> tuple[bool, str]:
    """Preview or safely clean one file, preserving a backup before any write."""
    try:
        original = file_path.read_text(encoding="utf-8")
        if not has_line_numbers(original):
            return False, "No strict sequential line numbering detected"
        cleaned = remove_line_numbers(original)
        if cleaned == original:
            return False, "No changes required"
        if dry_run:
            return True, "Would clean (preview only)"

        backup = file_path.with_name(file_path.name + ".bak")
        if backup.exists():
            return False, f"Backup already exists; refusing to overwrite: {backup.name}"
        shutil.copy2(file_path, backup)
        fd, temporary_name = tempfile.mkstemp(prefix=f".{file_path.name}.", dir=file_path.parent)
        try:
            with os.fdopen(fd, "w", encoding="utf-8", newline="") as stream:
                stream.write(cleaned)
            os.replace(temporary_name, file_path)
        except Exception:
            try:
                os.unlink(temporary_name)
            except OSError:
                pass
            raise
        return True, f"Cleaned; original preserved as {backup.name}"
    except (OSError, UnicodeError) as error:
        return False, f"Error: {error}"


def rename_html_files(repo_path: Path, dry_run: bool = True) -> list[tuple[Path, Path]]:
    """Preview or rename *.ext.html files, refusing collisions and symlinks."""
    renamed: list[tuple[Path, Path]] = []
    for root, dirs, files in os.walk(repo_path, followlinks=False):
        dirs[:] = [name for name in dirs if name not in SKIP_DIRS]
        for name in files:
            old_path = Path(root) / name
            if old_path.is_symlink() or not name.endswith(".html") or "." not in name[:-5]:
                continue
            new_path = old_path.with_name(name[:-5])
            if new_path.exists():
                continue
            if not dry_run:
                old_path.rename(new_path)
            renamed.append((old_path, new_path))
    return renamed


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--write", action="store_true", help="apply changes (default is preview only)")
    parser.add_argument("--rename", action="store_true", help="also rename *.ext.html files")
    args = parser.parse_args()
    dry_run = not args.write
    repo_path = Path(__file__).resolve().parent
    print(f"Processing repository: {repo_path}")
    print("PREVIEW ONLY — no files will be modified" if dry_run else "WRITE MODE — backups will be created")

    if args.rename:
        for old, new in rename_html_files(repo_path, dry_run=dry_run):
            print(f"{old.relative_to(repo_path)} -> {new.relative_to(repo_path)}")

    processed = cleaned = errors = 0
    for root, dirs, files in os.walk(repo_path, followlinks=False):
        dirs[:] = [name for name in dirs if name not in SKIP_DIRS]
        for name in files:
            path = Path(root) / name
            if name == Path(__file__).name or not is_text_file(path):
                continue
            processed += 1
            success, message = process_file(path, dry_run=dry_run)
            if success:
                cleaned += 1
                print(f"{path.relative_to(repo_path)}: {message}")
            elif message.startswith("Error:") or message.startswith("Backup already exists"):
                errors += 1
                print(f"{path.relative_to(repo_path)}: {message}")

    print(f"Summary: scanned={processed}, candidates={cleaned}, errors={errors}")
    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())

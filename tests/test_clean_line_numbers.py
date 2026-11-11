# SPDX-License-Identifier: EUPL-1.2
# SPDX-FileCopyrightText: 2026
"""Regression tests for conservative line-number cleanup."""

import tempfile
import unittest
from pathlib import Path

from clean_line_numbers import has_line_numbers, is_text_file, process_file, remove_line_numbers


def numbered(*lines):
    return "".join(f"{i}    {line}\n" for i, line in enumerate(lines, 1))


class CleanLineNumbersTests(unittest.TestCase):
    def test_removes_only_sequential_prefix_and_preserves_indentation(self):
        source = numbered("def f():", "    return 1", "", "class C:", "    pass")
        expected = "def f():\n    return 1\n\nclass C:\n    pass\n"
        self.assertTrue(has_line_numbers(source))
        self.assertEqual(remove_line_numbers(source), expected)

    def test_handles_multi_digit_prefixes_and_indented_lines(self):
        lines = ["fun f():"] + ["    value = 1"] * 8 + ["    return value", "class C:", "    pass"]
        source = "".join(f"{i}   {line}\n" for i, line in enumerate(lines, 1))
        expected = "\n".join(lines) + "\n"
        self.assertTrue(has_line_numbers(source))
        self.assertEqual(remove_line_numbers(source), expected)

    def test_does_not_strip_regular_numeric_source(self):
        source = "1 + 2\n2 + 3\n3 + 4\n4 + 5\n5 + 6\n"
        self.assertFalse(has_line_numbers(source))
        self.assertEqual(remove_line_numbers(source), source)

    def test_rejects_inconsistent_or_missing_sequence(self):
        source = "1    a\n2    b\n4    c\n5    d\n6    e\n"
        self.assertFalse(has_line_numbers(source))
        self.assertEqual(remove_line_numbers(source), source)

    def test_preserves_trailing_whitespace_and_blank_lines(self):
        source = numbered("alpha  ", "", "", "beta\t", "gamma")
        cleaned = remove_line_numbers(source)
        self.assertEqual(cleaned, "alpha  \n\n\nbeta\t\ngamma\n")

    def test_preserves_crlf_line_endings(self):
        source = "1    alpha\r\n2    beta\r\n3    gamma\r\n4    delta\r\n5    epsilon\r\n"
        self.assertEqual(
            remove_line_numbers(source),
            "alpha\r\nbeta\r\ngamma\r\ndelta\r\nepsilon\r\n",
        )

    def test_skips_backup_files_during_repository_scan(self):
        with tempfile.TemporaryDirectory() as directory:
            backup = Path(directory) / "source.txt.bak"
            backup.write_text(numbered("a", "b", "c", "d", "e"), encoding="utf-8")
            self.assertFalse(is_text_file(backup))

    def test_requires_at_least_five_lines(self):
        source = numbered("a", "b", "c", "d")
        self.assertFalse(has_line_numbers(source))

    def test_preview_does_not_modify_file(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "source.txt"
            source = numbered("a", "b", "c", "d", "e")
            path.write_text(source, encoding="utf-8")
            changed, message = process_file(path)
            self.assertTrue(changed)
            self.assertIn("preview", message)
            self.assertEqual(path.read_text(encoding="utf-8"), source)
            self.assertFalse(path.with_name("source.txt.bak").exists())

    def test_write_creates_backup_before_replacement(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "source.txt"
            source = numbered("a", "b", "c", "d", "e")
            path.write_text(source, encoding="utf-8")
            changed, _ = process_file(path, dry_run=False)
            self.assertTrue(changed)
            self.assertEqual(path.with_name("source.txt.bak").read_text(encoding="utf-8"), source)
            self.assertEqual(path.read_text(encoding="utf-8"), "a\nb\nc\nd\ne\n")

    def test_refuses_to_overwrite_existing_backup(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "source.txt"
            path.write_text(numbered("a", "b", "c", "d", "e"), encoding="utf-8")
            backup = path.with_name("source.txt.bak")
            backup.write_text("existing backup", encoding="utf-8")
            changed, message = process_file(path, dry_run=False)
            self.assertFalse(changed)
            self.assertIn("Backup already exists", message)
            self.assertEqual(backup.read_text(encoding="utf-8"), "existing backup")


if __name__ == "__main__":
    unittest.main()

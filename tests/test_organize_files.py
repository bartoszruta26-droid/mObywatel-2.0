# SPDX-License-Identifier: EUPL-1.2
# SPDX-FileCopyrightText: 2026
"""Regression tests for organize_files.py."""

import tempfile
import unittest
from pathlib import Path

from organize_files import organize_files


class OrganizeFilesTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.source = self.root / "source"
        self.output = self.root / "output"
        self.source.mkdir()

    def tearDown(self):
        self.temp.cleanup()

    def test_restores_nested_path(self):
        (self.source / "Sources__Components__Button.kt").write_text("content", encoding="utf-8")
        result = organize_files(str(self.source), str(self.output))
        self.assertEqual(result["processed"], 1)
        self.assertEqual(
            (self.output / "Sources" / "Components" / "Button.kt").read_text(encoding="utf-8"),
            "content",
        )

    def test_rejects_parent_traversal(self):
        (self.source / "safe__..__outside.txt").write_text("must not escape", encoding="utf-8")
        with self.assertRaises(ValueError):
            organize_files(str(self.source), str(self.output))
        self.assertFalse((self.root / "outside.txt").exists())

    def test_rejects_separator_embedded_in_segment(self):
        (self.source / "safe__..\\outside.txt").write_text("must not escape", encoding="utf-8")
        with self.assertRaises(ValueError):
            organize_files(str(self.source), str(self.output))

    def test_does_not_overwrite_existing_file(self):
        (self.source / "file.txt").write_text("new", encoding="utf-8")
        self.output.mkdir()
        (self.output / "file.txt").write_text("existing", encoding="utf-8")
        with self.assertRaises(FileExistsError):
            organize_files(str(self.source), str(self.output))
        self.assertEqual((self.output / "file.txt").read_text(encoding="utf-8"), "existing")

    def test_skips_symlinks(self):
        target = self.root / "external.txt"
        target.write_text("external", encoding="utf-8")
        try:
            (self.source / "linked.txt").symlink_to(target)
        except (OSError, NotImplementedError):
            self.skipTest("Symlinks are unavailable on this platform")
        result = organize_files(str(self.source), str(self.output))
        self.assertEqual(result["processed"], 0)
        self.assertEqual(result["skipped"], 1)

    def test_skips_hidden_and_manifest_files(self):
        (self.source / ".hidden").write_text("hidden", encoding="utf-8")
        (self.source / "_DIRECTORY_STRUCTURE.txt").write_text("manifest", encoding="utf-8")
        result = organize_files(str(self.source), str(self.output))
        self.assertEqual(result["processed"], 0)
        self.assertEqual(result["skipped"], 2)


if __name__ == "__main__":
    unittest.main()

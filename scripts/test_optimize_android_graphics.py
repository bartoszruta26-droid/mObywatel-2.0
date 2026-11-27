#!/usr/bin/env python3
# SPDX-FileCopyrightText: 2026 bartoszruta26-droid
# SPDX-License-Identifier: CC0-1.0
import sys
import tempfile
import unittest
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import optimize_android_graphics as optimizer


class GraphicOptimizationTests(unittest.TestCase):
    def test_existing_smaller_webp_removes_duplicate_png(self):
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory) / "document.png"
            target = Path(directory) / "document.webp"
            Image.new("RGB", (256, 256), "white").save(source, "PNG")
            Image.new("RGB", (32, 32), "white").save(target, "WEBP", quality=60)
            self.assertEqual(optimizer.webp_candidate(source), target)
            self.assertTrue(source.exists())
            self.assertTrue(target.exists())

    def test_conversion_keeps_only_smaller_webp(self):
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory) / "large.png"
            Image.new("RGB", (1024, 1024), "white").save(source, "PNG")
            original_size = source.stat().st_size
            result = optimizer.webp_candidate(source)
            if result.suffix == ".webp":
                self.assertTrue(source.exists())
                self.assertLessEqual(result.stat().st_size, original_size * 0.98)
            else:
                self.assertTrue(source.exists())
                self.assertFalse(result.with_suffix(".webp").exists())


    def test_animated_png_is_preserved_without_conversion(self):
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory) / "animated.png"
            frames = [Image.new("RGBA", (32, 32), color) for color in ("red", "blue")]
            frames[0].save(
                source, "PNG", save_all=True, append_images=frames[1:],
                duration=100, loop=0
            )
            self.assertTrue(optimizer.is_animated(source))
            self.assertIsNone(optimizer.webp_candidate(source))
            self.assertTrue(source.exists())
            self.assertFalse(source.with_suffix(".webp").exists())

    def test_nine_patch_is_never_converted(self):
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory) / "panel.9.png"
            Image.new("RGBA", (64, 64), (255, 255, 255, 255)).save(source, "PNG")
            self.assertIsNone(optimizer.webp_candidate(source))
            self.assertTrue(source.exists())


    def test_remote_manifest_paths_match_repository_and_resolver_allowlist(self):
        import json

        root = Path(__file__).resolve().parents[1]
        manifest_path = root / "obywatel-android/app/src/main/assets/graphic-thumbnails/manifest.json"
        resolver_path = root / "obywatel-android/app/src/main/java/pl/gov/obywatel/android/RemoteGraphicRepository.kt"
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        resolver = resolver_path.read_text(encoding="utf-8")

        self.assertIn('path.startsWith("graphic-originals/full-webp/")', resolver)
        self.assertIn('path.endsWith(".webp")', resolver)
        self.assertEqual(manifest["count"], len(manifest["items"]))
        for item in manifest["items"]:
            full_path = item["full_path"]
            self.assertTrue(full_path.startswith("graphic-originals/full-webp/"), full_path)
            self.assertTrue(full_path.endswith(".webp"), full_path)
            self.assertTrue((root / full_path).is_file(), full_path)


if __name__ == "__main__":
    unittest.main()

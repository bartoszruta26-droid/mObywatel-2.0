#!/usr/bin/env python3
# SPDX-FileCopyrightText: 2026 bartoszruta26-droid
# SPDX-License-Identifier: CC0-1.0
"""Losslessly optimize Android raster resources and create a thumbnail manifest.

The script preserves Android resource basenames (R.drawable identifiers). PNGs
are converted to lossless WebP only when the result is materially smaller.
JPEGs use high-quality WebP only when smaller. Nine-patch and animated files
are deliberately excluded.
"""
from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path

from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "obywatel-android/app/src/main/res"
FULL_ROOT = ROOT / "graphic-originals/full-webp"
SOURCE_ROOT = ROOT / "graphic-originals/source-png"
THUMB_ROOT = ROOT / "obywatel-android/app/src/main/assets/graphic-thumbnails"
MANIFEST = THUMB_ROOT / "manifest.json"
MIN_SOURCE_BYTES = 96 * 1024
THUMB_MAX_EDGE = 360


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def resource_dirs():
    """Yield full-resolution source directories kept outside the APK."""
    for name in ("drawable", "drawable-nodpi", "drawable-hdpi", "drawable-xhdpi",
                 "drawable-xxhdpi", "drawable-xxxhdpi"):
        folder = FULL_ROOT / name
        if folder.is_dir():
            yield folder


def is_animated(path: Path) -> bool:
    """Return whether a raster file contains multiple frames."""
    try:
        with Image.open(path) as image:
            return bool(getattr(image, "is_animated", False) or getattr(image, "n_frames", 1) > 1)
    except Exception:
        return False


def webp_candidate(source: Path) -> Path | None:
    if source.name.endswith(".9.png") or source.suffix.lower() not in {".png", ".jpg", ".jpeg"}:
        return None
    # APNG and other multi-frame sources must remain untouched; converting only
    # their first frame would silently destroy animation.
    if is_animated(source):
        return None
    target = source.with_suffix(".webp")
    if target.exists():
        try:
            with Image.open(target) as existing:
                existing.verify()
            if is_animated(target):
                target.unlink(missing_ok=True)
            elif target.stat().st_size <= source.stat().st_size * 0.98:
                # Preserve the original source in GitHub; WebP is an additional optimized copy.
                return target
        except Exception:
            target.unlink(missing_ok=True)
        else:
            target.unlink(missing_ok=True)
    try:
        with Image.open(source) as image:
            image = ImageOps.exif_transpose(image)
            if source.suffix.lower() == ".png":
                image.save(target, "WEBP", lossless=True, quality=100, method=6, exact=True)
            else:
                image.convert("RGB").save(target, "WEBP", quality=92, method=6)
        if target.stat().st_size <= source.stat().st_size * 0.98:
            # Preserve the original source in GitHub; WebP is an additional optimized copy.
            return target
        target.unlink(missing_ok=True)
        return source
    except Exception:
        target.unlink(missing_ok=True)
        raise


def main() -> None:
    THUMB_ROOT.mkdir(parents=True, exist_ok=True)
    # This directory contains generated thumbnails only; remove stale outputs
    # so the manifest and files always describe the same resource set.
    for stale in THUMB_ROOT.glob("*.webp"):
        if re.search(r"-[0-9a-f]{12}\.webp$", stale.name):
            stale.unlink()
    entries = []
    seen_full_paths = set()
    for folder in resource_dirs():
        for path in sorted(folder.iterdir()):
            if not path.is_file() or path.suffix.lower() not in {".png", ".jpg", ".jpeg", ".webp"}:
                continue
            if path.name.endswith(".9.png") or path.stat().st_size < MIN_SOURCE_BYTES:
                continue
            source_candidate = SOURCE_ROOT / path.parent.name / f"{path.stem}.png"
            original_path = source_candidate if source_candidate.is_file() else path
            if is_animated(path):
                continue
            full_path = webp_candidate(path) if path.suffix.lower() in {".png", ".jpg", ".jpeg"} else path
            if full_path is None or is_animated(full_path):
                continue
            full_relative = full_path.relative_to(ROOT).as_posix()
            if full_relative in seen_full_paths:
                continue
            seen_full_paths.add(full_relative)
            with Image.open(full_path) as image:
                image = ImageOps.exif_transpose(image)
                width, height = image.size
                if max(width, height) <= THUMB_MAX_EDGE:
                    continue
                thumb = image.copy()
                thumb.thumbnail((THUMB_MAX_EDGE, THUMB_MAX_EDGE), Image.Resampling.LANCZOS)
                relative = full_path.relative_to(FULL_ROOT).as_posix()
                key = hashlib.sha256(relative.encode("utf-8")).hexdigest()[:12]
                thumb_name = f"{full_path.stem}-{key}.webp"
                thumb_path = THUMB_ROOT / thumb_name
                if "A" in thumb.getbands():
                    thumb.save(thumb_path, "WEBP", lossless=True, quality=88, method=6, exact=True)
                else:
                    thumb.convert("RGB").save(thumb_path, "WEBP", quality=84, method=6)
            entries.append({
                "resource_name": full_path.stem,
                "resource_directory": full_path.parent.name,
                "full_path": full_path.relative_to(ROOT).as_posix(),
                "thumbnail_asset_path": thumb_path.relative_to(ROOT).as_posix(),
                "source_path_before_optimization": original_path.relative_to(ROOT).as_posix(),
                "width": width,
                "height": height,
                "full_bytes": full_path.stat().st_size,
                "thumbnail_bytes": thumb_path.stat().st_size,
                "full_sha256": sha256(full_path),
                "thumbnail_sha256": sha256(thumb_path),
                "full_format": full_path.suffix.lstrip(".").upper(),
                "thumbnail_format": "WEBP"
            })
    entries.sort(key=lambda item: item["full_path"])
    payload = {
        "schema_version": 1,
        "generated_by": "scripts/optimize_android_graphics.py",
        "policy": {
            "thumbnail_max_edge_px": THUMB_MAX_EDGE,
            "minimum_source_bytes": MIN_SOURCE_BYTES,
            "png_conversion": "lossless WebP only when at least 2% smaller",
            "jpeg_conversion": "quality 92 WebP only when at least 2% smaller",
            "nine_patch_and_animated_images": "excluded",
            "full_graphics_location": "graphic-originals/full-webp/",
            "original_sources_location": "graphic-originals/source-png/",
            "app_resources": "thumbnail-only WebP; original full-resolution files are not packaged in the APK"
        },
        "count": len(entries),
        "items": entries
    }
    MANIFEST.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(entries)} thumbnails in {THUMB_ROOT}")
    print(f"Manifest: {MANIFEST}")


if __name__ == "__main__":
    main()

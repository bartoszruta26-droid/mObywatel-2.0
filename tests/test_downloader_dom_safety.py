# SPDX-License-Identifier: EUPL-1.2
# SPDX-FileCopyrightText: 2026
"""Static regression checks for safe DOM rendering in the repository userscript."""

import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "mobywatel-downloader.user.js"


class DownloaderDomSafetyTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = SCRIPT.read_text(encoding="utf-8")

    def test_no_dynamic_inner_html_sink(self):
        self.assertNotRegex(self.source, r"\.innerHTML\s*=")

    def test_progress_path_is_rendered_as_text(self):
        self.assertIn("progressDiv.textContent", self.source)
        self.assertIn("progressDiv.style.whiteSpace = 'pre-line'", self.source)

    def test_info_panel_uses_text_nodes_and_event_listener(self):
        self.assertIn("details.textContent", self.source)
        self.assertIn("panel.appendChild(details)", self.source)
        self.assertIn("debugButton.addEventListener('click'", self.source)
        self.assertNotIn("onclick=", self.source)

    def test_download_object_url_is_not_revoked_synchronously(self):
        self.assertIn("window.setTimeout(() => URL.revokeObjectURL(url), 1000)", self.source)


if __name__ == "__main__":
    unittest.main()

#!/usr/bin/env python3
"""Generator plików wzorowych PDF do app/src/main/assets/docs/.

Użycie (z katalogu głównego repo):  python3 tools/make_sample_pdf.py
Tworzy proste, poprawne pliki PDF (Helvetica, A4) — bez zewnętrznych zależności.
Dzięki nim podgląd dokumentów w aplikacji działa od razu po instalacji.
"""


def make_pdf(pages_text, path):
    out = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
    offsets = {}

    def write_obj(num, body):
        offsets[num] = len(out)
        out.extend(f"{num} 0 obj\n".encode())
        out.extend(body.encode("latin-1"))
        out.extend(b"\nendobj\n")

    n_pages = len(pages_text)
    page_ids = list(range(3, 3 + n_pages))
    content_ids = [100 + i for i in range(n_pages)]
    font_id = 200

    # strony + ich treści
    for i, (pid, cid) in enumerate(zip(page_ids, content_ids)):
        lines = ["BT", "/F1 16 Tf", "60 780 Td"]
        first = True
        for ln in pages_text[i]:
            esc = ln.replace("\\", r"\\").replace("(", r"\(").replace(")", r"\)")
            if first:
                lines.append(f"({esc}) Tj")
                first = False
            else:
                lines += ["0 -28 Td", f"({esc}) Tj"]
        lines.append("ET")
        stream = "\n".join(lines)
        write_obj(cid, f"<< /Length {len(stream)} >>\nstream\n{stream}\nendstream")
        write_obj(
            pid,
            f"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
            f"/Contents {cid} 0 R /Resources << /Font << /F1 {font_id} 0 R >> >> >>",
        )

    write_obj(font_id, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")
    kids = " ".join(f"{p} 0 R" for p in page_ids)
    write_obj(1, "<< /Type /Catalog /Pages 2 0 R >>")
    write_obj(2, f"<< /Type /Pages /Kids [{kids}] /Count {n_pages} >>")

    max_num = max(offsets)
    xref_pos = len(out)
    out += f"xref\n0 {max_num + 1}\n".encode()
    out += b"0000000000 65535 f \n"
    for n in range(1, max_num + 1):
        out += (f"{offsets.get(n, 0):010d} 00000 {'n' if n in offsets else 'f'} \n").encode()
    out += (
        f"trailer\n<< /Size {max_num + 1} /Root 1 0 R >>\nstartxref\n{xref_pos}\n%%EOF\n"
    ).encode()

    with open(path, "wb") as f:
        f.write(bytes(out))
    print("utworzono:", path)


if __name__ == "__main__":
    make_pdf(
        [
            [
                "RZECZPOSPOLITA POLSKA",
                "DOWOD OSOBISTY - WZOR PODGLADOWY (DEMO)",
                "",
                "Nr dokumentu: ABC 123456",
                "Data wydania: 2022-03-15",
                "Wazny do: 2032-03-15",
                "Organ wydajacy: Prezydent Miasta",
                "",
                "DOKUMENT DEMONSTRACYJNY - NIE JEST DOKUMENTEM TOZSAMOSCI",
            ],
            [
                "Strona 2 - klauzula informacyjna (demo)",
                "Dane zawarte w pliku sa przykladowe.",
            ],
        ],
        "app/src/main/assets/docs/dowod_wzor.pdf",
    )

    make_pdf(
        [
            [
                "RZECZPOSPOLITA POLSKA",
                "PRAWO JAZDY - WZOR PODGLADOWY (DEMO)",
                "",
                "Nr dokumentu: WXJ 448821",
                "Kategoria: B",
                "Data wydania: 2021-06-01",
                "Wazny do: 2041-06-01",
                "Organ wydajacy: Starosta Powiatowy",
                "",
                "DOKUMENT DEMONSTRACYJNY",
            ],
        ],
        "app/src/main/assets/docs/prawojazdy_wzor.pdf",
    )

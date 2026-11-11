"""Coordinate-based table extraction for pages whose cells wrap over several lines.

The book draws every table the same way: an orange header band with the column
titles, then rows separated by thin grey rules. We read the column starts from
the header words and the row boundaries from the rules, then put each word into
its (row, column) cell. Text-order parsing cannot do this reliably because
wrapped cells interleave.
"""
import re

import pymupdf

from clean_text import fix

HEADER_FILL = (0.949, 0.663, 0.231)


def _close(a, b, tol=0.02):
    return a is not None and all(abs(x - y) < tol for x, y in zip(a, b))


def _words(page):
    """Words as (x0, y0, x1, y1, text), merging glyph runs PyMuPDF split mid-word."""
    raw = sorted(page.get_text("words"), key=lambda w: (round(w[1]), w[0]))
    out = []
    for w in raw:
        x0, y0, x1, y1, t = w[:5]
        if out:
            px0, py0, px1, py1, pt = out[-1]
            same_line = abs(py0 - y0) < 3
            if same_line and 0 <= x0 - px1 < 0.8 and re.search(r"[ঀ-৿]$", pt) and re.match(r"[ঀ-৿]", t):
                out[-1] = (px0, min(py0, y0), x1, max(py1, y1), pt + t)
                continue
        out.append((x0, y0, x1, y1, t))
    return out


def _cell_text(words):
    lines = []
    for w in sorted(words, key=lambda w: (w[1] + w[3]) / 2):
        mid = (w[1] + w[3]) / 2
        if lines and abs(lines[-1][0] - mid) < 5:
            lines[-1][1].append(w)
        else:
            lines.append([mid, [w]])
    text = " ".join(" ".join(x[4] for x in sorted(ws, key=lambda x: x[0])) for _, ws in lines)
    return re.sub(r"\s+", " ", fix(text)).strip()


def page_tables(page):
    """Return [{'title': str, 'columns': [...], 'rows': [[...]]}] for one page."""
    rects = []
    for d in page.get_drawings():
        for it in d["items"]:
            if it[0] == "re":
                rects.append((it[1], d.get("fill")))
    headers = sorted([r for r, f in rects if _close(f, HEADER_FILL) and r.height > 8], key=lambda r: r.y0)
    rules = sorted({round(r.y0, 1) for r, f in rects if r.height <= 2.5 and r.width > 100})
    words = _words(page)
    tables = []
    for hi, h in enumerate(headers):
        head_words = [w for w in words if h.y0 - 2 <= (w[1] + w[3]) / 2 <= h.y1 + 2 and h.x0 - 2 <= w[0] <= h.x1]
        head_words.sort(key=lambda w: w[0])
        # Column = cluster of header words separated by a gap > 14pt.
        cols = []
        for w in head_words:
            if cols and w[0] - cols[-1]["x1"] < 14:
                cols[-1]["words"].append(w[4])
                cols[-1]["x1"] = w[2]
            else:
                cols.append({"x0": w[0], "x1": w[2], "words": [w[4]]})
        if not cols:
            continue
        next_top = headers[hi + 1].y0 if hi + 1 < len(headers) else page.rect.height
        bounds = [h.y1] + [y for y in rules if h.y1 + 1 < y < next_top - 1]
        # A table ends at its last rule; text below belongs to the page body.
        rows = []
        for a, b in zip(bounds, bounds[1:]):
            inside = [w for w in words if a < (w[1] + w[3]) / 2 < b and h.x0 - 2 <= w[0] <= h.x1 + 2]
            if not inside:
                continue
            cells = [[] for _ in cols]
            for w in inside:
                ci = max(i for i, c in enumerate(cols) if w[0] >= c["x0"] - 6) if w[0] >= cols[0]["x0"] - 6 else 0
                cells[ci].append(w)
            row = [_cell_text(c) for c in cells]
            # Rules continue below some tables; a row with an empty cell is the next
            # section's heading spilling across columns, so the table has ended.
            if len(cols) > 1 and not all(row):
                break
            rows.append(row)
        # Title: the nearest text line just above the header band.
        above = [w for w in words if h.y0 - 30 < w[3] <= h.y0 + 1 and not any(abs(r - w[3]) < 3 for r in rules)]
        title = _cell_text(above) if above else ""
        tables.append({"title": title, "y": h.y0, "columns": [fix(" ".join(c["words"])) for c in cols], "rows": rows})
    return tables


def chapter_tables(pdf_path, first_page, last_page):
    doc = pymupdf.open(pdf_path)
    out = []
    for pn in range(first_page, last_page + 1):
        for t in page_tables(doc[pn - 1]):
            t["page"] = pn
            out.append(t)
    return out


if __name__ == "__main__":
    import json
    import sys

    res = chapter_tables(sys.argv[1], int(sys.argv[2]), int(sys.argv[3]))
    print(json.dumps(res, ensure_ascii=False, indent=1))

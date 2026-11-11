"""Extract clean Unicode text from the Vocabsolution PDF.

PyMuPDF keeps Bengali vowel signs attached (pdftotext inserts spurious
spaces), but it duplicates some glyphs after conjuncts (e.g. "অধ্যাায়").
We collapse those duplicates and patch the handful of words where a real
double letter was collapsed by mistake.

Usage: python3 -I clean_text.py BOOK.pdf OUT.txt
Each page is preceded by a line "=====PAGE n=====".
"""
import re
import sys
import unicodedata

import subprocess

import pymupdf

BN = r"\u0980-\u09FF"

PATCHES = {
    "পূর্বর্তী": "পূর্ববর্তী",
    "সর্ব্যাপী": "সর্বব্যাপী",
    "পূর্বিদ্যমান": "পূর্ববিদ্যমান",
    "বিপর্য়": "বিপর্যয়",
    "বিতর্কারী": "বিতর্ককারী",
    "শার্টি": "শার্টটি",
}


def fix(s: str) -> str:
    s = unicodedata.normalize("NFC", s)
    s = re.sub(r"(া[ংঃঁ])\1", r"\1", s)
    s = re.sub(r"([া-ৌঁ-ঃৗ])\1+", r"\1", s)
    s = re.sub(r"ং\s+ং", "ং", s)  # "বিল্ডিং ংয়ের" -> "বিল্ডিংয়ের"
    for bad, good in PATCHES.items():
        s = s.replace(bad, good)
    s = s.replace("’", "'").replace("‘", "'")
    return s


def rejoin(page_text: str, ref: str) -> str:
    """Join Bengali words PyMuPDF split across lines (e.g. "স্টি\nক").

    A break is joined only if pdftotext's layout output (same page) shows the
    two pieces on one line with nothing but optional stray spaces between.
    """
    pat = re.compile(rf"([{BN}]+)\n([{BN}]+)")

    def joinable(tail: str, head: str) -> bool:
        # pdftotext inserts stray spaces only after u/uu/ri vowel signs, so a
        # space at the boundary itself is allowed only after those signs.
        t, h = tail[-4:], head[:4]
        boundary = " ?" if t[-1] in "ুূৃ" else ""
        rx = (" ?".join(re.escape(c) for c in t) + boundary
              + " ?".join(re.escape(c) for c in h))
        return re.search(rx, ref) is not None

    out, pos = [], 0
    for m in pat.finditer(page_text):
        tail, head = m.group(1), m.group(2)
        if joinable(tail, head) and not joinable_line_end(tail, head, ref):
            out.append(page_text[pos:m.start()] + tail + head)
        else:
            out.append(page_text[pos:m.end()])
        pos = m.end()
    out.append(page_text[pos:])
    return "".join(out)


def joinable_line_end(tail: str, head: str, ref: str) -> bool:
    """True if ref has tail at a line end and head at the next line start."""
    rx = re.escape(tail[-4:]) + r" *\n *" + re.escape(head[:4])
    return re.search(rx, ref) is not None


def layout_pages(pdf: str) -> list[str]:
    txt = subprocess.run(["pdftotext", "-layout", pdf, "-"], capture_output=True,
                         text=True, check=True).stdout
    return [unicodedata.normalize("NFC", p) for p in txt.split("\f")]


def main(pdf: str, out: str) -> None:
    doc = pymupdf.open(pdf)
    refs = layout_pages(pdf)
    with open(out, "w", encoding="utf-8") as fh:
        for i, page in enumerate(doc, start=1):
            fh.write(f"=====PAGE {i}=====\n")
            ref = refs[i - 1] if i - 1 < len(refs) else ""
            fh.write(rejoin(fix(page.get_text()), ref))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])

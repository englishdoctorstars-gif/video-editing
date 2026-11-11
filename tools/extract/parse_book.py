"""Parse the cleaned Vocabsolution text into JSON content files for the app.

Usage:
    python3 -I clean_text.py BOOK.pdf clean.txt
    python3 -I parse_book.py clean.txt OUT_DIR

Writes one JSON file per content set and prints a validation report that
compares the parsed counts with the counts stated in the book.
"""
import json
import os
import re
import sys

BN_DIGITS = str.maketrans("০১২৩৪৫৬৭৮৯", "0123456789")
POS_MARK = {"■": "noun", "▲": "verb", "●": "adjective", "◆": "adverb", "○": "other"}
POS_ABBR = {"n": "noun", "v": "verb", "adj": "adjective", "adv": "adverb"}
POS_NAME = {"Noun": "noun", "Verb": "verb", "Adjective": "adjective", "Adverb": "adverb"}
HAS_BN = re.compile(r"[ঀ-৿]")
HAS_LATIN = re.compile(r"[A-Za-z]")


def bn_int(s: str) -> int:
    return int(s.translate(BN_DIGITS))


def is_bn(line: str) -> bool:
    """Line is mostly Bengali (more Bengali letters than Latin letters)."""
    return len(HAS_BN.findall(line)) > len(HAS_LATIN.findall(line))


def join(lines) -> str:
    out = ""
    for ln in lines:
        ln = ln.strip()
        if not ln:
            continue
        if not out:
            out = ln
        elif out.endswith("-") and not out.endswith(" -") and HAS_LATIN.search(out[-2:]):
            out += ln  # hyphenated English word wrap: keep hyphen ("year-long")
        else:
            out += " " + ln
    return re.sub(r"\s+", " ", out).strip()


def split_list(s: str):
    return [x.strip() for x in re.split(r",\s*", s) if x.strip()]


def load_pages(path: str):
    text = open(path, encoding="utf-8").read()
    parts = re.split(r"=====PAGE (\d+)=====\n", text)
    pages = {}
    for i in range(1, len(parts), 2):
        pages[int(parts[i])] = parts[i + 1]
    return pages


def body_lines(pages, first: int, last: int, header_prefix: str):
    """Lines of pdf pages first..last (inclusive) without running headers/footers."""
    out = []
    for p in range(first, last + 1):
        lines = pages[p].split("\n")
        skip_next = False
        for ln in lines:
            s = ln.strip()
            if skip_next:
                skip_next = False
                continue
            if s.startswith(header_prefix):
                skip_next = True  # the running sub-title follows the header line
                continue
            if s == "© English Doctor" or re.fullmatch(r"[০-৯]+", s) or not s:
                continue
            out.append(s)
    return out


def find_page(pages, needle: str, start: int = 1) -> int:
    for p in sorted(pages):
        if p >= start and needle in pages[p]:
            return p
    raise ValueError(needle)


def section(lines, start_pat: str, end_pat: str | None):
    i = next(k for k, l in enumerate(lines) if re.search(start_pat, l))
    j = len(lines)
    if end_pat:
        j = next(k for k in range(i + 1, len(lines)) if re.search(end_pat, lines[k]))
    return lines[i + 1:j]


# --------------------------------------------------------------- chapter 1
def parse_affixes(lines, kind: str):
    """Suffix rows: affix / POS / meaning / examples.  Prefix rows: affix / type / meaning / examples."""
    if kind == "suffix":
        head_re = re.compile(r"^-")
    else:
        head_re = re.compile(r"^[A-Z][a-z]*-(\s*/\s*[A-Za-z]+-)*$")
    rows, cur = [], None
    for ln in lines:
        if ln in ("প্রত্যয়", "উপসর্গ", "যে পদ গঠন করে", "ধরন", "অর্থ", "উদাহরণ") or ln.startswith("·"):
            continue
        if head_re.match(ln) and (cur is None or cur["stage"] != "affix" or kind == "prefix"):
            if cur and cur["stage"] == "affix" and kind == "suffix":
                cur["affix"].append(ln)
                continue
            cur = {"affix": [ln], "stage": "affix", "pos": None, "type": None, "meaning": [], "ex": []}
            rows.append(cur)
            continue
        if cur is None:
            continue
        if kind == "suffix" and ln[0] in POS_MARK and cur["stage"] == "affix":
            cur["pos"] = POS_NAME.get(ln[1:].strip(), "other")
            cur["stage"] = "meaning"
            continue
        if cur["stage"] == "affix":
            if kind == "suffix":
                cur["affix"].append(ln)  # e.g. "(noun)" or wrapped "-ness / -cy"
                continue
            cur["type"] = ln
            cur["stage"] = "meaning"
            continue
        if re.fullmatch(r"[a-z][a-z\-' ,()]*,?", ln) and not is_bn(ln):
            cur["ex"].append(ln)
            cur["stage"] = "ex"
        elif cur["stage"] == "meaning":
            cur["meaning"].append(ln)
        else:
            cur["ex"].append(ln)
    out = []
    for n, r in enumerate(rows, 1):
        item = {
            "id": f"{kind}_{n:02d}",
            "affix": join(r["affix"]),
            "meaningBn": join(r["meaning"]),
            "examples": split_list(join(r["ex"])),
        }
        if kind == "suffix":
            item["posFormed"] = r["pos"]
        else:
            item["typeBn"] = r["type"]
        out.append(item)
    return out


def parse_analysis(lines):
    out, i = [], 0
    lines = [l for l in lines if l not in ("শব্দ", "বিশ্লেষণ", "অর্থ")]
    while i < len(lines):
        m = re.match(r"^([A-Z][A-Za-z\-]+)\s*([■▲●◆])\s*(\w+)?$", lines[i])
        if not m:
            i += 1
            continue
        word, mark, abbr = m.group(1), m.group(2), m.group(3)
        i += 1
        if not abbr:
            abbr = lines[i]
            i += 1
        parts = []
        while i < len(lines) and not is_bn(lines[i]):
            parts.append(lines[i])
            i += 1
        meaning = []
        while i < len(lines) and is_bn(lines[i]):
            meaning.append(lines[i])
            i += 1
        out.append({
            "id": f"analysis_{len(out) + 1:02d}",
            "word": word,
            "pos": POS_MARK[mark],
            "parts": [p.strip() for p in join(parts).split("+")],
            "meaningBn": join(meaning),
        })
    return out


def parse_multiplying(lines):
    text = " ".join(lines)
    items = re.findall(r"([০-৯]+)\s*([A-Z][A-Za-z\-]+)", text)
    return [{"id": f"mult_{bn_int(n):03d}", "order": bn_int(n), "word": w} for n, w in items]


# --------------------------------------------------------------- chapter 2
def parse_roots(lines):
    families, cur, part, last = [], None, None, None
    i = 0
    while i < len(lines):
        ln = lines[i]
        nxt = lines[i + 1] if i + 1 < len(lines) else ""
        if re.fullmatch(r"[A-Z][A-Z\-]+", ln) and (re.match(r"[০-৯]+টি শব্দ", nxt) or nxt == "চলমান"):
            if nxt != "চলমান":
                cur = {"root": ln, "stated": bn_int(re.match(r"([০-৯]+)", nxt).group(1)),
                       "rootPlusSuffix": [], "prefixPlusRoot": []}
                families.append(cur)
            i += 2
            last = None
            continue
        if ln == "মূল + প্রত্যয়":
            part, last = "rootPlusSuffix", None
        elif ln == "উপসর্গ + মূল":
            part, last = "prefixPlusRoot", None
        elif ln and ln[0] in POS_MARK and cur is not None and part:
            last = {"word": ln[1:].strip(), "pos": POS_MARK[ln[0]], "meaningBn": ""}
            cur[part].append(last)
        elif last is not None and is_bn(ln):
            last["meaningBn"] = join([last["meaningBn"], ln])
        i += 1
    out = []
    for n, f in enumerate(families, 1):
        out.append({
            "id": f"root_{n:02d}",
            "root": f["root"],
            "statedCount": f["stated"],
            "rootPlusSuffix": f["rootPlusSuffix"],
            "prefixPlusRoot": f["prefixPlusRoot"],
        })
    return out


# --------------------------------------------------------------- chapter 3
def parse_memory_hooks(lines):
    starts = [k for k, l in enumerate(lines) if re.match(r"^[০-৯]+[A-Z][A-Za-z\-]+ /", l)]
    out = []
    for si, s in enumerate(starts):
        block = lines[s:starts[si + 1] if si + 1 < len(starts) else len(lines)]
        m = re.match(r"^([০-৯]+)([A-Za-z\-]+) (/[^/]+/) · (.+)$", block[0])
        order, word, ipa, bnp = bn_int(m.group(1)), m.group(2), m.group(3), m.group(4).strip()
        pos_line = next((l for l in block[1:4] if l and l[0] in POS_MARK), None)
        if pos_line is None:
            print("    !! memory hook without POS line:", block[:3])
            continue
        pos = POS_MARK[pos_line[0]]
        k = block.index("অর্থ") + 1
        mean_bn, mean_en = [], []
        while block[k] != "ধ্বনি-সূত্র":
            (mean_bn if is_bn(block[k]) else mean_en).append(block[k])
            k += 1
        k += 1
        split = block[k]
        k += 1
        hook = []
        while not block[k].startswith("→"):
            split += block[k]
            k += 1
        hook.append(block[k].lstrip("→ ").strip())
        k += 1
        story = []
        while not block[k].startswith("মূল সূত্র:"):
            story.append(block[k])
            k += 1
        cue = [block[k][len("মূল সূত্র:"):]]
        k += 1
        while k < len(block) and is_bn(block[k]):
            cue.append(block[k])
            k += 1
        ex_en, ex_bn, syn = [], [], []
        while k < len(block):
            b = block[k]
            if b.startswith("Synonyms:"):
                syn = split_list(join([b[len("Synonyms:"):]] + block[k + 1:]))
                break
            (ex_bn if is_bn(b) else ex_en).append(b)
            k += 1
        out.append({
            "id": f"hook_{order:03d}",
            "order": order,
            "word": word,
            "ipa": ipa,
            "bnPronunciation": bnp,
            "pos": pos,
            "meaningBn": join(mean_bn),
            "meaningEn": join(mean_en),
            "soundSplit": split,
            "soundHookBn": join(hook),
            "storyBn": join(story),
            "keyCueBn": join(cue),
            "exampleEn": join(ex_en),
            "exampleBn": join(ex_bn),
            "synonyms": syn,
        })
    return out


# --------------------------------------------------------------- chapter 4
def parse_confused(lines):
    starts = [k for k, l in enumerate(lines) if re.match(r"^[০-৯]{2,3}\S.*\s+vs\s+", l)]
    out = []
    for si, s in enumerate(starts):
        block = lines[s:starts[si + 1] if si + 1 < len(starts) else len(lines)]
        m = re.match(r"^([০-৯]+)(.+?)\s+vs\s+(.+)$", block[0])
        order = bn_int(m.group(1))
        k = 1
        homophone = False
        if block[k] == "একই উচ্চারণ":
            homophone = True
            k += 1
        sides = []
        for _ in range(2):
            hm = re.match(r"^(.+?)\s*([■▲●◆○])\s*\(([^)]+)\)", block[k])
            if hm is None:
                print("    !! confused pair layout:", order, block[:8])
                break
            word, mark = hm.group(1).strip(), hm.group(2)
            k += 1
            pm = re.match(r"^(/[^/]+/)\s*·\s*(.+)$", block[k])
            ipa, bnp = (pm.group(1), pm.group(2).strip()) if pm else ("", "")
            k += 1
            mean = []
            while not re.match(r"^.+?\s*[■▲●◆○]\s*\([^)]+\)", block[k]) and not block[k].startswith("পার্থক্য:"):
                mean.append(block[k])
                k += 1
            sides.append({"word": word, "pos": POS_MARK[mark], "ipa": ipa,
                          "bnPronunciation": bnp, "meaningBn": join(mean)})
        if len(sides) < 2:
            continue
        diff = []
        while k < len(block) and "›" not in block[k]:
            diff.append(block[k])
            k += 1
        diff_text = join(diff)
        diff_text = diff_text[len("পার্থক্য:"):].strip() if diff_text.startswith("পার্থক্য:") else diff_text
        examples = []
        while k < len(block):
            if "›" in block[k]:
                w, en = block[k].split("›", 1)
                examples.append({"word": w.strip(), "en": [en.strip()], "bn": []})
            elif examples:
                (examples[-1]["bn"] if is_bn(block[k]) else examples[-1]["en"]).append(block[k])
            k += 1
        mnemonic = ""
        mm = re.search(r"মনে রাখুন:\s*(.+)$", diff_text)
        if mm:
            mnemonic = mm.group(1).strip().rstrip("।")
            diff_text = diff_text[:mm.start()].strip()
        for side, ex in zip(sides, examples):
            side["exampleEn"] = join(ex["en"])
            side["exampleBn"] = join(ex["bn"])
        out.append({
            "id": f"pair_{order:03d}",
            "order": order,
            "isHomophone": homophone,
            "a": sides[0],
            "b": sides[1],
            "differenceBn": diff_text,
            "mnemonic": mnemonic,
        })
    return out


# --------------------------------------------------------------- chapter 5
def parse_synant(lines):
    lines = [l for l in lines if l not in ("শব্দ ও অর্থ", "সমার্থক (Synonyms)", "বিপরীত (Antonyms)")]
    heads = [k for k, l in enumerate(lines) if re.match(r"^[a-z][a-z\-' ]*\s[■▲●◆○]\s*\w+$", l)]
    out = []
    for hi, h in enumerate(heads):
        block = lines[h:heads[hi + 1] if hi + 1 < len(heads) else len(lines)]
        m = re.match(r"^(.+?)\s([■▲●◆○])\s*(\w+)$", block[0])
        rest = block[1:]
        mean = [l for l in rest if is_bn(l)]
        eng = [l for l in rest if not is_bn(l)]
        # Re-join wrapped cells: a cell continues while the line ends with a comma.
        cells, buf = [], []
        for l in eng:
            buf.append(l)
            if not l.endswith(","):
                cells.append(join(buf))
                buf = []
        if buf:
            cells.append(join(buf))
        syn = split_list(cells[0]) if cells else []
        ant = split_list(" ".join(cells[1:])) if len(cells) > 1 else []
        out.append({"id": f"synant_{len(out) + 1:03d}", "word": m.group(1), "pos": POS_MARK[m.group(2)],
                    "meaningBn": join(mean), "synonyms": syn, "antonyms": ant})
    return out


def parse_oneword(lines):
    raw = [l for l in lines if l not in ("বর্ণনা", "এক কথায়", "বাংলা")]
    lines = []
    for l in raw:
        # Some rows are flattened onto one line: "description answer বাংলা".
        m = re.match(r"^(.*?)\s*([a-z][a-z\-]*)\s*([\u0980-\u09FF].*)$", l)
        if m and HAS_LATIN.search(l) and " · " not in l:
            if m.group(1):
                lines.append(m.group(1))
            lines.extend([m.group(2), m.group(3)])
        else:
            lines.append(l)
    out, category, buf = [], None, []
    i = 0
    while i < len(lines):
        ln = lines[i]
        hm = re.match(r"^([\u0980-\u09FF ,ও]+?)\s*·\s*([A-Z][A-Za-z ,&]+)$", ln)
        if hm:
            category = {"bn": hm.group(1).strip(), "en": hm.group(2).strip()}
            buf = []
            i += 1
            continue
        if i + 1 < len(lines) and lines[i + 1].startswith("·") and is_bn(ln):
            category = {"bn": ln, "en": lines[i + 1].lstrip("· ").strip()}
            buf = []
            i += 2
            continue
        if is_bn(ln):
            # previous line(s): description..., answer
            if len(buf) >= 2:
                answer = buf[-1]
                out.append({"id": f"oneword_{len(out) + 1:03d}", "descriptionEn": join(buf[:-1]),
                            "answer": answer, "meaningBn": ln,
                            "categoryBn": category["bn"] if category else "",
                            "categoryEn": category["en"] if category else ""})
            elif out:
                out[-1]["meaningBn"] = join([out[-1]["meaningBn"], ln])
            buf = []
        else:
            buf.append(ln)
        i += 1
    return out


# --------------------------------------------------------------- chapter 11
OPT = {"ক": "ka", "খ": "kha", "গ": "ga", "ঘ": "gha"}
LEVELS = {1: "primary", 2: "primary", 3: "primary", 4: "intermediate", 5: "intermediate",
          6: "intermediate", 7: "intermediate", 8: "advanced", 9: "advanced", 10: "advanced"}
QTYPE = {"Synonym": "synonym", "Antonym": "antonym", "One-word Substitution": "one_word",
         "Correct Spelling": "spelling", "Confused Words": "confused", "Collocation": "collocation",
         "Fill in the Blank": "fill_blank", "Word Formation": "word_formation"}


def parse_mcq(lines):
    sets, cur, qtype, q = [], None, None, None
    answer_buf = None
    for ln in lines:
        m = re.match(r"^MCQ সেট ([০-৯]+)$", ln)
        if m:
            cur = {"set": bn_int(m.group(1)), "questions": []}
            sets.append(cur)
            q = None
            continue
        if cur is None:
            continue
        if answer_buf is not None:
            answer_buf += " " + ln
            if len(re.findall(r"[০-৯]+-[কখগঘ]", answer_buf)) >= len(cur["questions"]):
                apply_answers(cur, answer_buf)
                answer_buf = None
            continue
        if ln.startswith("উত্তর (সেট"):
            answer_buf = ln.split(":", 1)[1]
            if len(re.findall(r"[০-৯]+-[কখগঘ]", answer_buf)) >= len(cur["questions"]):
                apply_answers(cur, answer_buf)
                answer_buf = None
            continue
        tm = re.match(r"^(.+?) · ", ln)
        if tm and tm.group(1) in QTYPE:
            qtype = QTYPE[tm.group(1)]
            q = None
            continue
        if re.match(r"^[০-৯]+\.\s*", ln):
            q = {"number": bn_int(re.match(r"^([০-৯]+)", ln).group(1)), "type": qtype,
                 "question": ln.split(".", 1)[1].strip(), "options": {}, "_last": None}
            cur["questions"].append(q)
            continue
        om = re.match(r"^([কখগঘ])\)\s*(.*)$", ln)
        if om and q is not None:
            q["options"][OPT[om.group(1)]] = om.group(2).strip()
            q["_last"] = OPT[om.group(1)]
            continue
        if q is not None:
            if q["_last"]:
                q["options"][q["_last"]] = join([q["options"][q["_last"]], ln])
            else:
                q["question"] = join([q["question"], ln])
    out = []
    for s in sets:
        for q in s["questions"]:
            q.pop("_last", None)
            out.append({"id": f"mcq_s{s['set']:02d}_q{q['number']:02d}", "set": s["set"],
                        "level": LEVELS[s["set"]], **q})
    return out


def apply_answers(cur, text):
    key = {bn_int(n): OPT[o] for n, o in re.findall(r"([০-৯]+)-([কখগঘ])", text)}
    for q in cur["questions"]:
        q["answer"] = key.get(q["number"])


# --------------------------------------------------------------- main
def main(src: str, out_dir: str) -> None:
    pages = load_pages(src)
    os.makedirs(out_dir, exist_ok=True)

    def chapter(no_bn: str, next_no_bn: str | None):
        first = find_page(pages, f"পর্ব · অধ্যায় {no_bn}\n", 6)
        last = (find_page(pages, f"পর্ব · অধ্যায় {next_no_bn}\n", first + 1) - 1) if next_no_bn else max(pages)
        return body_lines(pages, first, last, f"অধ্যায় {no_bn} · ")

    ch1 = chapter("১", "২")
    data = {
        "suffixes": parse_affixes(section(ch1, r"^১\.৩ Suffixes", r"^১\.৪ Prefixes"), "suffix"),
        "prefixes": parse_affixes(section(ch1, r"^১\.৪ Prefixes", r"^১\.৫ "), "prefix"),
        "word_analysis": parse_analysis(section(ch1, r"^১\.৫ ", r"^১\.৬ ")),
        "multiplying_words": parse_multiplying(section(ch1, r"^১\.৬ ", None)),
        "root_families": parse_roots(chapter("২", "৩")),
        "memory_hooks": parse_memory_hooks(chapter("৩", "৪")),
        "confused_pairs": parse_confused(chapter("৪", "৫")),
    }
    ch5 = chapter("৫", "৬")
    data["synonyms_antonyms"] = parse_synant(section(ch5, r"^ক\. Synonyms", r"^খ\. One-word"))
    data["one_word"] = parse_oneword(section(ch5, r"^খ\. One-word", None))
    data["mcq"] = parse_mcq(chapter("১১", None))

    expected = {"suffixes": 48, "prefixes": 48, "word_analysis": 20, "multiplying_words": 120,
                "root_families": 96, "memory_hooks": 100, "confused_pairs": 100,
                "synonyms_antonyms": 144, "one_word": 130, "mcq": 250}
    print("Validation report")
    for name, items in data.items():
        flag = "OK " if len(items) == expected[name] else "!! "
        print(f"  {flag}{name:20s} parsed={len(items):5d} expected={expected[name]}")
        with open(os.path.join(out_dir, f"{name}.json"), "w", encoding="utf-8") as fh:
            json.dump(items, fh, ensure_ascii=False, indent=1)
    words = sum(len(f["rootPlusSuffix"]) + len(f["prefixPlusRoot"]) for f in data["root_families"])
    stated = sum(f["statedCount"] for f in data["root_families"])
    print(f"  root family words parsed={words} stated in headings={stated} (book total 2694)")
    for f in data["root_families"]:
        n = len(f["rootPlusSuffix"]) + len(f["prefixPlusRoot"])
        if n != f["statedCount"]:
            print(f"    !! {f['root']}: parsed {n}, heading says {f['statedCount']}")
    missing = [q["id"] for q in data["mcq"] if not q.get("answer") or len(q["options"]) != 4]
    if missing:
        print("  !! MCQ without answer/4 options:", missing)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])

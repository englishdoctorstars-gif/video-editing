"""Parsers for chapters 6-10 (Collocations, Listen/Read/Write Smart, Paraphrase Bank).

Called from parse_book.py; each function takes the chapter's body lines
(running headers and footers already removed).
"""
import re

from parse_book import BN, POS_MARK, bn_int, is_bn, join

HAS_LATIN = re.compile(r"[A-Za-z]")


# --------------------------------------------------------------- chapter 6
def parse_collocations(lines):
    """Rows: number, phrase (EN), meaning (BN), example (EN), translation (BN).

    Headers are either a power verb in capitals (MAKE) or a Daily Life
    category ("বাসা ও পরিবার · Home and Family"). The number may be glued to
    the phrase ("১০৩extended family").
    """
    out, group_bn, group_en = [], "", ""
    cur = None
    stage = None

    def flush():
        if cur:
            out.append({
                "id": f"coll_{cur['n']:03d}",
                "number": cur["n"],
                "section": "power_verbs" if cur["n"] <= 100 else "daily_life",
                "groupEn": cur["gen"],
                "groupBn": cur["gbn"],
                "phrase": join(cur["phrase"]),
                "meaningBn": join(cur["mean"]),
                "exampleEn": join(cur["exEn"]),
                "exampleBn": join(cur["exBn"]),
            })

    for ln in lines:
        if ln in ("নং", "শব্দজুটি ও অর্থ", "উদাহরণ") or ln.startswith("ক. Power") or ln.startswith("খ. Daily"):
            continue
        if re.fullmatch(r"[A-Z]{2,}", ln):
            flush(); cur = None
            group_en, group_bn = ln, ""
            continue
        gm = re.fullmatch(rf"([{BN} ,ও]+?)\s*·\s*([A-Z][A-Za-z ,&]+)", ln)
        if gm:
            flush(); cur = None
            group_bn, group_en = gm.group(1).strip(), gm.group(2).strip()
            continue
        nm = re.match(r"^([০-৯]{1,3})(.*)$", ln)
        if nm and (not nm.group(2) or HAS_LATIN.match(nm.group(2).strip() or "-")):
            flush()
            cur = {"n": bn_int(nm.group(1)), "gen": group_en, "gbn": group_bn,
                   "phrase": [], "mean": [], "exEn": [], "exBn": []}
            stage = "phrase"
            rest = nm.group(2).strip()
            if rest:
                cur["phrase"].append(rest)
            continue
        if cur is None:
            continue
        bn = is_bn(ln)
        if stage == "phrase":
            if bn:
                stage = "mean"; cur["mean"].append(ln)
            else:
                cur["phrase"].append(ln)
        elif stage == "mean":
            if bn:
                cur["mean"].append(ln)
            else:
                stage = "exEn"; cur["exEn"].append(ln)
        elif stage == "exEn":
            if bn:
                stage = "exBn"; cur["exBn"].append(ln)
            else:
                cur["exEn"].append(ln)
        else:
            cur["exBn"].append(ln)
    flush()
    return out


def parse_mistakes(lines):
    """Rows of three cells: wrong (EN), right (EN), meaning (BN)."""
    lines = [l for l in lines if l not in ("✗ ভুল", "✓ সঠিক", "অর্থ")]
    out, eng = [], []
    for ln in lines:
        if is_bn(ln):
            if len(eng) >= 2:
                # The right form is the last English line; wrapped wrong forms come first.
                half = len(eng) // 2
                wrong, right = (eng[:half], eng[half:]) if len(eng) % 2 == 0 else (eng[:-1], eng[-1:])
                out.append({"id": f"mistake_{len(out) + 1:02d}", "wrong": join(wrong), "right": join(right), "meaningBn": ln})
            elif out:
                out[-1]["meaningBn"] = join([out[-1]["meaningBn"], ln])
            eng = []
        else:
            eng.append(ln)
    return out


# --------------------------------------------------------------- chapter 7
def parse_listening_words(lines):
    out, section, sec_bn, sec_desc = [], 0, "", ""
    i = 0
    while i < len(lines):
        ln = lines[i]
        sm = re.match(r"^Section ([1-4]) · (.+)$", ln)
        if sm:
            section, sec_bn = int(sm.group(1)), sm.group(2).strip()
            sec_desc = lines[i + 1] if i + 1 < len(lines) and is_bn(lines[i + 1]) else ""
            i += 2 if sec_desc else 1
            continue
        wm = re.match(r"^([a-z][a-z\-' ]*?)\s*([■▲●◆○])\s*(\w+)$", ln)
        if wm and section:
            ipa = lines[i + 1] if i + 1 < len(lines) and lines[i + 1].startswith("/") else ""
            k = i + (2 if ipa else 1)
            mean = []
            # Meanings may mix scripts ("গাইডসহ (guided tour)"), so any Bengali letter counts.
            while k < len(lines) and re.search(f"[{BN}]", lines[k]) and not lines[k].startswith("Section"):
                mean.append(lines[k]); k += 1
            out.append({"id": f"listen_{len(out) + 1:03d}", "section": section, "sectionBn": sec_bn,
                        "sectionDescBn": sec_desc, "word": wm.group(1).strip(), "pos": POS_MARK[wm.group(2)],
                        "ipa": ipa, "meaningBn": join(mean)})
            i = k
            continue
        i += 1
    return out


def parse_pairs_en_bn(lines, prefix):
    """Alternating English term / Bengali meaning (Maps and Directions)."""
    lines = [l for l in lines if l not in ("শব্দ", "অর্থ")]
    out, eng = [], []
    for ln in lines:
        if is_bn(ln):
            if eng:
                out.append({"id": f"{prefix}_{len(out) + 1:02d}", "term": join(eng), "meaningBn": ln})
            elif out:
                out[-1]["meaningBn"] = join([out[-1]["meaningBn"], ln])
            eng = []
        else:
            eng.append(ln)
    return out


NUMBER_TIP_TITLES = ["টেলিফোন নম্বর", "তারিখ", "সময় ও টাকা", "-teen না -ty?", "বানান বলা", "শব্দসীমা", "উত্তর বদলে যাওয়া"]


def parse_number_tips(lines):
    out, cur = [], None
    for ln in lines:
        if ln in NUMBER_TIP_TITLES:
            cur = {"id": f"tip_{len(out) + 1:02d}", "titleBn": ln, "body": []}
            out.append(cur)
        elif cur:
            cur["body"].append(ln)
    for t in out:
        # Keep worked examples ("→") on their own lines.
        text = join(t.pop("body"))
        t["bodyBn"] = re.sub(r"। +", "।\n", text).strip()
    return out


def parse_spelling_traps(lines):
    lines = [l for l in lines if l not in ("শব্দ", "যেখানে ভুল হয়", "অর্থ")]
    out, i = [], 0
    while i < len(lines):
        word = lines[i]
        if is_bn(word) or not re.fullmatch(r"[A-Za-z][a-z\-' ]*", word):
            i += 1
            continue
        trap = [lines[i + 1]] if i + 1 < len(lines) else []
        k = i + 2
        while k < len(lines) and HAS_LATIN.search(lines[k]) and not re.fullmatch(r"[a-z][a-z\-' ]*", lines[k]):
            trap.append(lines[k]); k += 1
        meaning = lines[k] if k < len(lines) and is_bn(lines[k]) and not HAS_LATIN.search(lines[k]) else ""
        out.append({"id": f"spell_{len(out) + 1:02d}", "word": word, "trapBn": join(trap), "meaningBn": meaning})
        i = k + (1 if meaning else 0)
    return out


def parse_signposts(lines):
    lines = [l for l in lines if l not in ("সংকেত-শব্দ", "কী বোঝায়", "বাংলা")]
    out, eng, bn = [], [], []
    for ln in lines:
        if is_bn(ln):
            bn.append(ln)
            if len(bn) == 2:
                out.append({"id": f"signpost_{len(out) + 1:02d}", "signpost": join(eng), "functionBn": bn[0], "meaningBn": bn[1]})
                eng, bn = [], []
        else:
            eng.append(ln)
    return out


# --------------------------------------------------------------- chapter 8
def parse_passages(lines):
    out, subject_no, subject = [], 0, ""
    i = 0
    while i < len(lines):
        ln = lines[i]
        sm = re.match(r"^বিষয় ([০-৯]+) · (.+)$", ln)
        if sm:
            subject_no, subject = bn_int(sm.group(1)), sm.group(2).strip()
            i += 1
            continue
        tm = re.match(r"^Topic ([০-৯]+)\s*(.*)$", ln)
        if tm and subject_no:
            topic = bn_int(tm.group(1))
            title = tm.group(2).strip()
            k = i + 1
            if not title:
                title = lines[k]; k += 1
            body = []
            while k < len(lines) and lines[k] != "Vocab Box":
                body.append(lines[k]); k += 1
            k += 1
            if k < len(lines) and lines[k].startswith("শব্দ · উচ্চারণ"):
                k += 1
            vocab = []
            while k < len(lines) and lines[k] and lines[k][0] in POS_MARK:
                mark, word = lines[k][0], lines[k][1:].strip()
                ipa = lines[k + 1] if k + 1 < len(lines) and lines[k + 1].startswith("/") else ""
                k += 2 if ipa else 1
                mean = []
                while k < len(lines) and is_bn(lines[k]) and not lines[k].startswith("বিষয়"):
                    mean.append(lines[k]); k += 1
                vocab.append({"word": word, "pos": POS_MARK[mark], "ipa": ipa, "meaningBn": join(mean)})
            out.append({"id": f"read_{subject_no:02d}_{topic:02d}", "subjectNo": subject_no, "subject": subject,
                        "topicNo": topic, "title": title, "bodyBn": join(body), "vocab": vocab})
            i = k
            continue
        i += 1
    return out


# --------------------------------------------------------------- chapter 9
def parse_paraphrases(lines):
    text = " ".join(lines)
    topics = list(re.finditer(rf"([০-৯]{{2}}) · ([A-Z][A-Za-z ,&]+?) ([{BN}][{BN} ,ও]*?) · ([০-৯]+)টি", text))
    out = []
    for ti, t in enumerate(topics):
        chunk = text[t.end():topics[ti + 1].start() if ti + 1 < len(topics) else len(text)]
        topic_no = bn_int(t.group(1))
        entries = re.finditer(
            r"(?:^|\s)([০-৯]{1,3})\s+(.+?)\s+\(([^()]{1,25})\)\s+—\s+(.+?)\s+≈\s+(.+?)\s+·\s+≈\s+(.+?)(?=\s+[০-৯]{1,3}\s+\S|\s*$)",
            chunk,
        )
        for e in entries:
            out.append({
                "id": f"para_{topic_no:02d}_{bn_int(e.group(1)):03d}",
                "topicNo": topic_no,
                "topicEn": t.group(2).strip(),
                "topicBn": t.group(3).strip(),
                "number": bn_int(e.group(1)),
                "collocation": e.group(2).strip(),
                "structure": e.group(3).strip(),
                "meaningBn": e.group(4).strip(),
                "paraphrases": [e.group(5).strip(), e.group(6).strip()],
            })
    return out


# --------------------------------------------------------------- chapter 10
WRITING_SECTIONS = [
    ("task1", "ট্রেন্ড বা প্রবণতা বোঝানোর শব্দ", "Trends"),
    ("task1", "পরিবর্তনের মাত্রা", "Degree of change"),
    ("task1", "তুলনা ও অনুপাত", "Comparisons and proportions"),
    ("task1", "প্রক্রিয়া ও মানচিত্র", "Process and map"),
    ("task1", "Overview বাক্যের সূচনা", "Overview openers"),
    ("task2", "মতামত প্রকাশ", "Giving an opinion"),
    ("task2", "যুক্তি উপস্থাপন", "Presenting arguments"),
    ("task2", "বিপরীত যুক্তি", "Concession"),
    ("task2", "উদাহরণ", "Examples"),
    ("task2", "কারণ ও ফল", "Cause and effect"),
    ("task2", "উপসংহার", "Conclusion"),
    ("linking", "বাক্য ও ভাবনা জোড়ার শব্দ", "Linking words"),
    ("academic", "সাধারণ থেকে একাডেমিক ভাষায়", "From simple to academic"),
    ("mistakes", "Writing-এর প্রচলিত ভুল", "Common mistakes"),
]


def build_writing(tables, topic_lines):
    merged = []
    for t in tables:
        t["rows"] = [r for r in t["rows"] if not re.match(r"^[কখগঘঙ]\. ", r[0])]
        if merged and ("Write Smart" in t["title"]) and t["columns"] == merged[-1]["columns"]:
            merged[-1]["rows"].extend(t["rows"])
        else:
            merged.append({"columns": t["columns"], "rows": list(t["rows"])})
    assert len(merged) == len(WRITING_SECTIONS), [m["columns"] for m in merged]
    sections = []
    for (group, title_bn, title_en), m in zip(WRITING_SECTIONS, merged):
        sections.append({"id": f"write_{len(sections) + 1:02d}", "group": group, "titleBn": title_bn,
                         "titleEn": title_en, "columns": m["columns"], "rows": m["rows"]})
    return {"sections": sections, "topicVocab": parse_topic_vocab(topic_lines)}


def parse_topic_vocab(lines):
    """Two-column flowing list: "english phrase বাংলা অর্থ", Bengali may wrap to the next line."""
    cats, pending_en = [], None
    for ln in lines:
        cm = re.fullmatch(rf"([{BN} ,ও]+?)\s*·\s*([A-Z][A-Za-z ,&]+)", ln)
        if cm:
            cats.append({"categoryBn": cm.group(1).strip(), "categoryEn": cm.group(2).strip(), "items": []})
            continue
        if not cats:
            continue
        items = cats[-1]["items"]
        m = re.match(rf"^([A-Za-z][A-Za-z\-' ]*?)\s+([{BN}].*)$", ln)
        if m:
            items.append({"en": m.group(1).strip(), "bn": m.group(2).strip()})
            pending_en = None
        elif not HAS_LATIN.search(ln):
            if pending_en:
                items.append({"en": pending_en, "bn": ln}); pending_en = None
            elif items:
                items[-1]["bn"] = join([items[-1]["bn"], ln])
        else:
            pending_en = ln.strip()
    return cats

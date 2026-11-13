"""Copy the rendered ad screens into marketing/ad-screens/ as 24-bit PNGs.

    ./gradlew :app:recordPaparazziDebug --tests '*AdScreensTest*'
    python3 -I tools/release/collect_ad_screens.py
"""
import glob
import os
import re

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "app/src/test/snapshots/images")
DST = os.path.join(ROOT, "marketing/ad-screens")
os.makedirs(DST, exist_ok=True)
for f in sorted(glob.glob(os.path.join(SRC, "*AdScreensTest_*.png"))):
    m = re.search(r"_(\d\d_[a-z0-9_]+)\.png$", f)
    if not m:
        continue
    Image.open(f).convert("RGB").save(os.path.join(DST, m.group(1) + ".png"), "PNG", optimize=True)
    print(m.group(1))

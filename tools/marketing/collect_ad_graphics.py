"""Copy rendered ad graphics (end cards, how-to-buy, concept-2 frames) into marketing/ad-graphics/.

    ./gradlew :app:recordPaparazziDebug --tests '*AdGraphics*'
    python3 -I tools/marketing/collect_ad_graphics.py
"""
import glob
import os
import re

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "app/src/test/snapshots/images")
DST = os.path.join(ROOT, "marketing/ad-graphics")
os.makedirs(DST, exist_ok=True)
for f in sorted(glob.glob(os.path.join(SRC, "*AdGraphics*.png"))):
    name = re.search(r"Test_[A-Za-z0-9]+_(.+)\.png$", f).group(1)
    Image.open(f).convert("RGB").save(os.path.join(DST, name + ".png"), "PNG", optimize=True)
    print(name)

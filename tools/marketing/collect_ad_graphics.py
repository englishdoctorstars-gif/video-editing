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
os.makedirs(os.path.join(DST, "concept1"), exist_ok=True)
for f in sorted(glob.glob(os.path.join(SRC, "*Concept1FramesTest*.png"))):
    name = re.search(r"_(c1_\d\d_[a-z]+)\.png$", f).group(1)
    Image.open(f).convert("RGB").save(os.path.join(DST, "concept1", name + ".png"), "PNG", optimize=True)
    print("concept1/" + name)
os.makedirs(os.path.join(DST, "concept3"), exist_ok=True)
for f in sorted(glob.glob(os.path.join(SRC, "*Concept3LayersTest*.png"))):
    name = re.search(r"Test_[A-Za-z0-9]+_(.+)\.png$", f).group(1)
    img = Image.open(f).convert("RGBA")
    if name.startswith("bg_"):
        img = img.convert("RGB")  # backgrounds are opaque; other layers keep transparency
    img.save(os.path.join(DST, "concept3", name + ".png"), "PNG", optimize=True)
    print("concept3/" + name)
os.makedirs(os.path.join(DST, "combo"), exist_ok=True)
for f in sorted(glob.glob(os.path.join(SRC, "*ComboLayersTest*.png"))):
    name = re.search(r"Test_[A-Za-z0-9]+_(.+)\.png$", f).group(1)
    img = Image.open(f).convert("RGBA")
    if name.startswith("bg_") or name == "endcard":
        img = img.convert("RGB")
    img.save(os.path.join(DST, "combo", name + ".png"), "PNG", optimize=True)
    print("combo/" + name)
for f in sorted(glob.glob(os.path.join(SRC, "*AdGraphics*.png"))):
    name = re.search(r"Test_[A-Za-z0-9]+_(.+)\.png$", f).group(1)
    Image.open(f).convert("RGB").save(os.path.join(DST, name + ".png"), "PNG", optimize=True)
    print(name)

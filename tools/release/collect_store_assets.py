"""Copy rendered store graphics into the fastlane / Play Console layout.

    ./gradlew :app:recordPaparazziDebug --tests '*store*'
    python3 -I tools/release/collect_store_assets.py

Screenshots and the feature graphic are flattened to 24-bit RGB PNG because Play
rejects transparency there; the icon keeps 32-bit PNG as Play allows.
"""
import glob
import os
import shutil

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "app/src/test/snapshots/images")
META = os.path.join(ROOT, "fastlane/metadata/android")
LOCALES = {"bn": "bn-BD", "en": "en-US"}


def find(pattern):
    hits = glob.glob(os.path.join(SRC, pattern))
    if len(hits) != 1:
        raise SystemExit(f"expected one file for {pattern}, found {hits}")
    return hits[0]


def save_rgb(src, dst, size):
    img = Image.open(src)
    assert img.size == size, (src, img.size, size)
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    img.convert("RGB").save(dst, "PNG", optimize=True)


for prefix, locale in LOCALES.items():
    images = os.path.join(META, locale, "images")
    shutil.rmtree(os.path.join(images, "phoneScreenshots"), ignore_errors=True)
    os.makedirs(images, exist_ok=True)
    icon = Image.open(find("*StoreIconTest_icon*.png"))
    assert icon.size == (512, 512)
    icon.save(os.path.join(images, "icon.png"), "PNG", optimize=True)
    save_rgb(find("*StoreFeatureGraphicTest_featureGraphic*.png"), os.path.join(images, "featureGraphic.png"), (1024, 500))
    shots = sorted(glob.glob(os.path.join(SRC, f"*StoreScreenshotsTest_*_{prefix}_*.png")))
    assert len(shots) == 8, shots
    for s in shots:
        name = s.rsplit(f"_{prefix}_", 1)[1]
        save_rgb(s, os.path.join(images, "phoneScreenshots", name), (1080, 1920))
    print(locale, "icon, featureGraphic,", len(shots), "phone screenshots")

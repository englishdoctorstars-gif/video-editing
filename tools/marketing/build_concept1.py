"""Builds the ~30-second 1:1 ad for concept 1 "চায়ের দোকানের চ্যালেঞ্জ".

    python3 -I tools/marketing/build_concept1.py

Frames: marketing/ad-graphics/concept1/ (rendered by Concept1FramesTest).
Output: marketing/videos/concept1_tea_stall_30s_1x1.mp4 (1080×1080, 30 fps, H.264 + AAC).
Sound effects only (pop for speech bubbles, flip, ding); add music or a voice-over in CapCut.
To use AI footage later, replace a frame with a clip of the same length in the editor.
"""
import os
import subprocess

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = os.path.join(ROOT, "marketing/ad-graphics/concept1")
OUT = os.path.join(ROOT, "marketing/videos/concept1_tea_stall_30s_1x1.mp4")
FPS, SIZE, FADE = 30, 1080, 0.3

# (frame, seconds, slow zoom, sound at start)
TIMELINE = [
    ("c1_01_hook", 2.8, True, "pop"),
    ("c1_02_forgot", 2.7, True, "pop"),
    ("c1_03_tip", 1.8, True, "pop"),
    ("c1_04_card", 2.0, False, None),
    ("c1_05_story", 2.3, False, "flip"),
    ("c1_06_office", 2.6, True, "pop"),
    ("c1_07_formula", 2.6, True, "ding"),
    ("c1_08_montage", 4.2, True, None),
    ("c1_09_nextday", 1.2, False, None),
    ("c1_10_question", 2.0, True, "pop"),
    ("c1_11_answer", 2.2, True, "ding"),
    ("c1_12_endcard", 3.6, True, None),
]

SFX = {
    "pop": "sine=f=880:d=0.07,afade=t=out:st=0:d=0.07,volume=3.0",
    "flip": "anoisesrc=d=0.18:c=pink:a=0.6,highpass=f=1500,afade=t=in:d=0.05,afade=t=out:st=0.08:d=0.1",
    "ding": "sine=f=1568:d=0.6,afade=t=out:st=0.05:d=0.55,volume=3.5",
}

inputs, vf, af, cues = [], [], [], []
start = 0.0
prev = None
for i, (name, dur, zoom, sfx) in enumerate(TIMELINE):
    seg = dur + (FADE if i else 0)  # later frames overlap the previous one during the fade
    inputs += ["-loop", "1", "-framerate", str(FPS), "-t", f"{seg:.2f}", "-i", os.path.join(SRC, name + ".png")]
    frames = int(seg * FPS)
    z = (f"zoompan=z='1+0.04*on/{frames}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={SIZE}x{SIZE}:fps={FPS},"
         if zoom else "")
    vf.append(f"[{i}:v]scale={SIZE}:{SIZE},setsar=1,{z}format=yuv420p,fps={FPS},settb=1/{FPS}[s{i}]")
    if prev is None:
        prev, length = f"s{i}", seg
    else:
        vf.append(f"[{prev}][s{i}]xfade=transition=fade:duration={FADE}:offset={length - FADE:.2f}[v{i}]")
        start = length - FADE
        length = start + seg
        prev = f"v{i}"
    if sfx:
        cues.append((sfx, (start if i else 0.0) + 0.15))

total = length
af.append(f"anullsrc=r=44100:cl=stereo,atrim=0:{total:.2f}[bed]")
labels = ["[bed]"]
for k, (kind, at) in enumerate(cues):
    ms = int(at * 1000)
    af.append(f"{SFX[kind]},aformat=sample_rates=44100:channel_layouts=stereo,adelay={ms}|{ms}[x{k}]")
    labels.append(f"[x{k}]")
af.append("".join(labels) + f"amix=inputs={len(labels)}:duration=first:normalize=0[a]")

cmd = ["ffmpeg", "-y", "-loglevel", "error", *inputs, "-filter_complex", ";".join(vf + af),
       "-map", f"[{prev}]", "-map", "[a]", "-t", f"{total:.2f}", "-r", str(FPS),
       "-c:v", "libx264", "-preset", "medium", "-crf", "20", "-pix_fmt", "yuv420p", "-movflags", "+faststart",
       "-c:a", "aac", "-b:a", "128k", OUT]
subprocess.run(cmd, check=True)
print(f"{OUT} ({total:.1f}s)")

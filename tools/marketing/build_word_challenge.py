"""Builds the 15-second "শব্দ চ্যালেঞ্জ" Reels ad (concept 2) from marketing/ad-graphics/ frames.

    python3 -I tools/marketing/build_word_challenge.py

Output: marketing/videos/concept2_word_challenge_15s_9x16.mp4 (1080×1920, 30 fps, H.264 + AAC).
The audio is a countdown tick and a "ding"; add music or a voice-over on top in CapCut/Premiere.
"""
import os
import subprocess

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
G = os.path.join(ROOT, "marketing/ad-graphics")
OUT = os.path.join(ROOT, "marketing/videos/concept2_word_challenge_15s_9x16.mp4")
FPS, W, H, FADE = 30, 1080, 1920, 0.3

# (image, seconds on screen, slow zoom?)  The three countdown frames are joined with hard cuts.
COUNTDOWN = [("c2_01_count3", 1.0), ("c2_02_count2", 1.0), ("c2_03_count1", 1.0)]
SEGMENTS = [("c2_04_options", 2.0, True), ("c2_05_story", 3.0, True), ("c2_06_phone", 2.5, True),
            ("c2_07_answer", 1.5, False), ("endcard_9x16", 3.0, True)]

inputs, filters = [], []
idx = 0
for name, dur in COUNTDOWN:
    inputs += ["-loop", "1", "-framerate", str(FPS), "-t", str(dur), "-i", os.path.join(G, name + ".png")]
    filters.append(f"[{idx}:v]scale={W}:{H},setsar=1,format=yuv420p,fps={FPS},settb=1/{FPS}[cd{idx}]")
    idx += 1
filters.append("".join(f"[cd{i}]" for i in range(len(COUNTDOWN))) + f"concat=n={len(COUNTDOWN)}:v=1:a=0,fps={FPS},settb=1/{FPS}[v0]")

length = sum(d for _, d in COUNTDOWN)
prev = "v0"
for k, (name, dur, zoom) in enumerate(SEGMENTS, start=1):
    # Each later segment overlaps the previous one by FADE seconds.
    seg_len = dur + FADE
    inputs += ["-loop", "1", "-framerate", str(FPS), "-t", f"{seg_len:.2f}", "-i", os.path.join(G, name + ".png")]
    frames = int(seg_len * FPS)
    z = f"zoompan=z='1+0.035*on/{frames}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={W}x{H}:fps={FPS}," if zoom else ""
    filters.append(f"[{idx}:v]scale={W}:{H},setsar=1,{z}format=yuv420p,fps={FPS},settb=1/{FPS}[s{k}]")
    # The fade starts FADE seconds before the previous stream ends.
    filters.append(f"[{prev}][s{k}]xfade=transition=fade:duration={FADE}:offset={length - FADE:.2f}[v{k}]")
    length = length - FADE + seg_len
    prev = f"v{k}"
    idx += 1

total = length
# Audio: three countdown ticks, a ding on the answer reveal, on a silent bed.
answer_at = sum(d for _, d in COUNTDOWN) + sum(d for _, d, _ in SEGMENTS[:3])
audio = [
    f"anullsrc=r=44100:cl=stereo,atrim=0:{total:.2f}[bed]",
    "sine=f=1000:d=0.08,volume=3.0,aformat=channel_layouts=stereo[t0]",
    "sine=f=1000:d=0.08,volume=3.0,aformat=channel_layouts=stereo,adelay=1000|1000[t1]",
    "sine=f=1000:d=0.08,volume=3.0,aformat=channel_layouts=stereo,adelay=2000|2000[t2]",
    f"sine=f=1568:d=0.6,afade=t=out:st=0.05:d=0.55,volume=3.5,aformat=channel_layouts=stereo,adelay={int(answer_at*1000)}|{int(answer_at*1000)}[ding]",
    "[bed][t0][t1][t2][ding]amix=inputs=5:duration=first:normalize=0[a]",
]
graph = ";".join(filters + audio)
cmd = ["ffmpeg", "-y", "-loglevel", "error", *inputs, "-filter_complex", graph, "-map", f"[{prev}]", "-map", "[a]",
       "-t", f"{total:.2f}", "-r", str(FPS), "-c:v", "libx264", "-preset", "medium", "-crf", "20", "-pix_fmt", "yuv420p",
       "-movflags", "+faststart", "-c:a", "aac", "-b:a", "128k", OUT]
subprocess.run(cmd, check=True)
print(f"{OUT} ({total:.1f}s)")

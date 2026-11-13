"""Builds the "Vocabsolution Pro" sales ad (about 43 seconds) with sound effects only — no music.

    python3 -I tools/marketing/build_pro_ad.py              # 1:1  (1080×1080)
    python3 -I tools/marketing/build_pro_ad.py --vertical   # 9:16 (1080×1920, Reels safe zone)

Story: hook → reframe → four methods (dissect, root, story, exam) → proof (count-up) →
author → offer (৭৫০ টাকার বই → অ্যাপে ২০০ টাকা) → WhatsApp call to action.
Layers: marketing/ad-graphics/pro[916]/ (ProAdLayersTest / ProAdVerticalLayersTest).
Output: marketing/videos/vocabsolution_pro_ad_43s_{1x1,9x16}.mp4 and the shared
sound-effects stem vocabsolution_pro_ad_sfx.wav.
"""
import os
import subprocess
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import audio_synth as A  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
VERTICAL = "--vertical" in sys.argv
L = os.path.join(ROOT, "marketing/ad-graphics/pro916" if VERTICAL else "marketing/ad-graphics/pro")
OUT = os.path.join(ROOT, "marketing/videos/vocabsolution_pro_ad_43s_" + ("9x16" if VERTICAL else "1x1") + ".mp4")
WAV = os.path.join(ROOT, "marketing/videos/vocabsolution_pro_ad_sfx.wav")
TOTAL, FPS = 43.0, 30
W, H = (1080, 1920) if VERTICAL else (1080, 1080)
# Pixel position of the CTA button centre, kept fixed while it pulses.
PULSE_ANCHOR = (540, 751) if VERTICAL else (540, 524)

# Scene changes: a navy/gold wipe sweeps across at each of these times; the new background
# appears under it at mid-sweep.
WIPES = [3.3, 7.3, 11.5, 15.7, 19.9, 24.1, 28.5, 32.3, 36.9]
SCENE_BG = ["bg_navy", "bg_teal", "bg_navy", "bg_plum", "bg_navy", "bg_teal", "bg_plum", "bg_ink", "bg_navy"]
ENDS = WIPES[1:] + [TOTAL]

# (layer, in, out, animation, sound)
LAYERS = [
    ("hook_cloud", 0.1, 3.4, "zoom_in", "whoosh_soft"),
    ("hook_1", 0.6, 3.4, "up_small", "whoosh"),
    ("hook_2", 1.6, 3.4, "up_small", "buzz"),
    ("hook_strike", 2.5, 3.4, "fade", "slash"),

    ("re_1", 3.75, 7.4, "up_small", None),
    ("re_2", 4.3, 7.4, "pop", "impact"),
    ("re_logo", 5.1, 7.4, "pop", "shimmer"),

    ("m1_title", 7.75, 11.6, "down", None),
    ("m1_word", 8.1, 11.6, "fade", "pop"),
    ("m1_chip0", 8.8, 11.6, "up_small", "note:72"),
    ("m1_chip1", 9.15, 11.6, "up_small", "note:76"),
    ("m1_chip2", 9.5, 11.6, "up_small", "note:79"),
    ("m1_chip3", 9.85, 11.6, "up_small", "note:84"),
    ("m1_meaning", 10.5, 11.6, "up_small", "chime"),

    ("m2_title", 11.95, 15.8, "down", None),
    ("m2_core", 12.3, 15.8, "pop", "impact_soft"),
    ("m2_pair0", 12.8, 15.8, "pop", "sparkle"),
    ("m2_pair1", 13.15, 15.8, "pop", "sparkle"),
    ("m2_pair2", 13.5, 15.8, "pop", "sparkle"),
    ("m2_pair3", 13.85, 15.8, "pop", "sparkle"),
    ("m2_badge", 14.5, 15.8, "up_small", "chime"),

    ("m3_title", 16.15, 20.0, "down", None),
    ("m3_phone", 16.3, 20.0, "float", "whoosh"),
    ("m3_card0", 17.0, 20.0, "right", "whoosh_soft"),
    ("m3_card1", 17.8, 20.0, "right", "whoosh_soft"),
    ("m3_card2", 18.6, 20.0, "right", "chime"),

    ("m4_title", 20.35, 24.2, "down", None),
    ("m4_phone", 20.5, 24.2, "float", "whoosh"),
    ("m4_card0", 21.2, 24.2, "left", "chime"),
    ("m4_card1", 22.3, 24.2, "left", "tick_tock"),

    ("st_title", 24.55, 28.6, "down", None),
    ("st_tiles", 24.9, 28.6, "up_small", "whoosh_soft"),
] + [(f"st_num{k:02d}", 25.4 + (k - 1) * 0.09, (25.4 + k * 0.09) if k < 12 else 28.6, "count", "tick" if k < 12 else "chime")
     for k in range(1, 13)] + [
    ("au_card", 28.95, 32.4, "up", "impact_soft"),

    ("of_anchor", 32.75, 37.0, "down", "whoosh_soft"),
    ("of_price", 34.0, 37.0, "pop", "cash"),
    ("of_points", 35.2, 37.0, "up_small", "pop"),

    ("cta_top", 37.35, TOTAL, "down", "shimmer"),
    ("cta_button", 37.8, TOTAL, "pulse", "impact"),
    ("cta_pay", 38.7, TOTAL, "up_small", "pop"),
]
PULSE_PERIOD = 1.2


def ease(t_in, dur=0.55):
    return f"pow(1-min(1,max(0,(t-{t_in})/{dur})),3)"


def position(anim, t_in):
    e = ease(t_in)
    return {
        "left": (f"-620*{e}", "0"),
        "right": (f"620*{e}", "0"),
        "up": ("0", f"700*{ease(t_in, 0.7)}"),
        "float": ("0", f"700*{ease(t_in, 0.7)}+9*sin(2*PI*(t-{t_in})/3)"),
        "down": ("0", f"-110*{e}"),
        "up_small": ("0", f"60*{e}"),
        "pop": ("(W-w)/2", "(H-h)/2"),
        "zoom_in": ("(W-w)/2", "(H-h)/2"),
        "pulse": (f"{PULSE_ANCHOR[0]}*(1-w/W)", f"{PULSE_ANCHOR[1]}*(1-h/H)"),
    }.get(anim, ("0", "0"))


def layer_input(name, dur):
    return ["-loop", "1", "-framerate", str(FPS), "-t", f"{dur:.2f}", "-i", os.path.join(L, name + ".png")]


def build_video(silent_mp4):
    frames = int(TOTAL * FPS)
    inputs = ["-loop", "1", "-framerate", str(FPS), "-t", str(TOTAL), "-i", os.path.join(L, "bg_ink.png")]
    graph = [f"[0:v]scale={W}:{H},zoompan=z='1+0.06*on/{frames}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={W}x{H}:fps={FPS},"
             f"format=rgba,settb=1/{FPS}[base]"]
    plan = []  # (name, in, out, anim)
    # Particles drift over the first background, then each scene gets its own background + particles.
    plan.append(("particles", 0.0, WIPES[0] + 0.3, "drift"))
    for i, w in enumerate(WIPES):
        plan.append((SCENE_BG[i], w + 0.25, ENDS[i] + 0.3 if ENDS[i] < TOTAL else TOTAL, "bg"))
        plan.append(("particles", w + 0.25, ENDS[i] + 0.3 if ENDS[i] < TOTAL else TOTAL, "drift"))
    plan.sort(key=lambda p: p[1])
    content = [(n, a, b, an) for n, a, b, an, _ in LAYERS]
    if VERTICAL:
        content.append(("brand_line", WIPES[0] + 0.3, WIPES[-1] + 0.2, "fade"))
    # Order: every scene's background before that scene's content; wipes always on top.
    stack = sorted(plan + content, key=lambda p: (p[1], 0 if p[3] in ("bg", "drift") else 1))
    stack += [("wipe", w, w + 0.55, "wipe") for w in WIPES]

    prev = "base"
    for i, (name, t_in, t_out, anim) in enumerate(stack, start=1):
        dur = t_out - t_in + (0.0 if anim == "count" else 0.1)
        inputs += layer_input(name, dur)
        chain = f"[{i}:v]format=rgba,setpts=PTS-STARTPTS+{t_in}/TB"
        if anim == "bg":
            n = int(dur * FPS)
            chain += (f",zoompan=z='1+0.05*on/{n}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={W}x{H}:fps={FPS}"
                      f",setpts=PTS-STARTPTS+{t_in}/TB,format=rgba")
        elif anim == "pop":
            chain += f",scale=w='trunc(iw*(1+0.18*pow(1-min(1,max(0,(t-{t_in})/0.32)),2))/2)*2':h=-2:eval=frame"
        elif anim == "zoom_in":
            chain += f",scale=w='trunc(iw*(1.12-0.12*min(1,(t-{t_in})/3.3))/2)*2':h=-2:eval=frame"
        elif anim == "pulse":
            p = f"(1+0.16*pow(1-min(1,max(0,(t-{t_in})/0.32)),2)+0.045*max(0,sin(2*PI*(t-{t_in}-0.6)/{PULSE_PERIOD})))"
            chain += f",scale=w='trunc(iw*{p}/2)*2':h=-2:eval=frame"
        if anim not in ("wipe", "count"):
            chain += f",fade=t=in:st={t_in}:d={0.05 if anim == 'bg' else 0.3}:alpha=1"
            if t_out < TOTAL and anim != "bg":
                chain += f",fade=t=out:st={t_out - 0.2:.2f}:d=0.2:alpha=1"
        graph.append(chain + f"[l{i}]")
        if anim == "wipe":
            x, y = f"W-2*W*(0.5-0.5*cos(PI*min(1,max(0,(t-{t_in})/0.5))))", "0"
        elif anim == "drift":
            x, y = "0", f"-mod((t-{t_in})*14,H)"
        else:
            x, y = position(anim, t_in)
        graph.append(f"[{prev}][l{i}]overlay=x='{x}':y='{y}':eval=frame:eof_action=pass[v{i}]")
        prev = f"v{i}"
    graph.append(f"[{prev}]format=yuv420p[out]")
    script = OUT.replace(".mp4", "_graph.txt")
    with open(script, "w") as f:
        f.write(";".join(graph))
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", *inputs, "-filter_complex_script", script, "-map", "[out]",
                    "-t", str(TOTAL), "-r", str(FPS), "-c:v", "libx264", "-preset", "medium", "-crf", "18",
                    "-pix_fmt", "yuv420p", silent_mp4], check=True)
    os.remove(script)


# ---- sound effects only (no music) ---------------------------------------------------------

def gap(sec):
    return np.zeros(int(sec * A.SR), np.float32)


def cash():
    """Cash-register 'ka-ching': a short mechanical click, then two bright bells."""
    out = np.zeros(int(1.2 * A.SR), np.float32)
    A.place(out, A.tick(0.55), 0.0)
    A.place(out, A.tick(0.45), 0.05)
    A.place(out, A.chime(96, 0.42), 0.09)
    A.place(out, A.chime(100, 0.38), 0.17)
    A.place(out, A.impact(0.35), 0.0)
    return out


def sound(kind):
    if kind.startswith("note:"):
        return A.chime(int(kind[5:]), 0.3)
    return {
        "whoosh": lambda: A.whoosh(0.45, 0.42),
        "whoosh_soft": lambda: A.whoosh(0.4, 0.26),
        "pop": lambda: A.pop(0.32),
        "buzz": lambda: A.buzz(0.36),
        "impact": lambda: A.impact(0.55),
        "impact_soft": lambda: A.impact(0.34),
        "shimmer": lambda: A.shimmer(0.38),
        "sparkle": lambda: A.shimmer(0.18),
        "chime": lambda: A.chime(84, 0.38),
        "tick": lambda: A.tick(0.22),
        "cash": cash,
        "slash": lambda: np.concatenate([A.whoosh(0.18, 0.5), gap(0.0)]),
    }[kind]()


def effects():
    s = np.zeros(int(TOTAL * A.SR), np.float32)
    for w in WIPES:
        A.place(s, A.whoosh(0.55, 0.36, up=False), w - 0.05)
    for name, t_in, _, _, kind in LAYERS:
        if kind is None:
            continue
        if kind == "tick_tock":
            for j in range(4):
                A.place(s, A.tick(0.32), t_in + 0.1 + j * 0.25)
        else:
            A.place(s, sound(kind), t_in)
    # Two soft taps on the WhatsApp button pulse, then a closing sparkle.
    for at in (39.6, 40.8):
        A.place(s, A.pop(0.16), at)
    A.place(s, A.shimmer(0.3), 41.6)
    return s


def build_audio():
    if not os.path.exists(WAV) or not VERTICAL:
        A.write_wav(WAV, A.finish(0.9 * effects(), peak_db=-3.0))


def main():
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    build_audio()
    silent = OUT.replace(".mp4", "_silent.mp4")
    build_video(silent)
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", silent, "-i", WAV, "-map", "0:v", "-map", "1:a",
                    "-c:v", "copy", "-c:a", "aac", "-b:a", "192k",
                    "-af", "loudnorm=I=-19:TP=-2:LRA=15,alimiter=limit=0.79:level=false,aresample=48000",
                    "-shortest", "-movflags", "+faststart", OUT], check=True)
    os.remove(silent)
    print(OUT)


if __name__ == "__main__":
    main()

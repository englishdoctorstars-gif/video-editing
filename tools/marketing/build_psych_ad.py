"""Builds the psychology-led sales ad "৫ সেকেন্ডের চ্যালেঞ্জ" (47 seconds), sound effects only.

    python3 -I tools/marketing/build_psych_ad.py              # 1:1  (1080×1080)
    python3 -I tools/marketing/build_psych_ad.py --vertical   # 9:16 (1080×1920, Reels safe zone)
    python3 -I tools/marketing/build_psych_ad.py --full [--vertical]   # long cut with all nine features

No author/developer introduction. Flow: open question with a countdown → the viewer learns the
word in the ad (proof by experience) → realisation → pain → real cause → forgetting curve vs
1-3-7-30 review → future self → 10 minutes a day → value stack + price anchor → one clear CTA.
Layers: marketing/ad-graphics/psych[916]/ (PsychAdLayersTest / PsychAdVerticalLayersTest).
Output: marketing/videos/vocabsolution_challenge_ad_47s_{1x1,9x16}.mp4 and the shared
sound-effects stem vocabsolution_challenge_ad_sfx.wav.
"""
import os
import subprocess
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import audio_synth as A  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
VERTICAL = "--vertical" in sys.argv
L = os.path.join(ROOT, "marketing/ad-graphics/psych916" if VERTICAL else "marketing/ad-graphics/psych")
OUT = os.path.join(ROOT, "marketing/videos/vocabsolution_challenge_ad_47s_" + ("9x16" if VERTICAL else "1x1") + ".mp4")
WAV = os.path.join(ROOT, "marketing/videos/vocabsolution_challenge_ad_sfx.wav")
TOTAL, FPS = 47.0, 30
W, H = (1080, 1920) if VERTICAL else (1080, 1080)
PULSE_ANCHOR = (540, 685) if VERTICAL else (540, 450)   # CTA button centre, fixed while pulsing
BAR_Y = 1262 if VERTICAL else H - 8                     # watch-progress bar

WIPES = [3.6, 9.6, 13.4, 19.4, 24.8, 30.0, 34.8, 40.4]
SCENE_BG = ["bg_plum", "bg_navy", "bg_pain", "bg_navy", "bg_teal", "bg_plum", "bg_ink", "bg_navy"]
ENDS = WIPES[1:] + [TOTAL]
FLASHES = [17.4, 38.3]
STEPS = 10
RED0, RED_DT, GOLD0, GOLD_DT = 20.5, 0.10, 21.9, 0.13
E_END = 24.9

LAYERS = [
    ("a_stop", 0.1, 3.7, "pop", "impact"),
    ("a_q", 0.6, 3.7, "up_small", "whoosh_soft"),
    ("a_word", 1.0, 3.7, "pop", "pop"),
    ("a_t3", 1.5, 2.2, "count", "clock"),
    ("a_t2", 2.2, 2.9, "count", "clock"),
    ("a_t1", 2.9, 3.7, "count", "clock"),

    ("b_head", 3.95, 9.7, "down", None),
    ("b_word", 4.2, 9.7, "fade", "pop"),
    ("b_split", 5.0, 9.7, "up_small", "chime"),
    ("b_story", 6.0, 9.7, "up_small", "whoosh"),
    ("b_mean", 7.3, 9.7, "pop", "win"),

    ("c_1", 9.95, 13.5, "pop", "impact_soft"),
    ("c_2", 10.6, 13.5, "pop", "chime"),
    ("c_3", 11.6, 13.5, "up_small", "whoosh_soft"),
    ("c_4", 12.4, 13.5, "fade", None),

    ("d_head", 13.75, 19.5, "down", None),
    ("d_card0", 14.3, 19.5, "left", "buzz"),
    ("d_card1", 15.2, 19.5, "right", "buzz"),
    ("d_card2", 16.1, 19.5, "left", "buzz"),
    ("d_truth", 17.4, 19.5, "pop", "impact"),

    ("e_head", 19.75, E_END, "down", None),
    ("e_axes", 20.0, E_END, "fade", "whoosh_soft"),
] + [(f"e_red{k:02d}", RED0 + (k - 1) * RED_DT, RED0 + k * RED_DT if k < STEPS else E_END, "count", "fall" if k == 1 else None)
     for k in range(1, STEPS + 1)] + [
    (f"e_gold{k:02d}", GOLD0 + (k - 1) * GOLD_DT, GOLD0 + k * GOLD_DT if k < STEPS else E_END, "count",
     {1: "note:72", 3: "note:76", 6: "note:79", 9: "note:84"}.get(k))
    for k in range(1, STEPS + 1)] + [
    ("e_cap", 23.4, E_END, "up_small", "chime"),

    ("f_head", 25.15, 30.1, "down", None),
    ("f_row0", 25.7, 30.1, "left", "note:76"),
    ("f_row1", 26.5, 30.1, "left", "note:79"),
    ("f_row2", 27.3, 30.1, "left", "note:84"),
    ("f_tag", 28.3, 30.1, "pop", "shimmer"),

    ("g_head", 30.35, 34.9, "down", "whoosh_soft"),
    ("g_ph0", 30.7, 34.9, "float", "whoosh"),
    ("g_ph2", 30.9, 34.9, "float", None),
    ("g_ph1", 31.1, 34.9, "float", "whoosh"),
    ("g_lab", 32.0, 34.9, "up_small", "pop"),

    ("h_head", 35.15, 40.5, "down", None),
    ("h_row0", 35.5, 40.5, "left", "pop"),
    ("h_row1", 35.85, 40.5, "left", "pop"),
    ("h_row2", 36.2, 40.5, "left", "pop"),
    ("h_row3", 36.55, 40.5, "left", "pop"),
    ("h_old", 37.2, 40.5, "fade", "whoosh_soft"),
    ("h_strike", 37.8, 40.5, "fade", "slash"),
    ("h_price", 38.3, 40.5, "pop", "cash"),
    ("h_sub", 39.2, 40.5, "up_small", "pop"),

    ("i_line", 40.75, TOTAL, "down", "whoosh_soft"),
    ("i_btn", 41.5, TOTAL, "pulse", "impact"),
    ("i_pay", 42.4, TOTAL, "up_small", "pop"),
]
PULSE_PERIOD = 1.2
TAIL = 0.0   # how much later the closing scenes (offer, CTA) start than in the 47-second cut

# --full: the long "সব এক অ্যাপে" cut. Same opening (question → learn a word → pain), then a tour
# of nine features (4.6 s each), the review curve, and the same offer and call to action.
if "--full" in sys.argv:
    FEATURE, N_FEATURES, TOUR0 = 4.6, 9, 19.4
    tour_end = TOUR0 + 8 * FEATURE                      # features 1-8, then the review curve
    e_delta = tour_end - 19.4
    m9 = tour_end + 5.4                                 # feature 9 follows the curve
    TAIL = m9 + FEATURE - 30.0

    def moved(prefixes, delta):
        return [(n, a + delta, b + delta, an, snd) for n, a, b, an, snd in LAYERS if n.split("_")[0] in prefixes]

    def feature(k, start):
        side = "right" if k % 2 == 0 else "left"       # cards enter from the side opposite the phone
        end = start + FEATURE + 0.1
        return [(f"m{k}_title", start + 0.35, end, "down", "whoosh_soft"), (f"m{k}_phone", start + 0.5, end, "float", "whoosh")] + [
            (f"m{k}_c{j}", start + 1.1 + j * 0.7, end, side, f"note:{(72, 76, 79)[j]}") for j in range(3)]

    opening = [(("c_5" if n == "c_4" else n), a, b, an, snd) for n, a, b, an, snd in moved(("a", "b", "c", "d"), 0.0)]
    tour = [layer for k in range(8) for layer in feature(k, TOUR0 + k * FEATURE)]
    LAYERS = opening + tour + moved(("e",), e_delta) + feature(8, m9) + moved(("g", "h", "i"), TAIL)
    feature_bg = ["bg_teal", "bg_navy", "bg_plum", "bg_teal", "bg_navy", "bg_plum", "bg_teal", "bg_plum"]
    WIPES = [3.6, 9.6, 13.4] + [TOUR0 + k * FEATURE for k in range(8)] + [tour_end, m9, 30.0 + TAIL, 34.8 + TAIL, 40.4 + TAIL]
    SCENE_BG = ["bg_plum", "bg_navy", "bg_pain"] + feature_bg + ["bg_navy", "bg_teal", "bg_plum", "bg_ink", "bg_navy"]
    TOTAL = round(TOTAL + TAIL, 2)
    LAYERS = [(n, a, TOTAL if n.startswith("i_") else b, an, snd) for n, a, b, an, snd in LAYERS]
    ENDS = WIPES[1:] + [TOTAL]
    FLASHES = [17.4, 38.3 + TAIL]
    OUT = os.path.join(ROOT, f"marketing/videos/vocabsolution_full_ad_{int(TOTAL)}s_" + ("9x16" if VERTICAL else "1x1") + ".mp4")
    WAV = os.path.join(ROOT, "marketing/videos/vocabsolution_full_ad_sfx.wav")


def ease(t_in, dur=0.55):
    return f"pow(1-min(1,max(0,(t-{t_in})/{dur})),3)"


def position(anim, t_in):
    e = ease(t_in)
    return {
        "left": (f"-620*{e}", "0"),
        "right": (f"620*{e}", "0"),
        "float": ("0", f"700*{ease(t_in, 0.7)}+9*sin(2*PI*(t-{t_in})/3)"),
        "down": ("0", f"-110*{e}"),
        "up_small": ("0", f"60*{e}"),
        "pop": ("(W-w)/2", "(H-h)/2"),
        "pulse": (f"{PULSE_ANCHOR[0]}*(1-w/W)", f"{PULSE_ANCHOR[1]}*(1-h/H)"),
    }.get(anim, ("0", "0"))


def build_video(silent_mp4):
    frames = int(TOTAL * FPS)
    inputs = ["-loop", "1", "-framerate", str(FPS), "-t", str(TOTAL), "-i", os.path.join(L, "bg_ink.png")]
    graph = [f"[0:v]scale={W}:{H},zoompan=z='1+0.06*on/{frames}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={W}x{H}:fps={FPS},"
             f"format=rgba,settb=1/{FPS}[base]"]
    plan = [("particles", 0.0, WIPES[0] + 0.3, "drift")]
    for i, w in enumerate(WIPES):
        end = ENDS[i] + 0.3 if ENDS[i] < TOTAL else TOTAL
        plan += [(SCENE_BG[i], w + 0.25, end, "bg"), ("particles", w + 0.25, end, "drift")]
    content = [(n, a, b, an) for n, a, b, an, _ in LAYERS]
    if VERTICAL:
        content.append(("brand_line", 0.3, TOTAL, "fade"))
    stack = sorted(plan + content, key=lambda p: (p[1], 0 if p[3] in ("bg", "drift") else 1))
    stack += [("wipe", w, w + 0.55, "wipe") for w in WIPES]
    stack += [("@flash", t, t + 0.4, "flash") for t in FLASHES]
    stack += [("@bar", 0.0, TOTAL, "bar")]

    prev = "base"
    for i, (name, t_in, t_out, anim) in enumerate(stack, start=1):
        dur = t_out - t_in + (0.0 if anim in ("count", "flash", "bar") else 0.1)
        if name == "@flash":
            inputs += ["-f", "lavfi", "-t", f"{dur:.2f}", "-i", f"color=c=white:s={W}x{H}:r={FPS}"]
        elif name == "@bar":
            inputs += ["-f", "lavfi", "-t", f"{dur:.2f}", "-i", f"color=c=0xFFC83D:s={W}x8:r={FPS}"]
        else:
            inputs += ["-loop", "1", "-framerate", str(FPS), "-t", f"{dur:.2f}", "-i", os.path.join(L, name + ".png")]
        chain = f"[{i}:v]format=rgba,setpts=PTS-STARTPTS+{t_in}/TB"
        if anim == "bg":
            n = int(dur * FPS)
            chain += (f",zoompan=z='1+0.05*on/{n}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={W}x{H}:fps={FPS}"
                      f",setpts=PTS-STARTPTS+{t_in}/TB,format=rgba")
        elif anim == "pop":
            chain += f",scale=w='trunc(iw*(1+0.18*pow(1-min(1,max(0,(t-{t_in})/0.32)),2))/2)*2':h=-2:eval=frame"
        elif anim == "pulse":
            p = f"(1+0.16*pow(1-min(1,max(0,(t-{t_in})/0.32)),2)+0.045*max(0,sin(2*PI*(t-{t_in}-0.6)/{PULSE_PERIOD})))"
            chain += f",scale=w='trunc(iw*{p}/2)*2':h=-2:eval=frame"
        elif anim == "flash":
            chain += f",colorchannelmixer=aa=0.5,fade=t=out:st={t_in}:d=0.38:alpha=1"
        if anim not in ("wipe", "count", "flash", "bar"):
            chain += f",fade=t=in:st={t_in}:d={0.05 if anim == 'bg' else 0.3}:alpha=1"
            if t_out < TOTAL and anim != "bg":
                chain += f",fade=t=out:st={t_out - 0.2:.2f}:d=0.2:alpha=1"
        graph.append(chain + f"[l{i}]")
        if anim == "wipe":
            x, y = f"W-2*W*(0.5-0.5*cos(PI*min(1,max(0,(t-{t_in})/0.5))))", "0"
        elif anim == "drift":
            x, y = "0", f"-mod((t-{t_in})*14,H)"
        elif anim == "bar":
            x, y = f"-W+W*t/{TOTAL}", str(BAR_Y)
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

def mix(dur, *parts):
    out = np.zeros(int(dur * A.SR), np.float32)
    for snd, at in parts:
        A.place(out, snd, at)
    return out


def sound(kind):
    if kind.startswith("note:"):
        return A.chime(int(kind[5:]), 0.3)
    return {
        "whoosh": lambda: A.whoosh(0.45, 0.42),
        "whoosh_soft": lambda: A.whoosh(0.4, 0.26),
        "pop": lambda: A.pop(0.32),
        "buzz": lambda: A.buzz(0.3),
        "impact": lambda: A.impact(0.55),
        "impact_soft": lambda: A.impact(0.34),
        "shimmer": lambda: A.shimmer(0.38),
        "chime": lambda: A.chime(84, 0.38),
        "slash": lambda: A.whoosh(0.18, 0.5),
        "fall": lambda: A.whoosh(0.9, 0.34, up=False),
        # Countdown: a sharp tick over a low thump, like a heartbeat clock.
        "clock": lambda: mix(0.4, (A.tick(0.6), 0.0), (A.kick(0.32), 0.0)),
        # The "got it" moment: soft thump with a rising two-note bell.
        "win": lambda: mix(1.2, (A.impact(0.3), 0.0), (A.chime(79, 0.34), 0.0), (A.chime(86, 0.38), 0.13)),
        # Cash register: mechanical clicks, then two bright bells.
        "cash": lambda: mix(1.3, (A.impact(0.38), 0.0), (A.tick(0.55), 0.0), (A.tick(0.45), 0.05),
                            (A.chime(96, 0.42), 0.09), (A.chime(100, 0.38), 0.17)),
    }[kind]()


def effects():
    s = np.zeros(int(TOTAL * A.SR), np.float32)
    for w in WIPES:
        A.place(s, A.whoosh(0.55, 0.36, up=False), w - 0.05)
    for _, t_in, _, _, kind in LAYERS:
        if kind:
            A.place(s, sound(kind), t_in)
    for at in (43.3 + TAIL, 44.5 + TAIL):   # soft taps with the button pulse
        A.place(s, A.pop(0.16), at)
    A.place(s, A.shimmer(0.3), 45.4 + TAIL)
    return s


def main():
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    if not VERTICAL or not os.path.exists(WAV):
        A.write_wav(WAV, A.finish(0.9 * effects(), peak_db=-3.0))
    silent = OUT.replace(".mp4", "_silent.mp4")
    build_video(silent)
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", silent, "-i", WAV, "-map", "0:v", "-map", "1:a",
                    "-c:v", "copy", "-c:a", "aac", "-b:a", "192k",
                    "-af", "loudnorm=I=-19:TP=-3:LRA=15,alimiter=limit=0.6:level=false,aresample=48000",
                    "-shortest", "-movflags", "+faststart", OUT], check=True)
    os.remove(silent)
    print(OUT)


if __name__ == "__main__":
    main()

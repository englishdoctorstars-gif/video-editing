"""Builds concept 3 "স্বপ্নের Band" (IELTS): a 30-second 9:16 motion-graphics ad with
original synthesised music and sound effects.

    python3 -I tools/marketing/build_concept3.py

Layers: marketing/ad-graphics/concept3/ (Concept3LayersTest) and the shared end card
marketing/ad-graphics/endcard_9x16.png (AdGraphicsVerticalTest).
Output: marketing/videos/concept3_ielts_dream_30s_9x16.mp4 and the audio stem
marketing/videos/concept3_ielts_dream_music_sfx.wav (for re-editing).
"""
import math
import os
import subprocess
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import audio_synth as A  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
L = os.path.join(ROOT, "marketing/ad-graphics/concept3")
END = os.path.join(ROOT, "marketing/ad-graphics/endcard_9x16.png")
OUT = os.path.join(ROOT, "marketing/videos/concept3_ielts_dream_30s_9x16.mp4")
WAV = os.path.join(ROOT, "marketing/videos/concept3_ielts_dream_music_sfx.wav")
TOTAL, FPS, W, H, DP = 30.0, 30, 1080, 1920, 3.0

# ---- picture -------------------------------------------------------------------------------
# (layer, in, out, animation). Times in seconds. Backgrounds and the end card are layers too.
LAYERS = [
    ("bg_bright", 8.45, TOTAL, "bgfade"),
    ("bg_dawn", 21.45, TOTAL, "bgfade"),
    # Scene 1 — Reading: same idea, different words
    ("s1_label", 0.15, 4.45, "fade"),
    ("s1_question", 0.45, 4.45, "left"),
    ("s1_passage", 1.25, 4.45, "right"),
    ("s1_same", 2.2, 4.45, "up_small"),
    ("s1_stamp", 2.85, 4.45, "pop"),
    # Scene 2 — Listening: one spelling mistake
    ("s2_label", 4.7, 8.45, "fade"),
    ("s2_heard", 5.0, 8.45, "left"),
    ("s2_wrote", 5.9, 8.45, "right"),
    ("s2_rule", 7.0, 8.45, "pop"),
    # Scene 3 — the turn
    ("s3_line", 8.7, 10.75, "up_small"),
    ("s3_logo", 9.3, 10.75, "pop"),
    # Scenes 4–6 — features on real app screens
    ("s4_caption", 10.9, 14.15, "down"),
    ("s4_phone", 11.0, 14.15, "up"),
    ("s5_caption", 14.3, 17.75, "down"),
    ("s5_phone", 14.4, 17.75, "up"),
    ("s5_chip", 15.5, 17.75, "pop"),
    ("s6_caption", 17.9, 21.55, "down"),
    ("s6_phones", 18.0, 21.55, "up"),
    # Scene 7 — the dream
    ("s7_text", 21.8, 25.75, "up_small"),
    ("s7_path", 22.1, 25.75, "fade"),
    ("s7_plane", 22.3, 25.75, "plane"),
    # Scene 8 — end card (price, WhatsApp, bKash/Nagad, Facebook page)
    ("endcard", 25.6, TOTAL, "bgfade"),
]
PLANE_T0, PLANE_DUR = 22.4, 2.4
S, C, E = (48, 560), (70, 220), (318, 196)  # dp, same curve as Concept3LayersTest
PLANE_HALF = 28 * DP


def ease_out(t_in, dur=0.55):
    return f"pow(1-min(1,max(0,(t-{t_in})/{dur})),3)"


def position(anim, t_in):
    e = ease_out(t_in)
    return {
        "left": (f"-620*{e}", "0"),
        "right": (f"620*{e}", "0"),
        "up": (f"0", f"900*{ease_out(t_in, 0.7)}"),
        "down": (f"0", f"-160*{e}"),
        "up_small": (f"0", f"90*{e}"),
        "pop": ("(W-w)/2", "(H-h)/2"),
    }.get(anim, ("0", "0"))


def plane_xy():
    p = f"min(1,max(0,(t-{PLANE_T0})/{PLANE_DUR}))"
    u = f"(0.5-0.5*cos(PI*{p}))"
    bx = f"(pow(1-{u},2)*{S[0]*DP}+2*(1-{u})*{u}*{C[0]*DP}+pow({u},2)*{E[0]*DP}-{PLANE_HALF})"
    by = f"(pow(1-{u},2)*{S[1]*DP}+2*(1-{u})*{u}*{C[1]*DP}+pow({u},2)*{E[1]*DP}-{PLANE_HALF})"
    return bx, by


def build_video(silent_mp4):
    inputs = ["-loop", "1", "-framerate", str(FPS), "-t", str(TOTAL), "-i", os.path.join(L, "bg_night.png")]
    frames = int(TOTAL * FPS)
    graph = [f"[0:v]scale={W}:{H},zoompan=z='1+0.06*on/{frames}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={W}x{H}:fps={FPS},"
             f"format=rgba,settb=1/{FPS}[base]"]
    prev = "base"
    for i, (name, t_in, t_out, anim) in enumerate(LAYERS, start=1):
        path = END if name == "endcard" else os.path.join(L, name + ".png")
        dur = t_out - t_in + 0.1
        inputs += ["-loop", "1", "-framerate", str(FPS), "-t", f"{dur:.2f}", "-i", path]
        chain = f"[{i}:v]format=rgba,setpts=PTS-STARTPTS+{t_in}/TB"
        if anim == "bgfade" and name.startswith("bg_"):
            n = int(dur * FPS)
            chain += (f",zoompan=z='1+0.04*on/{n}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={W}x{H}:fps={FPS}"
                      f",setpts=PTS-STARTPTS+{t_in}/TB,format=rgba")
        if anim == "pop":
            # Starts 18% larger and settles to full size in 0.32 s (progress clamped to 0..1).
            chain += f",scale=w='trunc(iw*(1+0.18*pow(1-min(1,max(0,(t-{t_in})/0.32)),2))/2)*2':h=-2:eval=frame"
        fade_in = 0.4 if anim == "bgfade" else 0.3
        chain += f",fade=t=in:st={t_in}:d={fade_in}:alpha=1"
        if t_out < TOTAL:
            chain += f",fade=t=out:st={t_out - 0.25:.2f}:d=0.25:alpha=1"
        graph.append(chain + f"[l{i}]")
        x, y = plane_xy() if anim == "plane" else position(anim, t_in)
        graph.append(f"[{prev}][l{i}]overlay=x='{x}':y='{y}':eval=frame:eof_action=pass[v{i}]")
        prev = f"v{i}"
    graph.append(f"[{prev}]format=yuv420p[out]")
    cmd = ["ffmpeg", "-y", "-loglevel", "error", *inputs, "-filter_complex", ";".join(graph), "-map", "[out]",
           "-t", str(TOTAL), "-r", str(FPS), "-c:v", "libx264", "-preset", "medium", "-crf", "19", "-pix_fmt", "yuv420p", silent_mp4]
    subprocess.run(cmd, check=True)


# ---- sound ---------------------------------------------------------------------------------

def music():
    m = np.zeros(int(TOTAL * A.SR), np.float32)
    beat = 60 / 96
    # A: tension (0–8.6) — minor chords, heartbeat pulse
    t = 0.0
    for root, chord in [(45, (57, 60, 64)), (41, (53, 57, 60)), (38, (50, 53, 57)), (40, (52, 56, 59))]:
        for n in chord:
            A.place(m, A.piano(n, 2.4, 0.22), t)
        A.place(m, A.pad(chord, 2.4, 0.16, bright=900), t)
        A.place(m, A.bass(root - 12, 2.0, 0.22), t)
        t += 2.15
    for hb in np.arange(0.0, 7.6, 1.25):
        A.place(m, A.kick(0.28), hb)
        A.place(m, A.kick(0.18), hb + 0.22)
    # B + C: hopeful (8.6–25.8) — C G Am F, arpeggios, then a beat and melody
    prog = [(36, (60, 64, 67)), (31, (55, 59, 62)), (33, (57, 60, 64)), (29, (53, 57, 60))]
    bar = 4 * beat
    start, k = 8.6, 0
    melody = [76, 79, 81, 79, 76, 74, 76, 72]
    while start < 25.8:
        root, chord = prog[k % 4]
        A.place(m, A.pad(chord, bar + 0.4, 0.15 if start < 21.6 else 0.2), start)
        arp = [chord[0] + 12, chord[1] + 12, chord[2] + 12, chord[0] + 24, chord[2] + 12, chord[1] + 12, chord[0] + 12, chord[1] + 12]
        for j, n in enumerate(arp):
            if start + j * beat / 2 < 25.8:
                A.place(m, A.piano(n, 0.9, 0.16), start + j * beat / 2)
        for b in range(4):
            at = start + b * beat
            if at >= 25.8:
                break
            if b in (0, 2):
                A.place(m, A.bass(root, beat * 1.8, 0.3), at)
            if at >= 10.8:
                if b in (0, 2):
                    A.place(m, A.kick(0.55), at)
                A.place(m, A.hat(0.12), at + beat / 2)
            if at >= 14.2 and b in (1, 3):
                A.place(m, A.clap(0.18), at)
            if at >= 21.6:
                A.place(m, A.piano(melody[(k * 4 + b) % len(melody)], 1.0, 0.24), at)
        start += bar
        k += 1
    # D: resolve on C (25.8–30)
    for n in (48, 60, 64, 67, 72):
        A.place(m, A.piano(n, 4.0, 0.3), 25.8)
    A.place(m, A.pad((60, 64, 67, 72), 4.2, 0.22), 25.8)
    for n, at in [(76, 26.7), (79, 27.3), (84, 27.9)]:
        A.place(m, A.piano(n, 2.0, 0.2), at)
    fade = int(1.4 * A.SR)
    m[-fade:] *= np.linspace(1, 0, fade)
    return m


def effects():
    s = np.zeros(int(TOTAL * A.SR), np.float32)
    events = [
        (0.15, A.pop(0.3)), (0.45, A.whoosh(0.45)), (1.25, A.whoosh(0.45)), (2.2, A.pop(0.3)),
        (2.85, A.buzz(0.5)), (2.85, A.kick(0.5)),
        (4.7, A.pop(0.3)), (5.0, A.whoosh(0.45)), (5.15, A.chime(79, 0.3)), (5.9, A.whoosh(0.45)),
        (7.0, A.buzz(0.5)), (7.8, A.riser(0.8, 0.45)), (8.6, A.impact(0.55)), (8.7, A.whoosh(0.5, 0.4)),
        (9.3, A.shimmer(0.4)), (9.3, A.chime(84, 0.35)),
        (10.9, A.whoosh(0.4, 0.35)), (11.0, A.whoosh(0.6, 0.45)),
        (14.3, A.whoosh(0.4, 0.35)), (14.4, A.whoosh(0.6, 0.45)), (15.5, A.chime(88, 0.45)), (15.5, A.pop(0.25)),
        (17.9, A.whoosh(0.4, 0.35)), (18.0, A.whoosh(0.6, 0.45)),
        (21.45, A.whoosh(0.7, 0.4, up=False)), (22.4, A.whoosh(2.4, 0.3)),
        (25.6, A.impact(0.6)), (25.7, A.shimmer(0.35)),
    ]
    # Typing the misspelled word in scene 2
    for j, at in enumerate(np.cumsum([0.0, 0.09, 0.07, 0.11, 0.08, 0.06, 0.1, 0.07, 0.09, 0.08, 0.07])):
        events.append((6.0 + at, A.tick(0.3)))
    for at, snd in events:
        A.place(s, snd, at)
    return s


def build_audio():
    mus, sfx = music(), effects()
    # Duck the music a little under each effect for clarity.
    env = np.abs(sfx)
    win = int(0.08 * A.SR)
    env = np.convolve(env, np.ones(win) / win, mode="same")
    duck = 1 - 0.45 * np.clip(env / (env.max() or 1) * 3, 0, 1)
    mix = 0.75 * mus * duck + 0.9 * sfx
    A.write_wav(WAV, A.finish(mix, peak_db=-1.0))


def main():
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    build_audio()
    silent = OUT.replace(".mp4", "_silent.mp4")
    build_video(silent)
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", silent, "-i", WAV, "-map", "0:v", "-map", "1:a",
                    "-c:v", "copy", "-c:a", "aac", "-b:a", "192k", "-af", "loudnorm=I=-16:TP=-1.5:LRA=11",
                    "-shortest", "-movflags", "+faststart", OUT], check=True)
    os.remove(silent)
    print(OUT)


if __name__ == "__main__":
    main()

"""Builds the all-features 1:1 ad for the English Doctor Facebook page (about 55 seconds),
with layered motion graphics and original synthesised music and sound effects.

    python3 -I tools/marketing/build_combo.py

Layers: marketing/ad-graphics/combo/ (ComboLayersTest).
Output: marketing/videos/vocabsolution_all_features_55s_1x1.mp4 and the audio stem
marketing/videos/vocabsolution_all_features_music_sfx.wav.

    python3 -I tools/marketing/build_combo.py --sfx-only

Makes the voice-over version instead: same picture, sound effects only (no music), kept
quieter (about -24 LUFS, peaks near -6 dB) so a voice-over sits clearly on top.
Output: ..._sfx_only.mp4 and ..._sfx_only.wav. Reuses the picture of the main file if present.
"""
import os
import subprocess
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import audio_synth as A  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
L = os.path.join(ROOT, "marketing/ad-graphics/combo")
OUT = os.path.join(ROOT, "marketing/videos/vocabsolution_all_features_55s_1x1.mp4")
WAV = os.path.join(ROOT, "marketing/videos/vocabsolution_all_features_music_sfx.wav")
TOTAL, FPS, SIZE = 55.0, 30, 1080

# (layer, in, out, animation, sound). Scenes follow the story:
# hook → turn → dissection → root power → stories → IELTS paraphrase → listening/pronunciation
# → correct usage → exam + 1-3-7-30 → everything in one app → end card.
LAYERS = [
    ("hook_1", 0.2, 4.6, "up_small", "whoosh"),
    ("hook_2", 1.2, 4.6, "pop", "buzz"),
    ("hook_3", 2.3, 4.6, "up_small", "pop"),
    ("bg_blue", 4.45, 7.9, "bg", "impact"),
    ("turn_line", 4.7, 7.9, "up_small", "whoosh"),
    ("turn_logo", 5.2, 7.9, "pop", "shimmer"),

    ("d_head", 8.0, 13.4, "down", "whoosh_soft"),
    ("d_word", 8.4, 13.4, "fade", "pop"),
    ("d_parts", 9.6, 13.4, "pop", "snap"),
    ("d_meaning", 10.9, 13.4, "up_small", "chime"),

    ("bg_teal", 13.25, 19.4, "bg", "whoosh_down"),
    ("r_head", 13.5, 19.4, "down", None),
    ("r_center", 14.0, 19.4, "pop", "pop"),
    ("r_lines", 14.4, 19.4, "fade", "whoosh_soft"),
    ("r_ring1", 14.7, 19.4, "pop", "sparkle"),
    ("r_ring2", 15.6, 19.4, "pop", "sparkle"),
    ("r_footer", 16.8, 19.4, "up_small", "pop"),

    ("bg_plum", 19.25, 25.6, "bg", "whoosh_down"),
    ("s_head", 19.5, 25.6, "down", None),
    ("s_phone", 19.7, 25.6, "up", "whoosh"),
    ("s_step1", 20.5, 25.6, "right", "whoosh_soft"),
    ("s_step2", 21.6, 25.6, "right", "whoosh_soft"),
    ("s_step3", 22.7, 25.6, "pop", "chime"),
    ("s_quote", 23.6, 25.6, "fade", None),

    ("bg_blue", 25.45, 30.8, "bg", "whoosh_down"),
    ("p_head", 25.7, 30.8, "down", None),
    ("p_question", 26.1, 30.8, "left", "whoosh"),
    ("p_passage", 27.1, 30.8, "right", "whoosh"),
    ("p_link", 28.1, 30.8, "pop", "chime"),
    ("p_footer", 29.0, 30.8, "up_small", "pop"),

    ("bg_teal", 30.65, 36.2, "bg", "whoosh_down"),
    ("l_head", 30.9, 36.2, "down", None),
    ("l_phone", 31.1, 36.2, "up", "whoosh"),
    ("l_card1", 31.6, 36.2, "left", "chime_low"),
    ("l_card2", 32.7, 36.2, "left", "typing"),
    ("l_card3", 33.8, 36.2, "left", "whoosh_soft"),

    ("u_head", 36.3, 41.3, "down", "whoosh_down"),
    ("u_row1", 36.8, 41.3, "left", "fix"),
    ("u_row2", 37.7, 41.3, "left", "fix"),
    ("u_row3", 38.6, 41.3, "left", "fix"),
    ("u_footer", 39.6, 41.3, "up_small", "pop"),

    ("bg_plum", 41.15, 47.0, "bg", "whoosh_down"),
    ("e_head", 41.4, 47.0, "down", None),
    ("e_phone", 41.6, 47.0, "up", "whoosh"),
    ("e_timer", 42.3, 47.0, "right", "tick_tock"),
    ("e_dot1", 43.2, 47.0, "pop", "note:72"),
    ("e_dot2", 43.5, 47.0, "pop", "note:76"),
    ("e_dot3", 43.8, 47.0, "pop", "note:79"),
    ("e_dot4", 44.1, 47.0, "pop", "note:84"),
    ("e_text", 44.6, 47.0, "fade", None),

    ("bg_blue", 46.85, TOTAL, "bg", "impact_soft"),
    ("g_title", 47.1, 50.6, "down", "shimmer"),
    ("g_row1", 47.5, 50.6, "up", "whoosh"),
    ("g_row2", 48.1, 50.6, "up", "whoosh"),
    ("g_footer", 48.8, 50.6, "up_small", "pop"),

    ("endcard", 50.45, TOTAL, "bg", "impact"),
]
END_SHIMMER = 50.6


def ease_out(t_in, dur=0.55):
    return f"pow(1-min(1,max(0,(t-{t_in})/{dur})),3)"


def position(anim, t_in):
    e = ease_out(t_in)
    return {
        "left": (f"-620*{e}", "0"),
        "right": (f"620*{e}", "0"),
        "up": ("0", f"760*{ease_out(t_in, 0.7)}"),
        "down": ("0", f"-120*{e}"),
        "up_small": ("0", f"70*{e}"),
        "pop": ("(W-w)/2", "(H-h)/2"),
    }.get(anim, ("0", "0"))


def build_video(silent_mp4):
    inputs = ["-loop", "1", "-framerate", str(FPS), "-t", str(TOTAL), "-i", os.path.join(L, "bg_night.png")]
    frames = int(TOTAL * FPS)
    graph = [f"[0:v]scale={SIZE}:{SIZE},zoompan=z='1+0.08*on/{frames}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={SIZE}x{SIZE}:fps={FPS},"
             f"format=rgba,settb=1/{FPS}[base]"]
    prev = "base"
    for i, (name, t_in, t_out, anim, _) in enumerate(LAYERS, start=1):
        dur = t_out - t_in + 0.1
        inputs += ["-loop", "1", "-framerate", str(FPS), "-t", f"{dur:.2f}", "-i", os.path.join(L, name + ".png")]
        chain = f"[{i}:v]format=rgba,setpts=PTS-STARTPTS+{t_in}/TB"
        if anim == "bg":
            n = int(dur * FPS)
            chain += (f",zoompan=z='1+0.04*on/{n}':d=1:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s={SIZE}x{SIZE}:fps={FPS}"
                      f",setpts=PTS-STARTPTS+{t_in}/TB,format=rgba")
        if anim == "pop":
            chain += f",scale=w='trunc(iw*(1+0.16*pow(1-min(1,max(0,(t-{t_in})/0.32)),2))/2)*2':h=-2:eval=frame"
        chain += f",fade=t=in:st={t_in}:d={0.4 if anim == 'bg' else 0.3}:alpha=1"
        if t_out < TOTAL:
            chain += f",fade=t=out:st={t_out - 0.25:.2f}:d=0.25:alpha=1"
        graph.append(chain + f"[l{i}]")
        x, y = position(anim, t_in)
        graph.append(f"[{prev}][l{i}]overlay=x='{x}':y='{y}':eval=frame:eof_action=pass[v{i}]")
        prev = f"v{i}"
    graph.append(f"[{prev}]format=yuv420p[out]")
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", *inputs, "-filter_complex", ";".join(graph), "-map", "[out]",
                    "-t", str(TOTAL), "-r", str(FPS), "-c:v", "libx264", "-preset", "medium", "-crf", "19",
                    "-pix_fmt", "yuv420p", silent_mp4], check=True)


# ---- sound ---------------------------------------------------------------------------------

def music():
    m = np.zeros(int(TOTAL * A.SR), np.float32)
    beat = 60 / 100
    # Hook: tense minor chords with a heartbeat
    t = 0.0
    for root, chord in [(45, (57, 60, 64)), (41, (53, 57, 60))]:
        for n in chord:
            A.place(m, A.piano(n, 2.6, 0.22), t)
        A.place(m, A.pad(chord, 2.6, 0.16, bright=900), t)
        A.place(m, A.bass(root - 12, 2.2, 0.2), t)
        t += 2.2
    for hb in np.arange(0.0, 4.0, 1.1):
        A.place(m, A.kick(0.26), hb)
        A.place(m, A.kick(0.17), hb + 0.22)
    A.place(m, A.riser(0.75, 0.45), 3.7)
    # Turn: bright, airy
    A.place(m, A.pad((60, 64, 67, 72), 3.6, 0.2), 4.45)
    for j, n in enumerate([72, 76, 79, 84, 79, 76, 72, 76]):
        A.place(m, A.piano(n, 1.0, 0.15), 4.6 + j * 0.4)
    # Main groove: C G Am F, building section by section
    prog = [(36, (60, 64, 67)), (31, (55, 59, 62)), (33, (57, 60, 64)), (29, (53, 57, 60))]
    melody = [76, 79, 81, 79, 76, 74, 72, 74, 76, 79, 84, 81, 79, 76, 74, 76]
    bar, start, k = 4 * beat, 8.0, 0
    while start < 50.45:
        root, chord = prog[k % 4]
        A.place(m, A.pad(chord, bar + 0.4, 0.14 if start < 36 else 0.18), start)
        arp = [chord[0] + 12, chord[1] + 12, chord[2] + 12, chord[0] + 24, chord[2] + 12, chord[1] + 12, chord[0] + 12, chord[1] + 12]
        for j, n in enumerate(arp):
            at = start + j * beat / 2
            if at < 50.45:
                A.place(m, A.piano(n, 0.8, 0.14), at)
        for b in range(4):
            at = start + b * beat
            if at >= 50.45:
                break
            if b in (0, 2):
                A.place(m, A.bass(root, beat * 1.8, 0.28), at)
                A.place(m, A.kick(0.5), at)
            A.place(m, A.hat(0.1 if at < 19.25 else 0.13), at + beat / 2)
            if at >= 19.25 and b in (1, 3):
                A.place(m, A.clap(0.17), at)
            if at >= 36.3:
                A.place(m, A.piano(melody[(k * 4 + b) % len(melody)], 0.9, 0.2 if at < 46.85 else 0.26), at)
        start += bar
        k += 1
    # Resolve on C for the end card
    for n in (48, 60, 64, 67, 72):
        A.place(m, A.piano(n, 4.5, 0.3), 50.45)
    A.place(m, A.pad((60, 64, 67, 72), 4.6, 0.22), 50.45)
    for n, at in [(76, 51.4), (79, 52.0), (84, 52.6)]:
        A.place(m, A.piano(n, 2.2, 0.2), at)
    fade = int(1.5 * A.SR)
    m[-fade:] *= np.linspace(1, 0, fade)
    return m


def sound(kind):
    if kind.startswith("note:"):
        return A.chime(int(kind[5:]), 0.35)
    return {
        "whoosh": lambda: A.whoosh(0.45, 0.45),
        "whoosh_soft": lambda: A.whoosh(0.4, 0.28),
        "whoosh_down": lambda: A.whoosh(0.6, 0.35, up=False),
        "pop": lambda: A.pop(0.3),
        "buzz": lambda: A.buzz(0.42),
        "impact": lambda: A.impact(0.55),
        "impact_soft": lambda: A.impact(0.35),
        "shimmer": lambda: A.shimmer(0.4),
        "chime": lambda: A.chime(84, 0.42),
        "chime_low": lambda: A.chime(79, 0.32),
        "sparkle": lambda: A.shimmer(0.22),
        "snap": lambda: np.concatenate([A.tick(0.5), np.zeros(int(0.05 * A.SR), np.float32), A.tick(0.45),
                                        np.zeros(int(0.05 * A.SR), np.float32), A.tick(0.45)]) + 0,
    }[kind]()


def effects():
    s = np.zeros(int(TOTAL * A.SR), np.float32)
    for name, t_in, _, _, kind in LAYERS:
        if kind is None:
            continue
        if kind == "typing":
            for at in np.cumsum([0.0, 0.08, 0.07, 0.1, 0.07, 0.06, 0.09, 0.08, 0.07, 0.09]):
                A.place(s, A.tick(0.28), t_in + 0.2 + at)
        elif kind == "fix":  # wrong → right
            A.place(s, A.buzz(0.22)[: int(0.15 * A.SR)], t_in)
            A.place(s, A.chime(88, 0.3), t_in + 0.35)
        elif kind == "tick_tock":
            for j in range(4):
                A.place(s, A.tick(0.35), t_in + j * 0.25)
        else:
            A.place(s, sound(kind), t_in)
    A.place(s, A.shimmer(0.35), END_SHIMMER)
    return s


def build_audio():
    mus, sfx = music(), effects()
    env = np.convolve(np.abs(sfx), np.ones(int(0.08 * A.SR)) / int(0.08 * A.SR), mode="same")
    duck = 1 - 0.45 * np.clip(env / (env.max() or 1) * 3, 0, 1)
    A.write_wav(WAV, A.finish(0.75 * mus * duck + 0.9 * sfx, peak_db=-1.0))


SFX_OUT = OUT.replace(".mp4", "_sfx_only.mp4")
SFX_WAV = OUT.replace(".mp4", "_sfx_only.wav")


def build_sfx_only():
    """Voice-over bed: effects only, no music, leaving headroom for speech."""
    A.write_wav(SFX_WAV, A.finish(0.9 * effects(), peak_db=-6.0))
    picture = OUT
    if not os.path.exists(picture):
        picture = OUT.replace(".mp4", "_silent.mp4")
        build_video(picture)
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", picture, "-i", SFX_WAV, "-map", "0:v", "-map", "1:a",
                    "-c:v", "copy", "-c:a", "aac", "-b:a", "192k", "-af", "aresample=48000",
                    "-shortest", "-movflags", "+faststart", SFX_OUT], check=True)
    if picture != OUT:
        os.remove(picture)
    print(SFX_OUT)


def main():
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    if "--sfx-only" in sys.argv:
        build_sfx_only()
        return
    build_audio()
    silent = OUT.replace(".mp4", "_silent.mp4")
    build_video(silent)
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", silent, "-i", WAV, "-map", "0:v", "-map", "1:a",
                    "-c:v", "copy", "-c:a", "aac", "-b:a", "192k", "-af", "loudnorm=I=-16:TP=-1.5:LRA=11,alimiter=limit=0.79:level=false,aresample=48000",
                    "-shortest", "-movflags", "+faststart", OUT], check=True)
    os.remove(silent)
    print(OUT)


if __name__ == "__main__":
    main()

"""Small offline synthesiser for ad music and sound effects (no samples, no licensing).

All functions return float32 mono arrays at SR; mixing helpers place them on a timeline.
"""
import numpy as np
from scipy.signal import butter, sosfilt

SR = 44100
rng = np.random.default_rng(7)


def t_axis(dur):
    return np.arange(int(dur * SR)) / SR


def midi(n):
    return 440.0 * 2 ** ((n - 69) / 12)


def lowpass(x, cutoff, order=2):
    return sosfilt(butter(order, cutoff, "low", fs=SR, output="sos"), x)


def highpass(x, cutoff, order=2):
    return sosfilt(butter(order, cutoff, "high", fs=SR, output="sos"), x)


def bandpass(x, lo, hi, order=2):
    return sosfilt(butter(order, [lo, hi], "band", fs=SR, output="sos"), x)


def env_adsr(n, a=0.01, d=0.1, s=0.7, r=0.3):
    e = np.ones(n) * s
    ai, di, ri = int(a * SR), int(d * SR), int(r * SR)
    ai = min(ai, n); e[:ai] = np.linspace(0, 1, ai, endpoint=False)
    de = min(ai + di, n); e[ai:de] = np.linspace(1, s, de - ai, endpoint=False)
    if ri and n > ri:
        e[-ri:] *= np.linspace(1, 0, ri)
    return e


# ---- instruments -------------------------------------------------------------------------

def piano(note, dur, vel=0.5):
    t = t_axis(dur)
    f = midi(note)
    out = np.zeros_like(t)
    for k, amp in enumerate([1.0, 0.45, 0.25, 0.12, 0.06, 0.03], start=1):
        decay = np.exp(-t * (1.6 + 0.9 * k))
        out += amp * decay * (np.sin(2 * np.pi * f * k * t) + 0.3 * np.sin(2 * np.pi * f * k * 1.0015 * t))
    out *= np.minimum(1, t / 0.004)
    return (vel * out / 1.6).astype(np.float32)


def pad(notes, dur, vel=0.18, bright=1400):
    t = t_axis(dur)
    out = np.zeros_like(t)
    for n in notes:
        f = midi(n)
        for k in range(1, 5):
            out += (1 / k) * np.sin(2 * np.pi * f * k * t + rng.uniform(0, 6.28))
            out += (0.6 / k) * np.sin(2 * np.pi * f * k * 1.004 * t)
    out = lowpass(out, bright) * env_adsr(len(t), a=0.6, d=0.2, s=0.9, r=0.8)
    return (vel * out / (len(notes) * 2.5)).astype(np.float32)


def bass(note, dur, vel=0.35):
    t = t_axis(dur)
    f = midi(note)
    out = np.sin(2 * np.pi * f * t) + 0.25 * np.sin(4 * np.pi * f * t)
    return (vel * out * env_adsr(len(t), 0.01, 0.2, 0.7, 0.1)).astype(np.float32)


def kick(vel=0.8):
    t = t_axis(0.35)
    f = 45 + 95 * np.exp(-t * 28)
    ph = 2 * np.pi * np.cumsum(f) / SR
    return (vel * np.sin(ph) * np.exp(-t * 9)).astype(np.float32)


def hat(vel=0.18):
    t = t_axis(0.06)
    return (vel * highpass(rng.standard_normal(len(t)), 7000) * np.exp(-t * 70)).astype(np.float32)


def clap(vel=0.3):
    t = t_axis(0.18)
    n = bandpass(rng.standard_normal(len(t)), 900, 3000)
    return (vel * n * np.exp(-t * 22)).astype(np.float32)


# ---- sound effects -----------------------------------------------------------------------

def whoosh(dur=0.5, vel=0.55, up=True):
    t = t_axis(dur)
    noise = rng.standard_normal(len(t))
    lo, hi = bandpass(noise, 200, 900), bandpass(noise, 1500, 7000)
    p = t / dur if up else 1 - t / dur
    body = (1 - p) * lo + p * hi
    shape = np.sin(np.pi * t / dur) ** 2
    return (vel * body * shape).astype(np.float32)


def riser(dur=0.8, vel=0.4):
    t = t_axis(dur)
    noise = rng.standard_normal(len(t))
    out = np.zeros_like(t)
    steps = 8
    for i in range(steps):  # brighten in steps for a sweep
        seg = slice(int(i * len(t) / steps), int((i + 1) * len(t) / steps))
        out[seg] = bandpass(noise, 300 + 500 * i, 1500 + 1200 * i)[seg]
    sweep = np.sin(2 * np.pi * np.cumsum(300 + 900 * (t / dur) ** 2) / SR) * 0.25
    return (vel * (out + sweep) * (t / dur) ** 1.5).astype(np.float32)


def impact(vel=0.8):
    t = t_axis(1.2)
    boom = np.sin(2 * np.pi * (55 + 40 * np.exp(-t * 12)) * t) * np.exp(-t * 3.5)
    burst = lowpass(rng.standard_normal(len(t)), 1800) * np.exp(-t * 10) * 0.4
    return (vel * (boom + burst)).astype(np.float32)


def buzz(vel=0.45):
    parts = []
    for f in (190, 150):
        t = t_axis(0.13)
        sq = np.sign(np.sin(2 * np.pi * f * t)) * 0.6 + np.sin(2 * np.pi * f * t) * 0.4
        parts += [lowpass(sq, 2500) * env_adsr(len(t), 0.005, 0.05, 0.8, 0.03), np.zeros(int(0.04 * SR))]
    return (vel * np.concatenate(parts)).astype(np.float32)


def chime(base=84, vel=0.5):
    t = t_axis(1.4)
    f = midi(base)
    out = np.zeros_like(t)
    for ratio, amp, d in [(1, 1, 2.5), (2.0, 0.5, 3.5), (3.01, 0.3, 5), (4.17, 0.2, 7)]:
        out += amp * np.sin(2 * np.pi * f * ratio * t) * np.exp(-t * d)
    return (vel * out * np.minimum(1, t / 0.003) / 2).astype(np.float32)


def shimmer(vel=0.35):
    out = np.zeros(int(0.9 * SR), np.float32)
    for i, n in enumerate([84, 88, 91, 96, 100]):
        c = chime(n, 1.0)[: int(0.5 * SR)] * 0.5
        st = int(i * 0.06 * SR)
        out[st:st + len(c)] += c[: len(out) - st]
    return (vel * out).astype(np.float32)


def tick(vel=0.35):
    t = t_axis(0.025)
    return (vel * highpass(rng.standard_normal(len(t)), 2500) * np.exp(-t * 300)).astype(np.float32)


def pop(vel=0.5):
    t = t_axis(0.12)
    f = 900 * np.exp(-t * 18) + 250
    return (vel * np.sin(2 * np.pi * np.cumsum(f) / SR) * np.exp(-t * 30)).astype(np.float32)


# ---- mixing ------------------------------------------------------------------------------

def place(track, sound, at):
    i = int(at * SR)
    if i >= len(track):
        return
    n = min(len(sound), len(track) - i)
    track[i:i + n] += sound[:n]


def finish(x, peak_db=-1.0):
    x = np.tanh(x * 1.2) / np.tanh(1.2)  # gentle limiter
    peak = np.max(np.abs(x)) or 1
    return (x / peak * 10 ** (peak_db / 20)).astype(np.float32)


def write_wav(path, mono):
    from scipy.io import wavfile
    stereo = np.stack([mono, mono], axis=1)
    wavfile.write(path, SR, (stereo * 32767).astype(np.int16))

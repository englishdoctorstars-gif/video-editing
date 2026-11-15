"""Stereo sound design for the Vocabsolution ads: sound effects only, no music bed.

Every sound is synthesised here (no samples). The palette is built to avoid fatigue:
 - transitions rotate through four different designs and move across the stereo field;
 - interface sounds are soft tuned mallet/bell notes, so a run of cards plays a short rising
   motif that is different in every scene instead of the same click;
 - weight comes from sub "booms" on the few key moments (opening, the cause, price, CTA);
 - a light room reverb glues everything together.
"""
import numpy as np
from scipy.io import wavfile
from scipy.signal import butter, fftconvolve, sosfilt

SR = 44100
rng = np.random.default_rng(11)


def _t(dur):
    return np.arange(int(dur * SR)) / SR


def _lp(x, f, order=2):
    return sosfilt(butter(order, f, "low", fs=SR, output="sos"), x)


def _hp(x, f, order=2):
    return sosfilt(butter(order, f, "high", fs=SR, output="sos"), x)


def _fade(x, a=0.004, r=0.02):
    na, nr = max(1, int(a * SR)), max(1, int(r * SR))
    x = x.copy()
    x[:na] *= np.linspace(0, 1, na)
    x[-nr:] *= np.linspace(1, 0, nr)
    return x


def hz(midi):
    return 440.0 * 2 ** ((midi - 69) / 12)


# ---- tuned sounds ----------------------------------------------------------------------------

def mallet(midi, vel=0.3, dur=0.7):
    """Soft marimba-like note: warm fundamental, a quick 4th partial, a tiny felt attack."""
    t, f = _t(dur), hz(midi)
    x = np.sin(2 * np.pi * f * t) * np.exp(-t * 7)
    x += 0.35 * np.sin(2 * np.pi * 4 * f * t) * np.exp(-t * 22)
    x += 0.10 * np.sin(2 * np.pi * 9.2 * f * t) * np.exp(-t * 60)
    click = _lp(rng.standard_normal(len(t)), 3500) * np.exp(-t * 400) * 0.25
    return _fade(vel * (x + click))


def bell(midi, vel=0.3, dur=1.8):
    """Glassy bell with slightly inharmonic partials and a long, quiet tail."""
    t, f = _t(dur), hz(midi)
    x = np.zeros(len(t))
    for ratio, amp, dec in [(1, 1.0, 3.2), (2.0, 0.5, 4.2), (3.01, 0.32, 5.6), (4.17, 0.2, 7.5), (5.43, 0.12, 9.5), (6.8, 0.07, 12)]:
        if f * ratio < SR * 0.45:
            x += amp * np.sin(2 * np.pi * f * ratio * t + rng.uniform(0, 6.28)) * np.exp(-t * dec)
    return _fade(vel * 0.6 * x, 0.002, 0.1)


def sparkle(vel=0.2, dur=1.1, count=12, lo=84, hi=103):
    """A scatter of tiny high bells — returns (left, right)."""
    out = np.zeros((2, int((dur + 0.9) * SR)))
    scale = [0, 2, 4, 7, 9]
    for _ in range(count):
        midi = rng.integers(lo, hi)
        midi = midi - (midi % 12) + scale[rng.integers(0, 5)]
        note = bell(midi, vel * rng.uniform(0.5, 1.0), 0.8)
        at = int(rng.uniform(0, dur) ** 1.3 / dur ** 0.3 * SR)
        pan = rng.uniform(-0.8, 0.8)
        out[0, at:at + len(note)] += note * np.cos((pan + 1) * np.pi / 4)
        out[1, at:at + len(note)] += note * np.sin((pan + 1) * np.pi / 4)
    return out


def glide(f0, f1, dur, vel=0.16):
    """A soft sine sliding in pitch (the 'forgetting' fall)."""
    t = _t(dur)
    f = f0 * (f1 / f0) ** (t / dur)
    x = np.sin(2 * np.pi * np.cumsum(f) / SR) + 0.25 * np.sin(4 * np.pi * np.cumsum(f) / SR)
    return _fade(vel * x * np.sin(np.pi * t / dur) ** 0.6, 0.03, 0.15)


# ---- weight ----------------------------------------------------------------------------------

def boom(vel=0.6, dur=1.5, f0=95, f1=36):
    """Cinematic low hit: a falling sub with a soft, dark transient."""
    t = _t(dur)
    f = f1 + (f0 - f1) * np.exp(-t * 9)
    sub = np.sin(2 * np.pi * np.cumsum(f) / SR) * np.exp(-t * 3.6)
    body = _lp(rng.standard_normal(len(t)), 320) * np.exp(-t * 16) * 0.9
    snap = _lp(rng.standard_normal(len(t)), 2400) * np.exp(-t * 90) * 0.35
    return _fade(vel * np.tanh(1.4 * (sub + body + snap)), 0.001, 0.2)


def thud(vel=0.4, f0=130, f1=58):
    """Short, dull knock for the 'pain' cards."""
    t = _t(0.42)
    f = f1 + (f0 - f1) * np.exp(-t * 22)
    x = np.sin(2 * np.pi * np.cumsum(f) / SR) * np.exp(-t * 11)
    x += _lp(rng.standard_normal(len(t)), 700) * np.exp(-t * 60) * 0.5
    return _fade(vel * x, 0.001, 0.05)


def sour(midi, vel=0.11):
    """Two close, muffled notes: a quiet 'that's wrong' colour without a harsh buzzer."""
    t = _t(0.55)
    x = np.sin(2 * np.pi * hz(midi) * t) + np.sin(2 * np.pi * hz(midi + 1) * t)
    return _fade(vel * _lp(x * np.exp(-t * 6), 900), 0.01, 0.1)


def heartbeat(vel=0.45):
    out = np.zeros(int(0.6 * SR))
    for at, v in [(0.0, 1.0), (0.17, 0.7)]:
        b = thud(vel * v, 85, 46)
        i = int(at * SR)
        out[i:i + len(b)] += b[:len(out) - i]
    return out


def woodblock(vel=0.4, f=1500):
    t = _t(0.09)
    x = np.sin(2 * np.pi * f * t) * np.exp(-t * 70) + 0.5 * np.sin(2 * np.pi * f * 1.52 * t) * np.exp(-t * 110)
    return _fade(vel * x, 0.0005, 0.01)


# ---- air -------------------------------------------------------------------------------------

def swept(dur, f0, f1, q=1.6):
    """White noise through a band-pass whose centre glides from f0 to f1 (state-variable filter)."""
    n = int(dur * SR)
    x = rng.standard_normal(n)
    f = np.clip(f0 * (f1 / f0) ** (np.arange(n) / n), 40, 6500)
    fc = 2 * np.sin(np.pi * f / SR)
    low = band = 0.0
    out = np.empty(n)
    for i in range(n):
        low += fc[i] * band
        band += fc[i] * (x[i] - low - band / q)
        out[i] = band
    return out


def swish(dur=0.55, f0=500, f1=4200, vel=0.25, peak=0.62):
    """Airy pass-by. Returns mono; the caller pans it across the field."""
    t = _t(dur)
    env = np.where(t < peak * dur, (t / (peak * dur)) ** 2.2, np.exp(-(t - peak * dur) / (dur * 0.12)))
    return _fade(vel * swept(dur, f0, f1) * env[:int(dur * SR)], 0.01, 0.03)


def swell(dur=0.7, vel=0.2, midi=79):
    """Reverse-cymbal style rise with a faint reversed bell inside."""
    t = _t(dur)
    air = _hp(rng.standard_normal(len(t)), 3000) * (t / dur) ** 3
    tone = bell(midi, 0.5, dur)[::-1]
    return _fade(vel * (0.5 * air + tone[:len(t)]), 0.02, 0.012)


def riser(dur=0.8, vel=0.2):
    t = _t(dur)
    f = 180 * (720 / 180) ** (t / dur)
    tone = np.sin(2 * np.pi * np.cumsum(f) / SR) * (1 + 0.3 * np.sin(2 * np.pi * (6 + 14 * t / dur) * t))
    return _fade(vel * (0.6 * tone + 0.5 * swept(dur, 600, 5000)[:len(t)]) * (t / dur) ** 2, 0.02, 0.01)


# ---- compound sounds -------------------------------------------------------------------------

def register(vel=0.4):
    """Price moment: drawer click, a clear two-note ding and a few coins — returns (left, right)."""
    out = np.zeros((2, int(2.4 * SR)))

    def put(snd, at, pan=0.0):
        i = int(at * SR)
        snd = snd[:out.shape[1] - i]
        out[0, i:i + len(snd)] += snd * np.cos((pan + 1) * np.pi / 4)
        out[1, i:i + len(snd)] += snd * np.sin((pan + 1) * np.pi / 4)

    put(woodblock(vel * 0.7, 900), 0.0)
    put(woodblock(vel * 0.5, 1300), 0.045)
    put(bell(88, vel * 0.9, 1.9), 0.08)
    put(bell(93, vel * 0.8, 2.0), 0.2)
    for k in range(7):   # coins
        put(bell(int(rng.integers(98, 108)), vel * rng.uniform(0.18, 0.32), 0.35), 0.3 + k * 0.055 + rng.uniform(0, 0.02), rng.uniform(-0.7, 0.7))
    return out


def message(vel=0.34):
    """Friendly two-note 'new message' pop for the WhatsApp button."""
    out = np.zeros(int(1.0 * SR))
    for at, midi in [(0.0, 76), (0.1, 83)]:
        n = mallet(midi, vel, 0.6) + bell(midi + 12, vel * 0.25, 0.6)[:int(0.6 * SR)]
        i = int(at * SR)
        out[i:i + len(n)] += n[:len(out) - i]
    return out


# ---- mixing ----------------------------------------------------------------------------------

class Mix:
    def __init__(self, total):
        self.buf = np.zeros((2, int(total * SR) + SR))
        self.total = total

    def add(self, snd, at, pan=0.0, sweep=None):
        """pan in [-1, 1]; sweep=(from, to) moves a mono sound across the field while it plays."""
        i = max(0, int(at * SR))
        if snd.ndim == 2:
            n = min(snd.shape[1], self.buf.shape[1] - i)
            self.buf[:, i:i + n] += snd[:, :n]
            return
        n = min(len(snd), self.buf.shape[1] - i)
        p = np.linspace(sweep[0], sweep[1], len(snd))[:n] if sweep else pan
        self.buf[0, i:i + n] += snd[:n] * np.cos((p + 1) * np.pi / 4)
        self.buf[1, i:i + n] += snd[:n] * np.sin((p + 1) * np.pi / 4)

    def render(self, peak_db=-2.0, wet=0.16):
        t = _t(1.3)
        dry = self.buf
        out = np.empty_like(dry)
        for ch in range(2):   # small bright room, decorrelated left/right
            ir = _hp(_lp(rng.standard_normal(len(t)), 6500), 350) * np.exp(-t * 4.8)
            ir[:int(0.012 * SR)] = 0
            tail = fftconvolve(dry[ch], ir / np.sqrt(np.sum(ir ** 2)))[:dry.shape[1]]
            out[ch] = dry[ch] + wet * tail
        out = _hp(out, 28)
        out *= 10 ** (peak_db / 20) / np.max(np.abs(out))
        out = out[:, :int(self.total * SR)]
        fade = int(0.6 * SR)
        out[:, -fade:] *= np.linspace(1, 0, fade)
        return out


def write(path, stereo):
    wavfile.write(path, SR, (np.clip(stereo.T, -1, 1) * 32767).astype(np.int16))


# ---- the score -------------------------------------------------------------------------------

PENTA = [72, 74, 76, 79, 81, 84, 86, 88]
MOTIFS = [(0, 2, 4), (1, 2, 5), (0, 3, 4), (2, 3, 5), (1, 4, 5), (0, 2, 5), (2, 4, 6), (1, 3, 6), (3, 5, 7)]


def transition(mix, at, k):
    """Four designs in rotation; all travel right → left with the wipe."""
    kind = k % 4
    if kind == 0:
        mix.add(swish(0.6, 450, 4600, 0.26), at - 0.08, sweep=(0.8, -0.8))
    elif kind == 1:
        mix.add(swell(0.55, 0.16, 76 + (k % 3) * 2), at - 0.3, sweep=(0.5, -0.3))
        mix.add(thud(0.22, 100, 50), at + 0.24)
    elif kind == 2:
        mix.add(swish(0.5, 3800, 380, 0.24, peak=0.4), at - 0.02, sweep=(0.8, -0.8))
        mix.add(boom(0.2, 0.8, 70, 40), at + 0.2)
    else:
        mix.add(swish(0.32, 900, 5200, 0.17), at - 0.05, sweep=(0.9, 0.1))
        mix.add(swish(0.4, 700, 4400, 0.22), at + 0.12, sweep=(0.3, -0.9))


def score(layers, wipes, total, tail):
    """layers: (name, t_in, t_out, animation, kind) from the video builder."""
    mix = Mix(total)
    for k, w in enumerate(wipes):
        transition(mix, w, k)
    clock, row = 0, 0
    for name, at, _, anim, kind in layers:
        if not kind:
            continue
        pan = {"left": -0.4, "right": 0.4}.get(anim, 0.0)
        scene = name.split("_")[0]
        if scene.startswith("m") and kind.startswith("note:"):       # feature cards: a motif per scene
            i, j = int(scene[1:]), int(name[-1])
            midi = PENTA[MOTIFS[i % len(MOTIFS)][j]]
            mix.add(swish(0.22, 1500, 5200, 0.07), at - 0.04, sweep=(pan * 2, pan))
            mix.add(mallet(midi, 0.3) if i % 2 == 0 else bell(midi, 0.22, 1.0), at + 0.1, pan)
            if j == 2:
                mix.add(bell(midi + 12, 0.1, 1.2), at + 0.16, -pan)
        elif scene.startswith("m") and kind == "whoosh":             # phone floats up
            mix.add(swish(0.5, 220, 1500, 0.2), at, sweep=(-0.3, 0.3))
        elif scene.startswith("m"):
            continue                                                  # titles ride on the transition
        elif kind == "clock":
            mix.add(woodblock(0.42, 1500 if clock < 2 else 2100), at)
            mix.add(heartbeat(0.42 + 0.08 * clock), at)
            clock += 1
        elif kind == "impact" and name == "a_stop":
            mix.add(boom(0.6), at)
            mix.add(bell(67, 0.2, 2.2), at)
        elif kind == "impact" and name == "d_truth":
            mix.add(riser(0.75, 0.18), at - 0.75)
            mix.add(boom(0.68), at)
            mix.add(bell(72, 0.2, 2.4), at + 0.02)
        elif kind == "impact":                                        # CTA button
            mix.add(boom(0.42, 1.2), at)
            mix.add(message(), at + 0.12)
        elif kind == "impact_soft":
            mix.add(boom(0.3, 1.0, 80, 42), at)
        elif kind == "win":
            for j, midi in enumerate((84, 88, 91, 96)):
                mix.add(bell(midi, 0.26, 2.0), at + j * 0.085, (-0.4, 0.4, -0.2, 0.2)[j])
            mix.add(sparkle(0.16, 0.9), at + 0.3)
            mix.add(boom(0.28, 1.0, 80, 44), at)
        elif kind == "buzz":
            mix.add(thud(0.42 - 0.03 * row), at + 0.05, pan)
            mix.add(sour(55 - 2 * row), at + 0.05, pan)
            row += 1
        elif kind == "fall":
            mix.add(glide(760, 170, 1.0, 0.15), at, sweep=(-0.4, 0.4))
        elif kind == "cash":
            mix.add(riser(0.5, 0.14), at - 0.5)
            mix.add(boom(0.55, 1.4), at)
            mix.add(register(0.42), at + 0.03)
        elif kind == "slash":
            mix.add(swish(0.16, 5200, 1400, 0.24, peak=0.3), at, sweep=(-0.6, 0.6))
            mix.add(thud(0.16, 160, 90), at + 0.1)
        elif kind == "shimmer":
            mix.add(sparkle(0.2, 1.0), at)
        elif kind == "chime":
            mix.add(bell({"b_split": 88, "c_2": 91, "e_cap": 86}.get(name, 88), 0.26, 1.8), at + 0.05)
        elif kind == "whoosh":
            mix.add(swish(0.45, 300, 2400, 0.17), at, sweep=(-0.4, 0.4))
        elif kind == "whoosh_soft":
            if name in ("a_q", "e_axes"):
                mix.add(swish(0.35, 800, 3800, 0.1), at, sweep=(0.4, -0.4))
        elif kind == "pop":
            if scene == "h" and name.startswith("h_row"):             # checklist: rising ticks
                j = int(name[-1])
                mix.add(woodblock(0.16, 2200), at + 0.08, pan)
                mix.add(mallet((76, 79, 84, 88)[j], 0.26), at + 0.1, pan)
            elif name == "g_lab":
                for j, midi in enumerate((79, 84, 88)):
                    mix.add(mallet(midi, 0.24), at + 0.05 + j * 0.09, (-0.5, 0.0, 0.5)[j])
            else:
                mix.add(mallet({"a_word": 79, "b_word": 76, "h_sub": 91, "i_pay": 84}.get(name, 81), 0.28), at + 0.06)
        elif kind.startswith("note:"):                                # review bumps on the curve
            midi = int(kind[5:]) + 12
            mix.add(mallet(midi, 0.28), at, pan)
            mix.add(bell(midi + 12, 0.08, 0.9), at, pan)
    for at in (43.3 + tail, 44.5 + tail):                             # the button's pulse
        mix.add(thud(0.14, 150, 90), at)
        mix.add(mallet(83, 0.12), at)
    mix.add(sparkle(0.16, 1.2, 14), 45.2 + tail)
    mix.add(bell(72, 0.18, 2.0), 45.3 + tail)
    return mix.render()

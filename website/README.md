# English Doctor — website

Static landing page for the English Doctor domain (IELTS, Spoken English, Vocabsolution).
Colours come from the logo: royal blue `#0b45c9`, deep navy `#06277a`, golden yellow `#ffc81f`, white.

No build step. Open `index.html`, or upload the `website/` folder to any static host
(GitHub Pages, Netlify, cPanel `public_html`) and point the domain at it.

## Sections
Hero · Stats · Courses (level filter) · Mock Test · Our Books · Success Stories ·
Study Abroad Services · Free Resources · About / Mission · FAQ · Get in Touch · Footer.
Content follows the English Doctor BD reference site (engdoctorbd-zxkh2hvf.manus.space).

Contact: +880 1682-888114 · englishdoctorbd@gmail.com · Dhaka, Bangladesh.
The contact form needs no backend: it opens WhatsApp or the visitor's email app
pre-filled. "Enroll" buttons pre-select the course in the form.

## Pages
`index.html` (home) · `privacy.html` · `terms.html` · `404.html` · `robots.txt`

## Social and app links
Edit `LINKS` at the top of `main.js` (Facebook, YouTube, Instagram, LinkedIn, Google Play).
Any link left empty is hidden on every page, so the site never shows a dead link.
WhatsApp, phone and email are already live.

## Publishing (GitHub Pages)
`.github/workflows/website.yml` publishes the `website/` folder whenever it changes on the
repository's default branch (or run it by hand from the Actions tab).
1. Repo Settings → Pages → Source: **GitHub Actions**.
2. Merge this branch into the default branch.
3. For your own domain: Settings → Pages → Custom domain, then add the DNS records GitHub shows.
   `404.html` uses root paths (`/styles.css`), so it looks right on a custom domain.

Any other static host works too: upload the contents of `website/`.

## Optional extras
- A real mentor photo in the About section (currently the logo)
- A photo of the Vocabsolution book cover (currently drawn in CSS)

// Renders the cover HTML files to marketing/cover/ as print PDFs and 300-dpi PNGs.
//   node tools/marketing/build_cover.js
const { chromium } = require('playwright');
const path = require('path');
const COVERS = [
  { html: 'cover.html', out: 'vocabsolution_cover', w: 5.5, h: 8.5 },
  { html: 'cover_nutbolt.html', out: 'spoken_english_nut_bolt_cover', w: 8.5, h: 11 },
];
(async () => {
  const root = path.resolve(__dirname, '../..');
  const browser = await chromium.launch();
  for (const c of COVERS) {
    const page = await browser.newPage({ viewport: { width: c.w * 96, height: c.h * 96 }, deviceScaleFactor: 3.125 });
    await page.goto('file://' + path.join(__dirname, c.html));
    await page.evaluate(() => document.fonts.ready);
    await page.screenshot({ path: path.join(root, `marketing/cover/${c.out}.png`) });
    await page.pdf({ path: path.join(root, `marketing/cover/${c.out}.pdf`), width: c.w + 'in', height: c.h + 'in', printBackground: true });
    await page.close();
  }
  await browser.close();
})();

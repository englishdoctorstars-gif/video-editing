// Renders tools/marketing/cover.html to marketing/cover/ as a print PDF (5.5×8.5 in) and a 300-dpi PNG.
//   node tools/marketing/build_cover.js
const { chromium } = require('playwright');
const path = require('path');
(async () => {
  const root = path.resolve(__dirname, '../..');
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 528, height: 816 }, deviceScaleFactor: 3.125 });
  await page.goto('file://' + path.join(__dirname, 'cover.html'));
  await page.evaluate(() => document.fonts.ready);
  await page.screenshot({ path: path.join(root, 'marketing/cover/vocabsolution_cover.png') });
  await page.pdf({ path: path.join(root, 'marketing/cover/vocabsolution_cover.pdf'), width: '5.5in', height: '8.5in', printBackground: true });
  await browser.close();
})();

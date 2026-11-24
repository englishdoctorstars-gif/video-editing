// English Doctor — small progressive enhancements (site works without JS)
(function () {
  // Change this to the real WhatsApp number (country code, digits only)
  var WHATSAPP_NUMBER = '8801682888114';
  var EMAIL = 'englishdoctorbd@gmail.com';

  // Social / app links. Paste a URL to show that link everywhere on the page;
  // links left empty stay hidden, so the site never shows a dead link.
  var LINKS = {
    facebook: 'https://www.facebook.com/share/1CMyw5BAVg/?mibextid=wwXIfr',
    youtube: '',
    instagram: '',
    linkedin: '',
    play: ''   // Google Play page of the Vocabsolution app, once it is live
  };
  document.querySelectorAll('[data-link]').forEach(function (a) {
    var url = LINKS[a.dataset.link];
    if (url) a.href = url; else a.hidden = true;
  });

  document.getElementById('yr').textContent = new Date().getFullYear();

  // Mobile menu
  var toggle = document.querySelector('.nav-toggle');
  var nav = document.querySelector('.nav');
  toggle.addEventListener('click', function () {
    var open = nav.classList.toggle('is-open');
    toggle.setAttribute('aria-expanded', open);
  });
  nav.addEventListener('click', function (e) {
    if (e.target.tagName === 'A') { nav.classList.remove('is-open'); toggle.setAttribute('aria-expanded', false); }
  });

  // Course filter tabs
  var tabs = document.querySelectorAll('.tab');
  var cards = document.querySelectorAll('.card');
  tabs.forEach(function (tab) {
    tab.addEventListener('click', function () {
      tabs.forEach(function (t) { t.classList.remove('is-active'); });
      tab.classList.add('is-active');
      var f = tab.dataset.filter;
      cards.forEach(function (c) {
        c.hidden = f !== 'all' && c.dataset.mode.split(' ').indexOf(f) === -1;
      });
    });
  });

  function toBn(n) { return String(n); }

  function countUp(el) {
    var target = +el.dataset.count, start = null;
    function step(ts) {
      if (!start) start = ts;
      var p = Math.min((ts - start) / 1400, 1);
      el.textContent = toBn(Math.floor(target * (1 - Math.pow(1 - p, 3))));
      if (p < 1) requestAnimationFrame(step);
    }
    requestAnimationFrame(step);
  }

  // Count-up animation for the stats (numbers are already correct in the HTML)
  var counters = document.querySelectorAll('[data-count]');
  var reduce = window.matchMedia && matchMedia('(prefers-reduced-motion: reduce)').matches;
  if ('IntersectionObserver' in window && !reduce) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) { countUp(en.target); io.unobserve(en.target); }
      });
    }, { threshold: 0.5 });
    counters.forEach(function (el) { io.observe(el); });
  }

  // "Enroll" buttons pre-select the course in the contact form
  var form = document.getElementById('contactForm');
  document.querySelectorAll('[data-course]').forEach(function (a) {
    a.addEventListener('click', function () {
      var sel = form.elements.subject, v = a.dataset.course;
      for (var k = 0; k < sel.options.length; k++) if (sel.options[k].text === v) sel.selectedIndex = k;
    });
  });

  // Contact form: build the message, then offer real WhatsApp / email links
  var result = document.getElementById('formResult');
  var preview = document.getElementById('msgPreview');
  form.addEventListener('submit', function (e) {
    e.preventDefault();
    var d = new FormData(form);
    var body = 'Name: ' + d.get('name') + '\nEmail: ' + d.get('email') +
      (d.get('phone') ? '\nPhone: ' + d.get('phone') : '') + '\nSubject: ' + d.get('subject') +
      '\n\n' + d.get('message');
    document.getElementById('sendWa').href = 'https://wa.me/' + WHATSAPP_NUMBER + '?text=' + encodeURIComponent(body);
    document.getElementById('sendMail').href = 'mailto:' + EMAIL + '?subject=' + encodeURIComponent(d.get('subject')) + '&body=' + encodeURIComponent(body);
    preview.value = body;
    result.hidden = false;
    result.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth', block: 'nearest' });
  });
  document.getElementById('copyMsg').addEventListener('click', function (ev) {
    var btn = ev.currentTarget;
    function done() { btn.textContent = 'Copied'; setTimeout(function () { btn.textContent = 'Copy message'; }, 1500); }
    function fallback() { preview.focus(); preview.select(); }
    try {
      if (navigator.clipboard) navigator.clipboard.writeText(preview.value).then(done, fallback); else fallback();
    } catch (err) { fallback(); }
  });
  // ===== Shop: bKash / Nagad payment (one-tap send) =====
  var PAY_NUMBER = '01682888114';
  var bookSel = document.getElementById('p-book');
  var due = document.getElementById('payDue');
  var guide = document.getElementById('pay-guide');
  var guideApp = document.getElementById('guideApp');
  var guideAmount = document.getElementById('guideAmount');
  var openApp = document.getElementById('openApp');
  var methodInput = document.getElementById('p-method');
  var isAndroid = /Android/i.test(navigator.userAgent);
  var bnDigits = '০১২৩৪৫৬৭৮৯';
  function bn(n) { return String(n).replace(/\d/g, function (d) { return bnDigits[d]; }); }
  function price() { return bookSel.options[bookSel.selectedIndex].dataset.price; }
  function syncPrice() {
    due.innerHTML = 'পাঠাতে হবে: <b>৳' + bn(price()) + '</b>';
    guideAmount.textContent = '৳' + bn(price());
  }
  bookSel.addEventListener('change', syncPrice);
  document.querySelectorAll('[data-book]').forEach(function (a) {
    a.addEventListener('click', function () { bookSel.value = a.dataset.book; syncPrice(); });
  });

  // Resolves true only when the number really reached the clipboard
  function copyNumber() {
    function legacy() {
      try {
        var t = document.createElement('textarea');
        t.value = PAY_NUMBER; t.setAttribute('readonly', ''); t.style.position = 'fixed'; t.style.opacity = '0';
        document.body.appendChild(t); t.select();
        var ok = document.execCommand('copy');
        document.body.removeChild(t);
        return !!ok;
      } catch (err) { return false; }
    }
    try {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        return navigator.clipboard.writeText(PAY_NUMBER).then(function () { return true; }, function () { return legacy(); });
      }
    } catch (err) { /* fall through */ }
    return Promise.resolve(legacy());
  }
  var guideCopy = document.getElementById('guideCopy');
  function showCopyResult(ok) {
    guideCopy.innerHTML = ok
      ? '✓ নম্বর <b>01682-888114</b> কপি হয়েছে'
      : 'নম্বরটি লিখে নিন: <b>01682-888114</b>';
    guideCopy.classList.toggle('payguide__ok--manual', !ok);
  }

  document.querySelectorAll('[data-wallet]').forEach(function (w) {
    w.addEventListener('click', function (e) {
      e.preventDefault();
      var name = w.dataset.wallet;
      copyNumber().then(showCopyResult);
      methodInput.value = name;
      guideApp.textContent = name;
      syncPrice();
      document.querySelectorAll('[data-wallet]').forEach(function (x) { x.classList.toggle('is-picked', x === w); });
      guide.hidden = false;
      if (isAndroid) {
        // Button opens the wallet app on Android only when the buyer taps it (after reading the steps)
        openApp.href = 'intent:#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;package=' + w.dataset.app + ';end';
        openApp.textContent = name + ' অ্যাপ খুলুন';
        openApp.hidden = false;
      }
      guide.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth', block: 'nearest' });
    });
  });

  var payForm = document.getElementById('payForm');
  var payResult = document.getElementById('payResult');
  var payPreview = document.getElementById('payPreview');
  payForm.addEventListener('submit', function (e) {
    e.preventDefault();
    var d = new FormData(payForm);
    var item = bookSel.options[bookSel.selectedIndex].text;
    var msg = 'আসসালামু আলাইকুম, আমি পেমেন্ট করেছি।\n' +
      'কিনেছি: ' + item + '\n' +
      'মাধ্যম: ' + d.get('method') + ' (Send Money to ' + PAY_NUMBER + ')\n' +
      'টাকা: ৳' + price() + '\n' +
      'TrxID: ' + String(d.get('trx')).trim().toUpperCase() + '\n' +
      'নাম: ' + d.get('name');
    var url = 'https://wa.me/' + WHATSAPP_NUMBER + '?text=' + encodeURIComponent(msg);
    document.getElementById('payWa').href = url;
    payPreview.value = msg;
    payResult.hidden = false;
    payResult.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth', block: 'nearest' });
  });
  document.getElementById('payCopy').addEventListener('click', function (ev) {
    var btn = ev.currentTarget;
    try {
      navigator.clipboard.writeText(payPreview.value).then(function () {
        btn.textContent = 'কপি হয়েছে ✓'; setTimeout(function () { btn.textContent = 'বার্তা কপি করুন'; }, 1500);
      }, function () { payPreview.focus(); payPreview.select(); });
    } catch (err) { payPreview.focus(); payPreview.select(); }
  });
})();


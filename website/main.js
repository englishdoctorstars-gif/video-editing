// English Doctor — small progressive enhancements (site works without JS)
(function () {
  // Change this to the real WhatsApp number (country code, digits only)
  var WHATSAPP_NUMBER = '8801682888114';
  var EMAIL = 'englishdoctorbd@gmail.com';

  // Social / app links. Paste a URL to show that link everywhere on the page;
  // links left empty stay hidden, so the site never shows a dead link.
  var LINKS = {
    facebook: '',
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
})();

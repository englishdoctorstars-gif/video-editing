// English Doctor — small progressive enhancements (site works without JS)
(function () {
  // Change this to the real WhatsApp number (country code, digits only)
  var WHATSAPP_NUMBER = '8801XXXXXXXXX';

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

  // Bangla digits for counters
  var bn = '০১২৩৪৫৬৭৮৯';
  function toBn(n) { return n.toLocaleString('en-IN').replace(/\d/g, function (d) { return bn[d]; }); }

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

  // Reveal on scroll + counters
  var revealEls = document.querySelectorAll('.card, .feature, .free__item, .review, .path li, details');
  revealEls.forEach(function (el) { el.classList.add('reveal'); });
  var counters = document.querySelectorAll('[data-count]');

  if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (!en.isIntersecting) return;
        if (en.target.dataset.count) countUp(en.target); else en.target.classList.add('is-in');
        io.unobserve(en.target);
      });
    }, { threshold: 0.15 });
    revealEls.forEach(function (el) { io.observe(el); });
    counters.forEach(function (el) { io.observe(el); });
  } else {
    revealEls.forEach(function (el) { el.classList.add('is-in'); });
    counters.forEach(function (el) { el.textContent = toBn(+el.dataset.count); });
  }

  // Enrollment form -> WhatsApp
  document.getElementById('enrollForm').addEventListener('submit', function (e) {
    e.preventDefault();
    var d = new FormData(e.target);
    var msg = 'আসসালামু আলাইকুম, আমি ভর্তি হতে চাই।\nনাম: ' + d.get('name') +
      '\nমোবাইল: ' + d.get('phone') + '\nকোর্স: ' + d.get('course');
    window.open('https://wa.me/' + WHATSAPP_NUMBER + '?text=' + encodeURIComponent(msg), '_blank');
  });

  document.querySelector('.wa-float').href = 'https://wa.me/' + WHATSAPP_NUMBER;
})();

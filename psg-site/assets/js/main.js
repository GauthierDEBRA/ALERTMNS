// PSG Fan Site - JS principal

// Inject splash screen + scroll bar immediately
(function () {
  const splash = document.createElement("div");
  splash.className = "splash";
  splash.id = "splash";
  splash.innerHTML = `
    <svg class="splash-crest" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="splashBg" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stop-color="#0b5a94"/>
          <stop offset="1" stop-color="#002547"/>
        </linearGradient>
        <linearGradient id="splashGold" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#f5e59a"/>
          <stop offset="0.5" stop-color="#d4af37"/>
          <stop offset="1" stop-color="#8a6d15"/>
        </linearGradient>
      </defs>
      <circle cx="50" cy="50" r="47" fill="url(#splashBg)" stroke="url(#splashGold)" stroke-width="3"/>
      <circle cx="50" cy="50" r="40" fill="none" stroke="#DA291C" stroke-width="2"/>
      <text x="50" y="44" text-anchor="middle" fill="#fff" font-family="Arial Black, sans-serif" font-weight="900" font-size="14">PARIS</text>
      <text x="50" y="60" text-anchor="middle" fill="#d4af37" font-family="Arial Black, sans-serif" font-weight="900" font-size="16" letter-spacing="2">SG</text>
      <text x="50" y="73" text-anchor="middle" fill="#fff" font-family="Arial, sans-serif" font-weight="700" font-size="6" opacity="0.85">EST. 1970</text>
    </svg>
    <div class="splash-text">Ici c'est Paris</div>
  `;
  document.documentElement.appendChild(splash);

  const bar = document.createElement("div");
  bar.className = "scroll-bar";
  bar.id = "scroll-bar";
  document.documentElement.appendChild(bar);

  window.addEventListener("load", () => {
    setTimeout(() => splash.classList.add("hidden"), 1400);
  });

  window.addEventListener("scroll", () => {
    const h = document.documentElement;
    const pct = (h.scrollTop / (h.scrollHeight - h.clientHeight)) * 100;
    bar.style.width = pct + "%";
  });
})();

document.addEventListener("DOMContentLoaded", () => {
  // Nav toggle mobile
  const toggle = document.querySelector(".nav-toggle");
  const links = document.querySelector(".nav-links");
  if (toggle && links) toggle.addEventListener("click", () => links.classList.toggle("open"));

  // Inject animated crest SVG into all .nav-logo elements
  const crestSvg = `
    <svg class="crest-svg" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="crestBg" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stop-color="#0b5a94"/>
          <stop offset="1" stop-color="#002547"/>
        </linearGradient>
        <linearGradient id="crestGold" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#f5e59a"/>
          <stop offset="0.5" stop-color="#d4af37"/>
          <stop offset="1" stop-color="#8a6d15"/>
        </linearGradient>
      </defs>
      <circle cx="50" cy="50" r="47" fill="url(#crestBg)" stroke="url(#crestGold)" stroke-width="3"/>
      <circle cx="50" cy="50" r="40" fill="none" stroke="#DA291C" stroke-width="2"/>
      <path d="M25 35 L50 20 L75 35 L75 65 L50 80 L25 65 Z" fill="#DA291C" opacity="0.3"/>
      <text x="50" y="44" text-anchor="middle" fill="#fff" font-family="Arial Black, sans-serif" font-weight="900" font-size="14" letter-spacing="1">PARIS</text>
      <text x="50" y="60" text-anchor="middle" fill="#d4af37" font-family="Arial Black, sans-serif" font-weight="900" font-size="16" letter-spacing="2">SG</text>
      <text x="50" y="73" text-anchor="middle" fill="#fff" font-family="Arial, sans-serif" font-weight="700" font-size="6" letter-spacing="1" opacity="0.85">EST. 1970</text>
    </svg>`;
  document.querySelectorAll(".nav-logo").forEach(el => { el.innerHTML = crestSvg; });

  // Inject Eiffel tower SVG + particles into hero
  const hero = document.querySelector(".hero");
  if (hero) {
    const eiffelSvg = `
      <svg class="hero-bg-svg" viewBox="0 0 1400 500" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYEnd meet">
        <g fill="#fff">
          <!-- Eiffel Tower silhouette -->
          <path d="M690 50 L710 50 L712 90 L716 130 L722 180 L700 180 L678 180 L684 130 L688 90 Z"/>
          <path d="M678 180 L722 180 L730 240 L670 240 Z"/>
          <path d="M660 240 L740 240 L755 320 L645 320 Z"/>
          <path d="M635 320 L765 320 L790 430 L610 430 Z"/>
          <rect x="600" y="430" width="200" height="10"/>
          <!-- Base legs -->
          <path d="M605 440 L640 440 L650 495 L585 495 Z" opacity="0.85"/>
          <path d="M760 440 L795 440 L815 495 L750 495 Z" opacity="0.85"/>
          <!-- Lattice lines -->
          <g stroke="#fff" stroke-width="0.8" opacity="0.5" fill="none">
            <line x1="684" y1="130" x2="716" y2="130"/>
            <line x1="680" y1="150" x2="720" y2="150"/>
            <line x1="678" y1="180" x2="722" y2="180"/>
            <line x1="670" y1="210" x2="730" y2="210"/>
            <line x1="660" y1="270" x2="740" y2="270"/>
            <line x1="650" y1="300" x2="750" y2="300"/>
            <line x1="635" y1="360" x2="765" y2="360"/>
            <line x1="620" y1="395" x2="780" y2="395"/>
          </g>
          <!-- Skyline buildings -->
          <rect x="50" y="400" width="80" height="95" opacity="0.35"/>
          <rect x="135" y="370" width="60" height="125" opacity="0.35"/>
          <rect x="200" y="420" width="50" height="75" opacity="0.35"/>
          <rect x="255" y="385" width="90" height="110" opacity="0.35"/>
          <rect x="350" y="405" width="70" height="90" opacity="0.35"/>
          <rect x="425" y="390" width="60" height="105" opacity="0.35"/>
          <rect x="490" y="415" width="90" height="80" opacity="0.35"/>
          <rect x="830" y="410" width="70" height="85" opacity="0.35"/>
          <rect x="905" y="385" width="60" height="110" opacity="0.35"/>
          <rect x="970" y="400" width="80" height="95" opacity="0.35"/>
          <rect x="1055" y="420" width="50" height="75" opacity="0.35"/>
          <rect x="1110" y="380" width="90" height="115" opacity="0.35"/>
          <rect x="1205" y="410" width="70" height="85" opacity="0.35"/>
          <rect x="1280" y="395" width="60" height="100" opacity="0.35"/>
        </g>
      </svg>`;
    hero.insertAdjacentHTML("afterbegin", eiffelSvg);

    // Floating gold particles
    const particles = document.createElement("div");
    particles.className = "hero-particles";
    for (let i = 0; i < 25; i++) {
      const p = document.createElement("span");
      p.style.left = Math.random() * 100 + "%";
      p.style.bottom = "-10px";
      p.style.animationDelay = (Math.random() * 12) + "s";
      p.style.animationDuration = (10 + Math.random() * 8) + "s";
      p.style.width = p.style.height = (2 + Math.random() * 4) + "px";
      particles.appendChild(p);
    }
    hero.appendChild(particles);
  }

  // Countdown to next match
  const countdownEl = document.getElementById("hero-countdown");
  if (countdownEl) {
    const next = PSG_DATA.matches.find(m => m.status === "upcoming");
    if (next) {
      // Parse French date "15 AVR 2026"
      const months = { JAN:0,FEV:1,MAR:2,AVR:3,MAI:4,JUN:5,JUL:6,AOU:7,SEP:8,OCT:9,NOV:10,DEC:11 };
      const [d, m, y] = next.date.split(" ");
      const target = new Date(+y, months[m] || 0, +d, 21, 0, 0);
      const render = () => {
        const diff = target - new Date();
        if (diff <= 0) { countdownEl.innerHTML = '<span class="countdown-intro">Match en cours !</span>'; return; }
        const dd = Math.floor(diff / 86400000);
        const hh = Math.floor((diff % 86400000) / 3600000);
        const mm = Math.floor((diff % 3600000) / 60000);
        const ss = Math.floor((diff % 60000) / 1000);
        countdownEl.innerHTML = `
          <div style="width:100%">
            <span class="countdown-intro">⏱ Prochain match · ${next.home} vs ${next.away}</span>
            <div style="display:flex;gap:.5rem;flex-wrap:wrap">
              <div class="countdown-unit"><div class="countdown-value">${dd}</div><div class="countdown-label">Jours</div></div>
              <div class="countdown-unit"><div class="countdown-value">${String(hh).padStart(2,"0")}</div><div class="countdown-label">Heures</div></div>
              <div class="countdown-unit"><div class="countdown-value">${String(mm).padStart(2,"0")}</div><div class="countdown-label">Min</div></div>
              <div class="countdown-unit"><div class="countdown-value">${String(ss).padStart(2,"0")}</div><div class="countdown-label">Sec</div></div>
            </div>
          </div>`;
      };
      render();
      setInterval(render, 1000);
    }
  }

  // Animated counters (big-numbers)
  const counters = document.querySelectorAll(".big-number-value[data-count]");
  if (counters.length) {
    const animate = el => {
      const target = parseFloat(el.dataset.count);
      const suffix = el.dataset.suffix || "";
      const duration = 1600;
      const start = performance.now();
      const step = now => {
        const progress = Math.min((now - start) / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        const value = target * eased;
        el.textContent = (Number.isInteger(target) ? Math.floor(value) : value.toFixed(1)) + suffix;
        if (progress < 1) requestAnimationFrame(step);
        else el.textContent = target + suffix;
      };
      requestAnimationFrame(step);
    };
    const obs = new IntersectionObserver(entries => {
      entries.forEach(e => { if (e.isIntersecting) { animate(e.target); obs.unobserve(e.target); } });
    }, { threshold: 0.4 });
    counters.forEach(c => obs.observe(c));
  }

  // Formation pitch render
  const pitchEl = document.getElementById("pitch");
  if (pitchEl && PSG_DATA.formation) {
    const f = PSG_DATA.formation;
    let html = `
      <div class="pitch-formation-label">${f.label}</div>
      <div class="pitch-coach-label">Coach · ${f.coach}</div>
      <div class="pitch-lines"></div>
      <div class="pitch-box pitch-box-top"></div>
      <div class="pitch-box pitch-box-bot"></div>
      <div class="pitch-box-small top"></div>
      <div class="pitch-box-small bot"></div>
    `;
    f.xi.forEach(pl => {
      const full = PSG_DATA.players.find(p => p.id === pl.id);
      html += `
        <div class="pitch-player" style="left:${pl.x}%;top:${pl.y}%" data-id="${pl.id}">
          <div class="pitch-player-circle">${full?.num ?? "?"}</div>
          <div class="pitch-player-name">${pl.short}</div>
        </div>`;
    });
    pitchEl.innerHTML = html;
    pitchEl.querySelectorAll(".pitch-player").forEach(p =>
      p.addEventListener("click", () => openPlayerModal(+p.dataset.id))
    );
  }

  // ===== CURSOR TRAIL (desktop only) =====
  if (window.matchMedia("(hover: hover)").matches) {
    let lastSpark = 0;
    document.addEventListener("mousemove", e => {
      const now = Date.now();
      if (now - lastSpark < 40) return;
      lastSpark = now;
      const s = document.createElement("div");
      s.className = "cursor-spark";
      s.style.left = e.clientX + "px";
      s.style.top = e.clientY + "px";
      document.body.appendChild(s);
      setTimeout(() => s.remove(), 800);
    });
  }

  // ===== CONFETTI BURST =====
  const fireConfetti = (x, y) => {
    const colors = ["#DA291C", "#002547", "#d4af37", "#ffffff", "#0b5a94"];
    for (let i = 0; i < 50; i++) {
      const c = document.createElement("div");
      c.className = "confetti";
      c.style.left = x + "px";
      c.style.top = y + "px";
      c.style.background = colors[i % colors.length];
      c.style.animationDelay = (Math.random() * 0.3) + "s";
      c.style.animationDuration = (2 + Math.random() * 1.5) + "s";
      const dx = (Math.random() - 0.5) * 600;
      c.style.setProperty("transform", `translateX(${dx}px)`);
      c.animate(
        [
          { transform: `translate(0, 0) rotate(0deg)`, opacity: 1 },
          { transform: `translate(${dx}px, ${400 + Math.random() * 300}px) rotate(${Math.random() * 720}deg)`, opacity: 0 }
        ],
        { duration: 2000 + Math.random() * 1000, easing: "cubic-bezier(.4,.2,.2,1)" }
      );
      document.body.appendChild(c);
      setTimeout(() => c.remove(), 3500);
    }
  };
  window.__fireConf = fireConfetti;
  document.querySelectorAll(".trophy-card").forEach(card => {
    card.addEventListener("mouseenter", () => {
      const r = card.getBoundingClientRect();
      fireConfetti(r.left + r.width / 2, r.top + r.height / 2);
    });
    card.addEventListener("click", () => {
      const r = card.getBoundingClientRect();
      fireConfetti(r.left + r.width / 2, r.top + r.height / 2);
    });
  });

  // ===== PARALLAX on hero Eiffel =====
  const heroBg = document.querySelector(".hero-bg-svg");
  if (heroBg) {
    window.addEventListener("scroll", () => {
      const scroll = window.scrollY;
      if (scroll < 800) {
        heroBg.style.transform = `translateX(-50%) translateY(${scroll * 0.35}px)`;
      }
    });
  }

  // ===== THEME TOGGLE =====
  const navBar = document.querySelector(".nav");
  if (navBar && !document.querySelector(".theme-toggle")) {
    const btn = document.createElement("button");
    btn.className = "theme-toggle";
    btn.innerHTML = "🌙";
    btn.setAttribute("aria-label", "Changer le thème");
    btn.addEventListener("click", () => {
      document.body.classList.toggle("dark-theme");
      btn.innerHTML = document.body.classList.contains("dark-theme") ? "☀️" : "🌙";
      try { localStorage.setItem("psg-theme", document.body.classList.contains("dark-theme") ? "dark" : "light"); } catch(e){}
    });
    navBar.appendChild(btn);
    try {
      if (localStorage.getItem("psg-theme") === "dark") {
        document.body.classList.add("dark-theme");
        btn.innerHTML = "☀️";
      }
    } catch(e){}
  }

  // ===== QUOTE CAROUSEL =====
  const quoteWrap = document.getElementById("quote-carousel");
  if (quoteWrap && PSG_DATA.quotes) {
    let idx = 0;
    const slides = PSG_DATA.quotes.map((q, i) =>
      `<div class="quote-slide${i === 0 ? " active" : ""}" data-i="${i}">
        <div class="quote-text">" ${q.text} "</div>
        <div class="quote-author">— ${q.author}</div>
      </div>`).join("");
    const dots = PSG_DATA.quotes.map((_, i) =>
      `<button class="quote-dot${i === 0 ? " active" : ""}" data-i="${i}" aria-label="Citation ${i+1}"></button>`).join("");
    quoteWrap.innerHTML = `
      <div class="quote-inner">
        <div class="quote-icon">❝</div>
        ${slides}
        <div class="quote-dots">${dots}</div>
      </div>`;
    const go = n => {
      quoteWrap.querySelectorAll(".quote-slide").forEach((s, i) => s.classList.toggle("active", i === n));
      quoteWrap.querySelectorAll(".quote-dot").forEach((d, i) => d.classList.toggle("active", i === n));
      idx = n;
    };
    quoteWrap.querySelectorAll(".quote-dot").forEach(d =>
      d.addEventListener("click", () => go(+d.dataset.i))
    );
    setInterval(() => go((idx + 1) % PSG_DATA.quotes.length), 5000);
  }

  // ===== MATCH PREDICTION GAME =====
  const predictWrap = document.getElementById("predict");
  if (predictWrap) {
    const next = PSG_DATA.matches.find(m => m.status === "upcoming");
    if (next) {
      const homeInit = next.home.split(" ").map(w => w[0]).join("").slice(0, 3);
      const awayInit = next.away.split(" ").map(w => w[0]).join("").slice(0, 3);
      const isHomePsg = next.home.includes("PSG") || next.home.includes("Paris");
      const isAwayPsg = next.away.includes("PSG") || next.away.includes("Paris");
      predictWrap.innerHTML = `
        <div class="predict-card">
          <div class="predict-title">🔮 Pronostique le match</div>
          <div class="predict-sub">${next.comp} · ${next.date}</div>
          <div class="predict-teams">
            <div class="predict-team">
              <div class="team-badge ${isHomePsg ? "" : "team-other"}">${homeInit}</div>
              <div class="predict-team-name">${next.home}</div>
              <input type="number" class="predict-score-input" id="pred-h" min="0" max="9" value="0">
            </div>
            <div class="predict-vs">VS</div>
            <div class="predict-team">
              <div class="team-badge ${isAwayPsg ? "" : "team-other"}">${awayInit}</div>
              <div class="predict-team-name">${next.away}</div>
              <input type="number" class="predict-score-input" id="pred-a" min="0" max="9" value="0">
            </div>
          </div>
          <button class="btn btn-primary" id="pred-submit">Valider mon pronostic</button>
          <div class="predict-result" id="pred-result"></div>
        </div>`;
      document.getElementById("pred-submit").addEventListener("click", e => {
        const h = +document.getElementById("pred-h").value;
        const a = +document.getElementById("pred-a").value;
        const psgHome = isHomePsg;
        const psgScore = psgHome ? h : a;
        const oppScore = psgHome ? a : h;
        const result = document.getElementById("pred-result");
        let txt, cls;
        if (psgScore > oppScore) {
          txt = `🔥 Tu crois en Paris ! ${psgScore}-${oppScore} pour les Rouge et Bleu. Allez Paris !`;
          cls = "win";
        } else if (psgScore === oppScore) {
          txt = `⚖️ Match nul ${h}-${a} selon toi. Le Parc a toujours soif de victoire !`;
          cls = "draw";
        } else {
          txt = `😱 Quoi ?! Tu pronostiques une défaite ?! Allez quoi, crois en ton équipe !`;
          cls = "loss";
        }
        result.textContent = txt;
        result.className = "predict-result show " + cls;
        if (cls === "win") {
          const r = e.target.getBoundingClientRect();
          fireConfetti(r.left + r.width / 2, r.top);
        }
      });
    }
  }

  // ===== SEASON CHART =====
  const chartEl = document.getElementById("season-chart");
  if (chartEl) {
    const data = [
      { j: "J1", pts: 3 }, { j: "J5", pts: 13 }, { j: "J10", pts: 26 },
      { j: "J15", pts: 38 }, { j: "J18", pts: 46 }, { j: "J22", pts: 56 },
      { j: "J25", pts: 63 }, { j: "J28", pts: 70 }, { j: "J30", pts: 76 }
    ];
    const max = 90;
    chartEl.innerHTML = `
      <div style="margin-bottom:1.5rem;">
        <div style="font-weight:800;color:var(--psg-blue-dark);font-size:1.1rem;">Points accumulés en Ligue 1</div>
        <div style="font-size:.85rem;color:var(--text-muted);">Progression saison 2025-2026</div>
      </div>
      <div class="chart-bars">
        ${data.map(d => `<div class="chart-bar" data-val="${d.pts}" data-label="${d.j}" data-target="${(d.pts / max) * 100}"></div>`).join("")}
      </div>`;
    const obs = new IntersectionObserver(entries => {
      entries.forEach(e => {
        if (e.isIntersecting) {
          e.target.querySelectorAll(".chart-bar").forEach((b, i) => {
            setTimeout(() => { b.style.height = b.dataset.target + "%"; }, i * 80);
          });
          obs.unobserve(e.target);
        }
      });
    }, { threshold: 0.3 });
    obs.observe(chartEl);
  }

  // ===== SCROLL REVEAL =====
  const revealObs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (e.isIntersecting) {
        e.target.classList.add("visible");
        revealObs.unobserve(e.target);
      }
    });
  }, { threshold: 0.12, rootMargin: "0px 0px -60px 0px" });
  document.querySelectorAll(".section-header, .card, .player-card, .match-card, .trophy-card, .news-card, .scorer-row, .season-chart, .predict-card, .formation-pitch, .big-number").forEach(el => {
    el.classList.add("reveal");
    revealObs.observe(el);
  });

  // ===== STICKY NAV shadow on scroll =====
  const header = document.querySelector(".header");
  if (header) {
    const onScroll = () => header.classList.toggle("scrolled", window.scrollY > 40);
    window.addEventListener("scroll", onScroll);
    onScroll();
  }

  // ===== SECTION HEADERS — auto-inject eyebrow + data-num =====
  const sectionHeaders = document.querySelectorAll(".section-header");
  sectionHeaders.forEach((sh, i) => {
    const n = String(i + 1).padStart(2, "0");
    sh.setAttribute("data-num", n);
    const firstDiv = sh.querySelector("div");
    if (firstDiv && !firstDiv.querySelector(".eyebrow")) {
      const eye = document.createElement("span");
      eye.className = "eyebrow";
      const titles = ["Ici c'est Paris", "Paris Saint-Germain", "Rouge et Bleu", "Édition 2025-26", "Les Parisiens", "Histoire & Gloire"];
      eye.textContent = titles[i % titles.length];
      firstDiv.insertBefore(eye, firstDiv.firstChild);
    }
  });

  // ===== PLAYER CARDS — inject data-pos attribute =====
  document.querySelectorAll(".player-card").forEach(card => {
    const posEl = card.querySelector(".player-pos");
    if (posEl) {
      const txt = posEl.textContent.trim().toUpperCase();
      let key = "MID";
      if (txt.includes("GARDIEN") || txt.includes("GK")) key = "GK";
      else if (txt.includes("DÉFEN") || txt.includes("DEF")) key = "DEF";
      else if (txt.includes("ATTAQ") || txt.includes("FWD") || txt.includes("AILI")) key = "FWD";
      else if (txt.includes("MILIEU") || txt.includes("MID")) key = "MID";
      card.setAttribute("data-pos", key);
    }
  });

  // 3D tilt on player cards
  const bindTilt = card => {
    card.addEventListener("mousemove", e => {
      const r = card.getBoundingClientRect();
      const x = (e.clientX - r.left) / r.width - 0.5;
      const y = (e.clientY - r.top) / r.height - 0.5;
      card.style.transform = `translateY(-8px) perspective(900px) rotateY(${x * 12}deg) rotateX(${-y * 12}deg)`;
    });
    card.addEventListener("mouseleave", () => { card.style.transform = ""; });
  };
  // Will be applied after render in squad grid

  // Initiales joueurs
  const getInitials = (name) => name.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase();
  const posLabels = { GK: "Gardien", DEF: "Défenseur", MID: "Milieu", FWD: "Attaquant" };

  // HOME PAGE
  const heroStatsEl = document.getElementById("hero-stats");
  if (heroStatsEl) {
    heroStatsEl.innerHTML = PSG_DATA.heroStats.map(s =>
      `<div class="hero-stat"><div class="hero-stat-value">${s.value}</div><div class="hero-stat-label">${s.label}</div></div>`
    ).join("");
  }

  const newsEl = document.getElementById("news-grid");
  if (newsEl) {
    newsEl.innerHTML = PSG_DATA.news.map((n, i) => `
      <article class="news-card fade-in" style="animation-delay:${i * 0.08}s">
        <div class="news-image"><span class="news-tag">${n.tag}</span>${n.icon}</div>
        <div class="news-body">
          <div class="news-date">${n.date}</div>
          <h3 class="news-title">${n.title}</h3>
          <p class="news-excerpt">${n.excerpt}</p>
          <div class="news-link">Lire l'article →</div>
        </div>
      </article>`).join("");
  }

  const nextMatchesEl = document.getElementById("next-matches");
  if (nextMatchesEl) {
    const next = PSG_DATA.matches.filter(m => m.status === "upcoming").slice(0, 2);
    nextMatchesEl.innerHTML = next.map(m => renderMatchCard(m)).join("");
  }

  // PLAYERS PAGE
  const squadEl = document.getElementById("squad-grid");
  if (squadEl) {
    const renderSquad = (filter = "ALL") => {
      const list = filter === "ALL" ? PSG_DATA.players : PSG_DATA.players.filter(p => p.pos === filter);
      squadEl.innerHTML = list.map((p, i) => `
        <article class="player-card fade-in" style="animation-delay:${i * 0.04}s" data-id="${p.id}">
          <div class="player-photo">
            <span class="player-pos-badge">${posLabels[p.pos]}</span>
            <span class="player-number">${p.num}</span>
            <span class="player-initials">${getInitials(p.name)}</span>
          </div>
          <div class="player-info">
            <div class="player-name">${p.name}</div>
            <div class="player-meta">${p.nat} · ${p.age} ans</div>
            <div class="player-stats">
              <div class="player-stat"><div class="player-stat-value">${p.apps}</div><div class="player-stat-label">Matchs</div></div>
              <div class="player-stat"><div class="player-stat-value">${p.goals}</div><div class="player-stat-label">Buts</div></div>
              <div class="player-stat"><div class="player-stat-value">${p.assists ?? 0}</div><div class="player-stat-label">Passes</div></div>
            </div>
          </div>
        </article>`).join("");

      squadEl.querySelectorAll(".player-card").forEach(card => {
        card.addEventListener("click", () => openPlayerModal(+card.dataset.id));
        bindTilt(card);
      });
    };
    renderSquad();

    document.querySelectorAll(".tab[data-pos]").forEach(tab => {
      tab.addEventListener("click", () => {
        document.querySelectorAll(".tab[data-pos]").forEach(t => t.classList.remove("active"));
        tab.classList.add("active");
        renderSquad(tab.dataset.pos);
      });
    });
  }

  // STATS PAGE
  const statsGridEl = document.getElementById("stats-grid");
  if (statsGridEl) {
    statsGridEl.innerHTML = PSG_DATA.seasonStats.map((s, i) => `
      <div class="stat-box fade-in" style="animation-delay:${i * 0.05}s">
        <div class="stat-box-value">${s.value}</div>
        <div class="stat-box-label">${s.label}</div>
        <div class="stat-box-trend">${s.trend}</div>
      </div>`).join("");
  }

  const standingsEl = document.getElementById("standings");
  if (standingsEl) {
    standingsEl.innerHTML = PSG_DATA.standings.map(row => {
      const initials = row.team.split(" ").map(w => w[0]).join("").slice(0, 3);
      const isPsg = row.team.includes("Paris");
      return `<tr${row.highlight ? ' class="highlight"' : ""}>
        <td class="num"><strong>${row.pos}</strong></td>
        <td><div class="team-cell"><div class="team-badge ${isPsg ? "" : "team-other"}">${initials}</div>${row.team}</div></td>
        <td class="num">${row.p}</td>
        <td class="num">${row.w}</td>
        <td class="num">${row.d}</td>
        <td class="num">${row.l}</td>
        <td class="num">${row.gf}</td>
        <td class="num">${row.ga}</td>
        <td class="num">${row.gd > 0 ? "+" + row.gd : row.gd}</td>
        <td class="num"><strong>${row.pts}</strong></td>
      </tr>`;
    }).join("");
  }

  const scorersEl = document.getElementById("top-scorers");
  if (scorersEl) {
    const max = Math.max(...PSG_DATA.topScorers.map(s => s.goals));
    scorersEl.innerHTML = PSG_DATA.topScorers.map(s => {
      const medal = s.rank === 1 ? "gold" : s.rank === 2 ? "silver" : s.rank === 3 ? "bronze" : "";
      return `<div class="scorer-row">
        <div class="scorer-rank ${medal}">${s.rank}</div>
        <div class="scorer-info">
          <div class="scorer-name">${s.name}</div>
          <div class="scorer-bar"><div class="scorer-bar-fill" style="width:${(s.goals / max) * 100}%"></div></div>
        </div>
        <div class="scorer-goals">${s.goals}</div>
      </div>`;
    }).join("");
  }

  // MATCHES PAGE
  const matchesListEl = document.getElementById("matches-list");
  if (matchesListEl) {
    const renderMatches = (filter = "ALL") => {
      let list = PSG_DATA.matches;
      if (filter === "UPCOMING") list = list.filter(m => m.status === "upcoming");
      if (filter === "PLAYED") list = list.filter(m => m.status === "finished");
      matchesListEl.innerHTML = list.map(renderMatchCard).join("");
    };
    renderMatches();
    document.querySelectorAll(".tab[data-match]").forEach(tab => {
      tab.addEventListener("click", () => {
        document.querySelectorAll(".tab[data-match]").forEach(t => t.classList.remove("active"));
        tab.classList.add("active");
        renderMatches(tab.dataset.match);
      });
    });
  }

  // HISTORY PAGE
  const trophiesEl = document.getElementById("trophies");
  if (trophiesEl) {
    trophiesEl.innerHTML = PSG_DATA.trophies.map(t => `
      <div class="trophy-card">
        <div class="trophy-icon">${t.icon}</div>
        <div class="trophy-count">${t.count}</div>
        <div class="trophy-name">${t.name}</div>
      </div>`).join("");
  }

  const timelineEl = document.getElementById("timeline");
  if (timelineEl) {
    timelineEl.innerHTML = PSG_DATA.history.map(h => `
      <div class="timeline-item">
        <span class="timeline-year">${h.year}</span>
        <div class="timeline-title">${h.title}</div>
        <p class="timeline-desc">${h.desc}</p>
      </div>`).join("");
  }

  // MODAL
  const modalBackdrop = document.getElementById("player-modal");
  if (modalBackdrop) {
    modalBackdrop.addEventListener("click", e => {
      if (e.target === modalBackdrop || e.target.classList.contains("modal-close"))
        modalBackdrop.classList.remove("active");
    });
  }
});

function renderMatchCard(m) {
  const homeInit = m.home.split(" ").map(w => w[0]).join("").slice(0, 3);
  const awayInit = m.away.split(" ").map(w => w[0]).join("").slice(0, 3);
  const isHomePsg = m.home.includes("PSG") || m.home.includes("Paris");
  const isAwayPsg = m.away.includes("PSG") || m.away.includes("Paris");
  const scoreDisplay = m.status === "upcoming"
    ? '<div class="score" style="font-size:1.1rem">VS</div><div class="match-status">À venir</div>'
    : `<div class="score">${m.scoreH}<span class="score-sep">-</span>${m.scoreA}</div>
       <div class="match-status">Terminé <span class="match-result-badge result-${m.result}">${m.result === "win" ? "Victoire" : m.result === "draw" ? "Nul" : "Défaite"}</span></div>`;

  const goalsHtml = m.goals.length ? `
    <div class="goal-timeline">
      <div class="goal-timeline-title">⚡ Résumé des actions de but</div>
      <div class="goals">
        ${m.goals.map((g, gi) => `
          <div class="goal">
            <span class="goal-minute">${g.min}'</span>
            <span class="goal-icon">⚽</span>
            <div class="goal-desc">
              <span class="goal-scorer">${g.scorer}</span> (${g.team})
              ${g.assist ? `<span class="goal-assist"> — passe de ${g.assist}</span>` : ""}
              <div style="font-size:.82rem;color:#6b7280;margin-top:.15rem">${g.desc}</div>
              <button class="replay-btn" onclick="playGoalReplay(${m.id}, ${gi})">▶ Voir l'action</button>
            </div>
          </div>`).join("")}
      </div>
    </div>` : "";

  return `<article class="match-card">
    <span class="match-date">${m.date}</span>
    <span class="match-comp">${m.comp}</span>
    <div class="match-team match-team-home">
      <span class="team-name">${m.home}</span>
      <div class="team-badge ${isHomePsg ? "" : "team-other"}">${homeInit}</div>
    </div>
    <div class="match-score">${scoreDisplay}</div>
    <div class="match-team match-team-away">
      <div class="team-badge ${isAwayPsg ? "" : "team-other"}">${awayInit}</div>
      <span class="team-name">${m.away}</span>
    </div>
    ${goalsHtml}
  </article>`;
}

function buildRadarChart(stats) {
  if (!stats) return "";
  const labels = [
    { key: "pac", name: "Vitesse" },
    { key: "sho", name: "Tir" },
    { key: "pas", name: "Passe" },
    { key: "dri", name: "Drible" },
    { key: "def", name: "Défense" },
    { key: "phy", name: "Physique" }
  ];
  const cx = 160, cy = 140, r = 90;
  const n = labels.length;
  const points = labels.map((lab, i) => {
    const angle = (Math.PI * 2 * i) / n - Math.PI / 2;
    const val = (stats[lab.key] || 0) / 99;
    return { x: cx + Math.cos(angle) * r * val, y: cy + Math.sin(angle) * r * val, angle, val: stats[lab.key], label: lab.name };
  });
  const grids = [0.25, 0.5, 0.75, 1].map(ratio => {
    const pts = labels.map((_, i) => {
      const angle = (Math.PI * 2 * i) / n - Math.PI / 2;
      return `${cx + Math.cos(angle) * r * ratio},${cy + Math.sin(angle) * r * ratio}`;
    }).join(" ");
    return `<polygon class="radar-grid" points="${pts}"/>`;
  }).join("");
  const axes = labels.map((_, i) => {
    const angle = (Math.PI * 2 * i) / n - Math.PI / 2;
    return `<line class="radar-axis" x1="${cx}" y1="${cy}" x2="${cx + Math.cos(angle) * r}" y2="${cy + Math.sin(angle) * r}"/>`;
  }).join("");
  const shape = `<polygon class="radar-shape" points="${points.map(p => `${p.x},${p.y}`).join(" ")}"/>`;
  const dots = points.map(p => `<circle class="radar-point" cx="${p.x}" cy="${p.y}" r="3.5"/>`).join("");
  const labelsHtml = labels.map((lab, i) => {
    const angle = (Math.PI * 2 * i) / n - Math.PI / 2;
    const lx = cx + Math.cos(angle) * (r + 18);
    const ly = cy + Math.sin(angle) * (r + 18) + 3;
    return `<text class="radar-label" x="${lx}" y="${ly}">${lab.name}</text>
            <text class="radar-value" x="${lx}" y="${ly + 11}">${stats[lab.key]}</text>`;
  }).join("");
  return `<svg class="radar-chart" viewBox="0 0 320 280" xmlns="http://www.w3.org/2000/svg">
    ${grids}${axes}${shape}${dots}${labelsHtml}
  </svg>`;
}

function openPlayerModal(id) {
  const p = PSG_DATA.players.find(x => x.id === id);
  if (!p) return;
  const modal = document.getElementById("player-modal");
  const posLabels = { GK: "Gardien", DEF: "Défenseur", MID: "Milieu", FWD: "Attaquant" };
  document.getElementById("modal-title").textContent = p.name;
  document.getElementById("modal-sub").textContent = `${posLabels[p.pos]} · N°${p.num} · ${p.nat}`;
  const stats = (PSG_DATA.ratings || {})[p.id];
  const overall = stats ? Math.round((stats.pac + stats.sho + stats.pas + stats.dri + stats.def + stats.phy) / 6) : null;
  document.getElementById("modal-body").innerHTML = `
    ${overall ? `<div style="text-align:center;margin-bottom:.5rem;">
      <div style="display:inline-block;padding:.35rem 1rem;background:linear-gradient(135deg,#DA291C,#d4af37);color:#fff;border-radius:999px;font-weight:900;font-size:.9rem;letter-spacing:1px;">NOTE GLOBALE · ${overall}</div>
    </div>` : ""}
    ${buildRadarChart(stats)}
    <div class="modal-stats">
      <div class="modal-stat"><div class="modal-stat-value">${p.apps}</div><div class="modal-stat-label">Matchs</div></div>
      <div class="modal-stat"><div class="modal-stat-value">${p.goals}</div><div class="modal-stat-label">Buts</div></div>
      <div class="modal-stat"><div class="modal-stat-value">${p.assists ?? 0}</div><div class="modal-stat-label">Passes D.</div></div>
    </div>
    <div class="modal-detail-row"><span class="modal-detail-label">Âge</span><span class="modal-detail-value">${p.age} ans</span></div>
    <div class="modal-detail-row"><span class="modal-detail-label">Nationalité</span><span class="modal-detail-value">${p.nat}</span></div>
    <div class="modal-detail-row"><span class="modal-detail-label">Taille</span><span class="modal-detail-value">${p.height}</span></div>
    <div class="modal-detail-row"><span class="modal-detail-label">Au PSG depuis</span><span class="modal-detail-value">${p.joined}</span></div>
    ${p.cleanSheets != null ? `<div class="modal-detail-row"><span class="modal-detail-label">Clean sheets</span><span class="modal-detail-value">${p.cleanSheets}</span></div>` : ""}
  `;
  modal.classList.add("active");
}

// ===== GOAL REPLAY on mini pitch =====
function playGoalReplay(matchId, goalIdx) {
  const m = PSG_DATA.matches.find(x => x.id === matchId);
  if (!m || !m.goals[goalIdx]) return;
  const g = m.goals[goalIdx];
  const isPsgGoal = g.team === "PSG";

  // Generate a plausible ball path (4 points: start, assist pass, shot, goal)
  const paths = [
    [{x:20,y:50},{x:50,y:35},{x:75,y:25},{x:95,y:15}],
    [{x:10,y:60},{x:40,y:55},{x:70,y:40},{x:95,y:18}],
    [{x:30,y:30},{x:55,y:45},{x:75,y:30},{x:95,y:12}],
    [{x:15,y:45},{x:45,y:25},{x:70,y:35},{x:95,y:22}]
  ];
  const path = paths[goalIdx % paths.length];
  const pathD = `M ${path.map(p => `${p.x}% ${p.y}%`).join(" L ")}`;

  const modal = document.getElementById("player-modal");
  document.getElementById("modal-title").textContent = `⚽ But de ${g.scorer}`;
  document.getElementById("modal-sub").textContent = `${g.min}ᵉ minute · ${m.comp} · ${m.home} ${m.scoreH}-${m.scoreA} ${m.away}`;
  document.getElementById("modal-body").innerHTML = `
    <div class="mini-pitch">
      <svg class="mini-pitch-svg" viewBox="0 0 100 100" preserveAspectRatio="none">
        <rect x="1" y="1" width="98" height="98" class="mini-pitch-line"/>
        <line x1="50" y1="1" x2="50" y2="99" class="mini-pitch-line"/>
        <circle cx="50" cy="50" r="12" class="mini-pitch-line"/>
        <rect x="1" y="30" width="18" height="40" class="mini-pitch-line"/>
        <rect x="81" y="30" width="18" height="40" class="mini-pitch-line"/>
        <rect x="1" y="40" width="7" height="20" class="mini-pitch-line"/>
        <rect x="92" y="40" width="7" height="20" class="mini-pitch-line"/>
        <path d="${pathD}" class="ball-path" id="ball-trail"/>
        <circle cx="${path[0].x}" cy="${path[0].y}" r="2.8" class="ball" id="ball-anim"/>
      </svg>
    </div>
    <div style="text-align:center;margin-top:.5rem;">
      <div style="font-weight:800;color:var(--psg-blue-dark);font-size:1rem;">${g.scorer}${g.assist ? ` · passe de ${g.assist}` : ""}</div>
      <div style="color:var(--text-muted);font-size:.9rem;margin-top:.3rem;">${g.desc}</div>
    </div>
    <button class="btn btn-primary" id="replay-again" style="margin-top:1rem;width:100%;justify-content:center">🔁 Rejouer l'action</button>
  `;
  modal.classList.add("active");

  const animateBall = () => {
    const ball = document.getElementById("ball-anim");
    if (!ball) return;
    const frames = path.map(p => ({ cx: p.x, cy: p.y }));
    const steps = 100;
    let i = 0;
    const interp = (a, b, t) => a + (b - a) * t;
    const tick = () => {
      const segCount = frames.length - 1;
      const progress = i / steps;
      const segF = progress * segCount;
      const seg = Math.min(Math.floor(segF), segCount - 1);
      const t = segF - seg;
      ball.setAttribute("cx", interp(frames[seg].cx, frames[seg + 1].cx, t));
      ball.setAttribute("cy", interp(frames[seg].cy, frames[seg + 1].cy, t) - Math.sin(progress * Math.PI) * 6);
      i++;
      if (i <= steps) requestAnimationFrame(tick);
      else if (isPsgGoal) {
        const r = ball.getBoundingClientRect();
        if (typeof window.__fireConf === "function") window.__fireConf(r.left, r.top);
      }
    };
    tick();
  };
  setTimeout(animateBall, 300);
  const again = document.getElementById("replay-again");
  if (again) again.addEventListener("click", animateBall);
}
window.playGoalReplay = playGoalReplay;

/* =========================================================
   🚀 ADVANCED ENHANCEMENTS
   ========================================================= */
(function () {
  const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  // ===== CANVAS PARTICLE SYSTEM (hero) =====
  function initHeroParticles() {
    const hero = document.querySelector(".hero");
    if (!hero || reduced) return;
    const canvas = document.createElement("canvas");
    canvas.className = "hero-particles";
    hero.insertBefore(canvas, hero.firstChild);
    const ctx = canvas.getContext("2d");
    let w, h, particles = [];
    const resize = () => {
      w = canvas.width = hero.offsetWidth;
      h = canvas.height = hero.offsetHeight;
    };
    resize();
    window.addEventListener("resize", resize);
    const COUNT = Math.min(90, Math.floor((w * h) / 16000));
    for (let i = 0; i < COUNT; i++) {
      particles.push({
        x: Math.random() * w,
        y: Math.random() * h,
        vx: (Math.random() - 0.5) * 0.25,
        vy: -Math.random() * 0.5 - 0.1,
        r: Math.random() * 2 + 0.5,
        life: Math.random(),
        hue: Math.random() > 0.7 ? "#E30613" : "#D4AF37"
      });
    }
    const draw = () => {
      ctx.clearRect(0, 0, w, h);
      particles.forEach(p => {
        p.x += p.vx;
        p.y += p.vy;
        p.life += 0.005;
        if (p.y < -10) { p.y = h + 10; p.x = Math.random() * w; }
        if (p.x < -10) p.x = w + 10;
        if (p.x > w + 10) p.x = -10;
        const glow = 0.4 + Math.sin(p.life * 4) * 0.3;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fillStyle = p.hue;
        ctx.globalAlpha = glow;
        ctx.shadowColor = p.hue;
        ctx.shadowBlur = 8;
        ctx.fill();
      });
      ctx.globalAlpha = 1;
      ctx.shadowBlur = 0;
      requestAnimationFrame(draw);
    };
    draw();
  }
  initHeroParticles();

  // ===== SPLIT TEXT REVEAL on hero title =====
  function initSplitText() {
    if (reduced) return;
    const title = document.querySelector(".hero-title");
    if (!title || title.dataset.split) return;
    title.dataset.split = "1";
    const html = title.innerHTML;
    const walker = document.createElement("div");
    walker.innerHTML = html;
    const result = [];
    let charIdx = 0;
    walker.childNodes.forEach(node => {
      if (node.nodeType === 3) {
        const words = node.textContent.split(" ");
        words.forEach((w, wi) => {
          if (!w) return;
          let word = '<span class="split-word">';
          for (const ch of w) {
            word += `<span class="split-char" style="animation-delay:${charIdx * 30}ms">${ch}</span>`;
            charIdx++;
          }
          word += "</span>";
          result.push(word);
          if (wi < words.length - 1) result.push(" ");
        });
      } else if (node.nodeType === 1) {
        const words = (node.textContent || "").split(" ");
        let inner = "";
        words.forEach((w, wi) => {
          if (!w) return;
          let word = '<span class="split-word">';
          for (const ch of w) {
            word += `<span class="split-char" style="animation-delay:${charIdx * 30}ms">${ch}</span>`;
            charIdx++;
          }
          word += "</span>";
          inner += word;
          if (wi < words.length - 1) inner += " ";
        });
        const clone = node.cloneNode(false);
        clone.innerHTML = inner;
        result.push(clone.outerHTML);
      }
    });
    title.innerHTML = result.join("");
  }
  setTimeout(initSplitText, 1500);

  // ===== MAGNETIC BUTTONS =====
  if (!reduced && window.matchMedia("(hover:hover)").matches) {
    document.querySelectorAll(".btn").forEach(btn => {
      btn.addEventListener("mousemove", e => {
        const r = btn.getBoundingClientRect();
        const x = e.clientX - r.left - r.width / 2;
        const y = e.clientY - r.top - r.height / 2;
        btn.style.transform = `translate(${x * 0.25}px, ${y * 0.35}px)`;
      });
      btn.addEventListener("mouseleave", () => {
        btn.style.transform = "";
      });
    });
  }

  // ===== CUSTOM CURSOR =====
  if (window.matchMedia("(hover:hover)").matches && !reduced) {
    const cursor = document.createElement("div");
    cursor.className = "cursor-follower";
    document.body.appendChild(cursor);
    let x = 0, y = 0, tx = 0, ty = 0;
    document.addEventListener("mousemove", e => {
      tx = e.clientX; ty = e.clientY;
      cursor.classList.add("visible");
    });
    const loop = () => {
      x += (tx - x) * 0.2;
      y += (ty - y) * 0.2;
      cursor.style.left = x + "px";
      cursor.style.top = y + "px";
      requestAnimationFrame(loop);
    };
    loop();
    const growSel = "a, button, .btn, .player-card, .match-card, .trophy-card, .cmdk-item, [role=button]";
    document.addEventListener("mouseover", e => {
      if (e.target.closest(growSel)) cursor.classList.add("grow");
      else cursor.classList.remove("grow");
    });
    document.addEventListener("mousedown", () => cursor.classList.add("active"));
    document.addEventListener("mouseup", () => cursor.classList.remove("active"));
    document.addEventListener("mouseleave", () => cursor.classList.remove("visible"));
  }

  // ===== EASING COUNTER (replace linear) =====
  function animateCount(el, target, duration = 1600) {
    const start = performance.now();
    const from = 0;
    const easeOutQuart = t => 1 - Math.pow(1 - t, 4);
    const step = now => {
      const p = Math.min(1, (now - start) / duration);
      const v = Math.round(from + (target - from) * easeOutQuart(p));
      el.textContent = v.toLocaleString("fr-FR");
      if (p < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  }
  const countObs = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (e.isIntersecting) {
        const el = e.target;
        const target = parseInt(el.dataset.count || el.textContent.replace(/\D/g, ""), 10);
        if (!el.dataset.counted && !isNaN(target)) {
          el.dataset.counted = "1";
          el.dataset.count = target;
          animateCount(el, target);
        }
        countObs.unobserve(e.target);
      }
    });
  }, { threshold: 0.4 });
  document.querySelectorAll(".big-number, .trophy-count, [data-counter]").forEach(el => {
    const n = parseInt(el.textContent.replace(/\D/g, ""), 10);
    if (!isNaN(n)) {
      el.dataset.count = n;
      el.textContent = "0";
      countObs.observe(el);
    }
  });

  // ===== INFINITE MARQUEE =====
  function injectMarquee() {
    if (document.querySelector(".marquee")) return;
    const hero = document.querySelector(".hero");
    if (!hero) return;
    const items = [
      "13× Ligue 1",
      "Finaliste LDC 2020",
      "Champion d'Europe 2025",
      "15× Coupe de France",
      "Ici c'est Paris",
      "Rêver plus grand",
      "9× Coupe de la Ligue",
      "Rouge et Bleu",
      "Trophée des Champions ×12",
      "Parc des Princes"
    ];
    const content = items.map(t => `<span>${t}</span>`).join("");
    const m = document.createElement("div");
    m.className = "marquee";
    m.innerHTML = `<div class="marquee-track">${content}${content}</div>`;
    hero.insertAdjacentElement("afterend", m);
  }
  injectMarquee();

  // ===== COMMAND PALETTE (Cmd+K / Ctrl+K) =====
  const paletteItems = [
    { icon: "🏠", label: "Accueil", desc: "Page principale · Hero, stats, formation", href: "index.html" },
    { icon: "👕", label: "Effectif", desc: "Tous les joueurs du PSG 2025-26", href: "players.html" },
    { icon: "📊", label: "Statistiques", desc: "Tableaux, buteurs, progression", href: "stats.html" },
    { icon: "⚽", label: "Matchs", desc: "Résultats et actions de but", href: "matches.html" },
    { icon: "🏆", label: "Histoire", desc: "Trophées, légendes, timeline", href: "history.html" },
    { icon: "🌙", label: "Changer de thème", desc: "Basculer clair / sombre", action: "theme" },
    { icon: "🎉", label: "Déclencher des confettis", desc: "Pour le fun", action: "confetti" },
    { icon: "⌨️", label: "Raccourcis clavier", desc: "Voir toutes les touches", action: "help" }
  ];
  function buildPalette() {
    if (document.querySelector(".cmdk-backdrop")) return;
    const bd = document.createElement("div");
    bd.className = "cmdk-backdrop";
    bd.innerHTML = `
      <div class="cmdk" role="dialog" aria-label="Palette de commandes">
        <div class="cmdk-input-wrap">
          <span class="cmdk-icon">⚡</span>
          <input class="cmdk-input" placeholder="Chercher une page, une action..." autocomplete="off">
          <span class="cmdk-kbd">ESC</span>
        </div>
        <div class="cmdk-list"></div>
        <div class="cmdk-footer">
          <div>↑↓ Naviguer · ⏎ Valider · ESC Fermer</div>
          <div><span class="cmdk-kbd">⌘</span> <span class="cmdk-kbd">K</span></div>
        </div>
      </div>`;
    document.body.appendChild(bd);
    const input = bd.querySelector(".cmdk-input");
    const list = bd.querySelector(".cmdk-list");
    let selected = 0;
    const render = (q = "") => {
      const filtered = paletteItems.filter(it =>
        it.label.toLowerCase().includes(q.toLowerCase()) ||
        (it.desc && it.desc.toLowerCase().includes(q.toLowerCase()))
      );
      list.innerHTML = filtered.length
        ? filtered.map((it, i) => `
          <div class="cmdk-item ${i === selected ? "selected" : ""}" data-i="${i}" data-href="${it.href || ""}" data-action="${it.action || ""}">
            <div class="cmdk-item-icon">${it.icon}</div>
            <div>
              <div class="cmdk-item-label">${it.label}</div>
              <div class="cmdk-item-desc">${it.desc}</div>
            </div>
          </div>`).join("")
        : `<div style="padding:2rem;text-align:center;color:#9ca3af">Aucun résultat</div>`;
      list._filtered = filtered;
    };
    render();
    input.addEventListener("input", e => { selected = 0; render(e.target.value); });
    input.addEventListener("keydown", e => {
      const items = list._filtered || [];
      if (e.key === "ArrowDown") { selected = (selected + 1) % items.length; render(input.value); e.preventDefault(); }
      else if (e.key === "ArrowUp") { selected = (selected - 1 + items.length) % items.length; render(input.value); e.preventDefault(); }
      else if (e.key === "Enter" && items[selected]) {
        const it = items[selected];
        closePalette();
        if (it.href) window.location.href = it.href;
        else if (it.action === "theme") document.querySelector(".theme-toggle")?.click();
        else if (it.action === "confetti" && window.__fireConf) window.__fireConf(innerWidth / 2, innerHeight / 2);
        else if (it.action === "help") showShortcuts();
      }
    });
    list.addEventListener("click", e => {
      const item = e.target.closest(".cmdk-item");
      if (!item) return;
      const it = list._filtered[+item.dataset.i];
      closePalette();
      if (it.href) window.location.href = it.href;
      else if (it.action === "theme") document.querySelector(".theme-toggle")?.click();
      else if (it.action === "confetti" && window.__fireConf) window.__fireConf(innerWidth / 2, innerHeight / 2);
      else if (it.action === "help") showShortcuts();
    });
    bd.addEventListener("click", e => { if (e.target === bd) closePalette(); });
    window.__palette = bd;
  }
  function openPalette() {
    buildPalette();
    const bd = window.__palette;
    bd.classList.add("active");
    setTimeout(() => bd.querySelector(".cmdk-input")?.focus(), 50);
  }
  function closePalette() {
    window.__palette?.classList.remove("active");
  }
  window.__openPalette = openPalette;

  // ===== KEYBOARD SHORTCUTS =====
  document.addEventListener("keydown", e => {
    if (e.target.matches("input, textarea, [contenteditable]")) {
      if (e.key === "Escape") closePalette();
      return;
    }
    if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
      e.preventDefault();
      openPalette();
      return;
    }
    if (e.key === "Escape") closePalette();
    if (e.key === "?") { e.preventDefault(); showShortcuts(); }
    const navMap = {
      "1": "index.html",
      "2": "players.html",
      "3": "stats.html",
      "4": "matches.html",
      "5": "history.html"
    };
    if (navMap[e.key] && !e.metaKey && !e.ctrlKey) {
      window.location.href = navMap[e.key];
    }
    if (e.key.toLowerCase() === "t") {
      document.querySelector(".theme-toggle")?.click();
    }
  });

  // ===== ACHIEVEMENTS =====
  let toastStack = null;
  function ensureToastStack() {
    if (toastStack) return toastStack;
    toastStack = document.createElement("div");
    toastStack.className = "toast-stack";
    document.body.appendChild(toastStack);
    return toastStack;
  }
  function toast(title, text, icon = "🏆") {
    const stack = ensureToastStack();
    const el = document.createElement("div");
    el.className = "toast";
    el.innerHTML = `
      <div class="toast-icon">${icon}</div>
      <div class="toast-body">
        <div class="toast-title">${title}</div>
        <div class="toast-text">${text}</div>
      </div>`;
    stack.appendChild(el);
    setTimeout(() => el.remove(), 4800);
  }
  const achievements = new Set();
  function unlock(key, title, text, icon) {
    if (achievements.has(key)) return;
    achievements.add(key);
    toast(title, text, icon);
    try { localStorage.setItem("psg-ach", JSON.stringify([...achievements])); } catch (e) {}
  }
  try {
    const saved = JSON.parse(localStorage.getItem("psg-ach") || "[]");
    saved.forEach(k => achievements.add(k));
  } catch (e) {}

  setTimeout(() => unlock("welcome", "Bienvenue au Parc", "Tu viens de rejoindre les rangs du Collectif.", "🏟️"), 2500);

  let scrolledOnce = false;
  window.addEventListener("scroll", () => {
    if (!scrolledOnce && window.scrollY > 400) {
      scrolledOnce = true;
      unlock("scroll1", "Premier Dribble", "Tu as commencé à explorer le site.", "⚽");
    }
    const d = document.documentElement;
    const pct = d.scrollTop / (d.scrollHeight - d.clientHeight);
    if (pct > 0.9) unlock("bottom", "Marathon Parisien", "Tu as atteint le bas de la page. Respect.", "🏃");
  });

  document.querySelectorAll(".trophy-card").forEach(c => {
    c.addEventListener("mouseenter", () => unlock("trophy", "Touche d'Or", "Tu as survolé un trophée du PSG.", "🏆"), { once: true });
  });
  document.addEventListener("click", e => {
    if (e.target.closest(".replay-btn")) unlock("replay", "Action Décisive", "Tu as rejoué une action de but.", "🎬");
    if (e.target.closest(".player-card")) unlock("player", "Profil Joueur", "Tu connais tes joueurs.", "👕");
    if (e.target.closest(".cmdk-item")) unlock("cmdk", "Power User", "Tu utilises la palette de commandes.", "⚡");
  });

  // ===== KEYBOARD HINT BADGE =====
  if (!document.querySelector(".kbd-hint")) {
    const hint = document.createElement("button");
    hint.className = "kbd-hint";
    hint.innerHTML = `⚡ Appuie sur <kbd>⌘K</kbd> pour tout`;
    hint.addEventListener("click", openPalette);
    document.body.appendChild(hint);
  }

  // ===== SHORTCUTS HELP MODAL =====
  function showShortcuts() {
    toast("Raccourcis clavier", "1-5 pages · T thème · ⌘K palette · ? aide", "⌨️");
  }

  // ===== KONAMI CODE EASTER EGG =====
  const konami = ["ArrowUp","ArrowUp","ArrowDown","ArrowDown","ArrowLeft","ArrowRight","ArrowLeft","ArrowRight","b","a"];
  let konamiIdx = 0;
  document.addEventListener("keydown", e => {
    if (e.key === konami[konamiIdx] || e.key.toLowerCase() === konami[konamiIdx]) {
      konamiIdx++;
      if (konamiIdx === konami.length) {
        konamiIdx = 0;
        triggerEgg();
      }
    } else {
      konamiIdx = 0;
    }
  });
  function triggerEgg() {
    unlock("konami", "Code Konami ★", "Tu connais les vrais secrets. Légende.", "👑");
    const modal = document.createElement("div");
    modal.className = "egg-modal";
    modal.innerHTML = `
      <div class="egg-content">
        <div class="egg-title">Allez Paris !</div>
        <p class="egg-text">
          Tu viens d'entrer dans le cercle des vrais. Le Collectif Ultras Paris te salue.
          <br><br>
          <strong style="color:var(--gold)">Ici c'est Paris.</strong>
        </p>
        <button class="egg-close">Fermer</button>
      </div>`;
    document.body.appendChild(modal);
    modal.classList.add("active");
    modal.querySelector(".egg-close").onclick = () => modal.remove();
    modal.addEventListener("click", e => { if (e.target === modal) modal.remove(); });
    if (window.__fireConf) {
      for (let i = 0; i < 6; i++) {
        setTimeout(() => window.__fireConf(Math.random() * innerWidth, 0), i * 200);
      }
    }
  }
})();

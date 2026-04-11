// PSG Fan Site - JS principal
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
        ${m.goals.map(g => `
          <div class="goal">
            <span class="goal-minute">${g.min}'</span>
            <span class="goal-icon">⚽</span>
            <div class="goal-desc">
              <span class="goal-scorer">${g.scorer}</span> (${g.team})
              ${g.assist ? `<span class="goal-assist"> — passe de ${g.assist}</span>` : ""}
              <div style="font-size:.82rem;color:#6b7280;margin-top:.15rem">${g.desc}</div>
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

function openPlayerModal(id) {
  const p = PSG_DATA.players.find(x => x.id === id);
  if (!p) return;
  const modal = document.getElementById("player-modal");
  const posLabels = { GK: "Gardien", DEF: "Défenseur", MID: "Milieu", FWD: "Attaquant" };
  document.getElementById("modal-title").textContent = p.name;
  document.getElementById("modal-sub").textContent = `${posLabels[p.pos]} · N°${p.num} · ${p.nat}`;
  document.getElementById("modal-body").innerHTML = `
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

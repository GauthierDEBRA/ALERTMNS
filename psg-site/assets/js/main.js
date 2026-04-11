// PSG Fan Site - JS principal
document.addEventListener("DOMContentLoaded", () => {
  // Nav toggle mobile
  const toggle = document.querySelector(".nav-toggle");
  const links = document.querySelector(".nav-links");
  if (toggle && links) toggle.addEventListener("click", () => links.classList.toggle("open"));

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

      squadEl.querySelectorAll(".player-card").forEach(card =>
        card.addEventListener("click", () => openPlayerModal(+card.dataset.id))
      );
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

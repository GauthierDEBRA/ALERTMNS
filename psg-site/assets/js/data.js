// Données du site PSG Fan (saison 2025-2026)
const PSG_DATA = {
  club: {
    name: "Paris Saint-Germain",
    founded: 1970,
    stadium: "Parc des Princes",
    capacity: 47929,
    president: "Nasser Al-Khelaïfi",
    coach: "Luis Enrique",
    nickname: "Les Rouge et Bleu",
    city: "Paris, France"
  },

  heroStats: [
    { value: "13", label: "Ligue 1" },
    { value: "1", label: "Ligue des Champions" },
    { value: "15", label: "Coupes de France" },
    { value: "+55", label: "Ans d'histoire" }
  ],

  players: [
    // GARDIENS
    { id: 1, num: 1, name: "Gianluigi Donnarumma", pos: "GK", nat: "Italie", age: 27, apps: 32, goals: 0, assists: 0, cleanSheets: 14, height: "1.96 m", joined: 2021 },
    { id: 2, num: 39, name: "Matvey Safonov", pos: "GK", nat: "Russie", age: 26, apps: 8, goals: 0, assists: 0, cleanSheets: 3, height: "1.95 m", joined: 2024 },
    { id: 3, num: 80, name: "Arnau Tenas", pos: "GK", nat: "Espagne", age: 24, apps: 4, goals: 0, assists: 0, cleanSheets: 1, height: "1.89 m", joined: 2023 },

    // DÉFENSEURS
    { id: 4, num: 5, name: "Marquinhos", pos: "DEF", nat: "Brésil", age: 31, apps: 34, goals: 3, assists: 1, cleanSheets: 15, height: "1.83 m", joined: 2013 },
    { id: 5, num: 2, name: "Achraf Hakimi", pos: "DEF", nat: "Maroc", age: 27, apps: 33, goals: 6, assists: 8, cleanSheets: 13, height: "1.81 m", joined: 2021 },
    { id: 6, num: 25, name: "Nuno Mendes", pos: "DEF", nat: "Portugal", age: 23, apps: 31, goals: 2, assists: 5, cleanSheets: 12, height: "1.77 m", joined: 2021 },
    { id: 7, num: 35, name: "Lucas Beraldo", pos: "DEF", nat: "Brésil", age: 22, apps: 27, goals: 1, assists: 0, cleanSheets: 11, height: "1.86 m", joined: 2024 },
    { id: 8, num: 4, name: "Willian Pacho", pos: "DEF", nat: "Équateur", age: 24, apps: 33, goals: 2, assists: 0, cleanSheets: 14, height: "1.88 m", joined: 2024 },
    { id: 9, num: 21, name: "Lucas Hernandez", pos: "DEF", nat: "France", age: 29, apps: 22, goals: 1, assists: 2, cleanSheets: 9, height: "1.84 m", joined: 2023 },

    // MILIEUX
    { id: 10, num: 17, name: "Vitinha", pos: "MID", nat: "Portugal", age: 26, apps: 34, goals: 7, assists: 9, height: "1.72 m", joined: 2022 },
    { id: 11, num: 8, name: "Fabián Ruiz", pos: "MID", nat: "Espagne", age: 29, apps: 30, goals: 5, assists: 6, height: "1.89 m", joined: 2022 },
    { id: 12, num: 33, name: "Warren Zaïre-Emery", pos: "MID", nat: "France", age: 20, apps: 32, goals: 4, assists: 7, height: "1.78 m", joined: 2022 },
    { id: 13, num: 87, name: "João Neves", pos: "MID", nat: "Portugal", age: 21, apps: 31, goals: 5, assists: 4, height: "1.74 m", joined: 2024 },
    { id: 14, num: 19, name: "Lee Kang-in", pos: "MID", nat: "Corée du Sud", age: 25, apps: 28, goals: 6, assists: 8, height: "1.73 m", joined: 2023 },
    { id: 15, num: 24, name: "Senny Mayulu", pos: "MID", nat: "France", age: 19, apps: 18, goals: 3, assists: 2, height: "1.83 m", joined: 2022 },

    // ATTAQUANTS
    { id: 16, num: 10, name: "Ousmane Dembélé", pos: "FWD", nat: "France", age: 28, apps: 33, goals: 24, assists: 11, height: "1.78 m", joined: 2023 },
    { id: 17, num: 29, name: "Bradley Barcola", pos: "FWD", nat: "France", age: 23, apps: 34, goals: 18, assists: 10, height: "1.84 m", joined: 2023 },
    { id: 18, num: 9, name: "Gonçalo Ramos", pos: "FWD", nat: "Portugal", age: 24, apps: 29, goals: 15, assists: 4, height: "1.85 m", joined: 2023 },
    { id: 19, num: 14, name: "Désiré Doué", pos: "FWD", nat: "France", age: 20, apps: 30, goals: 12, assists: 9, height: "1.81 m", joined: 2024 },
    { id: 20, num: 23, name: "Randal Kolo Muani", pos: "FWD", nat: "France", age: 27, apps: 22, goals: 8, assists: 5, height: "1.87 m", joined: 2023 }
  ],

  standings: [
    { pos: 1, team: "Paris Saint-Germain", p: 30, w: 24, d: 4, l: 2, gf: 78, ga: 22, gd: 56, pts: 76, highlight: true },
    { pos: 2, team: "Olympique de Marseille", p: 30, w: 19, d: 6, l: 5, gf: 58, ga: 34, gd: 24, pts: 63 },
    { pos: 3, team: "AS Monaco", p: 30, w: 18, d: 5, l: 7, gf: 55, ga: 38, gd: 17, pts: 59 },
    { pos: 4, team: "Lille OSC", p: 30, w: 16, d: 8, l: 6, gf: 49, ga: 32, gd: 17, pts: 56 },
    { pos: 5, team: "OGC Nice", p: 30, w: 15, d: 7, l: 8, gf: 45, ga: 36, gd: 9, pts: 52 },
    { pos: 6, team: "Olympique Lyonnais", p: 30, w: 14, d: 6, l: 10, gf: 44, ga: 40, gd: 4, pts: 48 },
    { pos: 7, team: "RC Lens", p: 30, w: 13, d: 8, l: 9, gf: 42, ga: 38, gd: 4, pts: 47 },
    { pos: 8, team: "Stade Rennais", p: 30, w: 12, d: 7, l: 11, gf: 40, ga: 42, gd: -2, pts: 43 }
  ],

  topScorers: [
    { rank: 1, name: "Ousmane Dembélé", goals: 24 },
    { rank: 2, name: "Bradley Barcola", goals: 18 },
    { rank: 3, name: "Gonçalo Ramos", goals: 15 },
    { rank: 4, name: "Désiré Doué", goals: 12 },
    { rank: 5, name: "Randal Kolo Muani", goals: 8 },
    { rank: 6, name: "Vitinha", goals: 7 },
    { rank: 7, name: "Achraf Hakimi", goals: 6 },
    { rank: 8, name: "Lee Kang-in", goals: 6 }
  ],

  seasonStats: [
    { value: "78", label: "Buts marqués", trend: "+12 vs N-1" },
    { value: "22", label: "Buts encaissés", trend: "Meilleure D" },
    { value: "76", label: "Points L1", trend: "1er" },
    { value: "15", label: "Clean sheets", trend: "+3 vs N-1" },
    { value: "65%", label: "Possession moy.", trend: "1er L1" },
    { value: "18.3", label: "Tirs / match", trend: "+2.1" },
    { value: "89%", label: "Passes réussies", trend: "Top EU" },
    { value: "24", label: "Victoires", trend: "80%" }
  ],

  matches: [
    {
      id: 1, date: "6 AVR 2026", comp: "Ligue 1", status: "finished",
      home: "PSG", away: "Olympique Marseille", scoreH: 3, scoreA: 1, result: "win",
      goals: [
        { min: 12, team: "PSG", scorer: "Ousmane Dembélé", assist: "Vitinha", desc: "Frappe enroulée du gauche en lucarne après un une-deux dans la surface" },
        { min: 34, team: "PSG", scorer: "Bradley Barcola", assist: "Hakimi", desc: "Contre-attaque éclair, centre de Hakimi repris du plat du pied" },
        { min: 58, team: "OM", scorer: "A. Harit", assist: "", desc: "Tête décroisée sur corner" },
        { min: 82, team: "PSG", scorer: "Désiré Doué", assist: "João Neves", desc: "Action collective conclue d'une demi-volée splendide aux 18 mètres" }
      ]
    },
    {
      id: 2, date: "2 AVR 2026", comp: "Ligue des Champions", status: "finished",
      home: "PSG", away: "Real Madrid", scoreH: 2, scoreA: 2, result: "draw",
      goals: [
        { min: 22, team: "PSG", scorer: "Gonçalo Ramos", assist: "Barcola", desc: "Tête plongeante sur un centre en retrait" },
        { min: 41, team: "RMA", scorer: "Vinicius Jr", assist: "", desc: "Slalom dans la défense et frappe croisée" },
        { min: 67, team: "PSG", scorer: "Vitinha", assist: "Dembélé", desc: "Frappe du droit de l'entrée de la surface, petit filet opposé" },
        { min: 89, team: "RMA", scorer: "J. Bellingham", assist: "", desc: "Égalisation sur coup-franc direct" }
      ]
    },
    {
      id: 3, date: "29 MAR 2026", comp: "Ligue 1", status: "finished",
      home: "AS Monaco", away: "PSG", scoreH: 0, scoreA: 4, result: "win",
      goals: [
        { min: 8, team: "PSG", scorer: "Ousmane Dembélé", assist: "Zaïre-Emery", desc: "Récupération haute et finition précise au premier poteau" },
        { min: 25, team: "PSG", scorer: "Ousmane Dembélé", assist: "Barcola", desc: "Doublé sur une magnifique ouverture en profondeur" },
        { min: 54, team: "PSG", scorer: "Bradley Barcola", assist: "Nuno Mendes", desc: "Centre tendu repris de volée du gauche" },
        { min: 78, team: "PSG", scorer: "Désiré Doué", assist: "Lee Kang-in", desc: "Petit pont puis frappe enveloppée à ras de terre" }
      ]
    },
    {
      id: 4, date: "22 MAR 2026", comp: "Ligue 1", status: "finished",
      home: "PSG", away: "OGC Nice", scoreH: 2, scoreA: 0, result: "win",
      goals: [
        { min: 31, team: "PSG", scorer: "João Neves", assist: "Fabián Ruiz", desc: "Frappe des 25 mètres en pleine lucarne" },
        { min: 73, team: "PSG", scorer: "Bradley Barcola", assist: "Hakimi", desc: "Une-deux avec Hakimi et finition en force" }
      ]
    },
    {
      id: 5, date: "15 AVR 2026", comp: "Ligue des Champions", status: "upcoming",
      home: "Real Madrid", away: "PSG", scoreH: null, scoreA: null, result: "upcoming",
      goals: []
    },
    {
      id: 6, date: "19 AVR 2026", comp: "Ligue 1", status: "upcoming",
      home: "PSG", away: "Stade Rennais", scoreH: null, scoreA: null, result: "upcoming",
      goals: []
    }
  ],

  trophies: [
    { icon: "🏆", count: 13, name: "Ligue 1" },
    { icon: "🏅", count: 15, name: "Coupes de France" },
    { icon: "🥇", count: 9, name: "Coupes de la Ligue" },
    { icon: "⭐", count: 12, name: "Trophées des Champions" },
    { icon: "🌍", count: 1, name: "Coupe des Coupes" },
    { icon: "🏆", count: 1, name: "Ligue des Champions" }
  ],

  history: [
    { year: "1970", title: "Fondation du club", desc: "Naissance du Paris Saint-Germain après la fusion du Paris FC et du Stade Saint-Germain." },
    { year: "1974", title: "Arrivée au Parc des Princes", desc: "Le PSG s'installe dans son stade mythique qui ne le quittera plus." },
    { year: "1986", title: "Premier titre de champion de France", desc: "Sous la houlette de Gérard Houllier, Paris décroche sa première Ligue 1." },
    { year: "1996", title: "Coupe des Coupes d'Europe", desc: "Victoire historique 1-0 face au Rapid Vienne à Bruxelles. Premier titre européen." },
    { year: "2011", title: "Ère Qatar Sports Investments", desc: "QSI rachète le club et lance une nouvelle dimension pour Paris." },
    { year: "2013-2020", title: "Ère de domination nationale", desc: "Sept titres de champion en huit saisons, arrivée de superstars mondiales." },
    { year: "2020", title: "Première finale de Ligue des Champions", desc: "Parcours historique jusqu'à la finale de Lisbonne face au Bayern." },
    { year: "2023", title: "Nouvelle ère Luis Enrique", desc: "L'entraîneur espagnol prend les rênes et relance un projet centré sur le collectif." },
    { year: "2025", title: "Première Ligue des Champions !", desc: "Le Paris Saint-Germain remporte enfin sa première Ligue des Champions, consécration historique." }
  ],

  // Formation 4-3-3 — positions en % du terrain (x de gauche à droite, y du but adverse au but PSG)
  formation: {
    label: "4 - 3 - 3",
    coach: "Luis Enrique",
    xi: [
      { id: 1,  x: 50, y: 92, short: "Donnarumma" },
      { id: 5,  x: 18, y: 72, short: "Hakimi" },
      { id: 8,  x: 38, y: 76, short: "Pacho" },
      { id: 4,  x: 62, y: 76, short: "Marquinhos" },
      { id: 6,  x: 82, y: 72, short: "N. Mendes" },
      { id: 13, x: 30, y: 52, short: "J. Neves" },
      { id: 10, x: 50, y: 56, short: "Vitinha" },
      { id: 11, x: 70, y: 52, short: "F. Ruiz" },
      { id: 16, x: 22, y: 22, short: "Dembélé" },
      { id: 18, x: 50, y: 14, short: "G. Ramos" },
      { id: 17, x: 78, y: 22, short: "Barcola" }
    ]
  },

  news: [
    {
      tag: "Match", icon: "⚽", date: "7 AVRIL 2026",
      title: "Le Classique pour Paris : 3-1 face à l'OM",
      excerpt: "Porté par un Dembélé étincelant, le PSG s'impose au Parc et consolide son avance en tête."
    },
    {
      tag: "Mercato", icon: "📝", date: "5 AVRIL 2026",
      title: "Prolongation en vue pour Vitinha",
      excerpt: "Le milieu portugais serait proche d'une prolongation jusqu'en 2030 selon nos informations."
    },
    {
      tag: "Stade", icon: "🏟️", date: "3 AVRIL 2026",
      title: "Travaux au Parc : nouvelle tribune en 2027",
      excerpt: "Le projet de modernisation du Parc des Princes avance, avec une capacité bientôt portée à 52 000 places."
    }
  ]
};

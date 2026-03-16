// Stockage du token JWT en mémoire uniquement (pas localStorage)
// pour éviter le vol de token par XSS.
let _token = null

export const getToken = () => _token
export const setToken = (t) => { _token = t }
export const clearToken = () => { _token = null }

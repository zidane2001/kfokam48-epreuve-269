import { useCallback, useSyncExternalStore } from 'react';
import { api, getAuthToken, setAuthToken } from '../utils/api';

/**
 * Session utilisateur (evolution PO) : store externe partage — le header et les
 * gardes de routes voient la meme session. Token JWT + identite, revalidés via
 * /api/auth/me au demarrage (token expire -> deconnexion silencieuse).
 */

export type Role = 'ETUDIANT' | 'FORMATEUR';

export interface SessionUtilisateur {
  login: string;
  role: Role;
  etudiantId: string | null;
  promotionId: string | null;
}

interface EtatAuth {
  session: SessionUtilisateur | null;
  initialisation: boolean;
}

const SESSION_KEY = 'kfokam48-session';

function lireSession(): SessionUtilisateur | null {
  try {
    const raw = window.localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) as SessionUtilisateur : null;
  } catch {
    return null;
  }
}

function ecrireSession(s: SessionUtilisateur | null) {
  try {
    if (s) window.localStorage.setItem(SESSION_KEY, JSON.stringify(s));
    else window.localStorage.removeItem(SESSION_KEY);
  } catch {
    // stockage indisponible : session memoire
  }
}

let etat: EtatAuth = { session: lireSession(), initialisation: Boolean(getAuthToken()) };
const abonnes = new Set<() => void>();

function setEtat(partiel: Partial<EtatAuth>) {
  etat = { ...etat, ...partiel };
  abonnes.forEach((l) => l());
}

// Revalidation unique du token au chargement du module (SPA 100% client).
let demarre = false;
function demarrer() {
  if (demarre) return;
  demarre = true;
  if (!getAuthToken()) {
    setEtat({ session: null, initialisation: false });
    return;
  }
  api.whoAmI()
    .then((me) => {
      const s: SessionUtilisateur = {
        login: me.login,
        role: me.role,
        etudiantId: me.etudiantId == null ? null : String(me.etudiantId),
        promotionId: me.promotionId == null ? null : String(me.promotionId),
      };
      ecrireSession(s);
      setEtat({ session: s, initialisation: false });
    })
    .catch(() => {
      api.deconnexion();
      ecrireSession(null);
      setEtat({ session: null, initialisation: false });
    });
}

export function useAuthSession() {
  const etatCourant = useSyncExternalStore(
    (listener) => {
      abonnes.add(listener);
      demarrer();
      return () => abonnes.delete(listener);
    },
    () => etat,
    () => etat,
  );

  const connexion = useCallback(async (login: string, motDePasse: string) => {
    const res = await api.connexion(login, motDePasse);
    setAuthToken(res.token);
    const s: SessionUtilisateur = {
      login: res.login,
      role: res.role,
      etudiantId: res.etudiantId == null ? null : String(res.etudiantId),
      promotionId: res.promotionId == null ? null : String(res.promotionId),
    };
    ecrireSession(s);
    setEtat({ session: s, initialisation: false });
    // Compat design : l'espace etudiant lit son identite depuis useStudentIdentity.
    if (s.role === 'ETUDIANT' && s.etudiantId && s.promotionId) {
      try {
        window.localStorage.setItem(
          'kfokam48-identite-etudiant',
          JSON.stringify({ etudiantId: s.etudiantId, promotionId: s.promotionId }));
      } catch {
        // memoire seule
      }
    }
    return s;
  }, []);

  const deconnexion = useCallback(() => {
    api.deconnexion();
    ecrireSession(null);
    setEtat({ session: null, initialisation: false });
  }, []);

  return { session: etatCourant.session, initialisation: etatCourant.initialisation, connexion, deconnexion };
}

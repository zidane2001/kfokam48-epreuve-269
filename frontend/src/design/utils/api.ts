import type {
  ApiErrorBody,
  EtudiantDto,
  ExerciceDto,
  PresenceDto,
  PromotionDto,
  RelectureDto,
  SessionDto,
  StatutPresenceDto,
  SuiviDto } from
'../types/domain';

/**
 * Couche d'acces a l'API REST — seul point d'entree du frontend vers le backend (F3/ENF15).
 * Le design (frontendModel) etait branche sur un mockServer ; ici, un vrai fetch()
 * vers le backend Spring Boot expose par NEXT_PUBLIC_API_URL (defaut : localhost:8080).
 */

export const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(
  readonly status: number,
  readonly code: string,
  message: string)
  {
    super(message);
  }
}

type Method = 'GET' | 'POST' | 'PUT';

async function transport(method: Method, path: string, body?: unknown): Promise<{status: number;body: unknown;}> {
  const reponse = await fetch(`${API_BASE}${path}`, {
    method,
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  let corps: unknown = undefined;
  if (reponse.status !== 204) {
    corps = await reponse.json().catch(() => undefined);
  }
  return { status: reponse.status, body: corps };
}

async function request<T>(method: Method, path: string, body?: unknown): Promise<T> {
  let response: {status: number;body: unknown;};
  try {
    response = await transport(method, path, body);
  } catch {
    throw new ApiError(0, 'RESEAU', 'Impossible de joindre le serveur. Vérifiez votre connexion.');
  }
  if (response.status >= 400) {
    const err = (response.body ?? { code: 'ERREUR_INATTENDUE', message: 'Erreur inconnue.' }) as ApiErrorBody;
    throw new ApiError(response.status, err.code, err.message);
  }
  return response.body as T;
}

export function toApiError(e: unknown): ApiError {
  if (e instanceof ApiError) return e;
  return new ApiError(0, 'INCONNU', 'Une erreur inattendue est survenue.');
}

// Adapters : le design consomme des ids string et prenom/nom separes,
// le backend produit des ids number et un champ nom complet. On convertit ici,
// pour ne toucher a AUCUN composant du design.
const num = (v: string | number | null | undefined) => (v == null ? null : Number(v));
const str = (v: string | number | null | undefined) => (v == null ? null : String(v));
const splitNom = (nomComplet: string): { prenom: string; nom: string } => {
  const i = nomComplet.indexOf(' ');
  return i < 0 ? { prenom: nomComplet, nom: '' } : { prenom: nomComplet.slice(0, i), nom: nomComplet.slice(i + 1) };
};

export const api = {
  listerPromotions: async () => {
    const promotions = await request<{ id: number; nom: string; etudiants: { id: number; nom: string }[] }[]>('GET', '/api/promotions');
    return promotions.map(p => ({
      id: String(p.id),
      nom: p.nom,
      etudiants: p.etudiants.map(e => {
        const { prenom, nom } = splitNom(e.nom);
        return { id: String(e.id), prenom, nom, promotionId: String(p.id) };
      }),
    })) as PromotionDto[];
  },
  listerEtudiants: async (promotionId: string) => {
    const promotions = await api.listerPromotions();
    const p = promotions.find(x => x.id === promotionId);
    return (p?.etudiants ?? []) as EtudiantDto[];
  },

  listerSessions: async (promotionId?: string) => {
    const qs = promotionId ? `?promotionId=${promotionId}` : '';
    const sessions = await request<{
      id: number; titre: string; promotionId: number; promotionNom: string;
      code: string; ouvertureAt: string; expirationAt: string;
      statut: string; clotureAt: string | null; codeExpire: boolean;
      nbEtudiants?: number; nbPresents?: number; nbExercices?: number;
    }[]>('GET', `/api/sessions${qs}`);
    return sessions.map(s => ({
      id: String(s.id),
      titre: s.titre,
      promotionId: String(s.promotionId),
      promotionNom: s.promotionNom,
      code: s.code,
      ouvertureAt: s.ouvertureAt,
      expirationAt: s.expirationAt,
      statut: s.statut as 'OUVERTE' | 'CLOTUREE',
      clotureAt: s.clotureAt,
      codeExpire: s.codeExpire,
      nbEtudiants: s.nbEtudiants ?? 0,
      nbPresents: s.nbPresents ?? 0,
      nbExercices: s.nbExercices ?? 0,
    })) as SessionDto[];
  },
  obtenirSession: (sessionId: string) => api.listerSessions().then(liste => liste.find(s => s.id === sessionId)!),
  ouvrirSession: async (payload: {titre: string;promotionId: string;}) => {
    const s = await request<{ id: number; code: string; ouvertureAt: string; expirationAt: string }>(
      'POST', '/api/sessions', { titre: payload.titre, promotionId: Number(payload.promotionId) });
    const promotions = await api.listerPromotions();
    const promo = promotions.find(p => p.id === payload.promotionId);
    return {
      id: String(s.id), titre: payload.titre, promotionId: payload.promotionId,
      promotionNom: promo?.nom ?? '?', code: s.code,
      ouvertureAt: s.ouvertureAt, expirationAt: s.expirationAt,
      statut: 'OUVERTE' as const, clotureAt: null, codeExpire: false,
      nbEtudiants: promo?.etudiants?.length ?? 0, nbPresents: 0, nbExercices: 0,
    } as SessionDto;
  },
  cloturerSession: async (sessionId: string) => {
    await request<unknown>('POST', `/api/sessions/${sessionId}/cloture`);
    return api.obtenirSession(sessionId);
  },

  listerPresences: (sessionId: string) =>
    request<{ id: number; sessionId: number; etudiantId: number; etudiantNom: string; source: string; enregistreeAt: string }[]>(
      'GET', `/api/sessions/${sessionId}/presences`)
      .then(liste => liste.map(p => ({
        id: String(p.id), sessionId: String(p.sessionId), etudiantId: String(p.etudiantId),
        etudiantNom: p.etudiantNom, source: p.source as 'ETUDIANT' | 'FORMATEUR', enregistreeAt: p.enregistreeAt,
      }))) as Promise<PresenceDto[]>,
  marquerPresence: async (sessionId: string, payload: {etudiantId: string;code: string;}) => {
    const p = await request<{ id: number; sessionId: number; etudiantId: number; source: string }>(
      'POST', '/api/presences', { code: payload.code, etudiantId: Number(payload.etudiantId) });
    return {
      id: String(p.id), sessionId: String(p.sessionId), etudiantId: String(p.etudiantId),
      etudiantNom: '', source: p.source as 'ETUDIANT', enregistreeAt: new Date().toISOString(),
    } as PresenceDto;
  },
  ajouterPresenceManuelle: async (sessionId: string, payload: {etudiantId: string;}) => {
    const p = await request<{ id: number; sessionId: number; etudiantId: number; etudiantNom: string; source: string; enregistreeAt: string }>(
      'POST', `/api/sessions/${sessionId}/presences/manuelle`, { etudiantId: Number(payload.etudiantId) });
    return {
      id: String(p.id), sessionId: String(p.sessionId), etudiantId: String(p.etudiantId),
      etudiantNom: p.etudiantNom, source: p.source as 'FORMATEUR', enregistreeAt: p.enregistreeAt,
    } as PresenceDto;
  },
  statutPresence: async (sessionId: string, etudiantId: string) => {
    const s = await request<{ sessionId: number; etudiantId: number; present: boolean; source: string | null; enregistreeAt: string | null; tentativesRestantes: number; bloqueJusqua: string | null }>(
      'GET', `/api/sessions/${sessionId}/etudiants/${etudiantId}/statut-presence`);
    return {
      sessionId: String(s.sessionId), etudiantId: String(s.etudiantId),
      present: s.present, source: (s.source as 'ETUDIANT' | 'FORMATEUR' | null),
      enregistreeAt: s.enregistreeAt, tentativesRestantes: s.tentativesRestantes, bloqueJusqua: s.bloqueJusqua,
    } as StatutPresenceDto;
  },

  deposerExercice: async (sessionId: string, payload: {etudiantId: string;lien: string;}) => {
    const e = await request<{ id: number; statut: string }>(
      'POST', `/api/sessions/${sessionId}/exercices`, { etudiantId: Number(payload.etudiantId), lien: payload.lien });
    return {
      id: String(e.id), sessionId, sessionTitre: '', sessionStatut: 'OUVERTE',
      lien: payload.lien, deposeAt: new Date().toISOString(),
      statut: e.statut as ExerciceDto['statut'],
      lienModifiable: e.statut === 'EN_ATTENTE_RELECTEUR',
      note: null, commentaire: null, resultatDefinitif: false,
    } as ExerciceDto;
  },
  modifierLien: async (exerciceId: string, payload: {etudiantId: string;lien: string;}) => {
    const e = await request<{ id: number; sessionId: number; sessionTitre: string; sessionStatut: string; lien: string; deposeAt: string; statut: string; lienModifiable: boolean; note: number | null; commentaire: string | null; resultatDefinitif: boolean }>(
      'PUT', `/api/exercices/${exerciceId}/lien`, { etudiantId: Number(payload.etudiantId), lien: payload.lien });
    return {
      id: String(e.id), sessionId: String(e.sessionId), sessionTitre: e.sessionTitre,
      sessionStatut: e.sessionStatut as SessionDto['statut'], lien: e.lien, deposeAt: e.deposeAt,
      statut: e.statut as ExerciceDto['statut'], lienModifiable: e.lienModifiable,
      note: e.note, commentaire: e.commentaire, resultatDefinitif: e.resultatDefinitif,
    } as ExerciceDto;
  },
  listerExercicesEtudiant: (etudiantId: string) =>
    request<{ id: number; sessionId: number; sessionTitre: string; sessionStatut: string; lien: string; deposeAt: string; statut: string; lienModifiable: boolean; note: number | null; commentaire: string | null; resultatDefinitif: boolean }[]>(
      'GET', `/api/etudiants/${etudiantId}/exercices`)
      .then(liste => liste.map(e => ({
        id: String(e.id), sessionId: String(e.sessionId), sessionTitre: e.sessionTitre,
        sessionStatut: e.sessionStatut as SessionDto['statut'], lien: e.lien, deposeAt: e.deposeAt,
        statut: e.statut as ExerciceDto['statut'], lienModifiable: e.lienModifiable,
        note: e.note, commentaire: e.commentaire, resultatDefinitif: e.resultatDefinitif,
      }))) as Promise<ExerciceDto[]>,

  listerRelecturesEtudiant: (etudiantId: string) =>
    request<{ id: number; exerciceId: number; sessionId: number; sessionTitre: string; lien: string; note: number | null; commentaire: string | null; statut: string; modifiable: boolean; attribueeAt: string; rendueAt: string | null }[]>(
      'GET', `/api/etudiants/${etudiantId}/relectures`)
      .then(liste => liste.map(r => ({
        id: String(r.id), exerciceId: String(r.exerciceId), sessionId: String(r.sessionId),
        sessionTitre: r.sessionTitre, lien: r.lien, note: r.note, commentaire: r.commentaire,
        statut: r.statut as 'EN_ATTENTE' | 'RENDUE', modifiable: r.modifiable,
        attribueeAt: r.attribueeAt, rendueAt: r.rendueAt,
      }))) as Promise<RelectureDto[]>,
  soumettreRelecture: async (
  relectureId: string,
  payload: {relecteurId: string;note: number | null;commentaire: string;}) => {
    await request<{ id: number; exerciceId: number; note: number; commentaire: string; statutExercice: string }>(
      'PUT', `/api/relectures/${relectureId}`, {
        relecteurId: Number(payload.relecteurId), note: payload.note, commentaire: payload.commentaire,
      });
    return { id: relectureId };
  },

  obtenirSuivi: (promotionId: string, sessionId?: string) =>
    request<{ promotionId: number; sessionId: number | null; lignes: { etudiantId: number; nomComplet: string; presentSession: boolean | null; sourcePresence: string | null; statutExercice: string | null; presences: number; sessionsComptees: number; exercicesDeposes: number; notesRecues: number; moyenne: number | null; relecturesEnAttente: number }[]; totaux: { etudiants: number; presents: number | null; sessionsComptees: number; exercicesDeposes: number; relecturesEnAttente: number; moyennePromotion: number | null } }>(
      'GET', `/api/tableau?promotionId=${promotionId}${sessionId ? `&sessionId=${sessionId}` : ''}`)
      .then(suivi => ({
        promotionId: String(suivi.promotionId),
        sessionId: suivi.sessionId == null ? null : String(suivi.sessionId),
        lignes: suivi.lignes.map(l => ({
          etudiantId: String(l.etudiantId), nomComplet: l.nomComplet,
          presentSession: l.presentSession,
          sourcePresence: l.sourcePresence as 'ETUDIANT' | 'FORMATEUR' | null,
          statutExercice: l.statutExercice as ExerciceDto['statut'] | null,
          presences: l.presences, sessionsComptees: l.sessionsComptees,
          exercicesDeposes: l.exercicesDeposes, notesRecues: l.notesRecues,
          moyenne: l.moyenne, relecturesEnAttente: l.relecturesEnAttente,
        })),
        totaux: suivi.totaux,
      })) as Promise<SuiviDto>,

  reinitialiserDemo: async () => ({ reinitialise: false }) as { reinitialise: boolean; },

  /** Le vrai backend n'a pas de pub/sub : les pages rafraichissent via reload(). */
  onDataChange: (_callback: () => void) => () => undefined
};

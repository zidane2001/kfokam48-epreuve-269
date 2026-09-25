export type SessionStatut = 'OUVERTE' | 'CLOTUREE';
export type PresenceSource = 'ETUDIANT' | 'FORMATEUR';
export type ExerciceStatut = 'EN_ATTENTE_RELECTEUR' | 'RELECTEUR_ATTRIBUE' | 'RELU';
export type RelectureStatut = 'EN_ATTENTE' | 'RENDUE';

/** Identité choisie par l'étudiant dans la liste (aucun mot de passe). */
export interface Identite {
  etudiantId: string;
  promotionId: string;
}

export interface ApiErrorBody {
  code: string;
  message: string;
}

export interface PromotionDto {
  id: string;
  nom: string;
  etudiants?: EtudiantDto[];
}

export interface EtudiantDto {
  id: string;
  prenom: string;
  nom: string;
  promotionId: string;
}

export interface SessionDto {
  id: string;
  titre: string;
  promotionId: string;
  promotionNom: string;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
  statut: SessionStatut;
  clotureAt: string | null;
  codeExpire: boolean;
  nbEtudiants: number;
  nbPresents: number;
  nbExercices: number;
}

export interface PresenceDto {
  id: string;
  sessionId: string;
  etudiantId: string;
  etudiantNom: string;
  source: PresenceSource;
  enregistreeAt: string;
}

export interface StatutPresenceDto {
  sessionId: string;
  etudiantId: string;
  present: boolean;
  source: PresenceSource | null;
  enregistreeAt: string | null;
  tentativesRestantes: number;
  bloqueJusqua: string | null;
}

/** Vue « auteur » d'un exercice : ne contient jamais l'identité du relecteur (RG11, RG20). */
export interface ExerciceDto {
  id: string;
  sessionId: string;
  sessionTitre: string;
  sessionStatut: SessionStatut;
  lien: string;
  deposeAt: string;
  statut: ExerciceStatut;
  lienModifiable: boolean;
  note: number | null;
  commentaire: string | null;
  resultatDefinitif: boolean;
}

export interface RelectureDto {
  id: string;
  exerciceId: string;
  sessionId: string;
  sessionTitre: string;
  lien: string;
  note: number | null;
  commentaire: string | null;
  statut: RelectureStatut;
  modifiable: boolean;
  attribueeAt: string;
  rendueAt: string | null;
}

export interface SuiviLigneDto {
  etudiantId: string;
  nomComplet: string;
  presentSession: boolean | null;
  sourcePresence: PresenceSource | null;
  statutExercice: ExerciceStatut | null;
  presences: number;
  sessionsComptees: number;
  exercicesDeposes: number;
  notesRecues: number;
  moyenne: number | null;
  relecturesEnAttente: number;
}

export interface SuiviDto {
  promotionId: string;
  sessionId: string | null;
  lignes: SuiviLigneDto[];
  totaux: {
    etudiants: number;
    presents: number | null;
    sessionsComptees: number;
    exercicesDeposes: number;
    relecturesEnAttente: number;
    moyennePromotion: number | null;
  };
}
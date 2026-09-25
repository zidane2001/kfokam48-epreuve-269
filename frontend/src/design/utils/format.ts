import { format } from 'date-fns';
import { fr } from 'date-fns/locale';

/** Moyenne nulle = aucune note reçue : on affiche « — » car 0 est une note valide (décision 7.9). */
export function formatMoyenne(moyenne: number | null): string {
  if (moyenne === null) return '—';
  return moyenne.toLocaleString('fr-FR', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
}

export function formatHeure(iso: string): string {
  return format(new Date(iso), 'HH:mm');
}

export function formatDateHeure(iso: string): string {
  return format(new Date(iso), "d MMM yyyy 'à' HH:mm", { locale: fr });
}

export function formatDateCourte(iso: string): string {
  return format(new Date(iso), 'd MMM · HH:mm', { locale: fr });
}

export function formatCompteARebours(ms: number): string {
  const total = Math.max(0, Math.ceil(ms / 1000));
  const minutes = Math.floor(total / 60);
  const secondes = total % 60;
  return `${minutes.toString().padStart(2, '0')}:${secondes.toString().padStart(2, '0')}`;
}

export function initiales(prenom: string, nom: string): string {
  return `${prenom.charAt(0)}${nom.charAt(0)}`.toUpperCase();
}
import { useCallback, useState } from 'react';
import type { Identite } from '../types/domain';

const STORAGE_KEY = 'kfokam48-identite-etudiant';

export function useStudentIdentity() {
  const [identite, setIdentite] = useState<Identite | null>(() => lireIdentite());

  const choisir = useCallback((value: Identite) => {
    try {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
    } catch {

      // Identité conservée en mémoire uniquement.
    }setIdentite(value);
  }, []);

  const changer = useCallback(() => {
    try {
      window.localStorage.removeItem(STORAGE_KEY);
    } catch {

      // rien à nettoyer
    }setIdentite(null);
  }, []);

  return { identite, choisir, changer };
}

function lireIdentite(): Identite | null {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) as Identite : null;
  } catch {
    return null;
  }
}
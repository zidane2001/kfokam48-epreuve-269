import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/Card';
import { Connexion } from '../../auth/Connexion';
import { useAuthSession } from '../../auth/authStore';

/**
 * Evolution PO : plus de choix libre d'identite. L'etudiant se connecte avec ses
 * identifiants — son compte definit son identite. Garde le meme point d'entree
 * visuel que le design d'origine.
 */
export function IdentityPicker({ onSelect }: { onSelect: (identite: import('../../types/domain').Identite) => void; }) {
  const { session } = useAuthSession();

  if (session?.etudiantId && session?.promotionId) {
    onSelect({ etudiantId: session.etudiantId, promotionId: session.promotionId });
  }

  return (
    <div className="mx-auto w-full max-w-md space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Espace étudiant</CardTitle>
          <CardDescription>
            Connectez-vous avec vos identifiants : prenom.nom · mot de passe initial prenom.nom.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Connexion />
        </CardContent>
      </Card>
    </div>);

}

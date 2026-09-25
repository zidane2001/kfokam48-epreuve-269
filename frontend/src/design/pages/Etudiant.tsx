import React from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '../components/PageHeader';
import { StudentWorkspace } from '../components/etudiant/StudentWorkspace';
import { Connexion } from '../auth/Connexion';
import { useAuthSession } from '../auth/authStore';

/**
 * Espace etudiant (evolution PO) : l'identite n'est plus choisie librement dans
 * une liste — elle provient du compte connecte. « Changer d'identite » = deconnexion.
 */
export function Etudiant() {
  const { session, deconnexion } = useAuthSession();
  const navigate = useNavigate();

  const identite = session?.etudiantId && session?.promotionId
    ? { etudiantId: session.etudiantId, promotionId: session.promotionId }
    : null;

  if (!identite) {
    return (
      <div className="space-y-6">
        <PageHeader
          title="Espace étudiant"
          description="Enregistrez votre présence, déposez votre exercice, relisez celui d’un camarade et consultez vos résultats." />
        <Connexion />
      </div>);
  }

  return (
    <StudentWorkspace
      identite={identite}
      onChangeIdentity={() => {
        deconnexion();
        navigate('/');
      }} />);

}

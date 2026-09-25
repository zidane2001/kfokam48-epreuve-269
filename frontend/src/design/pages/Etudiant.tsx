import React from 'react';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '../components/PageHeader';
import { IdentityPicker } from '../components/etudiant/IdentityPicker';
import { StudentWorkspace } from '../components/etudiant/StudentWorkspace';
import { Connexion } from '../auth/Connexion';
import { useAuthSession, authRequise } from '../auth/authStore';
import { useStudentIdentity } from '../hooks/useStudentIdentity';

/**
 * Espace etudiant. Evolution PO (auth active) : l'identite provient du compte
 * connecte. Mode AUTH_REQUIS=false (contrat du sujet, Q1) : choix libre dans la
 * liste, comme prevu par le design d'origine.
 */
export function Etudiant() {
  const { session, deconnexion } = useAuthSession();
  const navigate = useNavigate();
  const originale = useStudentIdentity();

  // Mode contrat du sujet : comportement initial intact.
  if (!authRequise()) {
    const { identite, choisir, changer } = originale;
    if (identite) return <StudentWorkspace identite={identite} onChangeIdentity={changer} />;
    return (
      <div className="space-y-6">
        <PageHeader
          title="Espace étudiant"
          description="Enregistrez votre présence, déposez votre exercice, relisez celui d’un camarade et consultez vos résultats." />
        <IdentityPicker onSelect={choisir} />
      </div>);
  }

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

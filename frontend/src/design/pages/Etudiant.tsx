import React from 'react';
import { PageHeader } from '../components/PageHeader';
import { IdentityPicker } from '../components/etudiant/IdentityPicker';
import { StudentWorkspace } from '../components/etudiant/StudentWorkspace';
import { useStudentIdentity } from '../hooks/useStudentIdentity';

export function Etudiant() {
  const { identite, choisir, changer } = useStudentIdentity();

  if (identite) return <StudentWorkspace identite={identite} onChangeIdentity={changer} />;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Espace étudiant"
        description="Enregistrez votre présence, déposez votre exercice, relisez celui d’un camarade et consultez vos résultats." />
      
      <IdentityPicker onSelect={choisir} />
    </div>);

}
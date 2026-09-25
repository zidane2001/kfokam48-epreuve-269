import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { PageHeader } from '../components/PageHeader';
import { EmptyState } from '../components/feedback/EmptyState';
import { Lock } from 'lucide-react';
import { useAuthSession, type Role } from './authStore';

/** Garde de route (evolution PO) : connexion requise, role optionnel impose. */
export function ExigeConnexion({ role, children }: { role?: Role; children: React.ReactNode }) {
  const { session, initialisation } = useAuthSession();
  const location = useLocation();

  if (initialisation) {
    return <div className="py-16 text-center text-sm text-muted-foreground" role="status">Chargement…</div>;
  }
  if (!session) {
    return <Navigate to="/connexion" replace state={{ from: location.pathname + location.search }} />;
  }
  if (role && session.role !== role) {
    return (
      <div className="space-y-6">
        <PageHeader title="Accès refusé" />
        <EmptyState
          icon={Lock}
          title="Cet espace est réservé"
          description={`Connectez-vous avec un compte ${role === 'FORMATEUR' ? 'formateur' : 'étudiant'}.`} />
      </div>);
  }
  return <>{children}</>;
}

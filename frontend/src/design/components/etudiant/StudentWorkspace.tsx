import React from 'react';
import { LogOut } from 'lucide-react';
import { Button } from '../ui/Button';
import { Skeleton } from '../ui/Skeleton';
import { EmptyState } from '../feedback/EmptyState';
import { ErrorState } from '../feedback/ErrorState';
import { ExerciceCard } from './ExerciceCard';
import { PresenceCard } from './PresenceCard';
import { RelecturesCard } from './RelecturesCard';
import { ResultatsCard } from './ResultatsCard';
import { SessionHero } from './SessionHero';
import { useApiQuery } from '../../hooks/useApiQuery';
import { api } from '../../utils/api';
import type { Identite } from '../../types/domain';

interface StudentWorkspaceProps {
  identite: Identite;
  onChangeIdentity: () => void;
}

export function StudentWorkspace({ identite, onChangeIdentity }: StudentWorkspaceProps) {
  const { etudiantId, promotionId } = identite;
  const promotionsQ = useApiQuery(() => api.listerPromotions(), []);
  const etudiantsQ = useApiQuery(() => api.listerEtudiants(promotionId), [promotionId]);
  const sessionsQ = useApiQuery(() => api.listerSessions(promotionId), [promotionId]);
  const exercicesQ = useApiQuery(() => api.listerExercicesEtudiant(etudiantId), [etudiantId]);
  const relecturesQ = useApiQuery(() => api.listerRelecturesEtudiant(etudiantId), [etudiantId]);

  const sessionActive = sessionsQ.data?.find((s) => s.statut === 'OUVERTE') ?? null;
  const sessionActiveId = sessionActive?.id;
  const statutQ = useApiQuery(
    () => api.statutPresence(sessionActiveId as string, etudiantId),
    [sessionActiveId, etudiantId],
    { enabled: Boolean(sessionActiveId) }
  );

  if (etudiantsQ.error) return <ErrorState error={etudiantsQ.error} onRetry={etudiantsQ.reload} />;
  if (!etudiantsQ.data) {
    return (
      <div className="space-y-6" role="status" aria-label="Chargement de votre espace">
        <Skeleton className="h-48 w-full rounded-xl" />
        <div className="grid gap-6 lg:grid-cols-2">
          <Skeleton className="h-64 rounded-xl" />
          <Skeleton className="h-64 rounded-xl" />
        </div>
      </div>);

  }

  const etudiant = etudiantsQ.data.find((e) => e.id === etudiantId);
  if (!etudiant) {
    return (
      <EmptyState
        icon={LogOut}
        title="Identité introuvable"
        description="Cet étudiant n’existe plus dans la promotion."
        action={<Button onClick={onChangeIdentity}>Choisir mon nom</Button>} />);


  }

  const promotionNom = promotionsQ.data?.find((p) => p.id === promotionId)?.nom ?? null;
  const exerciceSession = sessionActive ?
  exercicesQ.data?.find((x) => x.sessionId === sessionActive.id) ?? null :
  null;

  return (
    <div className="space-y-6">
      <SessionHero
        etudiant={etudiant}
        promotionNom={promotionNom}
        session={sessionActive}
        sessionsLoading={!sessionsQ.data && !sessionsQ.error}
        statut={statutQ.data}
        exercice={exerciceSession}
        relectures={relecturesQ.data}
        onChangeIdentity={onChangeIdentity} />
      

      {sessionsQ.error && <ErrorState error={sessionsQ.error} onRetry={sessionsQ.reload} />}

      {sessionActive &&
      <div className="grid items-start gap-6 lg:grid-cols-2">
          <div className="space-y-6">
            <section id="presence" className="scroll-mt-20" aria-label="Étape 1 — Présence">
              <PresenceCard session={sessionActive} etudiantId={etudiantId} statutQ={statutQ} />
            </section>
            <section id="depot" className="scroll-mt-20" aria-label="Étape 2 — Dépôt">
              {exercicesQ.error ?
            <ErrorState error={exercicesQ.error} onRetry={exercicesQ.reload} /> :
            !exercicesQ.data ?
            <Skeleton className="h-56 w-full rounded-xl" /> :

            <ExerciceCard
              key={exerciceSession?.id ?? 'nouveau'}
              session={sessionActive}
              etudiantId={etudiantId}
              exercice={exerciceSession} />

            }
            </section>
          </div>
          <section id="relectures" className="scroll-mt-20" aria-label="Étape 3 — Relecture">
            <RelecturesCard query={relecturesQ} etudiantId={etudiantId} />
          </section>
        </div>
      }

      <div className={sessionActive ? '' : 'grid items-start gap-6 lg:grid-cols-2'}>
        <section id="resultats" className="scroll-mt-20" aria-label="Étape 4 — Résultats">
          <ResultatsCard query={exercicesQ} />
        </section>
        {!sessionActive &&
        <section id="relectures" className="scroll-mt-20" aria-label="Relectures">
            <RelecturesCard query={relecturesQ} etudiantId={etudiantId} />
          </section>
        }
      </div>
    </div>);

}
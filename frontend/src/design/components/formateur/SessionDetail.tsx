import React from 'react';
import { Clock, FileText, LayoutList, Lock, Users } from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '../ui/Alert';
import { ButtonLink } from '../ButtonLink';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/Card';
import { Skeleton } from '../ui/Skeleton';
import { SessionStatusBadge } from '../badges/SessionStatusBadge';
import { ErrorState } from '../feedback/ErrorState';
import { LoadingRows } from '../feedback/LoadingRows';
import { AttendanceCode } from './AttendanceCode';
import { CloseSessionButton } from './CloseSessionButton';
import { ManualPresenceForm } from './ManualPresenceForm';
import { PresenceTable } from './PresenceTable';
import { useApiQuery } from '../../hooks/useApiQuery';
import { api } from '../../utils/api';
import { formatDateHeure } from '../../utils/format';

export function SessionDetail({ sessionId }: {sessionId: string;}) {
  const sessionQ = useApiQuery(() => api.obtenirSession(sessionId), [sessionId]);
  const presencesQ = useApiQuery(() => api.listerPresences(sessionId), [sessionId]);
  const promotionId = sessionQ.data?.promotionId;
  const etudiantsQ = useApiQuery(() => api.listerEtudiants(promotionId as string), [promotionId], {
    enabled: Boolean(promotionId)
  });

  if (sessionQ.error) return <ErrorState error={sessionQ.error} onRetry={sessionQ.reload} />;
  if (!sessionQ.data) {
    return (
      <div className="space-y-6" role="status" aria-label="Chargement de la session">
        <Skeleton className="h-72 w-full rounded-xl" />
        <Skeleton className="h-64 w-full rounded-xl" />
      </div>);

  }

  const session = sessionQ.data;
  const closed = session.statut === 'CLOTUREE';
  const stats = [
  { icon: Users, label: 'Présents', value: `${session.nbPresents} / ${session.nbEtudiants}` },
  { icon: FileText, label: 'Exercices déposés', value: String(session.nbExercices) },
  { icon: Clock, label: 'Ouverte le', value: formatDateHeure(session.ouvertureAt) },
  {
    icon: Clock,
    label: closed ? 'Clôturée le' : 'Statut',
    value: closed && session.clotureAt ? formatDateHeure(session.clotureAt) : 'En cours'
  }];


  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div className="space-y-1.5">
              <div className="flex flex-wrap items-center gap-2">
                <CardTitle className="text-lg">{session.titre}</CardTitle>
                <SessionStatusBadge statut={session.statut} />
              </div>
              <CardDescription>{session.promotionNom}</CardDescription>
            </div>
            <div className="flex flex-wrap gap-2">
              <ButtonLink to={`/suivi?promotion=${session.promotionId}&session=${session.id}`}>
                <LayoutList /> Voir le suivi
              </ButtonLink>
              {!closed && <CloseSessionButton session={session} />}
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-5">
          {closed &&
          <Alert>
              <Lock />
              <AlertTitle>Session clôturée</AlertTitle>
              <AlertDescription>
                Présences et dépôts sont fermés, les relectures rendues sont définitives. Les relectures non rendues
                restent signalées « en attente » dans le tableau de suivi.
              </AlertDescription>
            </Alert>
          }
          <AttendanceCode session={session} />
          <dl className="grid grid-cols-2 gap-3 lg:grid-cols-4">
            {stats.map(({ icon: Icon, label, value }) =>
            <div key={label} className="rounded-lg border border-border p-3">
                <dt className="flex items-center gap-1.5 text-xs text-muted-foreground">
                  <Icon className="size-3.5" aria-hidden="true" />
                  {label}
                </dt>
                <dd className="mt-1 text-sm font-medium">{value}</dd>
              </div>
            )}
          </dl>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Présences</CardTitle>
          <CardDescription>
            Les présences ajoutées par le formateur sont distinguées de celles saisies par les étudiants.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {!closed &&
          <ManualPresenceForm
            sessionId={session.id}
            etudiants={etudiantsQ.data ?? []}
            presences={presencesQ.data ?? []}
            disabled={!etudiantsQ.data || !presencesQ.data} />

          }
          {presencesQ.error ?
          <ErrorState error={presencesQ.error} onRetry={presencesQ.reload} /> :
          !presencesQ.data ?
          <LoadingRows rows={4} className="h-10" /> :

          <PresenceTable presences={presencesQ.data} />
          }
        </CardContent>
      </Card>
    </div>);

}
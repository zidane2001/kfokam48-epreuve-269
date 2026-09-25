import React from 'react';
import { ClipboardCheck, Inbox } from 'lucide-react';
import { Badge } from '../ui/Badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/Card';
import { EmptyState } from '../feedback/EmptyState';
import { ErrorState } from '../feedback/ErrorState';
import { LoadingRows } from '../feedback/LoadingRows';
import { RelectureItem } from './RelectureItem';
import type { QueryResult } from '../../hooks/useApiQuery';
import type { RelectureDto } from '../../types/domain';

interface RelecturesCardProps {
  query: QueryResult<RelectureDto[]>;
  etudiantId: string;
}

export function RelecturesCard({ query, etudiantId }: RelecturesCardProps) {
  const enCours = query.data?.filter((r) => r.modifiable) ?? [];
  const passees = query.data?.filter((r) => !r.modifiable) ?? [];
  const aRendre = enCours.filter((r) => r.statut === 'EN_ATTENTE').length;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-start justify-between gap-2">
          <div className="space-y-1.5">
            <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Étape 3</p>
            <CardTitle className="flex items-center gap-2">
              <ClipboardCheck className="size-4" aria-hidden="true" /> Relectures
            </CardTitle>
            <CardDescription>Exercices attribués au hasard. L’auteur ne connaîtra pas votre identité.</CardDescription>
          </div>
          {aRendre > 0 && <Badge>{aRendre} à rendre</Badge>}
        </div>
      </CardHeader>
      <CardContent className="space-y-5">
        {query.error ?
        <ErrorState error={query.error} onRetry={query.reload} /> :
        !query.data ?
        <LoadingRows rows={2} className="h-32" /> :
        query.data.length === 0 ?
        <EmptyState
          icon={Inbox}
          title="Aucune relecture attribuée"
          description="Une relecture vous sera attribuée dès que vous serez présent et qu’un camarade déposera son exercice." /> :


        <>
            {enCours.length > 0 &&
          <div className="space-y-2">
                <h3 className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Session en cours</h3>
                <ul className="space-y-3">
                  {enCours.map((r) =>
              <RelectureItem key={`${r.id}-ouverte`} relecture={r} etudiantId={etudiantId} />
              )}
                </ul>
              </div>
          }
            {passees.length > 0 &&
          <div className="space-y-2">
                <h3 className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Sessions clôturées</h3>
                <ul className="space-y-3">
                  {passees.map((r) =>
              <RelectureItem key={`${r.id}-close`} relecture={r} etudiantId={etudiantId} />
              )}
                </ul>
              </div>
          }
          </>
        }
      </CardContent>
    </Card>);

}
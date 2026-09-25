import React from 'react';
import { Award, FileQuestion, EyeOff } from 'lucide-react';
import { Badge } from '../ui/Badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/Card';
import { ExerciceStatusBadge } from '../badges/ExerciceStatusBadge';
import { EmptyState } from '../feedback/EmptyState';
import { ErrorState } from '../feedback/ErrorState';
import { LoadingRows } from '../feedback/LoadingRows';
import type { QueryResult } from '../../hooks/useApiQuery';
import type { ExerciceDto } from '../../types/domain';

export function ResultatsCard({ query }: {query: QueryResult<ExerciceDto[]>;}) {
  return (
    <Card>
      <CardHeader>
        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Étape 4</p>
        <CardTitle className="flex items-center gap-2">
          <Award className="size-4" aria-hidden="true" /> Mes résultats
        </CardTitle>
        <CardDescription>Note et commentaire reçus pour chacun de vos exercices.</CardDescription>
      </CardHeader>
      <CardContent>
        {query.error ?
        <ErrorState error={query.error} onRetry={query.reload} /> :
        !query.data ?
        <LoadingRows rows={2} className="h-24" /> :
        query.data.length === 0 ?
        <EmptyState icon={FileQuestion} title="Aucun exercice déposé" description="Vos résultats apparaîtront ici après relecture." /> :

        <ul className="space-y-3">
            {query.data.map((x) =>
          <li key={x.id} className="rounded-lg border border-border p-4">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 space-y-1.5">
                    <p className="text-sm font-medium">{x.sessionTitre}</p>
                    <div className="flex flex-wrap items-center gap-1.5">
                      <ExerciceStatusBadge statut={x.statut} />
                      {x.note !== null &&
                  <Badge variant="secondary">{x.resultatDefinitif ? 'Définitive' : 'Provisoire'}</Badge>
                  }
                    </div>
                  </div>
                  <p className="shrink-0 text-right font-mono">
                    <span className="text-2xl font-semibold">{x.note !== null ? x.note : '—'}</span>
                    <span className="text-sm text-muted-foreground">/20</span>
                  </p>
                </div>
                {x.commentaire ?
            <blockquote className="mt-3 border-l-2 border-border pl-3 text-sm text-muted-foreground">
                    {x.commentaire}
                  </blockquote> :

            <p className="mt-3 text-sm text-muted-foreground">
                    {x.resultatDefinitif ? 'Relecture non rendue.' : 'En attente de relecture.'}
                  </p>
            }
                {x.note !== null &&
            <p className="mt-2 flex items-center gap-1.5 text-xs text-muted-foreground">
                    <EyeOff className="size-3" aria-hidden="true" /> Relecteur anonyme
                    {!x.resultatDefinitif && ' · la note peut évoluer jusqu’à la clôture'}
                  </p>
            }
              </li>
          )}
          </ul>
        }
      </CardContent>
    </Card>);

}
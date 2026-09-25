import React from 'react';
import { CalendarClock, Users } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/Card';
import { cn } from '../../utils/cn';
import { SessionStatusBadge } from '../badges/SessionStatusBadge';
import { EmptyState } from '../feedback/EmptyState';
import { ErrorState } from '../feedback/ErrorState';
import { LoadingRows } from '../feedback/LoadingRows';
import { formatDateCourte } from '../../utils/format';
import type { QueryResult } from '../../hooks/useApiQuery';
import type { SessionDto } from '../../types/domain';

interface SessionListProps {
  query: QueryResult<SessionDto[]>;
  selectedId: string | null;
  onSelect: (id: string) => void;
}

export function SessionList({ query, selectedId, onSelect }: SessionListProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Sessions</CardTitle>
        <CardDescription>Historique de toutes les promotions.</CardDescription>
      </CardHeader>
      <CardContent>
        {query.loading && !query.data ?
        <LoadingRows rows={3} className="h-16" /> :
        query.error ?
        <ErrorState error={query.error} onRetry={query.reload} /> :
        !query.data || query.data.length === 0 ?
        <EmptyState icon={CalendarClock} title="Aucune session" description="Ouvrez votre première session ci-dessus." /> :

        <ul className="space-y-2">
            {query.data.map((s) => {
            const active = s.id === selectedId;
            return (
              <li key={s.id}>
                  <button
                  type="button"
                  onClick={() => onSelect(s.id)}
                  aria-current={active ? 'true' : undefined}
                  className={cn(
                    'w-full rounded-lg border px-3 py-2.5 text-left transition-colors focus-visible:outline-none focus-visible:ring-[3px] focus-visible:ring-ring',
                    active ? 'border-primary bg-secondary' : 'border-border hover:bg-muted'
                  )}>
                  
                    <div className="flex items-start justify-between gap-2">
                      <span className="line-clamp-1 text-sm font-medium">{s.titre}</span>
                      <SessionStatusBadge statut={s.statut} />
                    </div>
                    <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-muted-foreground">
                      <span>{s.promotionNom.replace('KFOKAM48 · ', '')}</span>
                      <span>{formatDateCourte(s.ouvertureAt)}</span>
                      <span className="inline-flex items-center gap-1">
                        <Users className="size-3" aria-hidden="true" />
                        {s.nbPresents}/{s.nbEtudiants}
                      </span>
                    </div>
                  </button>
                </li>);

          })}
          </ul>
        }
      </CardContent>
    </Card>);

}
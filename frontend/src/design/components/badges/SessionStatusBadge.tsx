import React from 'react';
import { Badge } from '../ui/Badge';
import type { SessionStatut } from '../../types/domain';

export function SessionStatusBadge({ statut }: {statut: SessionStatut;}) {
  if (statut === 'OUVERTE') {
    return (
      <Badge variant="outline" className="gap-1.5">
        <span className="size-1.5 rounded-full bg-emerald-500" aria-hidden="true" />
        Ouverte
      </Badge>);

  }
  return (
    <Badge variant="secondary" className="gap-1.5">
      <span className="size-1.5 rounded-full bg-muted-foreground" aria-hidden="true" />
      Clôturée
    </Badge>);

}
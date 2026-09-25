import React from 'react';
import { UserCheck, UserCog } from 'lucide-react';
import { Badge } from '../ui/Badge';
import type { PresenceSource } from '../../types/domain';

export function SourceBadge({ source }: {source: PresenceSource;}) {
  return source === 'ETUDIANT' ?
  <Badge variant="outline" className="gap-1">
      <UserCheck className="size-3" aria-hidden="true" /> Étudiant
    </Badge> :

  <Badge variant="secondary" className="gap-1">
      <UserCog className="size-3" aria-hidden="true" /> Formateur
    </Badge>;

}
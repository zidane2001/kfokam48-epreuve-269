import React from 'react';
import { Badge } from '../ui/Badge';
import type { ExerciceStatut } from '../../types/domain';

const config: Record<ExerciceStatut, {label: string;dot: string;variant: 'outline' | 'secondary' | 'default';}> = {
  EN_ATTENTE_RELECTEUR: { label: 'En attente de relecteur', dot: 'bg-amber-500', variant: 'outline' },
  RELECTEUR_ATTRIBUE: { label: 'Relecture en cours', dot: 'bg-sky-500', variant: 'outline' },
  RELU: { label: 'Relu', dot: 'bg-emerald-500', variant: 'outline' }
};

export function ExerciceStatusBadge({ statut }: {statut: ExerciceStatut;}) {
  const { label, dot, variant } = config[statut];
  return (
    <Badge variant={variant} className="gap-1.5">
      <span className={`size-1.5 rounded-full ${dot}`} aria-hidden="true" />
      {label}
    </Badge>);

}
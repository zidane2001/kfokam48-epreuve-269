import React from 'react';
import { ClipboardList, FileText, Sigma, Users } from 'lucide-react';
import { Card, CardContent } from '../ui/Card';
import { formatMoyenne } from '../../utils/format';
import type { SuiviDto } from '../../types/domain';

export function SuiviSummary({ totaux }: {totaux: SuiviDto['totaux'];}) {
  const items = [
  totaux.presents !== null ?
  { icon: Users, label: 'Présents', value: `${totaux.presents} / ${totaux.etudiants}` } :
  { icon: Users, label: 'Étudiants · sessions', value: `${totaux.etudiants} · ${totaux.sessionsComptees}` },
  { icon: FileText, label: 'Exercices déposés', value: String(totaux.exercicesDeposes) },
  { icon: ClipboardList, label: 'Relectures en attente', value: String(totaux.relecturesEnAttente), warn: totaux.relecturesEnAttente > 0 },
  { icon: Sigma, label: 'Moyenne de la promotion', value: formatMoyenne(totaux.moyennePromotion) }];

  return (
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
      {items.map(({ icon: Icon, label, value, warn }) =>
      <Card key={label} size="sm">
          <CardContent>
            <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
              <Icon className="size-3.5" aria-hidden="true" />
              {label}
            </p>
            <p className="mt-1 flex items-center gap-2 text-2xl font-semibold tabular-nums">
              {value}
              {warn && <span className="size-2 rounded-full bg-amber-500" aria-label="Attention" />}
            </p>
          </CardContent>
        </Card>
      )}
    </div>);

}
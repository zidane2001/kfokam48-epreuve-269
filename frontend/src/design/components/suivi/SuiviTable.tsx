import React from 'react';
import { Badge } from '../ui/Badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/Table';
import { ExerciceStatusBadge } from '../badges/ExerciceStatusBadge';
import { SourceBadge } from '../badges/SourceBadge';
import { formatMoyenne } from '../../utils/format';
import type { SuiviDto } from '../../types/domain';

export function SuiviTable({ suivi }: {suivi: SuiviDto;}) {
  const parSession = suivi.sessionId !== null;
  return (
    <div className="rounded-lg border border-border bg-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="min-w-44">Étudiant</TableHead>
            <TableHead>{parSession ? 'Présence' : 'Présences'}</TableHead>
            <TableHead>{parSession ? 'Exercice' : 'Exercices déposés'}</TableHead>
            <TableHead className="text-right">Moyenne</TableHead>
            <TableHead className="text-right">Relectures en attente</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {suivi.lignes.map((l) =>
          <TableRow key={l.etudiantId}>
              <TableCell className="font-medium">{l.nomComplet}</TableCell>
              <TableCell>
                {parSession ?
              l.presentSession && l.sourcePresence ?
              <SourceBadge source={l.sourcePresence} /> :

              <span className="text-sm text-muted-foreground">Absent·e</span> :


              <span className="tabular-nums">
                    {l.presences} <span className="text-muted-foreground">/ {l.sessionsComptees}</span>
                  </span>
              }
              </TableCell>
              <TableCell>
                {parSession ?
              l.statutExercice ?
              <ExerciceStatusBadge statut={l.statutExercice} /> :

              <span className="text-sm text-muted-foreground">Non déposé</span> :


              <span className="tabular-nums">{l.exercicesDeposes}</span>
              }
              </TableCell>
              <TableCell className="text-right">
                <span className="font-mono font-medium tabular-nums">{formatMoyenne(l.moyenne)}</span>
                {l.notesRecues > 0 &&
              <span className="ml-1 text-xs text-muted-foreground">
                    ({l.notesRecues} note{l.notesRecues > 1 ? 's' : ''})
                  </span>
              }
              </TableCell>
              <TableCell className="text-right">
                {l.relecturesEnAttente > 0 ?
              <Badge variant="outline" className="gap-1.5">
                    <span className="size-1.5 rounded-full bg-amber-500" aria-hidden="true" />
                    {l.relecturesEnAttente}
                  </Badge> :

              <span className="text-muted-foreground tabular-nums">0</span>
              }
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>);

}
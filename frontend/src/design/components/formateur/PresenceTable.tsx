import React from 'react';
import { UserX } from 'lucide-react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/Table';
import { SourceBadge } from '../badges/SourceBadge';
import { EmptyState } from '../feedback/EmptyState';
import { formatHeure } from '../../utils/format';
import type { PresenceDto } from '../../types/domain';

export function PresenceTable({ presences }: {presences: PresenceDto[];}) {
  if (presences.length === 0) {
    return (
      <EmptyState
        icon={UserX}
        title="Aucune présence pour l’instant"
        description="Les étudiants apparaîtront ici dès qu’ils saisiront le code." />);


  }
  return (
    <div className="rounded-lg border border-border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Étudiant</TableHead>
            <TableHead>Source</TableHead>
            <TableHead className="text-right">Heure</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {presences.map((p) =>
          <TableRow key={p.id}>
              <TableCell className="font-medium">{p.etudiantNom}</TableCell>
              <TableCell>
                <SourceBadge source={p.source} />
              </TableCell>
              <TableCell className="text-right font-mono text-xs text-muted-foreground">{formatHeure(p.enregistreeAt)}</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>);

}
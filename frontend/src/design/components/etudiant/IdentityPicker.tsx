import React, { useEffect, useState } from 'react';
import { ChevronRight, Users } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/Card';
import { Label } from '../ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/Select';
import { EmptyState } from '../feedback/EmptyState';
import { ErrorState } from '../feedback/ErrorState';
import { LoadingRows } from '../feedback/LoadingRows';
import { useApiQuery } from '../../hooks/useApiQuery';
import { api } from '../../utils/api';
import { initiales } from '../../utils/format';
import type { Identite } from '../../types/domain';

export function IdentityPicker({ onSelect }: {onSelect: (identite: Identite) => void;}) {
  const promotionsQ = useApiQuery(() => api.listerPromotions(), []);
  const [promotionId, setPromotionId] = useState('');
  const etudiantsQ = useApiQuery(() => api.listerEtudiants(promotionId), [promotionId], {
    enabled: Boolean(promotionId)
  });

  useEffect(() => {
    if (!promotionId && promotionsQ.data && promotionsQ.data.length > 0) setPromotionId(promotionsQ.data[0].id);
  }, [promotionsQ.data, promotionId]);

  return (
    <div className="mx-auto w-full max-w-lg">
      <Card>
        <CardHeader>
          <CardTitle className="text-lg">Qui êtes-vous ?</CardTitle>
          <CardDescription>Choisissez votre nom dans la liste de votre promotion. Aucun mot de passe n’est requis.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {promotionsQ.error ?
          <ErrorState error={promotionsQ.error} onRetry={promotionsQ.reload} /> :

          <div className="space-y-2">
              <Label htmlFor="identite-promotion">Promotion</Label>
              <Select value={promotionId} onValueChange={setPromotionId} disabled={!promotionsQ.data}>
                <SelectTrigger id="identite-promotion" className="w-full">
                  <SelectValue placeholder="Chargement…" />
                </SelectTrigger>
                <SelectContent>
                  {(promotionsQ.data ?? []).map((p) =>
                <SelectItem key={p.id} value={p.id}>
                      {p.nom}
                    </SelectItem>
                )}
                </SelectContent>
              </Select>
            </div>
          }

          <div className="space-y-2">
            <p className="text-sm font-medium" id="liste-etudiants">
              Étudiants
            </p>
            {etudiantsQ.error ?
            <ErrorState error={etudiantsQ.error} onRetry={etudiantsQ.reload} /> :
            !etudiantsQ.data ?
            <LoadingRows rows={5} className="h-12" /> :
            etudiantsQ.data.length === 0 ?
            <EmptyState icon={Users} title="Aucun étudiant dans cette promotion" /> :

            <ul aria-labelledby="liste-etudiants" className="divide-y divide-border overflow-hidden rounded-lg border border-border">
                {etudiantsQ.data.map((e) =>
              <li key={e.id}>
                    <button
                  type="button"
                  onClick={() => onSelect({ etudiantId: e.id, promotionId: e.promotionId })}
                  className="flex w-full items-center gap-3 px-3 py-2.5 text-left text-sm transition-colors hover:bg-muted focus-visible:bg-muted focus-visible:outline-none">
                  
                      <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-secondary text-xs font-semibold">
                        {initiales(e.prenom, e.nom)}
                      </span>
                      <span className="flex-1 font-medium">
                        {e.prenom} {e.nom}
                      </span>
                      <ChevronRight className="size-4 text-muted-foreground" aria-hidden="true" />
                    </button>
                  </li>
              )}
              </ul>
            }
          </div>
        </CardContent>
      </Card>
    </div>);

}
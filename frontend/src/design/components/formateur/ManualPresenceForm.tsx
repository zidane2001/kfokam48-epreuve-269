import React, { useState } from 'react';
import { UserPlus } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '../ui/Button';
import { Label } from '../ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/Select';
import { Spinner } from '../ui/Spinner';
import { api, toApiError } from '../../utils/api';
import type { EtudiantDto, PresenceDto } from '../../types/domain';

interface ManualPresenceFormProps {
  sessionId: string;
  etudiants: EtudiantDto[];
  presences: PresenceDto[];
  disabled: boolean;
}

export function ManualPresenceForm({ sessionId, etudiants, presences, disabled }: ManualPresenceFormProps) {
  const [etudiantId, setEtudiantId] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const presentIds = new Set(presences.map((p) => p.etudiantId));
  const absents = etudiants.filter((e) => !presentIds.has(e.id));

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    try {
      const presence = await api.ajouterPresenceManuelle(sessionId, { etudiantId });
      toast.success(`Présence ajoutée pour ${presence.etudiantNom}`);
      setEtudiantId('');
    } catch (err) {
      toast.error(toApiError(err).message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-2 sm:flex-row sm:items-end">
      <div className="flex-1 space-y-2">
        <Label htmlFor="presence-manuelle">Ajouter une présence manuellement</Label>
        <Select value={etudiantId} onValueChange={setEtudiantId} disabled={disabled || absents.length === 0}>
          <SelectTrigger id="presence-manuelle" className="w-full">
            <SelectValue placeholder={absents.length === 0 ? 'Tous les étudiants sont présents' : 'Choisir un étudiant absent'} />
          </SelectTrigger>
          <SelectContent>
            {absents.map((e) =>
            <SelectItem key={e.id} value={e.id}>
                {e.prenom} {e.nom}
              </SelectItem>
            )}
          </SelectContent>
        </Select>
      </div>
      <Button type="submit" variant="outline" disabled={disabled || submitting || !etudiantId}>
        {submitting ? <Spinner /> : <UserPlus />}
        Ajouter
      </Button>
    </form>);

}
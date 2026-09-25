import React, { useState } from 'react';
import { AlertCircle, Info, Plus } from 'lucide-react';
import { toast } from 'sonner';
import { Alert, AlertDescription } from '../ui/Alert';
import { Button } from '../ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/Card';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/Select';
import { Spinner } from '../ui/Spinner';
import { api, toApiError, type ApiError } from '../../utils/api';
import type { PromotionDto, SessionDto } from '../../types/domain';

interface OpenSessionFormProps {
  promotions: PromotionDto[] | null;
  promotionsLoading: boolean;
  sessions: SessionDto[] | null;
  onCreated: (session: SessionDto) => void;
  onSelectSession: (id: string) => void;
}

export function OpenSessionForm({
  promotions,
  promotionsLoading,
  sessions,
  onCreated,
  onSelectSession
}: OpenSessionFormProps) {
  const [titre, setTitre] = useState('');
  const [promotionId, setPromotionId] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  // Information issue de l'API (statut des sessions) — le serveur reste seul juge (409).
  const sessionEnCours = sessions?.find((s) => s.promotionId === promotionId && s.statut === 'OUVERTE') ?? null;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const session = await api.ouvrirSession({ titre, promotionId });
      toast.success(`Session ouverte · code ${session.code}`);
      setTitre('');
      onCreated(session);
    } catch (err) {
      setError(toApiError(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Ouvrir une session</CardTitle>
        <CardDescription>Un code de présence valable 15 minutes est généré à l’ouverture.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div className="space-y-2">
            <Label htmlFor="session-titre">Titre de la session</Label>
            <Input
              id="session-titre"
              value={titre}
              onChange={(e) => setTitre(e.target.value)}
              placeholder="Ex. Tests d’intégration avec Spring"
              aria-invalid={error?.code === 'VALIDATION' && !titre.trim() ? true : undefined} />
            
          </div>
          <div className="space-y-2">
            <Label htmlFor="session-promotion">Promotion</Label>
            <Select value={promotionId} onValueChange={setPromotionId} disabled={promotionsLoading}>
              <SelectTrigger id="session-promotion" className="w-full">
                <SelectValue placeholder={promotionsLoading ? 'Chargement…' : 'Choisir une promotion'} />
              </SelectTrigger>
              <SelectContent>
                {(promotions ?? []).map((p) =>
                <SelectItem key={p.id} value={p.id}>
                    {p.nom}
                  </SelectItem>
                )}
              </SelectContent>
            </Select>
          </div>
          {sessionEnCours &&
          <Alert>
              <Info />
              <AlertDescription>
                <p>
                  « {sessionEnCours.titre} » est encore ouverte pour cette promotion. Clôturez-la avant d’en ouvrir une
                  nouvelle.
                </p>
                <Button
                type="button"
                variant="outline"
                size="sm"
                className="mt-2"
                onClick={() => onSelectSession(sessionEnCours.id)}>
                
                  Aller à la session en cours
                </Button>
              </AlertDescription>
            </Alert>
          }
          {error &&
          <Alert variant="destructive">
              <AlertCircle />
              <AlertDescription>{error.message}</AlertDescription>
            </Alert>
          }
          <Button type="submit" className="w-full" size="lg" disabled={submitting}>
            {submitting ? <Spinner /> : <Plus />}
            Ouvrir la session
          </Button>
        </form>
      </CardContent>
    </Card>);

}
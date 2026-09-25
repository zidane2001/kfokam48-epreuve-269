import React, { useState } from 'react';
import { AlertCircle, ExternalLink, FileUp, Info, Pencil } from 'lucide-react';
import { toast } from 'sonner';
import { Alert, AlertDescription } from '../ui/Alert';
import { Button } from '../ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/Card';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';
import { Spinner } from '../ui/Spinner';
import { ExerciceStatusBadge } from '../badges/ExerciceStatusBadge';
import { api, toApiError, type ApiError } from '../../utils/api';
import { formatHeure } from '../../utils/format';
import type { ExerciceDto, SessionDto } from '../../types/domain';

interface ExerciceCardProps {
  session: SessionDto;
  etudiantId: string;
  exercice: ExerciceDto | null;
}

export function ExerciceCard({ session, etudiantId, exercice }: ExerciceCardProps) {
  const [lien, setLien] = useState('');
  const [editing, setEditing] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      if (exercice) {
        await api.modifierLien(exercice.id, { etudiantId, lien });
        toast.success('Lien mis à jour');
        setEditing(false);
      } else {
        await api.deposerExercice(session.id, { etudiantId, lien });
        toast.success('Exercice déposé');
      }
      setLien('');
    } catch (err) {
      setError(toApiError(err));
    } finally {
      setSubmitting(false);
    }
  }

  const showForm = !exercice || editing;

  return (
    <Card>
      <CardHeader>
        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Étape 2</p>
        <CardTitle className="flex items-center gap-2">
          <FileUp className="size-4" aria-hidden="true" /> Mon exercice
        </CardTitle>
        <CardDescription>
          Un seul dépôt par session, possible jusqu’à la clôture — même après l’expiration du code.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {exercice &&
        <div className="space-y-3 rounded-lg border border-border p-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <ExerciceStatusBadge statut={exercice.statut} />
              <span className="text-xs text-muted-foreground">Déposé à {formatHeure(exercice.deposeAt)}</span>
            </div>
            <a
            href={exercice.lien}
            target="_blank"
            rel="noreferrer"
            className="flex items-center gap-1.5 break-all text-sm font-medium underline-offset-4 hover:underline">
            
              <ExternalLink className="size-3.5 shrink-0" aria-hidden="true" />
              {exercice.lien}
            </a>
            {exercice.statut === 'EN_ATTENTE_RELECTEUR' &&
          <p className="text-xs text-muted-foreground">
                Aucun autre étudiant présent pour l’instant : un relecteur sera attribué dès que possible.
              </p>
          }
            {exercice.lienModifiable ?
          !editing &&
          <Button
            variant="outline"
            size="sm"
            onClick={() => {
              setLien(exercice.lien);
              setEditing(true);
              setError(null);
            }}>
            
                  <Pencil /> Modifier le lien
                </Button> :


          <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
                <Info className="size-3.5" aria-hidden="true" />
                Un relecteur a été attribué : le lien n’est plus modifiable.
              </p>
          }
          </div>
        }

        {showForm &&
        <form onSubmit={handleSubmit} className="space-y-3" noValidate>
            <div className="space-y-2">
              <Label htmlFor="lien-exercice">{exercice ? 'Nouveau lien' : 'Lien de l’exercice'}</Label>
              <Input
              id="lien-exercice"
              type="url"
              inputMode="url"
              value={lien}
              onChange={(e) => setLien(e.target.value)}
              placeholder="https://github.com/votre-compte/exercice"
              aria-invalid={error ? true : undefined}
              className="h-10" />
            
            </div>
            {error &&
          <Alert variant="destructive">
                <AlertCircle />
                <AlertDescription>{error.message}</AlertDescription>
              </Alert>
          }
            <div className="flex flex-wrap gap-2">
              <Button type="submit" size="lg" disabled={submitting}>
                {submitting && <Spinner />}
                {exercice ? 'Enregistrer le lien' : 'Déposer'}
              </Button>
              {editing &&
            <Button type="button" variant="ghost" size="lg" onClick={() => {setEditing(false);setError(null);}}>
                  Annuler
                </Button>
            }
            </div>
          </form>
        }
      </CardContent>
    </Card>);

}
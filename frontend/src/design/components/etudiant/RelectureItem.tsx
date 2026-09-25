import React, { useState } from 'react';
import { AlertCircle, ExternalLink, Lock } from 'lucide-react';
import { toast } from 'sonner';
import { Alert, AlertDescription } from '../ui/Alert';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';
import { Spinner } from '../ui/Spinner';
import { Textarea } from '../ui/Textarea';
import { api, toApiError, type ApiError } from '../../utils/api';
import type { RelectureDto } from '../../types/domain';

interface RelectureItemProps {
  relecture: RelectureDto;
  etudiantId: string;
}

export function RelectureItem({ relecture, etudiantId }: RelectureItemProps) {
  const [note, setNote] = useState(relecture.note !== null ? String(relecture.note) : '');
  const [commentaire, setCommentaire] = useState(relecture.commentaire ?? '');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const rendue = relecture.statut === 'RENDUE';
  const idBase = `relecture-${relecture.id}`;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await api.soumettreRelecture(relecture.id, {
        relecteurId: etudiantId,
        note: note.trim() === '' ? null : Number(note),
        commentaire
      });
      toast.success(rendue ? 'Relecture mise à jour' : 'Relecture envoyée');
    } catch (err) {
      setError(toApiError(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <li className="space-y-3 rounded-lg border border-border p-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="min-w-0 space-y-1">
          <p className="text-sm font-medium">{relecture.sessionTitre}</p>
          <a
            href={relecture.lien}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-1.5 break-all text-xs text-muted-foreground underline-offset-4 hover:text-foreground hover:underline">
            
            <ExternalLink className="size-3 shrink-0" aria-hidden="true" />
            Ouvrir l’exercice à relire
          </a>
        </div>
        {rendue ?
        <Badge variant="outline" className="gap-1.5">
            <span className="size-1.5 rounded-full bg-emerald-500" aria-hidden="true" /> Rendue
          </Badge> :

        <Badge variant="outline" className="gap-1.5">
            <span className="size-1.5 rounded-full bg-amber-500" aria-hidden="true" /> À rendre
          </Badge>
        }
      </div>

      {relecture.modifiable ?
      <form onSubmit={handleSubmit} className="space-y-3" noValidate>
          <div className="grid gap-3 sm:grid-cols-[120px_1fr]">
            <div className="space-y-2">
              <Label htmlFor={`${idBase}-note`}>Note / 20</Label>
              <Input
              id={`${idBase}-note`}
              type="number"
              inputMode="numeric"
              min={0}
              max={20}
              step={1}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              aria-invalid={error?.code === 'NOTE_INVALIDE' ? true : undefined}
              className="h-10 font-mono" />
            
            </div>
            <div className="space-y-2">
              <Label htmlFor={`${idBase}-commentaire`}>Commentaire</Label>
              <Textarea
              id={`${idBase}-commentaire`}
              value={commentaire}
              onChange={(e) => setCommentaire(e.target.value)}
              placeholder="Points forts, points à améliorer…"
              rows={3}
              aria-invalid={error?.code === 'COMMENTAIRE_OBLIGATOIRE' ? true : undefined} />
            
            </div>
          </div>
          {error &&
        <Alert variant="destructive">
              <AlertCircle />
              <AlertDescription>{error.message}</AlertDescription>
            </Alert>
        }
          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="text-xs text-muted-foreground">Modifiable jusqu’à la clôture de la session.</p>
            <Button type="submit" disabled={submitting}>
              {submitting && <Spinner />}
              {rendue ? 'Mettre à jour' : 'Envoyer la relecture'}
            </Button>
          </div>
        </form> :

      <div className="space-y-2 rounded-md bg-muted p-3 text-sm">
          <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <Lock className="size-3" aria-hidden="true" />
            {rendue ? 'Définitive — session clôturée' : 'Non rendue avant la clôture de la session'}
          </p>
          {rendue &&
        <>
              <p className="font-mono text-base font-semibold">{relecture.note}/20</p>
              <p className="text-muted-foreground">{relecture.commentaire}</p>
            </>
        }
        </div>
      }
    </li>);

}
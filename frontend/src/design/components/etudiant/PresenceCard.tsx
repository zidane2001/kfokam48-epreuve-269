import React, { useEffect, useState } from 'react';
import { AlertCircle, CheckCircle2, KeyRound, ShieldAlert, TimerOff } from 'lucide-react';
import { toast } from 'sonner';
import { Alert, AlertDescription, AlertTitle } from '../ui/Alert';
import { Button } from '../ui/Button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/Card';
import { Input } from '../ui/Input';
import { Label } from '../ui/Label';
import { Spinner } from '../ui/Spinner';
import { ErrorState } from '../feedback/ErrorState';
import { LoadingRows } from '../feedback/LoadingRows';
import type { QueryResult } from '../../hooks/useApiQuery';
import { useNow } from '../../hooks/useNow';
import { api, toApiError, type ApiError } from '../../utils/api';
import { formatCompteARebours, formatHeure } from '../../utils/format';
import type { SessionDto, StatutPresenceDto } from '../../types/domain';

interface PresenceCardProps {
  session: SessionDto;
  etudiantId: string;
  statutQ: QueryResult<StatutPresenceDto>;
}

export function PresenceCard({ session, etudiantId, statutQ }: PresenceCardProps) {
  const now = useNow(1000);
  const [code, setCode] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  const statut = statutQ.data;
  const bloqueJusqua = statut?.bloqueJusqua ? new Date(statut.bloqueJusqua).getTime() : null;
  const bloque = bloqueJusqua !== null && bloqueJusqua > now;
  const codeExpireAffiche = new Date(session.expirationAt).getTime() <= now;

  // Fin du blocage : on redemande le statut à l'API.
  useEffect(() => {
    if (bloqueJusqua !== null && !bloque) {
      setError(null);
      statutQ.reload();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bloque]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await api.marquerPresence(session.id, { etudiantId, code });
      toast.success('Présence enregistrée');
      setCode('');
    } catch (err) {
      setError(toApiError(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Card>
      <CardHeader>
        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Étape 1</p>
        <CardTitle className="flex items-center gap-2">
          <KeyRound className="size-4" aria-hidden="true" /> Présence
        </CardTitle>
        <CardDescription>Saisissez le code affiché par votre formateur.</CardDescription>
      </CardHeader>
      <CardContent>
        {statutQ.error ?
        <ErrorState error={statutQ.error} onRetry={statutQ.reload} /> :
        !statut ?
        <LoadingRows rows={2} className="h-10" /> :
        statut.present ?
        <div className="flex items-start gap-3 rounded-lg border border-border bg-muted p-4">
            <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-emerald-600" aria-hidden="true" />
            <div>
              <p className="text-sm font-medium">Présence enregistrée</p>
              <p className="text-sm text-muted-foreground">
                {statut.enregistreeAt && `À ${formatHeure(statut.enregistreeAt)} · `}
                {statut.source === 'FORMATEUR' ? 'ajoutée par le formateur' : 'saisie par vous'}
              </p>
            </div>
          </div> :

        <form onSubmit={handleSubmit} className="space-y-3" noValidate>
            <div className="space-y-2">
              <Label htmlFor="code-presence">Code de présence</Label>
              <div className="flex gap-2">
                <Input
                id="code-presence"
                value={code}
                onChange={(e) => setCode(e.target.value.toUpperCase())}
                placeholder="Ex. K7Q4MX"
                autoComplete="off"
                autoCapitalize="characters"
                spellCheck={false}
                disabled={bloque || submitting}
                aria-invalid={error ? true : undefined}
                aria-describedby="code-aide"
                className="h-11 font-mono text-lg tracking-[0.3em] uppercase" />
              
                <Button type="submit" size="lg" className="h-11" disabled={bloque || submitting || !code.trim()}>
                  {submitting && <Spinner />}
                  Valider
                </Button>
              </div>
              <p id="code-aide" className="text-xs text-muted-foreground">
                {codeExpireAffiche ?
              `Code expiré à ${formatHeure(session.expirationAt)}.` :
              `Code valable jusqu’à ${formatHeure(session.expirationAt)}.`}{' '}
                {!bloque && `${statut.tentativesRestantes} tentative${statut.tentativesRestantes > 1 ? 's' : ''} restante${statut.tentativesRestantes > 1 ? 's' : ''}.`}
              </p>
            </div>

            {bloque && bloqueJusqua !== null ?
          <Alert variant="destructive">
                <ShieldAlert />
                <AlertTitle>Saisie temporairement bloquée</AlertTitle>
                <AlertDescription>
                  Trop de codes incorrects. Nouvelle tentative possible dans{' '}
                  <span className="font-mono font-medium tabular-nums">{formatCompteARebours(bloqueJusqua - now)}</span>.
                </AlertDescription>
              </Alert> :
          error ?
          <Alert variant="destructive">
                {error.code === 'CODE_EXPIRE' ? <TimerOff /> : <AlertCircle />}
                <AlertDescription>{error.message}</AlertDescription>
              </Alert> :
          codeExpireAffiche ?
          <Alert>
                <TimerOff />
                <AlertDescription>
                  Le code a expiré. Demandez au formateur d’ajouter votre présence. Vous pouvez toujours déposer votre exercice.
                </AlertDescription>
              </Alert> :
          null}
          </form>
        }
      </CardContent>
    </Card>);

}
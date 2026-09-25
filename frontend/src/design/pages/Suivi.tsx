import React, { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Presentation, Users } from 'lucide-react';
import { ButtonLink } from '../components/ButtonLink';
import { PageHeader } from '../components/PageHeader';
import { SessionStatusBadge } from '../components/badges/SessionStatusBadge';
import { Label } from '../components/ui/Label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/Select';
import { Skeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/feedback/EmptyState';
import { ErrorState } from '../components/feedback/ErrorState';
import { LoadingRows } from '../components/feedback/LoadingRows';
import { SuiviSummary } from '../components/suivi/SuiviSummary';
import { SuiviTable } from '../components/suivi/SuiviTable';
import { useApiQuery } from '../hooks/useApiQuery';
import { api } from '../utils/api';
import { formatDateCourte } from '../utils/format';

const TOUTES = 'toutes';

export function Suivi() {
  const promotionsQ = useApiQuery(() => api.listerPromotions(), []);
  const [params, setParams] = useSearchParams();
  const promotionId = params.get('promotion') ?? '';
  const sessionId = params.get('session') ?? TOUTES;

  const setFiltres = (promotion: string, session: string) => {
    const next: Record<string, string> = { promotion };
    if (session !== TOUTES) next.session = session;
    setParams(next, { replace: true });
  };

  useEffect(() => {
    if (!promotionId && promotionsQ.data && promotionsQ.data.length > 0) setFiltres(promotionsQ.data[0].id, TOUTES);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [promotionsQ.data, promotionId]);

  const setSessionId = (id: string) => setFiltres(promotionId, id);

  const sessionsQ = useApiQuery(() => api.listerSessions(promotionId), [promotionId], { enabled: Boolean(promotionId) });
  const suiviQ = useApiQuery(
    () => api.obtenirSuivi(promotionId, sessionId === TOUTES ? undefined : sessionId),
    [promotionId, sessionId],
    { enabled: Boolean(promotionId) }
  );

  function changerPromotion(id: string) {
    setFiltres(id, TOUTES);
  }

  const sessionChoisie = sessionsQ.data?.find((s) => s.id === sessionId) ?? null;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Tableau de suivi"
        description="Présence, exercices déposés, moyenne et relectures en attente pour chaque étudiant. Les moyennes sont calculées par le serveur." />
      

      {promotionsQ.error ?
      <ErrorState error={promotionsQ.error} onRetry={promotionsQ.reload} /> :

      <div className="grid gap-3 sm:grid-cols-2 lg:max-w-2xl">
          <div className="space-y-2">
            <Label htmlFor="suivi-promotion">Promotion</Label>
            <Select value={promotionId} onValueChange={changerPromotion} disabled={!promotionsQ.data}>
              <SelectTrigger id="suivi-promotion" className="w-full bg-background">
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
          <div className="space-y-2">
            <Label htmlFor="suivi-session">Périmètre</Label>
            <Select value={sessionId} onValueChange={setSessionId} disabled={!sessionsQ.data}>
              <SelectTrigger id="suivi-session" className="w-full bg-background">
                <SelectValue placeholder="Toutes les sessions" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={TOUTES}>Toutes les sessions</SelectItem>
                {(sessionsQ.data ?? []).map((s) =>
              <SelectItem key={s.id} value={s.id}>
                    {s.titre} · {formatDateCourte(s.ouvertureAt)}
                  </SelectItem>
              )}
              </SelectContent>
            </Select>
          </div>
        </div>
      }

      {suiviQ.error ?
      <ErrorState error={suiviQ.error} onRetry={suiviQ.reload} /> :
      !suiviQ.data ?
      <div className="space-y-6" role="status" aria-label="Chargement du tableau">
          <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
            {Array.from({ length: 4 }, (_, i) =>
          <Skeleton key={i} className="h-20 rounded-xl" />
          )}
          </div>
          <LoadingRows rows={6} className="h-11" />
        </div> :
      suiviQ.data.lignes.length === 0 ?
      <EmptyState icon={Users} title="Aucun étudiant dans cette promotion" /> :

      <div className="space-y-6">
          {sessionChoisie &&
        <div className="flex flex-col gap-2 rounded-xl border border-border bg-background p-4 sm:flex-row sm:items-center sm:justify-between">
              <div className="flex flex-wrap items-center gap-2">
                <span className="font-medium">{sessionChoisie.titre}</span>
                <SessionStatusBadge statut={sessionChoisie.statut} />
                <span className="text-sm text-muted-foreground">{formatDateCourte(sessionChoisie.ouvertureAt)}</span>
              </div>
              <ButtonLink to={`/formateur?session=${sessionChoisie.id}`} size="sm">
                <Presentation /> Gérer la session
              </ButtonLink>
            </div>
        }
          <SuiviSummary totaux={suiviQ.data.totaux} />
          <SuiviTable suivi={suiviQ.data} />
          <p className="text-xs text-muted-foreground">
            « — » indique qu’aucune note n’a encore été reçue (0 étant une note valide). Les relectures non rendues avant
            la clôture restent comptées comme en attente.
          </p>
        </div>
      }
    </div>);

}
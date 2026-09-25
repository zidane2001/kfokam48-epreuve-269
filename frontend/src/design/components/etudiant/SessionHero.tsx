import React from 'react';
import { Award, CalendarOff, Check, ClipboardCheck, FileUp, KeyRound, LogOut, Timer, TimerOff } from 'lucide-react';
import { Button } from '../ui/Button';
import { Card, CardContent } from '../ui/Card';
import { Skeleton } from '../ui/Skeleton';
import { SessionStatusBadge } from '../badges/SessionStatusBadge';
import { useNow } from '../../hooks/useNow';
import { cn } from '../../utils/cn';
import { formatCompteARebours, formatHeure, initiales } from '../../utils/format';
import type { EtudiantDto, ExerciceDto, RelectureDto, SessionDto, StatutPresenceDto } from '../../types/domain';

interface SessionHeroProps {
  etudiant: EtudiantDto;
  promotionNom: string | null;
  session: SessionDto | null;
  sessionsLoading: boolean;
  statut: StatutPresenceDto | null;
  exercice: ExerciceDto | null;
  relectures: RelectureDto[] | null;
  onChangeIdentity: () => void;
}

interface Step {
  id: string;
  label: string;
  detail: string;
  icon: typeof KeyRound;
  done: boolean;
  warn?: boolean;
}

export function SessionHero({
  etudiant,
  promotionNom,
  session,
  sessionsLoading,
  statut,
  exercice,
  relectures,
  onChangeIdentity
}: SessionHeroProps) {
  const now = useNow(1000);

  return (
    <Card>
      <CardContent className="space-y-5">
        <div className="flex items-center gap-3">
          <span className="flex size-11 shrink-0 items-center justify-center rounded-full bg-primary text-sm font-semibold text-primary-foreground">
            {initiales(etudiant.prenom, etudiant.nom)}
          </span>
          <div className="min-w-0 flex-1">
            <h1 className="truncate text-lg font-semibold tracking-tight">
              {etudiant.prenom} {etudiant.nom}
            </h1>
            <p className="truncate text-sm text-muted-foreground">{promotionNom ?? '…'}</p>
          </div>
          <Button variant="ghost" size="sm" onClick={onChangeIdentity}>
            <LogOut /> <span className="hidden sm:inline">Changer d’identité</span>
            <span className="sm:hidden">Changer</span>
          </Button>
        </div>

        <div className="border-t border-border pt-5">
          {sessionsLoading ?
          <div className="space-y-3">
              <Skeleton className="h-6 w-2/3" />
              <Skeleton className="h-16 w-full" />
            </div> :
          !session ?
          <div className="flex items-start gap-3">
              <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground">
                <CalendarOff className="size-4" aria-hidden="true" />
              </span>
              <div>
                <p className="text-sm font-medium">Aucune session ouverte</p>
                <p className="text-sm text-muted-foreground">
                  La présence et le dépôt seront disponibles dès que votre formateur ouvrira une session. Vos résultats
                  restent consultables ci-dessous.
                </p>
              </div>
            </div> :

          <SessionProgress session={session} statut={statut} exercice={exercice} relectures={relectures} now={now} />
          }
        </div>
      </CardContent>
    </Card>);

}

function SessionProgress({
  session,
  statut,
  exercice,
  relectures,
  now






}: {session: SessionDto;statut: StatutPresenceDto | null;exercice: ExerciceDto | null;relectures: RelectureDto[] | null;now: number;}) {
  const remaining = new Date(session.expirationAt).getTime() - now;
  const bloque = Boolean(statut?.bloqueJusqua && new Date(statut.bloqueJusqua).getTime() > now);
  const relecturesSession = (relectures ?? []).filter((r) => r.sessionId === session.id);
  const aRendre = relecturesSession.filter((r) => r.statut === 'EN_ATTENTE').length;

  const steps: Step[] = [
  {
    id: 'presence',
    label: 'Présence',
    icon: KeyRound,
    done: Boolean(statut?.present),
    warn: bloque,
    detail: statut?.present ?
    `Enregistrée${statut.enregistreeAt ? ` à ${formatHeure(statut.enregistreeAt)}` : ''}` :
    bloque ?
    'Saisie bloquée' :
    'Code à saisir'
  },
  {
    id: 'depot',
    label: 'Dépôt',
    icon: FileUp,
    done: Boolean(exercice),
    detail: exercice ? `Déposé à ${formatHeure(exercice.deposeAt)}` : 'Lien à déposer'
  },
  {
    id: 'relectures',
    label: 'Relecture',
    icon: ClipboardCheck,
    done: relecturesSession.length > 0 && aRendre === 0,
    detail:
    relecturesSession.length === 0 ? 'Aucune attribuée' : aRendre > 0 ? `${aRendre} à rendre` : 'Rendue'
  },
  {
    id: 'resultats',
    label: 'Résultat',
    icon: Award,
    done: exercice?.note !== null && exercice?.note !== undefined,
    detail:
    exercice && exercice.note !== null ? `${exercice.note}/20` : exercice ? 'En attente' : 'Après le dépôt'
  }];

  const current = steps.findIndex((s) => !s.done);

  function goTo(id: string) {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Session en cours</p>
          <p className="text-base font-semibold">{session.titre}</p>
        </div>
        <div className="flex flex-wrap items-center gap-2 text-sm">
          {remaining > 0 ?
          <span className="inline-flex items-center gap-1.5 rounded-md bg-muted px-2 py-1">
              <Timer className="size-3.5 text-muted-foreground" aria-hidden="true" />
              Code valable encore{' '}
              <span className="font-mono font-medium tabular-nums">{formatCompteARebours(remaining)}</span>
            </span> :

          <span className="inline-flex items-center gap-1.5 rounded-md bg-muted px-2 py-1 text-muted-foreground">
              <TimerOff className="size-3.5" aria-hidden="true" />
              Code expiré · dépôt ouvert jusqu’à la clôture
            </span>
          }
          <SessionStatusBadge statut={session.statut} />
        </div>
      </div>

      <ol className="grid grid-cols-2 gap-2 sm:grid-cols-4" aria-label="Progression dans la session">
        {steps.map((step, i) => {
          const Icon = step.icon;
          const isCurrent = i === current;
          return (
            <li key={step.id}>
              <button
                type="button"
                onClick={() => goTo(step.id)}
                aria-current={isCurrent ? 'step' : undefined}
                className={cn(
                  'flex h-full w-full items-start gap-2.5 rounded-lg border p-3 text-left transition-colors hover:bg-muted focus-visible:outline-none focus-visible:ring-[3px] focus-visible:ring-ring',
                  isCurrent ? 'border-primary' : 'border-border',
                  step.warn && 'border-amber-500'
                )}>
                
                <span
                  className={cn(
                    'flex size-7 shrink-0 items-center justify-center rounded-full text-xs font-semibold',
                    step.done ?
                    'bg-emerald-600 text-white' :
                    isCurrent ?
                    'bg-primary text-primary-foreground' :
                    'bg-muted text-muted-foreground'
                  )}
                  aria-hidden="true">
                  
                  {step.done ? <Check className="size-4" /> : <Icon className="size-3.5" />}
                </span>
                <span className="min-w-0">
                  <span className="block text-sm font-medium">{step.label}</span>
                  <span className={cn('block truncate text-xs', step.warn ? 'text-amber-700' : 'text-muted-foreground')}>
                    {step.detail}
                  </span>
                </span>
                <span className="sr-only">{step.done ? '(terminé)' : isCurrent ? '(étape en cours)' : '(à venir)'}</span>
              </button>
            </li>);

        })}
      </ol>
    </div>);

}
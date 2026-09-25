import React, { useState } from 'react';
import { AnimatePresence } from 'framer-motion';
import { Copy, Lock, Maximize2, Timer, TimerOff } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '../ui/Button';
import { ProjectedCode } from './ProjectedCode';
import { useNow } from '../../hooks/useNow';
import { cn } from '../../utils/cn';
import { formatCompteARebours, formatHeure } from '../../utils/format';
import type { SessionDto } from '../../types/domain';

export function AttendanceCode({ session }: {session: SessionDto;}) {
  const now = useNow(1000);
  const [projected, setProjected] = useState(false);
  const expiration = new Date(session.expirationAt).getTime();
  const total = expiration - new Date(session.ouvertureAt).getTime();
  const remaining = expiration - now;
  const closed = session.statut === 'CLOTUREE';
  const expired = remaining <= 0;
  const inactive = closed || expired;
  const pct = inactive ? 0 : Math.min(100, Math.max(0, remaining / total * 100));

  async function copy() {
    try {
      await navigator.clipboard.writeText(session.code);
      toast.success('Code copié');
    } catch {
      toast.error('Copie impossible — lisez le code à voix haute.');
    }
  }

  return (
    <section aria-labelledby="code-title" className="rounded-xl bg-neutral-950 p-4 text-white sm:p-5">
      <div className="flex items-center justify-between gap-2">
        <h3 id="code-title" className="text-sm font-medium text-neutral-400">
          Code de présence
        </h3>
        {!inactive &&
        <div className="flex gap-1">
            <Button variant="secondary" size="sm" onClick={copy}>
              <Copy /> <span className="hidden sm:inline">Copier</span>
            </Button>
            <Button variant="secondary" size="sm" onClick={() => setProjected(true)}>
              <Maximize2 /> Projeter
            </Button>
          </div>
        }
      </div>
      <div
        className={cn('mt-4 flex gap-1.5 sm:gap-2', inactive && 'opacity-30')}
        aria-label={`Code ${session.code.split('').join(' ')}`}>
        
        {session.code.split('').map((c, i) =>
        <span
          key={i}
          className="flex h-14 flex-1 items-center justify-center rounded-lg bg-white/10 font-mono text-3xl font-semibold sm:h-20 sm:text-5xl"
          aria-hidden="true">
          
            {c}
          </span>
        )}
      </div>
      <div className="mt-4 space-y-2">
        <div className="h-1 w-full overflow-hidden rounded-full bg-white/10" aria-hidden="true">
          <div
            className={cn(
              'h-full rounded-full transition-[width] duration-1000 ease-linear',
              pct < 20 ? 'bg-amber-400' : 'bg-emerald-400'
            )}
            style={{ width: `${pct}%` }} />
          
        </div>
        <p className="flex items-center gap-1.5 text-sm" role="timer" aria-live="off">
          {closed ?
          <>
              <Lock className="size-4 text-neutral-400" aria-hidden="true" />
              <span className="text-neutral-400">Session clôturée — le code n’est plus utilisable.</span>
            </> :
          expired ?
          <>
              <TimerOff className="size-4 text-amber-400" aria-hidden="true" />
              <span>
                Code expiré à {formatHeure(session.expirationAt)}.{' '}
                <span className="text-neutral-400">Ajoutez les retardataires manuellement ; les dépôts restent ouverts.</span>
              </span>
            </> :

          <>
              <Timer className="size-4 text-neutral-400" aria-hidden="true" />
              <span>
                Expire dans <span className="font-mono font-medium tabular-nums">{formatCompteARebours(remaining)}</span>
                <span className="text-neutral-400"> · à {formatHeure(session.expirationAt)}</span>
              </span>
            </>
          }
        </p>
      </div>
      <AnimatePresence>
        {projected && <ProjectedCode session={session} remaining={remaining} onClose={() => setProjected(false)} />}
      </AnimatePresence>
    </section>);

}
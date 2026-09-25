import React, { useEffect } from 'react';
import { motion } from 'framer-motion';
import { Timer, TimerOff, X } from 'lucide-react';
import { formatCompteARebours, formatHeure } from '../../utils/format';
import type { SessionDto } from '../../types/domain';

interface ProjectedCodeProps {
  session: SessionDto;
  remaining: number;
  onClose: () => void;
}

/** Affichage plein écran du code, pensé pour être projeté en salle. */
export function ProjectedCode({ session, remaining, onClose }: ProjectedCodeProps) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  const expired = remaining <= 0;

  return (
    <motion.div
      role="dialog"
      aria-modal="true"
      aria-label="Code de présence projeté"
      className="fixed inset-0 z-50 flex flex-col bg-neutral-950 p-6 text-white sm:p-10"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.18 }}>
      
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-neutral-400">{session.promotionNom}</p>
          <p className="text-xl font-semibold sm:text-2xl">{session.titre}</p>
        </div>
        <button
          type="button"
          onClick={onClose}
          autoFocus
          className="flex size-10 items-center justify-center rounded-full bg-white/10 transition-colors hover:bg-white/20 focus-visible:outline-none focus-visible:ring-[3px] focus-visible:ring-white/50"
          aria-label="Fermer la projection">
          
          <X className="size-5" />
        </button>
      </div>

      <div className="flex flex-1 flex-col items-center justify-center gap-8 text-center">
        <p className="text-lg text-neutral-400">Saisissez ce code dans l’espace étudiant</p>
        <div className={`flex gap-2 sm:gap-4 ${expired ? 'opacity-30' : ''}`}>
          {session.code.split('').map((c, i) =>
          <span
            key={i}
            className="flex h-20 w-14 items-center justify-center rounded-2xl bg-white/10 font-mono text-5xl font-semibold sm:h-36 sm:w-28 sm:text-8xl">
            
              {c}
            </span>
          )}
        </div>
        <p className="flex items-center gap-2 text-2xl sm:text-3xl">
          {expired ?
          <>
              <TimerOff className="size-7 text-amber-400" aria-hidden="true" />
              Code expiré à {formatHeure(session.expirationAt)}
            </> :

          <>
              <Timer className="size-7 text-neutral-400" aria-hidden="true" />
              <span className="font-mono tabular-nums">{formatCompteARebours(remaining)}</span>
            </>
          }
        </p>
      </div>
      <p className="text-center text-sm text-neutral-500">Échap pour fermer</p>
    </motion.div>);

}
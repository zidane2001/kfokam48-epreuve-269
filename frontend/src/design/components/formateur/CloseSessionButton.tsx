import React, { useState } from 'react';
import { Lock } from 'lucide-react';
import { toast } from 'sonner';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle } from
'../ui/AlertDialog';
import { Button } from '../ui/Button';
import { Spinner } from '../ui/Spinner';
import { api, toApiError } from '../../utils/api';
import type { SessionDto } from '../../types/domain';

export function CloseSessionButton({ session }: {session: SessionDto;}) {
  const [closing, setClosing] = useState(false);
  const [open, setOpen] = useState(false);

  async function handleClose() {
    setClosing(true);
    try {
      await api.cloturerSession(session.id);
      toast.success('Session clôturée');
      setOpen(false);
    } catch (e) {
      toast.error(toApiError(e).message);
    } finally {
      setClosing(false);
    }
  }

  return (
    <AlertDialog open={open} onOpenChange={setOpen}>
      {/* NB: le design system embarque un AlertDialogTrigger sans support asChild,
          qui rend un <button> dans le <button> (erreur d'hydratation).
          Le Button sert donc de declencheur direct, le dialogue est controle. */}
      <Button variant="destructive" disabled={closing} onClick={() => setOpen(true)}>
        {closing ? <Spinner /> : <Lock />}
        Clôturer
      </Button>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Clôturer « {session.titre} » ?</AlertDialogTitle>
          <AlertDialogDescription>
            Plus aucune présence ni dépôt ne sera accepté. Les relectures envoyées deviennent définitives ; celles
            non rendues resteront en attente dans le tableau de suivi. Cette action est irréversible.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Annuler</AlertDialogCancel>
          <AlertDialogAction onClick={handleClose}>Clôturer la session</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>);

}
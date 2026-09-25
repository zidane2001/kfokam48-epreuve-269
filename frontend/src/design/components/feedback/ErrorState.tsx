import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '../ui/Alert';
import { Button } from '../ui/Button';
import type { ApiError } from '../../utils/api';

interface ErrorStateProps {
  error: ApiError;
  onRetry?: () => void;
  title?: string;
}

export function ErrorState({ error, onRetry, title = 'Chargement impossible' }: ErrorStateProps) {
  return (
    <Alert variant="destructive">
      <AlertCircle />
      <AlertTitle>{title}</AlertTitle>
      <AlertDescription>
        <p>{error.message}</p>
        {onRetry &&
        <Button variant="outline" size="sm" className="mt-2" onClick={onRetry}>
            <RefreshCw /> Réessayer
          </Button>
        }
      </AlertDescription>
    </Alert>);

}
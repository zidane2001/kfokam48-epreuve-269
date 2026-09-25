import React from 'react';
import { Skeleton } from '../ui/Skeleton';

interface LoadingRowsProps {
  rows?: number;
  className?: string;
}

export function LoadingRows({ rows = 3, className = 'h-12' }: LoadingRowsProps) {
  return (
    <div className="space-y-2" role="status" aria-label="Chargement">
      {Array.from({ length: rows }, (_, i) =>
      <Skeleton key={i} className={`w-full rounded-lg ${className}`} />
      )}
    </div>);

}
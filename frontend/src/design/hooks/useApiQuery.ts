import { useCallback, useEffect, useRef, useState } from 'react';
import { api, toApiError, type ApiError } from '../utils/api';

export interface QueryResult<T> {
  data: T | null;
  error: ApiError | null;
  loading: boolean;
  reload: () => void;
}

/**
 * Charge une ressource via la couche API, gère chargement / erreur,
 * et se rafraîchit silencieusement quand les données changent ailleurs.
 */
export function useApiQuery<T>(
fetcher: () => Promise<T>,
deps: ReadonlyArray<unknown>,
options: {enabled?: boolean;} = {})
: QueryResult<T> {
  const enabled = options.enabled ?? true;
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(enabled);
  const fetcherRef = useRef(fetcher);
  fetcherRef.current = fetcher;
  const requestId = useRef(0);

  const run = useCallback(async (silent: boolean) => {
    const id = ++requestId.current;
    if (!silent) setLoading(true);
    try {
      const result = await fetcherRef.current();
      if (id === requestId.current) {
        setData(result);
        setError(null);
      }
    } catch (e) {
      if (id === requestId.current) setError(toApiError(e));
    } finally {
      if (id === requestId.current) setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!enabled) {
      requestId.current++;
      setData(null);
      setLoading(false);
      return;
    }
    setData(null);
    setError(null);
    run(false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, enabled]);

  useEffect(() => {
    if (!enabled) return undefined;
    return api.onDataChange(() => run(true));
  }, [enabled, run]);

  const reload = useCallback(() => {
    run(false);
  }, [run]);

  return { data, error, loading, reload };
}
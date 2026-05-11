import { useEffect, useState, useCallback } from 'react'
import type { GameState } from '../types/game'
import { authHeaders } from '../utils/auth'

function useChessGame(gameId: number | null) {
  const [gameState, setGameState] = useState<GameState | null>(null)

  useEffect(() => {
    if (gameId === null) return

    fetch(`/api/game/${gameId}/state`, { headers: authHeaders() })
      .then((r) => (r.ok ? (r.json() as Promise<GameState>) : null))
      .then((data) => { if (data) setGameState(data) })
      .catch(() => {})

    const source = new EventSource(`/api/game/${gameId}/events`)
    source.onmessage = (event: MessageEvent) => {
      setGameState(JSON.parse(event.data) as GameState)
    }
    source.onerror = () => {}

    return () => source.close()
  }, [gameId])

  const move = useCallback(async (from: string, to: string) => {
    const r = await fetch(`/api/game/${gameId}/move`, {
      method: 'POST',
      headers: { ...authHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify({ from, to }),
    })
    if (r.ok) setGameState((await r.json()) as GameState)
    return r.ok
  }, [gameId])

  const promote = useCallback(async (piece: string) => {
    const r = await fetch(`/api/game/${gameId}/promote`, {
      method: 'POST',
      headers: { ...authHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify({ piece }),
    })
    if (r.ok) setGameState((await r.json()) as GameState)
  }, [gameId])

  const resign = useCallback(async () => {
    const r = await fetch(`/api/game/${gameId}/resign`, { method: 'POST', headers: authHeaders() })
    if (r.ok) setGameState((await r.json()) as GameState)
  }, [gameId])

  const offerDraw = useCallback(async () => {
    const r = await fetch(`/api/game/${gameId}/draw-offer`, { method: 'POST', headers: authHeaders() })
    if (r.ok) setGameState((await r.json()) as GameState)
  }, [gameId])

  const acceptDraw = useCallback(async () => {
    const r = await fetch(`/api/game/${gameId}/draw-accept`, { method: 'POST', headers: authHeaders() })
    if (r.ok) setGameState((await r.json()) as GameState)
  }, [gameId])

  const declineDraw = useCallback(async () => {
    const r = await fetch(`/api/game/${gameId}/draw-decline`, { method: 'POST', headers: authHeaders() })
    if (r.ok) setGameState((await r.json()) as GameState)
  }, [gameId])

  const sendMessage = useCallback(async (text: string) => {
    const r = await fetch(`/api/game/${gameId}/chat`, {
      method: 'POST',
      headers: { ...authHeaders(), 'Content-Type': 'application/json' },
      body: JSON.stringify({ text }),
    })
    if (r.ok) setGameState((await r.json()) as GameState)
  }, [gameId])

  const reportTimeout = useCallback(async () => {
    const r = await fetch(`/api/game/${gameId}/timeout`, { method: 'POST', headers: authHeaders() })
    if (r.ok) setGameState((await r.json()) as GameState)
  }, [gameId])

  return { gameState, move, promote, resign, offerDraw, acceptDraw, declineDraw, sendMessage, reportTimeout }
}

export default useChessGame

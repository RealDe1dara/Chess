import { useCallback, useEffect } from 'react'
import type { GameState } from '../types/game'

const SOUNDS = {
  move:         '/sounds/move.mp3',
  capture:      '/sounds/capture.mp3',
  check:        '/sounds/check.mp3',
  checkmate:    '/sounds/checkmate.mp3',
  castling:     '/sounds/castling.mp3',
  promote:      '/sounds/promote.mp3',
  'game-start': '/sounds/game-start.mp3',
  'low-time':   '/sounds/low-time.mp3',
} as const

type SoundName = keyof typeof SOUNDS

export function useSoundEffects() {
  // On the first user interaction, play one silent sound to register audio intent
  // with the browser. After this, all subsequent play() calls are allowed.
  useEffect(() => {
    const unlock = () => {
      const a = new Audio(SOUNDS.move)
      a.volume = 0
      void a.play().then(() => a.pause()).catch(() => {})
    }
    document.addEventListener('pointerdown', unlock, { once: true })
    return () => document.removeEventListener('pointerdown', unlock)
  }, [])

  // Create a fresh Audio object every time so there are no reset artifacts
  // from reusing an element that was previously played or paused.
  return useCallback((name: SoundName) => {
    const audio = new Audio(SOUNDS[name])
    void audio.play().catch((err) => console.warn('Sound play blocked:', name, err))
  }, [])
}

export function detectSound(prev: GameState, next: GameState): SoundName | null {
  if (prev.status !== 'FINISHED' && next.status === 'FINISHED') return 'checkmate'
  if (prev.status !== 'ACTIVE' && next.status === 'ACTIVE') return 'game-start'

  if (!prev.board || !next.board) return null

  if (
    (next.whiteInCheck && !prev.whiteInCheck) ||
    (next.blackInCheck && !prev.blackInCheck)
  ) return 'check'

  if (prev.promotionPending && !next.promotionPending) return 'promote'

  if (prev.currentTurn === next.currentTurn && prev.status === next.status) return null

  let prevCount = 0
  let nextCount = 0
  for (let r = 0; r < 8; r++) {
    for (let c = 0; c < 8; c++) {
      if (prev.board[r][c]) prevCount++
      if (next.board[r][c]) nextCount++
    }
  }
  if (nextCount < prevCount) return 'capture'

  // Castling — king jumped 2 squares from its starting square
  if (prev.board[7][4] === 'WK' && next.board[7][4] !== 'WK' &&
      (next.board[7][6] === 'WK' || next.board[7][2] === 'WK')) return 'castling'
  if (prev.board[0][4] === 'BK' && next.board[0][4] !== 'BK' &&
      (next.board[0][6] === 'BK' || next.board[0][2] === 'BK')) return 'castling'

  return 'move'
}

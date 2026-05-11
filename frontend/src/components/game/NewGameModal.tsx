import { useEffect, useRef, useState } from 'react'
import { FaChess, FaCopy, FaGlobe, FaPlus, FaRandom, FaRobot, FaSignInAlt, FaUsers } from 'react-icons/fa'
import type { Difficulty, GameMode, GameState } from '../../types/game'
import { authHeaders } from '../../utils/auth'
import '../../css/game/new-game-modal.css'

type NewGameModalProps = {
  open: boolean
  onClose: () => void
  onGameReady: (gameId: number) => void
}

type HumanColor = 'WHITE' | 'RANDOM' | 'BLACK'

const TIME_OPTIONS: { label: string; value: number | null }[] = [
  { label: 'No limit', value: null },
  { label: '1 min',   value: 60 },
  { label: '3 min',   value: 180 },
  { label: '5 min',   value: 300 },
  { label: '10 min',  value: 600 },
]

const DIFFICULTY_OPTIONS: { label: string; sub: string; value: Difficulty }[] = [
  { label: 'Easy',   sub: 'Makes blunders',    value: 'EASY' },
  { label: 'Mid',    sub: 'Solid play',         value: 'MID' },
  { label: 'Hard',   sub: 'Finds most tactics', value: 'HARD' },
  { label: 'Expert', sub: 'Near-perfect',       value: 'EXPERT' },
]

function NewGameModal({ open, onClose, onGameReady }: NewGameModalProps) {
  const [loading, setLoading]               = useState<string | null>(null)
  const [timeLimitSeconds, setTimeLimitSeconds] = useState<number | null>(null)
  const [mode, setMode] = useState<GameMode>('LOCAL')
  const [difficulty, setDifficulty] = useState<Difficulty>('MID')
  const [humanColor, setHumanColor] = useState<HumanColor>('WHITE')
  const [onlineAction, setOnlineAction] = useState<'create' | 'join' | null>(null)
  const [joinId, setJoinId] = useState('')
  const [createdGameId, setCreatedGameId] = useState<number | null>(null)
  const [copied, setCopied] = useState(false)
  const [shouldRender, setShouldRender] = useState(open)
  const [isClosing, setIsClosing] = useState(false)
  const joinInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    if (open) {
      setShouldRender(true)
      setIsClosing(false)
      return
    }
    if (shouldRender) {
      setIsClosing(true)
      const timerId = window.setTimeout(() => setShouldRender(false), 220)
      return () => window.clearTimeout(timerId)
    }
  }, [open, shouldRender])

  useEffect(() => {
    if (onlineAction === 'join') window.setTimeout(() => joinInputRef.current?.focus(), 50)
  }, [onlineAction])

  const handleModeChange = (m: GameMode) => {
    setMode(m)
    if (m !== 'REMOTE') setOnlineAction(null)
    setCreatedGameId(null)
  }

  const createGame = async () => {
    setLoading('create')
    try {
      const r = await fetch('/api/game/create', {
        method: 'POST',
        headers: { ...authHeaders(), 'Content-Type': 'application/json' },
        body: JSON.stringify({
          mode,
          timeLimitSeconds: mode === 'COMPUTER' ? null : timeLimitSeconds,
          difficulty: mode === 'COMPUTER' ? difficulty : undefined,
          humanColor: mode === 'COMPUTER' ? humanColor : undefined,
        }),
      })
      if (r.ok) {
        const state = (await r.json()) as GameState
        if (mode === 'LOCAL' || mode === 'COMPUTER') {
          onGameReady(state.id)
        } else {
          setCreatedGameId(state.id)
        }
      }
    } finally {
      setLoading(null)
    }
  }

  const joinGame = async () => {
    const id = parseInt(joinId, 10)
    if (isNaN(id)) return
    setLoading('join')
    try {
      const r = await fetch(`/api/game/${id}/join`, { method: 'POST', headers: authHeaders() })
      if (r.ok) onGameReady(id)
    } finally {
      setLoading(null)
    }
  }

  const copyId = () => {
    if (createdGameId === null) return
    void navigator.clipboard.writeText(String(createdGameId))
    setCopied(true)
    window.setTimeout(() => setCopied(false), 1800)
  }

  const isRemote   = mode === 'REMOTE'
  const isComputer = mode === 'COMPUTER'

  const canPlay =
    !isRemote ||
    (onlineAction === 'create' && createdGameId === null) ||
    (onlineAction === 'join' && joinId.trim().length > 0)

  if (!shouldRender) return null

  return (
    <div className={`modal-overlay ${isClosing ? 'closing' : 'open'}`} onClick={onClose}>
      <div className={`new-game-modal ${isClosing ? 'closing' : 'open'}`} onClick={(e) => e.stopPropagation()}>
        <button type="button" className="modal-close-btn" onClick={onClose} aria-label="Close">
          ✕
        </button>
        <h2>Start a new game</h2>

        {/* Time control — hidden for Computer mode */}
        {!isComputer && (
          <div className="time-control">
            <span className="time-control-label">Time control</span>
            <div className="time-options">
              {TIME_OPTIONS.map((opt) => (
                <button
                  key={String(opt.value)}
                  type="button"
                  className={`time-option ${timeLimitSeconds === opt.value ? 'time-option--active' : ''}`}
                  onClick={() => setTimeLimitSeconds(opt.value)}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="play-mode">
          <span className="time-control-label">Game mode</span>
          <div className="mode-options">
            <button
              type="button"
              className={`mode-option ${mode === 'COMPUTER' ? 'mode-option--active' : ''}`}
              onClick={() => handleModeChange('COMPUTER')}
              disabled={loading !== null}
            >
              <FaRobot className="mode-option-icon" />
              <span className="mode-option-title">Computer</span>
              <span className="mode-option-sub">vs Stockfish</span>
            </button>
            <button
              type="button"
              className={`mode-option ${mode === 'LOCAL' ? 'mode-option--active' : ''}`}
              onClick={() => handleModeChange('LOCAL')}
              disabled={loading !== null}
            >
              <FaUsers className="mode-option-icon" />
              <span className="mode-option-title">Local</span>
              <span className="mode-option-sub">Same device</span>
            </button>
            <button
              type="button"
              className={`mode-option ${mode === 'REMOTE' ? 'mode-option--active' : ''}`}
              onClick={() => handleModeChange('REMOTE')}
              disabled={loading !== null}
            >
              <FaGlobe className="mode-option-icon" />
              <span className="mode-option-title">Online</span>
              <span className="mode-option-sub">Separate devices</span>
            </button>
          </div>
        </div>

        {/* Computer-specific options */}
        {isComputer && (
          <>
            <div className="difficulty-section">
              <span className="time-control-label">Difficulty</span>
              <div className="difficulty-options">
                {DIFFICULTY_OPTIONS.map((d) => (
                  <button
                    key={d.value}
                    type="button"
                    className={`difficulty-btn ${difficulty === d.value ? 'difficulty-btn--active' : ''}`}
                    onClick={() => setDifficulty(d.value)}
                    disabled={loading !== null}
                  >
                    <span className="difficulty-label">{d.label}</span>
                    <span className="difficulty-sub">{d.sub}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="color-section">
              <span className="time-control-label">Play as</span>
              <div className="color-options">
                <button
                  type="button"
                  className={`color-btn color-btn--white ${humanColor === 'WHITE' ? 'color-btn--active' : ''}`}
                  onClick={() => setHumanColor('WHITE')}
                  disabled={loading !== null}
                >
                  <FaChess className="color-king color-king--white" />
                  <span className="color-label">White</span>
                </button>
                <button
                  type="button"
                  className={`color-btn color-btn--random ${humanColor === 'RANDOM' ? 'color-btn--active' : ''}`}
                  onClick={() => setHumanColor('RANDOM')}
                  disabled={loading !== null}
                >
                  <FaRandom className="color-piece-icon" />
                  <span className="color-label">Random</span>
                </button>
                <button
                  type="button"
                  className={`color-btn color-btn--black ${humanColor === 'BLACK' ? 'color-btn--active' : ''}`}
                  onClick={() => setHumanColor('BLACK')}
                  disabled={loading !== null}
                >
                  <FaChess className="color-king color-king--black" />
                  <span className="color-label">Black</span>
                </button>
              </div>
            </div>
          </>
        )}

        {/* Online sub-options */}
        {isRemote && !createdGameId && (
          <div className="online-actions">
            <span className="time-control-label">What do you want to do?</span>
            <div className="online-action-options">
              <button
                type="button"
                className={`online-action-btn ${onlineAction === 'create' ? 'online-action-btn--active' : ''}`}
                onClick={() => setOnlineAction('create')}
                disabled={loading !== null}
              >
                <FaPlus className="online-action-icon" />
                <span className="online-action-title">Create game</span>
                <span className="online-action-sub">Share the ID with a friend</span>
              </button>
              <button
                type="button"
                className={`online-action-btn ${onlineAction === 'join' ? 'online-action-btn--active' : ''}`}
                onClick={() => setOnlineAction('join')}
                disabled={loading !== null}
              >
                <FaSignInAlt className="online-action-icon" />
                <span className="online-action-title">Join game</span>
                <span className="online-action-sub">Enter a game ID</span>
              </button>
            </div>

            {onlineAction === 'join' && (
              <div className="join-id-row">
                <input
                  ref={joinInputRef}
                  type="number"
                  className="join-id-input"
                  placeholder="Game ID"
                  value={joinId}
                  onChange={(e) => setJoinId(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter' && joinId.trim()) void joinGame() }}
                  disabled={loading !== null}
                />
              </div>
            )}
          </div>
        )}

        {/* Created game — share ID */}
        {createdGameId !== null && (
          <div className="created-game-panel">
            <p className="created-game-label">Game created! Share this ID:</p>
            <div className="created-game-id-row">
              <span className="created-game-id">#{createdGameId}</span>
              <button type="button" className="copy-id-btn" onClick={copyId}>
                <FaCopy />
                {copied ? 'Copied!' : 'Copy'}
              </button>
            </div>
            <p className="created-game-hint">Waiting for your opponent to join…</p>
            <button type="button" className="play-btn" onClick={() => onGameReady(createdGameId)}>
              Open board
            </button>
          </div>
        )}

        {/* Play / Join button */}
        {createdGameId === null && (
          <button
            type="button"
            className="play-btn"
            onClick={() => {
              if (isRemote && onlineAction === 'join') void joinGame()
              else void createGame()
            }}
            disabled={loading !== null || !canPlay}
          >
            {loading === 'create' && 'Creating…'}
            {loading === 'join'   && 'Joining…'}
            {loading === null && (isRemote && onlineAction === 'join' ? 'Join' : 'Play')}
          </button>
        )}
      </div>
    </div>
  )
}

export default NewGameModal

import { useState, useCallback, useMemo, useEffect, useRef } from 'react'
import { FaHandshake, FaFlag, FaCheck, FaTimes, FaPaperPlane } from 'react-icons/fa'
import useChessGame from '../hooks/useChessGame'
import { useSoundEffects, detectSound } from '../hooks/useSoundEffects'
import type { GameState } from '../types/game'
import { authHeaders } from '../utils/auth'
import '../css/game/chess-game-page.css'

const PIECE_IMAGES: Record<string, string> = {
  WK: '/pieces/white_king.svg',  WQ: '/pieces/white_queen.svg',
  WR: '/pieces/white_rook.svg',  WB: '/pieces/white_bishop.svg',
  WN: '/pieces/white_knight.svg', WP: '/pieces/white_pawn.svg',
  BK: '/pieces/black_king.svg',  BQ: '/pieces/black_queen.svg',
  BR: '/pieces/black_rook.svg',  BB: '/pieces/black_bishop.svg',
  BN: '/pieces/black_knight.svg', BP: '/pieces/black_pawn.svg',
}

const PIECE_VALUES: Record<string, number> = { P: 1, N: 3, B: 3, R: 5, Q: 9 }
const STARTING_COUNTS: Record<string, number> = {
  WP: 8, WN: 2, WB: 2, WR: 2, WQ: 1,
  BP: 8, BN: 2, BB: 2, BR: 2, BQ: 1,
}
const CAPTURE_SORT: Record<string, number> = { P: 0, N: 1, B: 2, R: 3, Q: 4 }

function calcMaterial(board: (string | null)[][] | null | undefined) {
  if (!board) return { capturedByWhite: [] as string[], capturedByBlack: [] as string[], advantage: 0 }

  const current: Record<string, number> = {}
  for (let r = 0; r < 8; r++)
    for (let c = 0; c < 8; c++) {
      const p = board[r][c]
      if (p) current[p] = (current[p] ?? 0) + 1
    }

  const capturedByWhite: string[] = []
  const capturedByBlack: string[] = []

  for (const [piece, startCount] of Object.entries(STARTING_COUNTS)) {
    const n = Math.max(0, startCount - (current[piece] ?? 0))
    const arr = piece.startsWith('B') ? capturedByWhite : capturedByBlack
    for (let i = 0; i < n; i++) arr.push(piece)
  }

  const byType = (a: string, b: string) => CAPTURE_SORT[a[1]] - CAPTURE_SORT[b[1]]
  capturedByWhite.sort(byType)
  capturedByBlack.sort(byType)

  const score = (arr: string[]) => arr.reduce((s, p) => s + (PIECE_VALUES[p[1]] ?? 0), 0)
  const advantage = score(capturedByWhite) - score(capturedByBlack)

  return { capturedByWhite, capturedByBlack, advantage }
}

const FILES = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h']

type ChessGamePageProps = {
  gameId: number
  username: string
  onLeave: () => void
}

function ChessGamePage({ gameId, username, onLeave }: ChessGamePageProps) {
  const {
    gameState, move, promote, resign, offerDraw, acceptDraw, declineDraw,
    sendMessage, reportTimeout,
  } = useChessGame(gameId)

  const play         = useSoundEffects()
  const prevStateRef = useRef<GameState | null>(null)

  useEffect(() => {
    const prev = prevStateRef.current
    prevStateRef.current = gameState
    if (!gameState) return
    if (!prev) {
      // First state load: LOCAL/COMPUTER games start as ACTIVE immediately,
      // so the WAITING→ACTIVE transition is never observed. Play it here instead.
      if (gameState.status === 'ACTIVE') setTimeout(() => play('game-start'), 300)
      return
    }
    const sound = detectSound(prev, gameState)
    if (sound) play(sound)
  }, [gameState, play])

  const [selected, setSelected]                   = useState<string | null>(null)
  const [validTargets, setValidTargets]           = useState<string[]>([])
  const [eloWhite, setEloWhite]                   = useState<number | null>(null)
  const [eloBlack, setEloBlack]                   = useState<number | null>(null)
  const [displayTimeWhite, setDisplayTimeWhite]   = useState<number | null>(null)
  const [displayTimeBlack, setDisplayTimeBlack]   = useState<number | null>(null)
  const [chatInput, setChatInput]                 = useState('')
  const [showLeaveConfirm, setShowLeaveConfirm]   = useState(false)

  const chatEndRef        = useRef<HTMLDivElement>(null)
  const timeoutFiredRef      = useRef(false)
  const lowTimeFiredWhiteRef = useRef(false)
  const lowTimeFiredBlackRef = useRef(false)

  useEffect(() => {
    const white = gameState?.playerWhite
    const black = gameState?.playerBlack
    if (!white) return

    const fetchElo = async (player: string, setter: (n: number | null) => void) => {
      try {
        const r = await fetch(`/api/elo/chess/${encodeURIComponent(player)}`, { headers: authHeaders() })
        if (r.ok) setter((await r.json()) as number)
      } catch { /* non-critical */ }
    }

    void fetchElo(white, setEloWhite)
    if (black) void fetchElo(black, setEloBlack)
  }, [gameState?.playerWhite, gameState?.playerBlack])

  useEffect(() => {
    if (gameState?.timeWhiteMs != null) {
      setDisplayTimeWhite(gameState.timeWhiteMs)
      if (gameState.timeWhiteMs > 20_000) lowTimeFiredWhiteRef.current = false
    }
    if (gameState?.timeBlackMs != null) {
      setDisplayTimeBlack(gameState.timeBlackMs)
      if (gameState.timeBlackMs > 20_000) lowTimeFiredBlackRef.current = false
    }
    timeoutFiredRef.current = false
  }, [gameState?.timeWhiteMs, gameState?.timeBlackMs])

  // When a player's clock hits 0, the client POSTs /timeout so the server can
  // verify and end the game (server has its own 2-second tolerance check).
  useEffect(() => {
    if (gameState?.status !== 'ACTIVE' || !gameState?.timeLimitSeconds) return

    const id = setInterval(() => {
      const isWhite = gameState.currentTurn === 'WHITE'
      const setter  = isWhite ? setDisplayTimeWhite : setDisplayTimeBlack
      setter((prev) => {
        if (prev === null) return prev
        const next = Math.max(0, prev - 1000)
        const firedRef = isWhite ? lowTimeFiredWhiteRef : lowTimeFiredBlackRef
        if (next <= 20_000 && next > 0 && !firedRef.current) {
          firedRef.current = true
          play('low-time')
        }
        if (next === 0 && !timeoutFiredRef.current) {
          timeoutFiredRef.current = true
          void reportTimeout()
        }
        return next
      })
    }, 1000)

    return () => clearInterval(id)
  }, [gameState?.status, gameState?.currentTurn, gameState?.timeLimitSeconds, reportTimeout])

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [gameState?.chatMessages?.length])

  const { capturedByWhite, capturedByBlack, advantage } = useMemo(
    () => calcMaterial(gameState?.board),
    [gameState?.board],
  )

  const isFlipped = useMemo(() => {
    if (!gameState) return false
    if (gameState.mode === 'LOCAL') return gameState.currentTurn === 'BLACK'
    return gameState.playerBlack === username
  }, [gameState, username])

  const isMyTurn = useCallback(() => {
    if (!gameState || gameState.status !== 'ACTIVE') return false
    if (gameState.promotionPending) return false
    if (gameState.mode === 'LOCAL') return true
    return (
      (gameState.currentTurn === 'WHITE' && gameState.playerWhite === username) ||
      (gameState.currentTurn === 'BLACK' && gameState.playerBlack === username)
    )
  }, [gameState, username])

  const canPromote = useMemo(() => {
    if (!gameState || gameState.status !== 'ACTIVE' || !gameState.promotionPending) return false
    if (gameState.mode === 'LOCAL') return true
    return (
      (gameState.currentTurn === 'WHITE' && gameState.playerWhite === username) ||
      (gameState.currentTurn === 'BLACK' && gameState.playerBlack === username)
    )
  }, [gameState, username])

  // In LOCAL mode, drawProposedBy is always the same username as the accepter,
  // so we can never say "it's my own offer" — show the accept/decline banner always.
  const isMyDrawOffer      = gameState?.mode !== 'LOCAL' && gameState?.drawProposedBy === username
  const hasIncomingDrawOffer = !!(gameState?.drawProposedBy && !isMyDrawOffer)

  const handleSquareClick = useCallback(
    async (visualRow: number, visualCol: number) => {
      if (!gameState?.board || !isMyTurn()) return

      const actualRow = isFlipped ? 7 - visualRow : visualRow
      const actualCol = isFlipped ? 7 - visualCol : visualCol
      const notation  = FILES[actualCol] + String(8 - actualRow)
      const piece     = gameState.board[actualRow][actualCol]
      const currentColor = gameState.currentTurn

      if (selected === notation) { setSelected(null); setValidTargets([]); return }

      if (selected !== null && validTargets.includes(notation)) {
        setSelected(null); setValidTargets([])
        await move(selected, notation)
        return
      }

      const isCurrentPlayerPiece =
        piece !== null &&
        ((currentColor === 'WHITE' && piece.startsWith('W')) ||
         (currentColor === 'BLACK' && piece.startsWith('B')))

      if (isCurrentPlayerPiece) {
        setSelected(notation)
        const r = await fetch(`/api/game/${gameId}/valid-moves?from=${notation}`, {
          headers: authHeaders(),
        })
        if (r.ok) setValidTargets((await r.json()) as string[])
      } else {
        setSelected(null); setValidTargets([])
      }
    },
    [gameState, isMyTurn, isFlipped, selected, validTargets, move, gameId],
  )

  const playerLabel = (color: 'WHITE' | 'BLACK'): string => {
    if (!gameState) return '—'
    if (gameState.mode === 'LOCAL') return color === 'WHITE' ? 'White' : 'Black'
    return color === 'WHITE' ? (gameState.playerWhite ?? '—') : (gameState.playerBlack ?? '—')
  }

  const playerElo = (color: 'WHITE' | 'BLACK') =>
    color === 'WHITE' ? eloWhite : eloBlack

  const playerTime = (color: 'WHITE' | 'BLACK') =>
    color === 'WHITE' ? displayTimeWhite : displayTimeBlack

  const formatTime = (ms: number | null): string => {
    if (ms === null) return ''
    const totalSec = Math.max(0, Math.ceil(ms / 1000))
    const m = Math.floor(totalSec / 60)
    const s = totalSec % 60
    return `${m}:${s.toString().padStart(2, '0')}`
  }

  const handleBackClick = useCallback(() => {
    if (gameState?.status === 'ACTIVE') {
      setShowLeaveConfirm(true)
    } else {
      onLeave()
    }
  }, [gameState?.status, onLeave])

  const handleResignAndLeave = useCallback(async () => {
    if (gameState?.status === 'ACTIVE') await resign()
    onLeave()
  }, [gameState?.status, resign, onLeave])

  const handleSendMessage = async () => {
    const text = chatInput.trim()
    if (!text) return
    setChatInput('')
    await sendMessage(text)
  }

  const topColor:    'WHITE' | 'BLACK' = isFlipped ? 'WHITE' : 'BLACK'
  const bottomColor: 'WHITE' | 'BLACK' = isFlipped ? 'BLACK' : 'WHITE'

  const isColorActive = (color: 'WHITE' | 'BLACK') =>
    gameState?.currentTurn === color && gameState.status === 'ACTIVE'

  const statusText = (): string => {
    if (!gameState) return 'Loading…'
    if (gameState.status === 'WAITING') return 'Waiting for opponent…'
    if (gameState.status === 'FINISHED') {
      if (gameState.result === 'DRAW') return 'Draw!'
      const winnerLabel = gameState.result === 'WHITE_WON'
        ? playerLabel('WHITE') : playerLabel('BLACK')
      return `${winnerLabel} wins!`
    }
    if (gameState.promotionPending) return 'Choose promotion piece'
    return `${playerLabel(gameState.currentTurn as 'WHITE' | 'BLACK')}'s turn`
  }

  const formatWinReason = (reason: string | null): string => {
    if (!reason) return 'by game end'
    switch (reason) {
      case 'CHECKMATE': return 'by checkmate'
      case 'TIMEOUT': return 'on time'
      case 'RESIGN': return 'by resignation'
      default: return `by ${reason.toLowerCase().replaceAll('_', ' ')}`
    }
  }

  const formatDrawReason = (reason: string | null): string => {
    if (!reason) return 'by draw'
    switch (reason) {
      case 'AGREEMENT': return 'by agreement'
      case 'STALEMATE': return 'by stalemate'
      case 'INSUFFICIENT_MATERIAL': return 'by insufficient material'
      case 'THREEFOLD_REPETITION': return 'by threefold repetition'
      default: return `by ${reason.toLowerCase().replaceAll('_', ' ')}`
    }
  }

  const finishedReasonText = useMemo(() => {
    if (!gameState || gameState.status !== 'FINISHED') return ''
    if (gameState.result === 'DRAW') return formatDrawReason(gameState.drawReason)
    return formatWinReason(gameState.winReason)
  }, [gameState])

  const board      = gameState?.board
  const rankLabels = isFlipped ? [1, 2, 3, 4, 5, 6, 7, 8] : [8, 7, 6, 5, 4, 3, 2, 1]
  const fileLabels = isFlipped ? [...FILES].reverse() : FILES
  const lastMoveSquares = new Set([gameState?.lastMoveFrom, gameState?.lastMoveTo].filter(Boolean))

  const PlayerStrip = ({ color }: { color: 'WHITE' | 'BLACK' }) => {
    const t = playerTime(color)
    const isDanger = t !== null && t <= 20_000
    const elo = playerElo(color)
    const captured = color === 'WHITE' ? capturedByWhite : capturedByBlack
    const adv = color === 'WHITE' ? advantage : -advantage

    return (
      <div className={`board-player-strip ${isColorActive(color) ? 'active-turn' : ''}`}>
        <span className={`strip-dot ${color === 'WHITE' ? 'white-dot' : 'black-dot'}`} />
        <span className="strip-name">{playerLabel(color)}</span>
        {elo !== null && elo > 0 && <span className="strip-elo">({elo})</span>}
        {captured.map((piece, i) => (
          <img key={i} src={PIECE_IMAGES[piece]} alt={piece} className="capture-icon" draggable={false} />
        ))}
        {adv > 0 && <span className="strip-advantage">+{adv}</span>}
        <span className="strip-spacer" />
        {(gameState?.timeLimitSeconds ?? 0) > 0 && t !== null && (
          <span className={`strip-clock ${isDanger ? 'strip-clock--danger' : ''}`}>
            {formatTime(t)}
          </span>
        )}
      </div>
    )
  }

  return (
    <div className="chess-game-page">
      <div className="game-layout">

        <div className="panel-topbar">
          <button type="button" className="back-btn" onClick={handleBackClick}>← Menu</button>
          <span className="game-id-label">Game #{gameId}</span>
        </div>

        <div className="board-column">

          <PlayerStrip color={topColor} />

          <div className="board-grid">
            {Array.from({ length: 8 }, (_, visualRow) =>
              Array.from({ length: 8 }, (_, visualCol) => {
                const actualRow = isFlipped ? 7 - visualRow : visualRow
                const actualCol = isFlipped ? 7 - visualCol : visualCol
                const notation  = FILES[actualCol] + String(8 - actualRow)
                const piece     = board?.[actualRow]?.[actualCol] ?? null
                const isLight    = (actualRow + actualCol) % 2 === 0
                const isSelected = selected === notation
                const isTarget   = validTargets.includes(notation)
                const isCheckedKing =
                  (piece === 'WK' && !!gameState?.whiteInCheck) ||
                  (piece === 'BK' && !!gameState?.blackInCheck)
                const isLastMove = lastMoveSquares.has(notation)

                return (
                  <button
                    key={notation}
                    type="button"
                    className={[
                      'board-square',
                      isLight    ? 'light'    : 'dark',
                      isSelected ? 'selected' : '',
                      isTarget   ? 'target'   : '',
                      isCheckedKing ? 'in-check' : '',
                      isLastMove && !isSelected ? 'last-move' : '',
                    ].filter(Boolean).join(' ')}
                    onClick={() => void handleSquareClick(visualRow, visualCol)}
                    aria-label={`${notation}${piece ? ` (${piece})` : ''}`}
                  >
                    {piece && (
                      <img
                        src={PIECE_IMAGES[piece]}
                        alt={piece}
                        className="piece-img"
                        draggable={false}
                      />
                    )}
                    {isTarget && !piece && <span className="target-dot" />}
                    {visualCol === 0 && (
                      <span className="coord-rank">{rankLabels[visualRow]}</span>
                    )}
                    {visualRow === 7 && (
                      <span className="coord-file">{fileLabels[visualCol]}</span>
                    )}
                  </button>
                )
              }),
            )}
          </div>

          <PlayerStrip color={bottomColor} />
        </div>

        <div className="game-panel">

          {gameState?.status === 'WAITING' && gameState.mode === 'REMOTE' && (
            <p className="waiting-hint">Share Game #{gameId} with your opponent to join</p>
          )}

          <p className={`panel-status ${gameState?.status === 'FINISHED' ? 'panel-status--finished' : ''}`}>
            {statusText()}
          </p>
          {gameState?.status === 'FINISHED' && (
            <p className="panel-status-reason">{finishedReasonText}</p>
          )}

          {canPromote && (
            <div className="promotion-picker">
              {(['Q', 'R', 'B', 'N'] as const).map((p) => {
                const code = (gameState?.currentTurn === 'BLACK' ? 'B' : 'W') + p
                return (
                  <button key={p} type="button" className="promo-btn"
                    onClick={() => void promote(p)} aria-label={`Promote to ${p}`}>
                    <img src={PIECE_IMAGES[code]} alt={p} className="promo-img" draggable={false} />
                  </button>
                )
              })}
            </div>
          )}

          {hasIncomingDrawOffer && !gameState?.promotionPending && (
            <div className="draw-offer-banner">
              <span>{gameState?.drawProposedBy} offers a draw</span>
              <div className="draw-offer-actions">
                <button type="button" className="accept-draw-btn" onClick={() => void acceptDraw()}>
                  <FaCheck className="btn-icon" /> Accept
                </button>
                <button type="button" className="decline-draw-btn" onClick={() => void declineDraw()}>
                  <FaTimes className="btn-icon" /> Decline
                </button>
              </div>
            </div>
          )}

          {gameState?.status === 'ACTIVE' && !gameState.promotionPending && (
            <div className="panel-actions">
              {isMyDrawOffer ? (
                <span className="draw-pending-label">Draw offered…</span>
              ) : !hasIncomingDrawOffer ? (
                <button type="button" className="action-btn draw-btn" onClick={() => void offerDraw()}>
                  <FaHandshake className="btn-icon" /> Offer draw
                </button>
              ) : null}

              <button type="button" className="action-btn resign-btn" onClick={() => void resign()}>
                <FaFlag className="btn-icon" /> Resign
              </button>
            </div>
          )}

          {gameState?.status === 'FINISHED' && (
            // <div className="result-card">
              <button type="button" className="action-btn back-to-menu-btn" onClick={onLeave}>
                Back to menu
              </button>
            // </div>
          )}

          <div className="chat-box">
            <div className="chat-messages">
              {(gameState?.chatMessages ?? []).map((msg, i) => (
                <div key={i} className={`chat-msg ${msg.sender === username ? 'chat-msg--mine' : ''}`}>
                  <span className="chat-sender">{msg.sender}</span>
                  <span className="chat-text">{msg.text}</span>
                </div>
              ))}
              <div ref={chatEndRef} />
            </div>
            <div className="chat-input-row">
              <input
                type="text"
                className="chat-input"
                placeholder="Message…"
                value={chatInput}
                maxLength={500}
                onChange={(e) => setChatInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') void handleSendMessage() }}
              />
              <button type="button" className="chat-send-btn" onClick={() => void handleSendMessage()}>
                <FaPaperPlane />
              </button>
            </div>
          </div>

        </div>
      </div>

      {showLeaveConfirm && (
        <div className="leave-confirm-overlay" onClick={() => setShowLeaveConfirm(false)}>
          <div className="leave-confirm-modal" onClick={(e) => e.stopPropagation()}>
            <p className="leave-confirm-title">Resign and leave?</p>
            <p className="leave-confirm-body">
              {gameState?.mode === 'LOCAL'
                ? 'The game is still in progress. Are you sure you want to leave?'
                : 'Leaving now will count as a resignation. Your opponent will win.'}
            </p>
            <div className="leave-confirm-actions">
              <button type="button" className="leave-cancel-btn" onClick={() => setShowLeaveConfirm(false)}>
                Cancel
              </button>
              <button type="button" className="leave-resign-btn" onClick={() => void handleResignAndLeave()}>
                {gameState?.mode === 'LOCAL' ? 'Leave' : 'Resign & Leave'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default ChessGamePage

import { useEffect, useState } from 'react'
import { FaMedal } from 'react-icons/fa'
import '../../css/menu/player-stats-section.css'

type PlayerStatsSectionProps = {
  username: string
  elo: number | null
  userRating: number | null
  totalGames: number
  wins: number
  draws: number
  losses: number
  onSetRating: (value: number) => Promise<void>
}

function PlayerStatsSection({ username, elo, userRating, totalGames, wins, draws, losses, onSetRating }: PlayerStatsSectionProps) {
  const [draftRating, setDraftRating] = useState<number>(userRating ?? 0)
  const [hoverRating, setHoverRating] = useState(0)

  useEffect(() => {
    setDraftRating(userRating ?? 0)
  }, [userRating])

  const canSetRating = draftRating > 0 && draftRating !== (userRating ?? 0)

  const winRate = totalGames > 0 ? Math.round((wins / totalGames) * 100) : 0

  return (
    <section className="player-stats-section menu-card">
      <h2>Stats</h2>
      <div className="identity-block">
        <strong className="name">{username}</strong>
        <p className="elo-line">
          <span>{elo ?? '—'}</span>
          <FaMedal aria-hidden="true" />
        </p>
      </div>

      <p className="games-row">You played {totalGames} {(totalGames === 1) ? "game" : "games"}</p>
      <div className="wdl-labels">
        <span className="win">W</span>
        <span className="draw">D</span>
        <span className="loss">L</span>
      </div>
      <div className="wdl-values">
        <strong className="win">{wins}</strong>
        <strong className="draw">{draws}</strong>
        <strong className="loss">{losses}</strong>
      </div>
      {totalGames > 0 && (
        <div className="winrate-bar-wrap" title={`Win rate: ${winRate}%`}>
          <div className="winrate-bar" style={{ width: `${winRate}%` }} />
          <span className="winrate-label">{winRate}% win rate</span>
        </div>
      )}

      <div className="your-rate">
        <span>Your rate</span>
        <div
          className="rating-picker"
          role="radiogroup"
          aria-label="Set your rating"
          onMouseLeave={() => setHoverRating(0)}
        >
          {Array.from({ length: 5 }, (_, index) => index + 1).map((value) => {
            const isHoverFill = hoverRating > 0 && hoverRating > (userRating ?? 0) && value <= hoverRating
            const isActive = isHoverFill || value <= draftRating
            return (
              <button
                key={value}
                type="button"
                className={`pick-star ${isActive ? 'active' : ''}`}
                onClick={() => setDraftRating(value)}
                onMouseEnter={() => setHoverRating(value)}
                aria-label={`Rate ${value} star${value > 1 ? 's' : ''}`}
              >
                ★
              </button>
            )
          })}
        </div>
        {canSetRating && (
          <button type="button" className="small-btn set-rating-btn" onClick={() => void onSetRating(draftRating)}>
            Set rating
          </button>
        )}
      </div>
    </section>
  )
}

export default PlayerStatsSection

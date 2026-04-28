import type { EloEntry } from '../../types/menu'
import { FaCrown, FaMedal, FaStar } from 'react-icons/fa'
import '../../css/menu/top-players-section.css'

type TopPlayersSectionProps = {
  topElo: EloEntry[]
  currentUsername: string
}

function TopPlayersSection({ topElo, currentUsername }: TopPlayersSectionProps) {
  const rankIcon = (index: number) => {
    if (index === 0) {
      return <FaCrown className="rank-icon crown" aria-label="1st place" />
    }
    if (index === 1) {
      return <FaMedal className="rank-icon silver" aria-label="2nd place" />
    }
    if (index === 2) {
      return <FaMedal className="rank-icon bronze" aria-label="3rd place" />
    }
    return <FaStar className="rank-icon star" aria-label={`${index + 1}th place`} />
  }

  return (
    <section className="top-players-section menu-card">
      <h2>Top players</h2>
      <ol>
        {topElo.length === 0 ? (
          <li>No ELO records yet.</li>
        ) : (
          topElo.slice(0, 10).map((entry, index) => (
            <li key={entry.ident} className={entry.player === currentUsername ? 'me' : ''}>
              <span className="player-line">
                {rankIcon(index)}
                <span className="rank-number">{index + 1}.</span>
                <span>{entry.player}</span>
              </span>
              <strong>{entry.elo}</strong>
            </li>
          ))
        )}
      </ol>
    </section>
  )
}

export default TopPlayersSection

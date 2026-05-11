import type { EloEntry } from '../../types/menu'
import { FaCrown, FaMedal, FaStar } from 'react-icons/fa'
import '../../css/menu/top-players-section.css'

const TOP_N = 10

type TopPlayersSectionProps = {
  topElo: EloEntry[]
  currentUsername: string
}

function TopPlayersSection({ topElo, currentUsername }: TopPlayersSectionProps) {
  const rankIcon = (index: number) => {
    if (index === 0) return <FaCrown className="rank-icon crown" aria-label="1st place" />
    if (index === 1) return <FaMedal className="rank-icon silver" aria-label="2nd place" />
    if (index === 2) return <FaMedal className="rank-icon bronze" aria-label="3rd place" />
    return <FaStar className="rank-icon star" aria-label={`${index + 1}th place`} />
  }

  const slots = Array.from({ length: TOP_N }, (_, i) => topElo[i] ?? null)

  return (
    <section className="top-players-section menu-card">
      <h2>Hall of Fame</h2>
      <div className="hof-header">
        <span className="hof-col-rank" aria-hidden="true">#</span>
        <span />
        <span className="hof-col-name">Name</span>
        <span className="hof-col-elo">ELO</span>
      </div>
      <ol>
        {slots.map((entry, index) => (
          <li
            key={entry?.ident ?? `empty-${index}`}
            className={entry?.player === currentUsername ? 'me' : entry === null ? 'empty' : ''}
          >
            {rankIcon(index)}
            <span className="rank-number">{index + 1}.</span>
            <span className="player-name">{entry ? entry.player : '—'}</span>
            <strong className="elo-value">{entry ? entry.elo : '—'}</strong>
          </li>
        ))}
      </ol>
    </section>
  )
}

export default TopPlayersSection

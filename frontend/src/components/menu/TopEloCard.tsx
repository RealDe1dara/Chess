import type { EloEntry } from '../../types/menu'
import '../../css/menu/top-elo-card.css'

type TopEloCardProps = {
  topElo: EloEntry[]
}

function TopEloCard({ topElo }: TopEloCardProps) {
  return (
    <section className="top-elo-card menu-card">
      <h2>Top players by ELO</h2>
      <ol>
        {topElo.length === 0 ? (
          <li>No ELO records yet.</li>
        ) : (
          topElo.slice(0, 8).map((entry) => (
            <li key={entry.ident}>
              <span>{entry.player}</span>
              <strong>{entry.elo}</strong>
            </li>
          ))
        )}
      </ol>
    </section>
  )
}

export default TopEloCard

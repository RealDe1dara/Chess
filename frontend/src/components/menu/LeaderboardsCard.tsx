import type { EloEntry, ScoreEntry } from '../../types/menu'
import '../../css/menu/leaderboards-card.css'

type LeaderboardsCardProps = {
  topScores: ScoreEntry[]
  topElo: EloEntry[]
}

function LeaderboardsCard({ topScores, topElo }: LeaderboardsCardProps) {
  return (
    <section className="leaderboards-card menu-card">
      <h2>Leaderboards</h2>
      <div className="leaderboards-grid">
        <article>
          <h3>Top score</h3>
          <ol>
            {topScores.length === 0 ? (
              <li>No scores yet.</li>
            ) : (
              topScores.slice(0, 5).map((score) => (
                <li key={score.ident}>
                  <span>{score.player}</span>
                  <strong>{score.points}</strong>
                </li>
              ))
            )}
          </ol>
        </article>

        <article>
          <h3>Top ELO</h3>
          <ol>
            {topElo.length === 0 ? (
              <li>No ELO records yet.</li>
            ) : (
              topElo.slice(0, 5).map((entry) => (
                <li key={entry.ident}>
                  <span>{entry.player}</span>
                  <strong>{entry.elo}</strong>
                </li>
              ))
            )}
          </ol>
        </article>
      </div>
    </section>
  )
}

export default LeaderboardsCard

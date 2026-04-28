import type { ScoreEntry } from '../../types/menu'
import '../../css/menu/top-scores-card.css'

type TopScoresCardProps = {
  topScores: ScoreEntry[]
}

function TopScoresCard({ topScores }: TopScoresCardProps) {
  return (
    <section className="top-scores-card menu-card">
      <h2>Top scores</h2>
      <ol>
        {topScores.length === 0 ? (
          <li>No scores yet.</li>
        ) : (
          topScores.slice(0, 8).map((score) => (
            <li key={score.ident}>
              <span>{score.player}</span>
              <strong>{score.points}</strong>
            </li>
          ))
        )}
      </ol>
    </section>
  )
}

export default TopScoresCard

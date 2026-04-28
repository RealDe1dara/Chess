import '../../css/menu/last-games-section.css'

type LastGameItem = {
  id: string
  playedOn: string
  pair: string
  result: 'Won' | 'Lost' | 'Draw' | 'Played'
}

type LastGamesSectionProps = {
  games: LastGameItem[]
  onNewGame: () => void
}

function LastGamesSection({ games, onNewGame }: LastGamesSectionProps) {
  return (
    <section className="last-games-section menu-card">
      <header>
        <h2>Last games</h2>
        <button type="button" className="new-game-btn" onClick={onNewGame}>
          New game
        </button>
      </header>
      <div className="games-list">
        {games.length === 0 ? (
          <p className="empty">No games yet.</p>
        ) : (
          games.map((game) => (
            <article key={game.id} className="game-item">
              <p className="pair">{game.pair}</p>
              <p className={`result ${game.result.toLowerCase()}`}>{game.result}</p>
              <time>{new Date(game.playedOn).toLocaleString()}</time>
            </article>
          ))
        )}
      </div>
    </section>
  )
}

export default LastGamesSection

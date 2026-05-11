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
      {games.length === 0 ? (
        <p className="empty">No games yet.</p>
      ) : (
        <div className="games-table">
          <div className="games-table-header">
            <span>Opponent</span>
            <span>Date</span>
            <span>Result</span>
          </div>
          {games.map((game) => (
            <div key={game.id} className="game-row">
              <span className="row-pair">{game.pair}</span>
              <time className="row-date">{new Date(game.playedOn).toLocaleString()}</time>
              <span className={`row-result ${game.result.toLowerCase()}`}>{game.result}</span>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

export default LastGamesSection

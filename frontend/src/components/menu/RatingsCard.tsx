import '../../css/menu/ratings-card.css'

type RatingsCardProps = {
  userRating: number | null
  averageRating: number | null
}

function RatingsCard({ userRating, averageRating }: RatingsCardProps) {
  const averageDisplay = averageRating === null ? '—' : averageRating.toFixed(1)

  return (
    <section className="ratings-card menu-card">
      <h2>Ratings</h2>
      <div className="ratings-grid">
        <article>
          <span>Your rating</span>
          <strong>{userRating ?? '—'}</strong>
        </article>
        <article>
          <span>Game average</span>
          <strong>{averageDisplay}</strong>
        </article>
      </div>
    </section>
  )
}

export default RatingsCard

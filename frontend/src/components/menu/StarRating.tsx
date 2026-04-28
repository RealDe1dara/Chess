import '../../css/menu/star-rating.css'

type StarRatingProps = {
  value: number | null
  labelPrefix?: string
}

function StarRating({ value, labelPrefix = 'Rating' }: StarRatingProps) {
  if (value === null) {
    return <p className="star-rating-empty">No ratings yet.</p>
  }

  const roundedValue = Math.round(value * 2) / 2
  const stars = Array.from({ length: 5 }, (_, index) => {
    const fill = Math.max(0, Math.min(1, roundedValue - index))
    if (fill >= 1) {
      return 'full'
    }
    if (fill >= 0.5) {
      return 'half'
    }
    return 'empty'
  })

  return (
    <div className="star-rating" aria-label={`${labelPrefix} ${roundedValue.toFixed(1)} out of 5`}>
      <div className="star-row">
        {stars.map((type, index) => (
          <span key={index} className={`star ${type}`} aria-hidden="true">
            ★
          </span>
        ))}
      </div>
      {/*<small>{roundedValue.toFixed(1)} / 5</small>*/}
    </div>
  )
}

export default StarRating

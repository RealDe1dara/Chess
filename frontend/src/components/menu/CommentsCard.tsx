import type { CommentEntry } from '../../types/menu'
import '../../css/menu/comments-card.css'

type CommentsCardProps = {
  comments: CommentEntry[]
  draft: string
  onDraftChange: (value: string) => void
  onAddComment: () => void
}

function CommentsCard({ comments, draft, onDraftChange, onAddComment }: CommentsCardProps) {
  return (
    <section className="comments-card menu-card">
      <h2>Comments</h2>
      <div className="comment-list">
        {comments.length === 0 ? (
          <p className="empty">No comments yet.</p>
        ) : (
          comments.slice(0, 6).map((entry) => (
            <article key={entry.ident} className="comment-item">
              <header>
                <strong>{entry.player}</strong>
                <time>{new Date(entry.commentedOn).toLocaleDateString()}</time>
              </header>
              <p>{entry.comment}</p>
            </article>
          ))
        )}
      </div>
      <label>
        <span>Add your comment</span>
        <textarea value={draft} onChange={(event) => onDraftChange(event.target.value)} maxLength={300} />
      </label>
      <button type="button" className="small-btn" onClick={onAddComment}>
        Add comment
      </button>
    </section>
  )
}

export default CommentsCard

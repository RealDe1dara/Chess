import { useState } from 'react'
import PlayerStatsSection from './PlayerStatsSection'
import TopPlayersSection from './TopPlayersSection'
import CommentsCard from './CommentsCard'
import type { EloEntry, CommentEntry } from '../../types/menu'
import '../../css/menu/left-tab-panel.css'

type Tab = 'stats' | 'hall' | 'comments'

type LeftTabPanelProps = {
  username: string
  elo: number | null
  userRating: number | null
  totalGames: number
  wins: number
  draws: number
  losses: number
  onSetRating: (value: number) => Promise<void>
  topElo: EloEntry[]
  comments: CommentEntry[]
  commentDraft: string
  onCommentDraftChange: (value: string) => void
  onAddComment: () => void
}

const TAB_LABELS: Record<Tab, string> = {
  stats: 'Stats',
  hall: 'Hall of Fame',
  comments: 'Comments',
}

function LeftTabPanel({
  username, elo, userRating, totalGames, wins, draws, losses, onSetRating,
  topElo, comments, commentDraft, onCommentDraftChange, onAddComment,
}: LeftTabPanelProps) {
  const [activeTab, setActiveTab] = useState<Tab>('stats')

  return (
    <div className="left-tab-panel menu-card">
      <div className="ltp-tab-bar" role="tablist">
        {(Object.keys(TAB_LABELS) as Tab[]).map((tab) => (
          <button
            key={tab}
            type="button"
            role="tab"
            aria-selected={activeTab === tab}
            className={`ltp-tab-btn ${activeTab === tab ? 'ltp-tab-btn--active' : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {TAB_LABELS[tab]}
          </button>
        ))}
      </div>

      <div className="ltp-tab-body">
        {activeTab === 'stats' && (
          <PlayerStatsSection
            username={username}
            elo={elo}
            userRating={userRating}
            totalGames={totalGames}
            wins={wins}
            draws={draws}
            losses={losses}
            onSetRating={onSetRating}
          />
        )}
        {activeTab === 'hall' && (
          <TopPlayersSection topElo={topElo} currentUsername={username} />
        )}
        {activeTab === 'comments' && (
          <CommentsCard
            comments={comments}
            draft={commentDraft}
            onDraftChange={onCommentDraftChange}
            onAddComment={onAddComment}
          />
        )}
      </div>
    </div>
  )
}

export default LeftTabPanel
